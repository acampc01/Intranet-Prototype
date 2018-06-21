package com.intranet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.intranet.model.File;
import com.intranet.model.Folder;

@Repository("fileRepository")
public interface FileRepository extends JpaRepository<File, Integer>{
	File getOne(Integer id);
	File findByParentAndName(Folder parent, String name);
}