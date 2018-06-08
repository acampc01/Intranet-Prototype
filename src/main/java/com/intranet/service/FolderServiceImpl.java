package com.intranet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.intranet.model.Folder;
import com.intranet.repository.FolderRepository;

@Service("folderService")
public class FolderServiceImpl implements FolderService{
	
	@Autowired
	private FolderRepository folderRepository;
	
	@Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Override
	public void save(Folder folder) {
		folder.setPath(bCryptPasswordEncoder.encode(folder.getPath()));
		folderRepository.save(folder);
	}

	@Override
	public void update(Folder folder) {
		folderRepository.save(folder);
	}
}
