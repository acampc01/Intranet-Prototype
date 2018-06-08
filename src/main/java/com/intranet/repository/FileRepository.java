package com.intranet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.intranet.model.File;

@Repository("fileRepository")
public interface FileRepository extends JpaRepository<File, Integer>{
	
}