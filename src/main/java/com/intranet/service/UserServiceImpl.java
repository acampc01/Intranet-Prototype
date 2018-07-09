package com.intranet.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.intranet.DemoApplication;
import com.intranet.model.Folder;
import com.intranet.model.Role;
import com.intranet.model.User;
import com.intranet.repository.FolderRepository;
import com.intranet.repository.RoleRepository;
import com.intranet.repository.UserRepository;

@Service("userService")
public class UserServiceImpl implements UserService{

	private final static String UPLOADED_FOLDER = DemoApplication.getFolderPath();

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private FolderRepository folderRepository;

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

		String path = UPLOADED_FOLDER + user.getEmail().split("@")[0];
		java.io.File f = new java.io.File(path);
		if(!f.exists()) {
			userRepository.save(user);
			f.mkdirs();

			Folder folder = new Folder();
			folder.setName(user.getEmail().split("@")[0]);
			folder.setOwner(user);
			folder.setParent(null);
			folderRepository.save(folder);

			user.setRoot(folder);
			userRepository.save(user);
		}
	}

	@Override
	public void update(User user) {
		userRepository.save(user);
	}

	@Override
	public List<User> findAll(User user) {
		if(user.isAdmin())
			return userRepository.findAll();
		return new ArrayList<User>();
	}

	@Override
	public List<User> findConfirms(User user) {
		if(user.isAdmin())
				return userRepository.findAllByActive(0);
		return new ArrayList<User>();
	}

}
