package com.intranet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.intranet.model.File;
import com.intranet.model.Folder;
import com.intranet.repository.FileRepository;

@Service("fileService")
public class FileServiceImpl implements FileService{

	@Autowired
	private FileRepository fileRepository;
	
//	@Autowired
//    private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Override
	public void save(File file) {
		//file.setPath(bCryptPasswordEncoder.encode(file.getPath()));
		fileRepository.save(file);
	}
	
	@Override
	public void update(File file) {
		fileRepository.save(file);
	}

	@Override
	public File findByParentAndName(Folder parent, String name) {
		// TODO Auto-generated method stub
		return fileRepository.findByParentAndName(parent,name);
	}
	
}
