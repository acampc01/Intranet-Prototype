package com.intranet.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.intranet.DemoApplication;
import com.intranet.model.Encryptor;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@RestController
public class FileRestController {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private UserService userService;

	@Autowired
	private FileService fileService;

	@Autowired
	private FolderService folderService;

	@RequestMapping(path = "/user/file/data/{id_file}", method = RequestMethod.GET)
	public ResponseEntity<Map<String, Object>> autocompleteNames(@PathVariable("id_file") String nid){
		Integer id = Integer.parseInt(Encryptor.decrypt(nid));

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());

		HashMap<String, Object> map = null;
		File file = fileService.findById(id);
		try {
			if(user.getSharedFiles().contains(file) || file.getOwner().equals(user)) {
				map = new HashMap<String, Object>();
				HashMap<String, String> datos = new HashMap<String, String>();
				datos.put("name", file.getName().split("\\.")[0]);
				datos.put("format", file.getFormat());
				datos.put("owner", file.getOwner().getName());
				datos.put("parent", file.getParent().getName());
				datos.put("access", "" + file.getSharedUsers().size());
				datos.put("creation", new SimpleDateFormat("dd-MM-yyyy hh:MM").format(file.getCreation()));
				datos.put("update", new SimpleDateFormat("dd-MM-yyyy hh:MM").format(file.getLastUpdate()));

				if(file.getDownload() != null)
					datos.put("download", new SimpleDateFormat("dd-MM-yyyy hh:MM").format(file.getDownload()));
				else
					datos.put("download", null);

				map.put(nid, datos);
				return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
			}
		}catch(EntityNotFoundException e) {
			log.error(e.getMessage());
		}

		Folder folder = folderService.findById(id);
		try {
			if(user.getSharedFolders().contains(folder) || folder.getOwner().equals(user)) {
				map = new HashMap<String, Object>();
				HashMap<String, String> datos = new HashMap<String, String>();
				datos.put("name", folder.getName());
				datos.put("format", null);
				datos.put("owner", folder.getOwner().getName());
				datos.put("parent", folder.getParent().getName());
				datos.put("access", "" + folder.getSharedUsers().size());
				datos.put("creation", new SimpleDateFormat("dd-MM-yyyy hh:MM").format(folder.getCreation()));
				datos.put("update", new SimpleDateFormat("dd-MM-yyyy hh:MM").format(folder.getLastUpdate()));

				if(folder.getDownload() != null)
					datos.put("download", new SimpleDateFormat("dd-MM-yyyy hh:MM").format(folder.getDownload()));
				else
					datos.put("download", null);

				map.put(nid, datos);
				return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
			}
		}catch(EntityNotFoundException e) {
			log.error(e.getMessage());
		}

		if(map == null) {
			log.error("Incorrect file id");
			return new ResponseEntity<Map<String, Object>>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<Map<String, Object>>(HttpStatus.BAD_REQUEST);
	}
	
	@RequestMapping(value="/user/file/{id_file}", method = RequestMethod.GET)
	public ModelAndView embed(@PathVariable("id_file") String nid) {
		Integer id = Integer.parseInt(Encryptor.decrypt(nid));

		ModelAndView modelAndView = new ModelAndView();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());

		try {
			File file = fileService.findById(id);
			if(user.getSharedFiles().contains(file) || file.getOwner().equals(user)) {
				modelAndView.addObject("file", file);

				if(file.getFormat().equals("odt") || file.getFormat().equals("doc") || file.getFormat().equals("docx") || file.getFormat().equals("docm")) {
					modelAndView.setViewName("user/file");
					return modelAndView;
				}
				
				if(file.getFormat().equals("pdf")) {
					modelAndView.setViewName("user/file");
					return modelAndView;
				}

				List<String> content = null;
				try {
					content = Files.readAllLines(Paths.get(getPath(file)));
					List<String> lines = new ArrayList<String>();
					for (String line : content) {
						line = line.replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
						line = line.replaceAll(" ", "&nbsp;");
						line = line.replace("<", "&#60;");
						line = line.replace(">", "&#62;");
						lines.add(line);
					}
					modelAndView.addObject("content", lines);

					modelAndView.setViewName("user/file");
					return modelAndView;
				} catch (IOException e) {
					log.error("Reading not a plain text file");
				}
			}
		}catch(EntityNotFoundException e) {
			log.error(e.getMessage());
		}

