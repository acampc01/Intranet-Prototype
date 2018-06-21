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

import com.intranet.DemoApplication;
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
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());
		ModelAndView modelAndView = new ModelAndView(new RedirectView("/user/files/" + user.getRoot().getId() , true));
		return modelAndView;
	}

	@RequestMapping(value = "/user/files/{id_folder}", method = RequestMethod.GET)
	public ModelAndView getFolderFiles(@PathVariable("id_folder") Integer id) {
		Folder folder = folderService.findById(id);

		ModelAndView modelAndView = new ModelAndView();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());

		if(user.getSharedFolders().contains(folder) || folder.getOwner().equals(user)) {

			modelAndView.addObject("user", user);
			modelAndView.addObject("root", folder);
			modelAndView.addObject("files", folder.getFiles());
			modelAndView.addObject("folders", folder.getFolders());

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

			modelAndView.setViewName("user/files");
			return modelAndView;
		}

		modelAndView.setView(new RedirectView("/user/files"));
		return modelAndView;
	}

	private String getPath(File file) {
		String path = "";

		Folder aux = new Folder();
		aux.setParent(file.getParent());
		while(aux.getParent() != null) {
			path = aux.getParent().getName() + "//" + path;
			aux = aux.getParent();
		}

		path = DemoApplication.getFolderPath() + path;
		path += file.getName();
		return path;
	}

}

