package com.intranet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DemoApplication {

	private final static String UPLOADED_FOLDER = "C://Users//Alex//Desktop//temp//";
	
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
	
	public static String getFolderPath() {
		return UPLOADED_FOLDER;
	}
	
}
