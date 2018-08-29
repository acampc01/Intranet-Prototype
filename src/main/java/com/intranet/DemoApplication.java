package com.intranet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DemoApplication {

	/**
	 * Ruta raiz del arbol de archivos
	 */
	private final static String UPLOADED_FOLDER = System.getProperty("user.home") + "/data/"; //"C:/Users/Alex/Desktop/temp/";
	
	/**
	 * Spring Boot Launch
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	public static String getFolderPath() {
		return UPLOADED_FOLDER;
	}

}
