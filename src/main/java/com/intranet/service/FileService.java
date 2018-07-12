package com.intranet.service;

import java.util.Set;

import com.intranet.model.File;
import com.intranet.model.Folder;
import com.intranet.model.User;

public interface FileService {
	public void save(File file);
	public void update(File file);
	public File findById(Integer id);
	Set<File> findByOwner(User owner);
	File findByParentAndName(Folder parent, String name);
	public void remove(File file);
}
