package com.intranet.service;

import java.util.List;

import com.intranet.model.User;

public interface UserService {
	public User findUserByEmail(String email);
	public void save(User user);
	public void update(User user);
	public List<User> findAll(User user);
}