		modelAndView.setView(new RedirectView("/user/files/" + user.getRoot().encrypt() , true));
		return modelAndView;
	}

	@PostMapping(path = "/user/edit/file/{id_file}", consumes = "text/plain")
	public ResponseEntity<File> createFolder(@PathVariable("id_file") String nid, @RequestBody String name) {
		Integer id = Integer.parseInt(Encryptor.decrypt(nid));

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());

		File file = fileService.findById(id);
		try {
			if(file.getOwner().equals(user) || file.getSharedUsers().contains(user)) {
				File rename = new File();
				rename.setParent(file.getParent());
				rename.setName(name.concat("." + file.getFormat()));

				try {
					java.io.File exist = new java.io.File(getPath(rename));
					if(!exist.exists())
						Files.move(new java.io.File(getPath(file)).toPath(), exist.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
					else {
						log.error("Error renaming file");
						return new ResponseEntity<File>(HttpStatus.CONFLICT);
					}
				} catch (IOException ex) {
					log.error("Error renaming file");
					return new ResponseEntity<File>(HttpStatus.CONFLICT);
				}

				file.setName(name.concat("." + file.getFormat()));
				fileService.update(file);
				return new ResponseEntity<File>(HttpStatus.OK);
			}
		} catch (EntityNotFoundException e) {
			log.error(e.getMessage());
		}

		Folder folder = folderService.findById(id);
		try {
			if(folder.getOwner().equals(user) || folder.getSharedUsers().contains(user)) {
				Folder rename = new Folder();
				rename.setParent(folder.getParent());
				rename.setName(name);

				java.io.File oldD = new java.io.File(getPath(folder));
				java.io.File newD = new java.io.File(getPath(rename));

				if(!newD.exists()) {
					if(!oldD.renameTo(newD))
						return new ResponseEntity<File>(HttpStatus.CONFLICT);
				} else {
					log.error("Error renaming folder");
					return new ResponseEntity<File>(HttpStatus.CONFLICT);
				}

				folder.setName(name);
				folderService.update(folder);
				return new ResponseEntity<File>(HttpStatus.OK);
			}
		} catch (EntityNotFoundException e) {
			log.error(e.getMessage());
		}

		return new ResponseEntity<File>(HttpStatus.BAD_REQUEST);
	}

	@RequestMapping(value = "/user/share/files/{id_resource}", method = RequestMethod.PUT)
	public ResponseEntity<String> shareFiles(@RequestParam("emails[]") String[] emails, @PathVariable("id_resource") String nid){
		Integer id = Integer.parseInt(Encryptor.decrypt(nid));

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User u = userService.findUserByEmail(auth.getName());

		if(emails.length != 0) {
			for (int i = 0; i < emails.length; i++) {
				emails[i] = emails[i].split("-")[1].trim();
				User user = userService.findUserByEmail(emails[i]);
				if(user != null) {
					try {
						File file = fileService.findById(id);
						shareFile(file, u, user);
					} catch (EntityNotFoundException e) {
						log.error(e.getMessage());
					}

					try {
						Folder folder = folderService.findById(id);
						shareFolder(folder, u, user);
					} catch (EntityNotFoundException e) {
						log.error(e.getMessage());
					}
				}
			}
			return new ResponseEntity<String>(HttpStatus.OK);
		}

		return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
	}

	@RequestMapping(value = "/user/file/shared/users/{id_resource}", method = RequestMethod.GET)
	public HashMap<String, Object> sharedWith(@PathVariable("id_resource") String nid){
		Integer id = Integer.parseInt(Encryptor.decrypt(nid));

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
		} catch (EntityNotFoundException e) {
			log.error(e.getMessage());
		}

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
		} catch (EntityNotFoundException e) {
			log.error(e.getMessage());
		}

		return new HashMap<String,Object>();
	}

	@RequestMapping(value = "/user/upload/files/{id_folder}", method = RequestMethod.POST)
	public ResponseEntity<File> uploadFileMulti(@RequestParam("files") MultipartFile[] uploadfiles, @PathVariable("id_folder") String nid) {
		Integer id = Integer.parseInt(Encryptor.decrypt(nid));

		String uploadedFileName = Arrays.stream(uploadfiles).map(x -> x.getOriginalFilename())
				.filter(x -> !StringUtils.isEmpty(x)).collect(Collectors.joining(" , "));

		if (StringUtils.isEmpty(uploadedFileName)) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		try {
			saveUploadedFiles(Arrays.asList(uploadfiles), id);
		} catch (IOException e) {
			log.error(e.getMessage());
			return new ResponseEntity<File>(HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<File>(HttpStatus.OK);
	}

	//	@RequestMapping(value = "/user/upload/folder/{id_folder}", method = RequestMethod.POST)
	//	public ResponseEntity<File> uploadFolderMulti(@RequestParam("files") MultipartFile[] uploadfiles, @PathVariable("id_folder") String nid, @RequestBody String fName) {
	//		Integer id = Integer.parseInt(Encryptor.decrypt(nid));
	//
	//		String uploadedFileName = Arrays.stream(uploadfiles).map(x -> x.getOriginalFilename())
	//				.filter(x -> !StringUtils.isEmpty(x)).collect(Collectors.joining(" , "));
	//
	//		if (StringUtils.isEmpty(uploadedFileName)) {
	//			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	//		}
	//
	//		try {
	//			saveUploadedFiles(Arrays.asList(uploadfiles), id);
	//		} catch (IOException e) {
	//			log.error(e.getMessage());
	//			return new ResponseEntity<File>(HttpStatus.BAD_REQUEST);
	//		}
	//
	//		return new ResponseEntity<File>(HttpStatus.OK);
	//	}

	@RequestMapping("/user/download/file/{id_file}")
	public ResponseEntity<Resource> downloadFile(@PathVariable("id_file") String nid, HttpServletRequest request) {
		Integer id = Integer.parseInt(Encryptor.decrypt(nid));

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
				} catch (IOException ex) {
					log.info("Could not determine file type");
				}

				if(contentType == null) {
					contentType = "application/octet-stream";
				}

				file.setDownload(new Date());
				fileService.update(file);

				return ResponseEntity.ok()
						.contentType(MediaType.parseMediaType(contentType))
						.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename().replace("%20", " ")+ "\"")
						.body(resource);
			}
		}catch(EntityNotFoundException e) {
			log.error(e.getMessage());
		}

		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
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

	@Async
	private void saveUploadedFiles(List<MultipartFile> files, int id) throws IOException {
		Folder folder = folderService.findById(id);
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());

		if(folder.getOwner().equals(user) || user.getSharedFolders().contains(folder)) {
			for (MultipartFile file : files) {
				//				if (file.isEmpty()) {
				//					continue;
				//				}

				File f = new File();
				f.setName(file.getOriginalFilename());
				f.setFormat(file.getOriginalFilename().split("\\.")[ (file.getOriginalFilename().split("\\.").length-1) ].toLowerCase());
				f.setParent(folder);
				f.setOwner(user);
				fileService.save(f);

				String path = getPath(f);
				if(fileService.findByParentAndName(f.getParent(), f.getName()) != null) {
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