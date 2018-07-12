package com.intranet.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.intranet.model.File;
import com.intranet.model.Folder;
import com.intranet.repository.FolderRepository;

@Service("folderService")
public class FolderServiceImpl implements FolderService{
	
	@Autowired
	private FileService fileService;
	
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
	public Folder findById(Integer id) {
		return folderRepository.getOne(id);
	}

	@Override
	public void remove(Folder folder) {
		if(!folder.getSharedUsers().isEmpty()) {
			
		}else {
			for (Folder fold : folder.getFolders()) {
				remove(fold);
			}
			for (File file : folder.getFiles()) {
				fileService.remove(file);
			}
			
			folder.getFiles().clear();
			folder.getFolders().clear();
			if(folder.getParent() != null)
				folder.getParent().getFolders().remove(folder);
			folderRepository.delete(folder);
		}
	}
}
