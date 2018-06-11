package com.intranet.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.intranet.model.Folder;
import com.intranet.service.FolderService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class FileRestController {
	
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
		
		System.out.println(folder.getName());
		System.out.println(folder.getPath());
		
		for (MultipartFile file : files) {
			if (file.isEmpty()) {
				continue;
			}
			byte[] bytes = file.getBytes();
			Path path = Paths.get(folder.getPath() + "//" + file.getOriginalFilename());
			Files.write(path, bytes);
		}
	}

}