package com.intranet.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.intranet.model.Folder;
import com.intranet.model.User;
import com.intranet.service.FolderService;
import com.intranet.service.UserService;

@RestController
public class FolderRestController {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private FolderService folderService;
	
	@RequestMapping(value = "/refreshFolders", method = RequestMethod.GET)
	public String refreshTable(Model model) {
	    return "user/files :: #tablaFolders";
	}
	
	@SuppressWarnings("rawtypes")
	@PostMapping(path = "/user/create/folder", consumes = "text/plain")
	public ResponseEntity<?> createFolder(@RequestBody String name) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());
		
		Folder newFolder = new Folder();
		newFolder.setName(name);
		newFolder.setPath(user.getRoot().getPath() + "//" + name);
		newFolder.setOwner(user);
		
		java.io.File f = new java.io.File(newFolder.getPath());
		if(!f.exists()) {
			f.mkdirs();
		}
		
		user.getFolders().add(newFolder);
		user.getRoot().getFolders().add(newFolder);
		folderService.save(newFolder);
		
		folderService.update(user.getRoot());
		userService.update(user);
		
		return new ResponseEntity(HttpStatus.OK);
	}
	
}
