package com.intranet.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
	
	@SuppressWarnings("rawtypes")
	@PostMapping(path = "/user/create/folder/{id_folder}", consumes = "text/plain")
	public ResponseEntity<?> createFolder(@PathVariable("id_folder") Integer id, @RequestBody String name) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());
		
		Folder root = folderService.findById(id);
		Folder newFolder = new Folder();
		newFolder.setName(name);
		newFolder.setPath(root.getPath() + "//" + name);
		newFolder.setOwner(user);
		
		java.io.File f = new java.io.File(newFolder.getPath());
		if(!f.exists()) {
			f.mkdirs();
			
			root.getFolders().add(newFolder);
			folderService.save(newFolder);
			folderService.update(root);
			userService.update(user);
		}
		return new ResponseEntity(HttpStatus.OK);
	}
	
}
