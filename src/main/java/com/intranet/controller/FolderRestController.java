package com.intranet.controller;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.intranet.DemoApplication;
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
		newFolder.setParent(root);
		newFolder.setOwner(user);

		java.io.File f = new java.io.File(getPath(newFolder));
		if(!f.exists()) {
			f.mkdirs();

			root.getFolders().add(newFolder);
			folderService.save(newFolder);
			folderService.update(root);
			userService.update(user);
		}
		return new ResponseEntity(HttpStatus.OK);
	}

	@RequestMapping("/user/download/folder/{id_folder}")
	public ResponseEntity<Resource> downloadFile(@PathVariable("id_folder") Integer id, HttpServletRequest request) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());
		Folder folder = folderService.findById(id);

		try {
			createZip(folder);
			
			if(user.getSharedFolders().contains(folder) || folder.getOwner().equals(user)) {
				Path pathFile = Paths.get(getPath(folder) + ".zip");
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
				
//				java.io.File f = pathFile.toFile();
//				f.delete();
				
				return ResponseEntity.ok()
						.contentType(MediaType.parseMediaType(contentType))
						.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
						.body(resource);
			}
		} catch (IOException e1) {}

		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	private void createZip(Folder folder) throws IOException {
		String sourceFile = getPath(folder);
		FileOutputStream fos = new FileOutputStream(getPath(folder) + ".zip");
		ZipOutputStream zipOut = new ZipOutputStream(fos);
		java.io.File fileToZip = new java.io.File(sourceFile);
		zipFile(fileToZip, fileToZip.getName(), zipOut);
		zipOut.close();
		fos.close();
	}

	private void zipFile(java.io.File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
		if (fileToZip.isHidden()) {
			return;
		}
		if (fileToZip.isDirectory()) {
			java.io.File[] children = fileToZip.listFiles();
			for (java.io.File childFile : children) {
				zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
			}
			return;
		}
		FileInputStream fis = new FileInputStream(fileToZip);
		ZipEntry zipEntry = new ZipEntry(fileName);
		zipOut.putNextEntry(zipEntry);
		byte[] bytes = new byte[1024];
		int length;
		while ((length = fis.read(bytes)) >= 0) {
			zipOut.write(bytes, 0, length);
		}
		fis.close();
	}

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
