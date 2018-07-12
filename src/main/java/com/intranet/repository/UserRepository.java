package com.intranet.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.intranet.model.User;

@Repository("userRepository")
public interface UserRepository extends JpaRepository<User, Integer> {
	User getOne(Integer id);
	User findByEmail(String email);
	List<User> findAllByActive(int i);
}
