package com.intranet.service;

import java.util.List;

import com.intranet.model.Folder;
import com.intranet.model.User;

public interface FolderService {
	public void save(Folder folder);
	public void update(Folder folder);
	public Folder findById(Long id);
	public void remove(Folder folder);
	public List<Folder> findAll(User user);
}
