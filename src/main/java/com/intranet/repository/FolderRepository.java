package com.intranet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.intranet.model.Folder;

@Repository("folderRepository")
public interface FolderRepository extends JpaRepository<Folder, Integer>{
	
}