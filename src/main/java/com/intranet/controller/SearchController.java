package com.intranet.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import com.intranet.DemoApplication;
import com.intranet.model.File;
import com.intranet.model.Folder;
import com.intranet.model.User;
import com.intranet.service.FileService;
import com.intranet.service.UserService;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;

import org.apache.poi.EmptyFileException;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class SearchController {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private UserService userService;

	@Autowired
	private FileService fileService;

	@RequestMapping(value="/user/search", method = RequestMethod.GET)
	public ModelAndView search() {
		ModelAndView modelAndView = new ModelAndView();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());
		modelAndView.addObject("user", user);
		modelAndView.addObject("notifications", userService.findConfirms(user));
		modelAndView.setViewName("user/search");
		return modelAndView;
	}

	@PostMapping(path = "/user/autocomplete", consumes = "text/plain", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> autocomplete(@RequestBody String search){
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());

		HashMap<String, Object> map = createJSON(search, user);

		if(map == null)
			return new ResponseEntity<Map<String, Object>>(HttpStatus.INTERNAL_SERVER_ERROR);

		return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
	}

	@PostMapping(path = "/user/autocomplete/name", consumes = "text/plain", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<Object, Object>> autocompleteNames(@RequestBody String name){
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());

		List<User> users = userService.findAll();
		if(users == null)
			return new ResponseEntity<Map<Object, Object>>(HttpStatus.INTERNAL_SERVER_ERROR);

		users.remove(user);
		HashMap<Object, Object> map = createJSON(name, users);

		if(map == null)
			return new ResponseEntity<Map<Object, Object>>(HttpStatus.INTERNAL_SERVER_ERROR);

		return new ResponseEntity<Map<Object, Object>>(map, HttpStatus.OK);
	}

	@Async
	public HashMap<String, Object> createJSON(String search, User user){
		HashMap<String, Object> map = new HashMap<>();
		List<File> files = new ArrayList<File>(fileService.findByOwner(user));
		files.addAll(user.getSharedFiles());
		for (File file : files) {
			if(file.getName().toLowerCase().split("\\.")[0].equals(search.toLowerCase()) || file.getName().toLowerCase().split("\\.")[0].contains(search.toLowerCase())) {
				String[] datos = {file.getName(), ""+file.getParent().encrypt(), new SimpleDateFormat("dd-MM-yyyy hh:MM").format(file.getLastUpdate()), file.getParent().getName(), file.getOwner().getName(), "Found by name"};
				map.put(file.encrypt(), datos);
			}
			switch(file.getFormat()) {
			case "pdf":
				readPDF(map, search, file);
				break;
			case "doc":
				readDocFile(map, search, file);
				break;
			case "docx":
				readDocxFile(map, search, file);
				break;
			default:
				readDefault(map, search, file);
				break;
			}
		}
		return map;
	}

	@Async
	public HashMap<Object, Object> createJSON(String name, List<User> users){
		HashMap<Object, Object> map = new HashMap<Object, Object>();
		try {
			for (User user : users) {
				if(user.getEmail().split("@")[0].toLowerCase().contains(name.toLowerCase()) || name.equals("empty")) {
					HashMap<String, String> datos = new HashMap<String, String>();
					datos.put("id", user.encrypt());
					datos.put("email", user.getEmail());
					datos.put("name", user.getName());
					map.put(user.encrypt(), datos);
				}		
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			return null;
		}
		return map;
	}

	@Async
	private String getPath(File file) {
		String path = "";
		Folder aux = new Folder();
		aux.setParent(file.getParent());
		while(aux.getParent() != null) {
			path = aux.getParent().getName() + "/" + path;
			aux = aux.getParent();
		}

		path = DemoApplication.getFolderPath() + path;
		path += file.getName();
		return path;
	}

	@Async
	public void readDocFile(HashMap<String, Object> map, String search, File file) {
		try {	
			java.io.File f = new java.io.File(getPath(file));
			FileInputStream fis = new FileInputStream(f.getAbsolutePath());
			HWPFDocument doc = new HWPFDocument(fis);
			WordExtractor we = new WordExtractor(doc);
			String[] paragraphs = we.getParagraphText();

			for (String para : paragraphs) {
				if(para.toString().toLowerCase().contains(search.toLowerCase())) {
					fis.close();
					we.close();
					
					String[] datos = {file.getName(), "" + file.getParent().encrypt(), new SimpleDateFormat("dd-MM-yyyy hh:MM").format(file.getLastUpdate()), file.getParent().getName(), file.getOwner().getName(), "Found inside"};
					map.put(file.encrypt(), datos);
					return;
				}
			}
			fis.close();
			we.close();
		} catch (IOException | EmptyFileException e) {
			log.debug(e.getMessage());
		}
	}

	@Async
	public void readDocxFile(HashMap<String,Object> map, String search, File file) {
		try {
			java.io.File f = new java.io.File(getPath(file));
			FileInputStream fis = new FileInputStream(f.getAbsolutePath());
			XWPFDocument document = new XWPFDocument(fis);
			List<XWPFParagraph> paragraphs = document.getParagraphs();
			for (XWPFParagraph para : paragraphs) {
				if(para.getText().toLowerCase().contains(search.toLowerCase())) {
					fis.close();
					document.close();
					
					String[] datos = {file.getName(), "" + file.getParent().encrypt(), new SimpleDateFormat("dd-MM-yyyy hh:MM").format(file.getLastUpdate()), file.getParent().getName(), file.getOwner().getName(), "Found inside"};
					map.put(file.encrypt(), datos);
					return;
				}
			}
			fis.close();
			document.close();
		} catch (IOException | EmptyFileException e) {
			log.debug(e.getMessage());
		}
	}
	
	@Async
	public void readPDF(HashMap<String, Object> map, String search, File file) {
		try {
			List<Integer> pages = new ArrayList<Integer>();
			PdfReader reader = new PdfReader(getPath(file));
			PdfReaderContentParser parser = new PdfReaderContentParser(reader);
			TextExtractionStrategy strategy;
			for (int i = 1; i <= reader.getNumberOfPages(); i++) {
				strategy = parser.processContent(i, new SimpleTextExtractionStrategy());
				if(strategy.getResultantText().toLowerCase().contains(search.toLowerCase()))
					pages.add(i);
			}
			reader.close();
			if(!pages.isEmpty()) {
				String[] datos = {file.getName(), "" + file.getParent().encrypt(), new SimpleDateFormat("dd-MM-yyyy hh:MM").format(file.getLastUpdate()), file.getParent().getName(), file.getOwner().getName(), "Pages " + pages.toString()};
				map.put(file.encrypt(), datos);
			}
		} catch (IOException | EmptyFileException e) {
			log.debug(e.getMessage());
		}
	}
	
	@Async
	public void readDefault(HashMap<String, Object> map, String search, File file) {
		try {
			List<Integer> coincidences = new ArrayList<Integer>();
			int l = 1;
			for (String line : Files.readAllLines(Paths.get(getPath(file)))) {
				if(line.toLowerCase().contains(search.toLowerCase()))
					coincidences.add(l);
				l++;
			}
			if(!coincidences.isEmpty()) {
				String[] datos = {file.getName(), "" + file.getParent().encrypt(), new SimpleDateFormat("dd-MM-yyyy hh:MM").format(file.getLastUpdate()), file.getParent().getName(), file.getOwner().getName(), "Lines " + coincidences.toString()};
				map.put(file.encrypt(), datos);
			}
		} catch (IOException e) {
			log.debug("Invalid plain text format");
		}
	}

}
