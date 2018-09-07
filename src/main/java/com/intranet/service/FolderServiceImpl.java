package com.intranet.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.intranet.model.Folder;
import com.intranet.model.User;
import com.intranet.repository.FolderRepository;

@Service("folderService")
public class FolderServiceImpl implements FolderService{

	@Autowired
	private FolderRepository folderRepository;

	@Override
	public void save(Folder folder) {
		folderRepository.save(folder);
	}

	@Override
	public void update(Folder folder) {
		folder.setLastUpdate(new Date());
		folderRepository.save(folder);
	}

	@Override
	public Folder findById(Long id) {
		return folderRepository.getOne(id);
	}

	@Override
	public void remove(Folder folder) {
		folder.getFiles().clear();
		folder.getFolders().clear();
		if(folder.getParent() != null)
			folder.getParent().getFolders().remove(folder);
		folderRepository.delete(folder);
	}

	@Override
	public List<Folder> findAll(User user) {
		if(user.isAdmin())
			return folderRepository.findAll();
		else
			return null;
	}
}
