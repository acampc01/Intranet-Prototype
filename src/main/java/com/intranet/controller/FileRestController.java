package com.intranet.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.intranet.model.File;
import com.intranet.service.FileService;
import com.intranet.service.FolderService;

@Controller
public class FileRestController {
	
}
//	private static String UPLOADED_FOLDER = ".\\Files\\";
//	
//	@Autowired
//	private FileService fileService;
//	
//	@Autowired
//	private FolderService folderService;
//	
//	@RequestMapping(value="/user/upload", method = RequestMethod.GET)
//	public ModelAndView registration(){
//		ModelAndView modelAndView = new ModelAndView();
//		modelAndView.setViewName("user/upload");
//		return modelAndView;
//	}
//	
//    @PostMapping("user/upload") // //new annotation since 4.3
//    public ModelAndView singleFileUpload(@RequestParam("file") MultipartFile file) {
//    	ModelAndView modelAndView = new ModelAndView();
//    	modelAndView.setViewName("user/upload");
//    	
//    	if (file.isEmpty()) {
//           	modelAndView.addObject("successMessage", "Please select a file to upload");
//            return modelAndView;
//        }
//       
//        try {
//            // Get the file and save it somewhere
//        	File modelFile = new File();
//            modelFile.setName(file.getOriginalFilename());
//            modelFile.setPath(UPLOADED_FOLDER);
//            
//            byte[] bytes = file.getBytes();
//            modelFile.setDatos(bytes);
//            
//            fileService.uploadFile(modelFile);
//            
//            Path path = Paths.get(UPLOADED_FOLDER + SecurityContextHolder.getContext().getAuthentication().getName() + "_" + file.getOriginalFilename());
//            Files.write(path, bytes);
//
//            modelAndView.addObject("successMessage",
//                    "You successfully uploaded '" + file.getOriginalFilename() + "'");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//       return modelAndView;
//    }
	
//	@RequestMapping(value = "/user/upload", method = RequestMethod.POST)
//	public ModelAndView saveFile(@Valid File file, BindingResult bindingResult) {
//		ModelAndView modelAndView = new ModelAndView();
//		fileService.uploadFile(file);
//		modelAndView.addObject("file", new File());
//		modelAndView.setViewName("user/upload");
//		return modelAndView;
//	}
	
//}
