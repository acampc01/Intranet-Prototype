package com.intranet.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.intranet.model.File;
import com.intranet.model.Folder;
import com.intranet.model.User;
import com.intranet.service.NotificationService;
import com.intranet.service.UserService;

@Controller
public class SharedFilesController {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private NotificationService notifyService;

	@RequestMapping(value="/user/shared", method = RequestMethod.GET)
	public ModelAndView myShared(){
		ModelAndView modelAndView = new ModelAndView();

		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			User user = userService.findUserByEmail(auth.getName());
			
			modelAndView.addObject("user", user);
			modelAndView.addObject("notifications", userService.findConfirms(user));
			modelAndView.addObject("notifies", notifyService.findByType("Advice"));
			
			List<File> files = new ArrayList<File>(user.getSharedFiles());
			Collections.sort(files, new Comparator<File>(){
	           	@Override
				public int compare(File o1, File o2) {
					 return o2.getLastUpdate().compareTo(o1.getLastUpdate());
				}
	        });
			
			List<Folder> folders = new ArrayList<Folder>(user.getSharedFolders());
			Collections.sort(folders, new Comparator<Folder>(){
	           	@Override
				public int compare(Folder o1, Folder o2) {
					 return o2.getLastUpdate().compareTo(o1.getLastUpdate());
				}
	        });
			modelAndView.addObject("files", files);
			modelAndView.addObject("folders", folders);
			
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
		}catch(Exception e) {
			log.error(e.getMessage());
			modelAndView.setView(new RedirectView("/user/files"));
			return modelAndView;
		}
	}
}
