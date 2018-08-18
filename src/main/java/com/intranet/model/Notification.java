package com.intranet.model;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "notif")
public class Notification {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "notif_id")
	private int id;
	
	@Column(name = "type")
	private String type;
	
	@Column(name = "content")
	private String content;
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User sender;
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "file_id", nullable = true)
	private File file;

	@Column(name = "creation", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date creation;
	
//	@ManyToMany(mappedBy="notifys")
//	private Set<User> users;

	@PrePersist
	protected void onCreate() {
		creation = new Date();
	}

	public String encrypt() {
		return Encryptor.encrypt(new Integer(id).toString());
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public User getSender() {
		return sender;
	}

	public void setSender(User sender) {
		this.sender = sender;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Date getCreation() {
		return creation;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}
	
//	public Set<User> getUsers() {
//		return users;
//	}
//
//	public void setUsers(Set<User> users) {
//		this.users = users;
//	}

	
	
}
