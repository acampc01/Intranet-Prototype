package com.intranet.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.intranet.model.User;
import com.intranet.service.FileService;
import com.intranet.service.FolderService;
import com.intranet.service.NotificationService;
import com.intranet.service.UserService;

@Controller
public class ProfileController {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private NotificationService notifyService;
	
	@Autowired
	private FileService fileService;

	@Autowired
	private FolderService folderService;
	
	@RequestMapping(value = "/user/profile", method = RequestMethod.GET)
	public ModelAndView profile() {
		ModelAndView modelAndView = new ModelAndView();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());

		modelAndView.addObject("user", user);
		modelAndView.addObject("notifications", userService.findConfirms(user));
		modelAndView.addObject("notifies", notifyService.findByType("Advice"));
		modelAndView.setViewName("user/profile");
		return modelAndView;
	}
	
}
