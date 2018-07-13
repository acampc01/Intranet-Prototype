package com.intranet.service;

import java.util.Date;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.intranet.model.File;
import com.intranet.model.Folder;
import com.intranet.model.User;
import com.intranet.repository.FileRepository;

@Service("fileService")
public class FileServiceImpl implements FileService{

	@Autowired
	private FileRepository fileRepository;
	
	@Override
	public void save(File file) {
		fileRepository.save(file);
	}
	
	@Override
	public void update(File file) {
		file.setLastUpdate(new Date());
		fileRepository.save(file);
	}

	@Override
	public File findByParentAndName(Folder parent, String name) {
		return fileRepository.findByParentAndName(parent, name);
	}

	@Override
	public File findById(Integer id) {
		return fileRepository.getOne(id); 
	}

	@Override
	public Set<File> findByOwner(User owner) {
		Set<File> files = fileRepository.findByOwner(owner);
		files.addAll(owner.getSharedFiles());
		return files;
	}

	@Override
	public void remove(File file) {
		if(!file.getSharedUsers().isEmpty()) {
			
		} else {
			file.getParent().getFiles().remove(file);
			fileRepository.delete(file);
		}
	}
	
}
