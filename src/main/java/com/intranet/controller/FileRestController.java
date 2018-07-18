package com.intranet.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;
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
		ModelAndView modelAndView = new ModelAndView();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());
		File file = fileService.findById(id);

		try {
			if(user.getSharedFiles().contains(file) || file.getOwner().equals(user)) {
				java.io.File src = new java.io.File(getPath(file));
				java.io.File temp = new java.io.File("src/main/resources/static/js/pdf/web/" + file.getName());

				try {
					Files.copy(Paths.get(src.getPath()) , Paths.get(temp.getPath()), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					e.printStackTrace();
				}

				modelAndView.addObject("pdf", file.getName());
				temp.delete();
			}
		}catch(EntityNotFoundException e) {}

		modelAndView.setViewName("user/file");
		return modelAndView;
	}

	@RequestMapping(value = "/user/share/files/{id_resource}", method = RequestMethod.PUT)
	public ResponseEntity<String> shareFiles(@RequestParam("emails[]") String[] emails, @PathVariable("id_resource") Integer id){
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User u = userService.findUserByEmail(auth.getName());
		
		if(emails.length != 0) {
			for (int i = 0; i < emails.length; i++) {
				User user = userService.findUserByEmail(emails[i]);
				if(user != null) {
					try {
						File file = fileService.findById(id);
						shareFile(file, u, user);
					} catch (EntityNotFoundException e) {}
					
					try {
						Folder folder = folderService.findById(id);
						shareFolder(folder, u, user);
					} catch (EntityNotFoundException e) {}
				}
			}
			return new ResponseEntity<String>(HttpStatus.OK);
		}
			
		return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
	}
	
	@RequestMapping(value = "/user/file/shared/users/{id_resource}", method = RequestMethod.GET)
	public HashMap<String, Object> sharedWith(@PathVariable("id_resource") Integer id){
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());
		
		File file = fileService.findById(id);
		try {
			if(file.getOwner().equals(user) || file.getSharedUsers().contains(user)) {
				HashMap<String, Object> map = new HashMap<String, Object>();
				for (User u : file.getSharedUsers()) {
					HashMap<String, String> data = new HashMap<String, String>();
					data.put("name", u.getName());
					data.put("email", u.getEmail());
					map.put(String.valueOf(u.getId()), data);
				}
				return map;
			}
		} catch (EntityNotFoundException e) {}
		
		Folder folder = folderService.findById(id);
		try {
			if(folder.getOwner().equals(user) || folder.getSharedUsers().contains(user)) {
				HashMap<String, Object> map = new HashMap<String, Object>();
				for (User u : folder.getSharedUsers()) {
					HashMap<String, String> data = new HashMap<String, String>();
					data.put("name", u.getName());
					data.put("email", u.getEmail());
					map.put(String.valueOf(u.getId()), data);
				}
				return map;
			}
		} catch (EntityNotFoundException e) {}
		
		return new HashMap<String,Object>();
	}
	
	@Async
	private void shareFolder(Folder folder, User owner, User user) {
		if( (folder != null && !folder.getSharedUsers().contains(user) && !folder.getOwner().equals(user)) && (folder.getOwner().equals(owner) || folder.getSharedUsers().contains(owner))) {
			for (Folder f : folder.getFolders()) {
				shareFolder(f, owner, user);
			}
			
			for (File file : folder.getFiles()) {
				shareFile(file, owner, user);
			}
			
			folder.getSharedUsers().add(user);
			user.getSharedFolders().add(folder);
			folderService.update(folder);
			userService.update(user);
		}
	}
	
	@Async
	private void shareFile(File file, User owner, User user) {
		if( (file != null && !file.getSharedUsers().contains(user) && !file.getOwner().equals(user)) && (file.getOwner().equals(owner) || file.getSharedUsers().contains(owner))) {
			file.getSharedUsers().add(user);
			user.getSharedFiles().add(file);
			fileService.update(file);
			userService.update(user);
		}
	}
	
	@RequestMapping(value = "/user/upload/files/{id_folder}", method = RequestMethod.POST)
	public ResponseEntity<File> uploadFileMulti(@RequestParam("files") MultipartFile[] uploadfiles, @PathVariable("id_folder") Integer id) {
		String uploadedFileName = Arrays.stream(uploadfiles).map(x -> x.getOriginalFilename())
				.filter(x -> !StringUtils.isEmpty(x)).collect(Collectors.joining(" , "));

		if (StringUtils.isEmpty(uploadedFileName)) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		try {
			saveUploadedFiles(Arrays.asList(uploadfiles), id);
		} catch (IOException e) {
			return new ResponseEntity<File>(HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<File>(HttpStatus.OK);
	}

	@RequestMapping("/user/download/{id_file}")
	public ResponseEntity<Resource> downloadFile(@PathVariable("id_file") Integer id, HttpServletRequest request) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());
		File file = fileService.findById(id);
		
		try {
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
				
				file.setDownload(new Date());
				fileService.update(file);
				
				return ResponseEntity.ok()
						.contentType(MediaType.parseMediaType(contentType))
						.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
						.body(resource);
			}
		}catch(EntityNotFoundException e) {}

		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}

	@Async
	private void saveUploadedFiles(List<MultipartFile> files, int id) throws IOException {
		Folder folder = folderService.findById(id);
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());

		if(folder.getOwner().equals(user) || user.getSharedFolders().contains(folder)) {
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