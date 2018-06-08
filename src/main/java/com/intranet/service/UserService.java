package com.intranet.service;

import com.intranet.model.User;

public interface UserService {
	public User findUserByEmail(String email);
	public void saveUser(User user);
	public void update(User user);
}
