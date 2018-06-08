package com.intranet.service;

import com.intranet.model.User;

public interface UserService {
	public User findUserByEmail(String email);
	public void save(User user);
	public void update(User user);
}
