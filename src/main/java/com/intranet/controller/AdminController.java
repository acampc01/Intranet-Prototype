package com.intranet.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.intranet.DemoApplication;
import com.intranet.model.File;
import com.intranet.model.Folder;
import com.intranet.model.User;
import com.intranet.service.FileService;
import com.intranet.service.FolderService;
import com.intranet.service.UserService;

@RestController
public class AdminController {

	private final static String UPLOADED_FOLDER = DemoApplication.getFolderPath();

	@Autowired
	private UserService userService;

	@Autowired
	private FileService fileService;

	@Autowired
	private FolderService folderService;

//	@RequestMapping(value = "/admin/prueba", method = RequestMethod.GET)
//	public ResponseEntity<User> prueba() {
//		Folder f = folderService.findById(39);
//		clear(f);
//		return new ResponseEntity<User>(HttpStatus.NO_CONTENT);
//	}
	
	@RequestMapping(value = "/admin/home", method = RequestMethod.GET)
	public ModelAndView home() {
		ModelAndView modelAndView = new ModelAndView();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());
		List<User> urs = userService.findAll(user);
		List<User> users = new ArrayList<User>();
		for (User u : urs) {
			if(!u.isAdmin() && u.getActive() == 1) {
				users.add(u);
			}
		}

		modelAndView.addObject("user", user);
		modelAndView.addObject("users", users);
		modelAndView.addObject("notifications", userService.findConfirms(user));
		modelAndView.setViewName("/admin/home");
		return modelAndView;
	}

	@RequestMapping(value = "/admin/accept/{id_user}", method = RequestMethod.PUT)
	public ResponseEntity<User> acceptUser(@PathVariable("id_user") Integer id) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());

		if(user != null && user.isAdmin()) {
			User u = userService.findOne(id);

			String path = UPLOADED_FOLDER + u.getEmail().split("@")[0];
			java.io.File f = new java.io.File(path);
			if(!f.exists()) {
				userService.update(u);
				f.mkdirs();

				Folder folder = new Folder();
				folder.setName(u.getEmail().split("@")[0]);
				folder.setOwner(u);
				folder.setParent(null);
				folderService.save(folder);

				u.setRoot(folder);
				userService.update(u);
			}

			if(u.getActive() == 0)
				u.setActive(1);
			userService.update(u);
			return new ResponseEntity<User>(HttpStatus.NO_CONTENT);
		}

		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@RequestMapping(value = "/admin/refuse/{id_user}", method = RequestMethod.DELETE)
	public ResponseEntity<User> refuseUser(@PathVariable("id_user") Integer id) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());

		if(user != null && user.isAdmin()) {
			User u = userService.findOne(id);
			userService.remove(u);
			return new ResponseEntity<User>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<User>(HttpStatus.NOT_FOUND);
	}

	@RequestMapping(value = "/admin/disarm/{id_user}", method = RequestMethod.PUT)
	public ResponseEntity<User> deactiveUser(@PathVariable("id_user") Integer id) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());

		if(user != null && user.isAdmin()) {
			User u = userService.findOne(id);
			u.setActive(0);
			userService.update(u);
			return new ResponseEntity<User>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<User>(HttpStatus.NOT_FOUND);
	}

	@RequestMapping(value = "/admin/delete/{id_user}", method = RequestMethod.DELETE)
	public ResponseEntity<User> deleteUser(@PathVariable("id_user") Integer id) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());

		if(user != null && user.isAdmin()) {
			User u = userService.findOne(id);
			if(u != null && !u.isAdmin()) {
				Folder root = u.getRoot();
				clear(root);
				userService.remove(u);
				return new ResponseEntity<User>(HttpStatus.NO_CONTENT);
			}
		}
		return new ResponseEntity<User>(HttpStatus.NOT_FOUND);
	}
	
	@RequestMapping(value = "/file/delete/{id_file}", method = RequestMethod.DELETE)
	public ResponseEntity<File> deleteFile(@PathVariable("id_file") Integer id) {
		File file = fileService.findById(id);
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());

		try {
			if(user != null && file != null) {
				if(file.getOwner().equals(user) || file.getSharedUsers().contains(user)){
					clear(file);
					return new ResponseEntity<File>(HttpStatus.NO_CONTENT);
				}
			}
		} catch (EntityNotFoundException e) {}
		
		try {
			Folder folder = folderService.findById(id);
			if(user != null && folder != null) {
				if(folder.getOwner().equals(user) || folder.getSharedUsers().contains(user)){
					clear(folder);
					return new ResponseEntity<File>(HttpStatus.NO_CONTENT);
				}
			}
		} catch (EntityNotFoundException e) {}
		
		return new ResponseEntity<File>(HttpStatus.NOT_FOUND);
	}

	@Async
	private void clear(Folder folder) {
		if(folder != null)
			if(folder.isShared()) {
				try {
					User no = folder.getSharedUsers().iterator().next();
					givesFolder(no, folder);
				} catch (IOException e) {}
			}else {
				for (Folder son : folder.getFolders()) {
					clear(son);
				}
				for (File son : folder.getFiles()) {
					clear(son);
				}
				
				try {
					java.io.File f = new java.io.File(getPath(folder));
					if(f.isDirectory()) {
						FileUtils.forceDelete(f);
						folderService.remove(folder);
					}
				} catch (IOException e) {}
			}
	}
	
	@Async
	private void clear(File file) {
		if(file != null)
			if(file.isShared()) {
				try {
					User no = file.getSharedUsers().iterator().next();
					givesFile(no, file);
				} catch (IOException e) {}
			}else {
				try {
					java.io.File f = new java.io.File(getPath(file));
					if(f.isFile()) {
						fileService.remove(file);
						FileUtils.forceDelete(f);
					}
				} catch (IOException e) {}
			}
	}

	private void givesFile(User no, File file) throws IOException {
		java.io.File f = new java.io.File(getPath(file));
		FileUtils.moveFile(f, new java.io.File(getPath(no.getRoot())));
		
		file.setOwner(no);
		no.getSharedFiles().remove(file);
		file.getSharedUsers().remove(no);
		
		fileService.update(file);
		userService.update(no);
	}

	private void givesFolder(User no, Folder folder) throws IOException {
		java.io.File f = new java.io.File(getPath(folder));
		FileUtils.moveDirectory(f, new java.io.File(getPath(no.getRoot())));
		
		folder.setOwner(no);
		no.getSharedFolders().remove(folder);
		folder.getSharedUsers().remove(no);
		
		folderService.update(folder);
		userService.update(no);
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

	@Async
	private String getPath(Folder folder) {
		String path = "";

		Folder aux = new Folder();
		aux.setParent(folder.getParent());
		while(aux.getParent() != null) {
			path = aux.getParent().getName() + "//" + path;
			aux = aux.getParent();
		}

		path = DemoApplication.getFolderPath() + path;
		path += folder.getName();
		return path;
	}

}
