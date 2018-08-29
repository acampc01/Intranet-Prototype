package com.intranet.service;

import java.util.List;
import java.util.Set;

import com.intranet.model.Notification;
import com.intranet.model.User;

public interface UserService {
	public User findUserByEmail(String email);
	public void save(User user);
	public void update(User user);
	public void remove(User user);
	public User findOne(Long id);
	public List<User> findAll();
	public Set<Notification> findConfirms(User user);
}
