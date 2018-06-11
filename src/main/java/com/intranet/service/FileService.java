package com.intranet.service;

import com.intranet.model.File;

public interface FileService {
	File findByPath(String path);
	public void save(File file);
	public void update(File file);
}
