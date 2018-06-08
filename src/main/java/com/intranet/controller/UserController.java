package com.intranet.controller;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.intranet.model.File;
import com.intranet.model.Folder;
import com.intranet.model.User;
import com.intranet.service.UserService;

@Controller
public class UserController {

	@Autowired
	private UserService userService;

	@RequestMapping(value="/user/files", method = RequestMethod.GET)
	public ModelAndView myFiles(){
		ModelAndView modelAndView = new ModelAndView();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());
		modelAndView.addObject("user", user);
		modelAndView.addObject("files", user.getFiles());
		modelAndView.addObject("folders", user.getFolders());
		
		Date last = new Date(0);
		for (File file : user.getFiles()) {
			if(file.getLastUpdate().after(last))
				last = file.getLastUpdate();
		}
		for (Folder folder : user.getFolders()) {
			if(folder.getLastUpdate().after(last))
				last = folder.getLastUpdate();
		}
		
		if( !last.equals(new Date(0)) )
			modelAndView.addObject("lastDate", last);
		else
			modelAndView.addObject("lastDate", new Date());
		
		modelAndView.setViewName("user/files");
		return modelAndView;
	}
	
	@RequestMapping(value="/user/shared", method = RequestMethod.GET)
	public ModelAndView shared(){
		ModelAndView modelAndView = new ModelAndView();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());
		modelAndView.addObject("user", user);
		modelAndView.addObject("files", user.getSharedFiles());
		modelAndView.addObject("folders", user.getSharedFolders());
		
		Date last = new Date(0);
		for (File file : user.getFiles()) {
			if(file.getLastUpdate().after(last))
				last = file.getLastUpdate();
		}
		for (Folder folder : user.getFolders()) {
			if(folder.getLastUpdate().after(last))
				last = folder.getLastUpdate();
		}
		
		if( !last.equals(new Date(0)) )
			modelAndView.addObject("lastDate", last);
		else
			modelAndView.addObject("lastDate", new Date());
		
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
