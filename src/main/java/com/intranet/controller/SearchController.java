package com.intranet.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

@Controller
public class SearchController {

	@Autowired
	private UserService userService;

	@Autowired
	private FileService fileService;

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
	public ResponseEntity<Map<String, Object>> autocompleteNames(@RequestBody String name){
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());
		
		List<User> users = userService.findAll(user);
		if(users == null)
			return new ResponseEntity<Map<String, Object>>(HttpStatus.INTERNAL_SERVER_ERROR);
		
		HashMap<String, Object> map = createJSON(name, users);
		
		if(map == null)
			return new ResponseEntity<Map<String, Object>>(HttpStatus.INTERNAL_SERVER_ERROR);
		
		return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
	}
	
	@RequestMapping(value="/user/search", method = RequestMethod.GET)
	public ModelAndView search() {
		ModelAndView modelAndView = new ModelAndView();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());
		modelAndView.addObject("user", user);
		modelAndView.setViewName("user/search");
		return modelAndView;
	}
	
	@Async
	public HashMap<String, Object> createJSON(String search, User user){
		HashMap<String, Object> map = new HashMap<>();
		try {
			Set<File> files = fileService.findByOwner(user);
			for (File file : files) {
				if(file.getName().toLowerCase().split("\\.")[0].equals(search.toLowerCase()) || file.getName().toLowerCase().split("\\.")[0].contains(search.toLowerCase())) {
					String[] datos = {file.getName(), ""+file.getParent().getId(), file.getLastUpdate().toString(), file.getParent().getName(), file.getOwner().getName()};
					map.put(""+file.getId(), datos);
				}
				switch(file.getFormat()) {
				case "pdf":
					List<Integer> pages = new ArrayList<Integer>();
					PdfReader reader = new PdfReader(getPath(file));
					PdfReaderContentParser parser = new PdfReaderContentParser(reader);
					TextExtractionStrategy strategy;
			        for (int i = 1; i <= reader.getNumberOfPages(); i++) {
			            strategy = parser.processContent(i, new SimpleTextExtractionStrategy());
			            if(strategy.getResultantText().toLowerCase().contains(search.toLowerCase())){
			            	pages.add(i);
			            }
			        }
			        reader.close();
			        if(!pages.isEmpty()) {
			        	String[] datos = {file.getName(), ""+file.getParent().getId(), file.getLastUpdate().toString(), file.getParent().getName(), file.getOwner().getName(), pages.toString()};
						map.put(""+file.getId(), datos);
			        }
					break;
				default:

					break;
				}
			}
		} catch (Exception e) {
			return null;
		}
		return map;
	}

	@Async
	public HashMap<String, Object> createJSON(String name, List<User> users){
		HashMap<String, Object> map = new HashMap<>();
		try {
			for (User user : users) {
				if(user.getEmail().split("@")[0].toLowerCase().contains(name.toLowerCase()) && !user.isAdmin()) {
					String[] datos = {user.getName(), user.getEmail()};
					map.put(String.valueOf(user.getId()), datos);
				}		
			}
		} catch (Exception e) {
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
			path = aux.getParent().getName() + "//" + path;
			aux = aux.getParent();
		}

		path = DemoApplication.getFolderPath() + path;
		path += file.getName();
		return path;
	}

}
