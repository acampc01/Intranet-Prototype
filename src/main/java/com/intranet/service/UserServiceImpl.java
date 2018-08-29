package com.intranet.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.intranet.model.Notification;
import com.intranet.model.Role;
import com.intranet.model.User;
import com.intranet.repository.NotificationRepository;
import com.intranet.repository.RoleRepository;
import com.intranet.repository.UserRepository;

@Service("userService")
public class UserServiceImpl implements UserService{
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private NotificationRepository notifyRepository;
	
	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Override
	public User findUserByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	@Override
	public void save(User user) {
		user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));

		Role role = roleRepository.findByRole("USER");
		if(role == null) {
			role = new Role();
			role.setRole("USER");
			roleRepository.save(role);
		}
		user.setRoles(new HashSet<Role>(Arrays.asList(role)));
		userRepository.save(user);
		
		Notification notif = new Notification();
		notif.setSender(user);
		notif.setType("Confirm");
		notifyRepository.save(notif);
	}

	@Override
	public void remove(User user) {
		user.setRoot(null);
		user.getSharedFiles().clear();
		user.getSharedFolders().clear();
		user.getRoles().clear();
		userRepository.delete(user);
	}
	
	@Override
	public void update(User user) {
		userRepository.save(user);
	}
	
	@Override
	public User findOne(Long id) {
		return userRepository.getOne(id);
	}

	@Override
	public List<User> findAll() {
		return userRepository.findAllByActive(1);
	}
	
	@Override
	public Set<Notification> findConfirms(User user) {
		if(user.isAdmin())
			return notifyRepository.findAllByType("Confirm");
		return Collections.emptySet();
	}

//	String path = UPLOADED_FOLDER + user.getEmail().split("@")[0];
//	java.io.File f = new java.io.File(path);
//	if(!f.exists()) {
//		userRepository.save(user);
//		f.mkdirs();
//
//		Folder folder = new Folder();
//		folder.setName(user.getEmail().split("@")[0]);
//		folder.setOwner(user);
//		folder.setParent(null);
//		folderRepository.save(folder);
//
//		user.setRoot(folder);
//		userRepository.save(user);
//	}
	
}
