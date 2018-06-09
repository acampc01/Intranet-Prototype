package com.intranet.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FolderRestController {

	@SuppressWarnings("rawtypes")
	@PostMapping(path = "/user/create/folder", consumes = "text/plain")
	public ResponseEntity<?> createFolder(@RequestBody String name) {
		
		return new ResponseEntity(HttpStatus.OK);
	}
	
}
