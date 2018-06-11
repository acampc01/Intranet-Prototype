package com.intranet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.intranet.model.File;
import com.intranet.repository.FileRepository;

@Service("fileService")
public class FileServiceImpl implements FileService{

	@Autowired
	private FileRepository fileRepository;
	
//	@Autowired
//    private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Override
	public void save(File file) {
		//TODO fix while.
		String name = file.getName();
		String path = file.getPath();
		
		File aux = fileRepository.findByPath(file.getPath());
		int i = 1;
		while(aux != null) {
			file.setName(name.split("\\.")[0] + "("+i+")." + name.split("\\.")[1]);
			file.setPath(path.split("\\.")[0] + " ("+i+")." + path.split("\\.")[1]);
			aux = fileRepository.findByPath(file.getPath());
			i++;
		}
		
		//file.setPath(bCryptPasswordEncoder.encode(file.getPath()));
		fileRepository.save(file);
	}
	
	@Override
	public void update(File file) {
		fileRepository.save(file);
	}

	@Override
	public File findByPath(String path) {
		return fileRepository.findByPath(path);
	}
	
}
