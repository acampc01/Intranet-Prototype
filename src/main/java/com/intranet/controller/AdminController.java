package com.intranet.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityNotFoundException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.intranet.DemoApplication;
import com.intranet.model.Encryptor;
import com.intranet.model.File;
import com.intranet.model.Folder;
import com.intranet.model.User;
import com.intranet.service.FileService;
import com.intranet.service.FolderService;
import com.intranet.service.UserService;

@RestController
public class AdminController {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private final static String UPLOADED_FOLDER = DemoApplication.getFolderPath();

	@Autowired
	private UserService userService;

	@Autowired
	private FileService fileService;

	@Autowired
	private FolderService folderService;
	
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
	public ResponseEntity<User> acceptUser(@PathVariable("id_user") String nid) {
		Integer id = Integer.parseInt(Encryptor.decrypt(nid));

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
		log.error("Non admin user trying to accept a user.");
		
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@RequestMapping(value = "/admin/refuse/{id_user}", method = RequestMethod.DELETE)
	public ResponseEntity<User> refuseUser(@PathVariable("id_user") String nid) {
		Integer id = Integer.parseInt(Encryptor.decrypt(nid));

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());

		if(user != null && user.isAdmin()) {
			User u = userService.findOne(id);
			userService.remove(u);
			return new ResponseEntity<User>(HttpStatus.NO_CONTENT);
		}
		log.error("Non admin user trying to refuse a user.");
		
		return new ResponseEntity<User>(HttpStatus.NOT_FOUND);
	}

	@RequestMapping(value = "/admin/delete/{id_user}", method = RequestMethod.DELETE)
	public ResponseEntity<User> deleteUser(@PathVariable("id_user") String nid) {
		Integer id = Integer.parseInt(Encryptor.decrypt(nid));
		
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
		log.error("Non admin user trying to delete a user.");
		return new ResponseEntity<User>(HttpStatus.NOT_FOUND);
	}
	
	@RequestMapping(value = "/file/delete/{id_file}", method = RequestMethod.DELETE)
	public ResponseEntity<File> deleteFile(@PathVariable("id_file") String nid) {
		Integer id = Integer.parseInt(Encryptor.decrypt(nid));
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());

		try {
			File file = fileService.findById(id);
			if(user != null && file != null) {
				if(file.getOwner().equals(user)){
					clear(file);
					return new ResponseEntity<File>(HttpStatus.NO_CONTENT);
				}
				if(file.getSharedUsers().contains(user)) {
					file.getSharedUsers().remove(user);
					user.getSharedFiles().remove(file);
					fileService.update(file);
					userService.update(user);
					return new ResponseEntity<File>(HttpStatus.NO_CONTENT);
				}
			}
		} catch (EntityNotFoundException e) {
			log.debug("Deleting Resource: " + e.getMessage());
		}
		
		try {
			Folder folder = folderService.findById(id);
			if(user != null && folder != null) {
				if(folder.getOwner().equals(user)){
					clear(folder);
					return new ResponseEntity<File>(HttpStatus.NO_CONTENT);
				}
				if(folder.getSharedUsers().contains(user)) {
					folder.getSharedUsers().remove(user);
					user.getSharedFolders().remove(folder);
					folderService.update(folder);
					userService.update(user);
					return new ResponseEntity<File>(HttpStatus.NO_CONTENT);
				}
			}
		} catch (EntityNotFoundException e) {
			log.debug("Deleting Resource: " + e.getMessage());
		}
		
