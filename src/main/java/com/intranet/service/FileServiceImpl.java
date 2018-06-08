package com.intranet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.intranet.model.File;
import com.intranet.repository.FileRepository;

@Service("fileService")
public class FileServiceImpl implements FileService{

	@Autowired
	private FileRepository fileRepository;
	
	@Override
	public void uploadFile(File file) {
		fileRepository.save(file);
	}
	
}
