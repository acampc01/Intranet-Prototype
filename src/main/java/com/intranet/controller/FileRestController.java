package com.intranet.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.intranet.DemoApplication;
import com.intranet.model.File;
import com.intranet.model.Folder;
import com.intranet.model.User;
import com.intranet.service.FileService;
import com.intranet.service.FolderService;
import com.intranet.service.UserService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class FileRestController {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private FileService fileService;
	
	@Autowired
	private FolderService folderService;
	
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

	public String getPath(File file) {
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