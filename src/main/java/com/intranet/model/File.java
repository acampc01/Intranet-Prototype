package com.intranet.model;

import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "file")
public class File {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "file_id")
	private int id;

	@Column(name = "name")
	private String name;
	
	@Column(name = "format")
	private String format;
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private Folder parent;

	@Column(name = "creation", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date creation;

	@Column(name = "lastUpdate", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastUpdate;
	
	@Column(name = "download")
	@Temporal(TemporalType.TIMESTAMP)
	private Date download;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User owner;

	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "user_files_s", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "file_id"))
	private Set<User> sharedUsers;

	@PrePersist
	protected void onCreate() {
		lastUpdate = creation = new Date();
	}

	@PreUpdate
	protected void onUpdate() {
		lastUpdate = new Date();
	}
	
	public boolean isShared() {
		return sharedUsers.size() != 0;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public Date getCreation() {
		return creation;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public Folder getParent() {
		return parent;
	}

	public void setParent(Folder parent) {
		this.parent = parent;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public Date getDownload() {
		return download;
	}

	public void setDownload(Date download) {
		this.download = download;
	}
	
	public Set<User> getSharedUsers() {
		return sharedUsers;
	}

	public void setSharedUsers(Set<User> sharedUsers) {
		this.sharedUsers = sharedUsers;
	}
}
