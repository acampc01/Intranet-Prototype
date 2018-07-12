package com.intranet.service;

import com.intranet.model.Folder;

public interface FolderService {
	public void save(Folder folder);
	public void update(Folder folder);
	public Folder findById(Integer id);
	public void remove(Folder folder);
}
