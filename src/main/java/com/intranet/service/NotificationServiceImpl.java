package com.intranet.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.intranet.model.Notification;
import com.intranet.model.User;
import com.intranet.repository.NotificationRepository;

@Service("notifyService")
public class NotificationServiceImpl implements NotificationService{
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private NotificationRepository notifyRepository;

	@Override
	public Set<Notification> findByType(String type) {
		return notifyRepository.findAllByType(type);
	}
	
	@Override
	public Notification findBySender(User sender) {
		return notifyRepository.findBySender(sender);
	}

	@Override
	public void remove(Notification notify) {
		notifyRepository.delete(notify);
	}

	@Override
	public void save(Notification notify) {
		notifyRepository.save(notify);
	}

	@Override
	public Notification findOne(Integer id) {
		return notifyRepository.getOne(id);
	}

	@Override
	public Set<Notification> findByCreationAndType(Date date, String type) {
		return notifyRepository.findByTypeAndCreation(type, date);
	}
	
	@Override
	public List<Date> findByTypeLastMonth(String type) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -7);
		List<Notification> ns = new ArrayList<Notification>(notifyRepository.findByTypeAndCreationAfter(type, cal.getTime()));
		Set<Date> set = new HashSet<Date>();
		for (Notification notification : ns) {
			try {
				Date date = new SimpleDateFormat("yyyy-MM-dd").parse(notification.getCreation().toString());
				set.add(date);
			} catch (ParseException e) {
				log.error("Error parsin date");
			}
		}
		List<Date> toSort = new ArrayList<Date>(set);
		Collections.sort(toSort , new Comparator<Date>(){
           	@Override
			public int compare(Date o1, Date o2) {
				 return o1.compareTo(o2);
			}
        });
		return toSort;
	}
	
}
