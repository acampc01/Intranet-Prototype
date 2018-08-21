package com.intranet.controller;

import javax.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.intranet.model.Encryptor;
import com.intranet.model.Notification;
import com.intranet.model.User;
import com.intranet.service.NotificationService;
import com.intranet.service.UserService;

@RestController
public class NotifyController {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private NotificationService notifyService;
	
	@RequestMapping(value = "/admin/notify", method = RequestMethod.PUT)
	public ResponseEntity<Notification> notify(@RequestParam("datos[]") String[] data) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());

		if(user != null && user.isAdmin()) {
			Notification notify = new Notification();
			notify.setSender(user);
			notify.setType(data[0]);
			notify.setContent(data[1]);
			notifyService.save(notify);
			return new ResponseEntity<Notification>(HttpStatus.NO_CONTENT);
		}
		
		log.debug("non admin user trying to create a notify");
		return new ResponseEntity<Notification>(HttpStatus.NOT_FOUND);
	}
	
	@RequestMapping(value = "/admin/notify/delete/{notif_id}", method = RequestMethod.DELETE)
	public ResponseEntity<Notification> notify(@PathVariable("notif_id") String nid) {
		Integer id = Integer.parseInt(Encryptor.decrypt(nid));
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());

		try {
			if(user != null && user.isAdmin()) {
				Notification notify = notifyService.findOne(id);
				notifyService.remove(notify);
				return new ResponseEntity<Notification>(HttpStatus.NO_CONTENT);
			}
		} catch (EntityNotFoundException e) {
			log.debug("Entity not found");
			return new ResponseEntity<Notification>(HttpStatus.NOT_FOUND);
		}
		
		log.debug("non admin user trying to create a notify");
		return new ResponseEntity<Notification>(HttpStatus.NOT_FOUND);
	}
	

}
