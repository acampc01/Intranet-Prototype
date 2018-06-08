package com.intranet.service;

import java.util.Arrays;
import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.intranet.model.Role;
import com.intranet.model.User;
import com.intranet.repository.RoleRepository;
import com.intranet.repository.UserRepository;

@Service("userService")
public class UserServiceImpl implements UserService{

	@Autowired
	private UserRepository userRepository;
	
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
        user.setActive(1);
       
        Role role = roleRepository.findByRole("USER");
        if(role == null) {
        	role = new Role();
        	role.setRole("USER");
        	roleRepository.save(role);
        }
        user.setRoles(new HashSet<Role>(Arrays.asList(role)));
        
		userRepository.save(user);
	}

	@Override
	public void update(User user) {
		userRepository.save(user);
	}
	
}
