package com.intranet.controller;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.intranet.model.File;
import com.intranet.model.Folder;
import com.intranet.model.User;
import com.intranet.service.FolderService;
import com.intranet.service.UserService;

@Controller
public class SharedFilesController {

	@Autowired
	private UserService userService;
	
	@Autowired
	private FolderService folderService;
	
	@RequestMapping(value="/user/shared", method = RequestMethod.GET)
	public ModelAndView shared(){
		ModelAndView modelAndView = new ModelAndView();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());

		modelAndView.addObject("user", user);
		modelAndView.addObject("files", user.getSharedFiles());
		modelAndView.addObject("folders", user.getSharedFolders());
		modelAndView.addObject("notifications", userService.findConfirms(user));

		Date last = new Date(0);
		for (File file : user.getSharedFiles()) {
			if(file.getLastUpdate().after(last))
				last = file.getLastUpdate();
		}
		for (Folder folder : user.getSharedFolders()) {
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

	@RequestMapping(value="/user/shared/{id_folder}", method = RequestMethod.GET)
	public ModelAndView shared(@PathVariable("id_folder") Integer id){
		Folder rootFolder = folderService.findById(id);

		ModelAndView modelAndView = new ModelAndView();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());

		if(user.getSharedFolders().contains(rootFolder)) {
			modelAndView.addObject("user", user);
			modelAndView.addObject("root", rootFolder);
			modelAndView.addObject("files", user.getSharedFiles());
			modelAndView.addObject("folders", user.getSharedFolders());

			Date last = new Date(0);
			for (File file : user.getSharedFiles()) {
				if(file.getLastUpdate().after(last))
					last = file.getLastUpdate();
			}
			for (Folder folder : user.getSharedFolders()) {
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
		
		modelAndView.setView(new RedirectView("/user/shared"));
		return modelAndView;
	}
}
