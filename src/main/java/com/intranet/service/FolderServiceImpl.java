package com.intranet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.intranet.model.Folder;
import com.intranet.repository.FolderRepository;

@Service("folderService")
public class FolderServiceImpl implements FolderService{
	
	@Autowired
	private FolderRepository folderRepository;
	
	@Override
	public void saveFolder(Folder folder) {
		folderRepository.save(folder);
	}
}