		return new ResponseEntity<File>(HttpStatus.NOT_FOUND);
	}

	@RequestMapping(path = "/user/data/{id_user}", method = RequestMethod.GET)
	public ResponseEntity<Map<String, Object>> autocompleteNames(@PathVariable("id_user") String nid){
		Integer id = Integer.parseInt(Encryptor.decrypt(nid));
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());
		
		HashMap<String, Object> map = null;
		try {
			if(user != null && user.isAdmin()) {
				User u = userService.findOne(id);
				if(u != null && !u.isAdmin()) {
					map = new HashMap<String, Object>();
					HashMap<String, String> datos = new HashMap<String, String>();
					datos.put("name", u.getName());
					datos.put("lname", u.getLastName());
					datos.put("email", u.getEmail());

					if(u.getActive() == 1)
						datos.put("active", "Yes");
					else
						datos.put("active", "No");
					
					if(u.isAdmin())
						datos.put("role", "ADMIN");
					else
						datos.put("role", "USER");
					
					datos.put("creation", new SimpleDateFormat("dd-MM-yyyy hh:MM").format(u.getCreation()));
					
					if(u.getLastConnect() != null)
						datos.put("update", new SimpleDateFormat("dd-MM-yyyy hh:MM").format(u.getLastConnect()));
					else
						datos.put("update", null);
					
					map.put(nid, datos);
				}
			}
		}catch(EntityNotFoundException e) {
			e.printStackTrace();
			log.error(e.getMessage());
		}

		if(map == null) {
			log.error("Error getting user data");
			return new ResponseEntity<Map<String, Object>>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
	}
	
	@PostMapping(path = "/user/edit/{id_user}")
	public ResponseEntity<File> createFolder(@PathVariable("id_user") String nid, @RequestParam("datos[]") String[] data) {
		Integer id = Integer.parseInt(Encryptor.decrypt(nid));

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());

		try {
			if(user != null && user.isAdmin()) {
				User u = userService.findOne(id);
				if(u != null && !u.isAdmin()) {
					u.setName(data[0]);
					u.setLastName(data[1]);
					
					try {
						User aux = userService.findUserByEmail(data[2]);
						if(aux == null || (aux != null && aux.equals(u))) {
							u.setEmail(data[2]);
							userService.update(u);
							return new ResponseEntity<File>(HttpStatus.OK);
						}
					}catch(EntityNotFoundException e) {
						log.error("Duplicated email");
						return new ResponseEntity<File>(HttpStatus.CONFLICT);
					}
				}
			}
		}catch(EntityNotFoundException e) {
			e.printStackTrace();
			log.error(e.getMessage());
		}

		log.error("Error editing");
		return new ResponseEntity<File>(HttpStatus.BAD_REQUEST);
	}
	
	@Async
	private void clear(Folder folder) {
		if(folder != null)
			if(folder.isShared()) {
				try {
					User no = folder.getSharedUsers().iterator().next();
					givesFolder(no, folder);
				} catch (IOException e) {
					log.debug("Error with shared folder: " + e.getMessage());
				}
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
						folderService.remove(folder);
						FileUtils.forceDelete(f);
					}
				} catch (IOException e) {
					log.debug("Deleting folder: " + e.getMessage());
				}
			}
	}
	
	@Async
	private void clear(File file) {
		if(file != null)
			if(file.isShared()) {
				try {
					User no = file.getSharedUsers().iterator().next();
					givesFile(no, file);
				} catch (IOException e) {
					log.debug("Error with shared file: " + e.getMessage());
				}
			}else {
				try {
					java.io.File f = new java.io.File(getPath(file));
					if(f.isFile()) {
						fileService.remove(file);
						FileUtils.forceDelete(f);
					}
				} catch (IOException e) {
					log.debug("Deleting file: " + e.getMessage());
				}
			}
	}

	@Async
	private void givesFile(User no, File file) throws IOException {
		java.io.File f = new java.io.File(getPath(file));
		FileUtils.moveFile(f, new java.io.File( getPath(no.getRoot())+"//"+file.getName()));
		
		no.getSharedFiles().remove(file);
		file.getSharedUsers().remove(no);
		file.setOwner(no);
		if(file.getParent().getParent() == null) {
			file.getParent().getFiles().remove(file);
			file.setParent(no.getRoot());
			no.getRoot().getFiles().add(file);
		}
		
		fileService.update(file);
		userService.update(no);
	}

	@Async
	private void givesFolder(User no, Folder folder) throws IOException {
		java.io.File f = new java.io.File(getPath(folder));
		FileUtils.moveDirectory(f, new java.io.File(getPath(no.getRoot())+"//"+folder.getName()));
		
		folder.setOwner(no);
		folder.getParent().getFolders().remove(folder);
		folder.setParent(no.getRoot());
		folder.getParent().getFolders().add(folder);
		
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
