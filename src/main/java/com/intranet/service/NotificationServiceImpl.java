package com.intranet.service;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.intranet.model.Notification;
import com.intranet.model.User;
import com.intranet.repository.NotificationRepository;

@Service("notifyService")
public class NotificationServiceImpl implements NotificationService{
	
	@Autowired
	private NotificationRepository notifyRepository;

	@Override
	public Set<Notification> findByType(String type) {
		return notifyRepository.findAllByType(type);
	}
	
	@Override
	public Notification findBySender(User sender) {
		return notifyRepository.findBySender(sender);
	}

	@Override
	public void remove(Notification notify) {
		notifyRepository.delete(notify);
	}

	@Override
	public void save(Notification notify) {
		notifyRepository.save(notify);
	}

	@Override
	public Notification findOne(Integer id) {
		return notifyRepository.getOne(id);
	}
	
}
