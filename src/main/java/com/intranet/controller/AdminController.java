package com.intranet.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import com.intranet.model.User;
import com.intranet.service.UserService;

@Controller
public class AdminController {

	@Autowired
	private UserService userService;
	
	@RequestMapping(value = "/admin/home", method = RequestMethod.GET)
	public ModelAndView home() {
		ModelAndView modelAndView = new ModelAndView();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());
		modelAndView.addObject("user", user);
		modelAndView.addObject("notifications", userService.findConfirms(user));
		modelAndView.setViewName("/admin/home");
		return modelAndView;
	}
	
}
