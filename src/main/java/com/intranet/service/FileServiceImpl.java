package com.intranet.service;

import java.util.Date;
import java.util.List;
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
	public File findById(Long id) {
		return fileRepository.getOne(id); 
	}

	@Override
	public Set<File> findByOwner(User owner) {
		return fileRepository.findByOwner(owner);
	}

	@Override
	public void remove(File file) {
		file.getSharedUsers().clear();
		if(file.getParent() != null)
			file.getParent().getFiles().remove(file);
		fileRepository.delete(file);
	}

	@Override
	public List<File> findAll(User user) {
		if(user.isAdmin())
			return fileRepository.findAll();
		else
			return null;
	}
	
}
