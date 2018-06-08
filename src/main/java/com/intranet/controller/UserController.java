package com.intranet.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.intranet.model.File;
import com.intranet.model.User;
import com.intranet.service.FileService;
import com.intranet.service.UserService;

@Controller
public class UserController {

	@Autowired
	private UserService userService;
	
	@Autowired
	private FileService fileService;

	@RequestMapping(value="/user/files", method = RequestMethod.GET)
	public ModelAndView myFiles(){
		ModelAndView modelAndView = new ModelAndView();
		
//		File file = new File();
//		file.setName("Example");
//		file.setPath("/File/Path/Example");
//		file.setImg("/resources/static/img/pdf-icon.png");
//		fileService.uploadFile(file);
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());
//		user.getFiles().add(file);
//		userService.update(user);
		
		modelAndView.addObject("files", user.getFiles());
		
		modelAndView.setViewName("user/files");
		return modelAndView;
	}
	
	@RequestMapping(value="/user/shared", method = RequestMethod.GET)
	public ModelAndView shared(){
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("user/shared");
		return modelAndView;
	}
	
//	@RequestMapping(value="/admin/index", method = RequestMethod.GET)
//	public ModelAndView admin(){
//		ModelAndView modelAndView = new ModelAndView();
//		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//		User user = userService.findUserByEmail(auth.getName());
//		modelAndView.addObject("userName", "Welcome " + user.getName() + " " + user.getLastName() + " (" + user.getEmail() + ") with user role " + user.getRoles().iterator().next().getRole() );
//		modelAndView.addObject("adminMessage","Content Available Only for Users with Admin Role");
//		modelAndView.setViewName("admin/index");
//		return modelAndView;
//	}
	
}
