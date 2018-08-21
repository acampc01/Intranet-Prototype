package com.intranet.repository;

import java.util.Date;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.intranet.model.Notification;
import com.intranet.model.User;

@Repository("notifyRepository")
public interface NotificationRepository extends JpaRepository<Notification, Integer>{
	Notification findBySender(User sender);
	Set<Notification> findAllByType(String type);
	Set<Notification> findByTypeAndCreation(String type, Date date);
	Set<Notification> findByTypeAndCreationAfter(String type, Date creation);
}
