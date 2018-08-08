package com.intranet.service;

import java.util.Set;

import com.intranet.model.Notification;
import com.intranet.model.User;

public interface NotificationService {
	public Set<Notification> findByType(String type);
	public Notification findBySender(User sender);
	public void remove(Notification notify);
	public void save(Notification notify);
}
