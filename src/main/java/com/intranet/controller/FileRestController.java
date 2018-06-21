package com.intranet.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.intranet.DemoApplication;
import com.intranet.model.File;
import com.intranet.model.Folder;
import com.intranet.model.User;
import com.intranet.service.FileService;
import com.intranet.service.FolderService;
import com.intranet.service.UserService;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@RestController
public class FileRestController {

	@Autowired
	private UserService userService;

	@Autowired
	private FileService fileService;

	@Autowired
	private FolderService folderService;

	@RequestMapping(value="/user/file/{id_file}", method = RequestMethod.GET)
	public ModelAndView embed(@PathVariable("id_file") Integer id) {
		File file = fileService.findById(id);
		ModelAndView modelAndView = new ModelAndView();

		java.io.File src = new java.io.File(getPath(file));
		java.io.File temp = new java.io.File("src/main/resources/static/js/pdf/web/" + file.getName());

		try {
			Files.copy(Paths.get(src.getPath()) , Paths.get(temp.getPath()), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}

		modelAndView.addObject("pdf", file.getName());
		temp.delete();

		modelAndView.setViewName("user/file");
		return modelAndView;
	}
	
	@SuppressWarnings("rawtypes")
	@PostMapping("/user/upload/files/{id_folder}")
	public ResponseEntity<?> uploadFileMulti(@RequestParam("files") MultipartFile[] uploadfiles, @PathVariable("id_folder") Integer id) {
		String uploadedFileName = Arrays.stream(uploadfiles).map(x -> x.getOriginalFilename())
				.filter(x -> !StringUtils.isEmpty(x)).collect(Collectors.joining(" , "));

		if (StringUtils.isEmpty(uploadedFileName)) {
			return new ResponseEntity(HttpStatus.OK);
		}

		try {
			saveUploadedFiles(Arrays.asList(uploadfiles), id);
		} catch (IOException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity(HttpStatus.OK);
	}

	@RequestMapping("/user/download/{id_file}")
	public ResponseEntity<Resource> downloadFile(@PathVariable("id_file") Integer id, HttpServletRequest request) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());
		File file = fileService.findById(id);

		if(user.getSharedFiles().contains(file) || file.getOwner().equals(user)) {
			Path pathFile = Paths.get(getPath(file));
			Resource resource = null;
			try {
				resource = new UrlResource(pathFile.toUri());
			} catch (MalformedURLException e) {}

			String contentType = null;
			try {
				contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
			} catch (IOException ex) {}

			if(contentType == null) {
				contentType = "application/octet-stream";
			}

			return ResponseEntity.ok()
					.contentType(MediaType.parseMediaType(contentType))
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
					.body(resource);
		}
		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}


	private void saveUploadedFiles(List<MultipartFile> files, int id) throws IOException {
		Folder folder = folderService.findById(id);
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());

		for (MultipartFile file : files) {
			if (file.isEmpty()) {
				continue;
			}

			File f = new File();
			f.setName(file.getOriginalFilename());
			f.setFormat(file.getOriginalFilename().split("\\.")[ (file.getOriginalFilename().split("\\.").length-1) ]);
			f.setParent(folder);
			f.setOwner(user);
			fileService.save(f);

			String path = getPath(f);

			if(fileService.findByParentAndName(f.getParent(),f.getName()) != null) {
				folder.getFiles().add(f);
				folderService.update(folder);
			}

			Files.write(Paths.get(path), file.getBytes());
		}
	}

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