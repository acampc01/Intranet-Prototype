package com.intranet.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

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
public class OwnFilesController {

	@Autowired
	private UserService userService;

	@Autowired
	private FolderService folderService;

	@RequestMapping(value="/user/files", method = RequestMethod.GET)
	public ModelAndView myFiles(){
		ModelAndView modelAndView = new ModelAndView();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());
		if(user != null) {
			user.setLastConnect(new Date());
			userService.update(user);
			modelAndView.setView(new RedirectView("/user/files/" + user.getRoot().getId() , true));
			return modelAndView;
		}
		
		modelAndView.setView(new RedirectView("/login"));
		return modelAndView;
	}

	@RequestMapping(value = "/user/files/{id_folder}", method = RequestMethod.GET)
	public ModelAndView getFolderFiles(@PathVariable("id_folder") Integer id) {
		Folder folder = folderService.findById(id);
		
		ModelAndView modelAndView = new ModelAndView();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());
		
		try {
			if(user.getSharedFolders().contains(folder) || folder.getOwner().equals(user)) {
				modelAndView.addObject("user", user);
				modelAndView.addObject("root", folder);
				modelAndView.addObject("notifications", userService.findConfirms(user));
				
				List<File> files = new ArrayList<File>(folder.getFiles());
				Collections.sort(files, new Comparator<File>(){
		           	@Override
					public int compare(File o1, File o2) {
						 return o2.getLastUpdate().compareTo(o1.getLastUpdate());
					}
		        });
				for (File file : new ArrayList<File>(files)) {
					if(!file.getSharedUsers().contains(user) && !file.getOwner().equals(user))
						files.remove(file);
				}
				
				List<Folder> folders = new ArrayList<Folder>(folder.getFolders());
				Collections.sort(folders, new Comparator<Folder>(){
		           	@Override
					public int compare(Folder o1, Folder o2) {
						 return o2.getLastUpdate().compareTo(o1.getLastUpdate());
					}
		        });
				for (Folder fold : new ArrayList<Folder>(folders)) {	
					if(!fold.getSharedUsers().contains(user) && !fold.getOwner().equals(user))
						folders.remove(fold);
				}
				modelAndView.addObject("files", files);
				modelAndView.addObject("folders", folders);

				Date last = new Date(0);
				for (File file : user.getRoot().getFiles()) {
					if(file.getLastUpdate().after(last))
						last = file.getLastUpdate();
				}

				if(folder.getLastUpdate().after(last))
					last = folder.getLastUpdate();

				if( !last.equals(new Date(0)) )
					modelAndView.addObject("lastDate", last);
				else
					modelAndView.addObject("lastDate", new Date());

				modelAndView.setViewName("/user/files");
				return modelAndView;
			}

			modelAndView.setView(new RedirectView("/user/files"));
			return modelAndView;
		}catch(Exception e) {
			modelAndView.setView(new RedirectView("/user/files"));
			return modelAndView;
		}
	}
}

