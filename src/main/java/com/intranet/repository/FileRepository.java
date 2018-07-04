package com.intranet.repository;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.intranet.model.File;
import com.intranet.model.Folder;
import com.intranet.model.User;

@Repository("fileRepository")
public interface FileRepository extends JpaRepository<File, Integer>{
	File getOne(Integer id);
	File findByParentAndName(Folder parent, String name);
	Set<File> findByOwner(User owner);
}