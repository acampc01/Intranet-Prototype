package com.intranet.service;

import com.intranet.model.File;
import com.intranet.model.Folder;

public interface FileService {
	public void save(File file);
	public void update(File file);
	File findByParentAndName(Folder parent, String name);
}
