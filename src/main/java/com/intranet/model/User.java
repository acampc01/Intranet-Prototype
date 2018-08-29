
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
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Transient;

@Entity
@Table(name = "user")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "user_id")
	private long id;
	
	@Column(name = "email")
	private String email;
	
	@Column(name = "password")
	@Transient
	private String password;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "last_name")
	private String lastName;
	
	@Column(name = "active")
	private int active = 0;
	
	@CreatedDate
	@Temporal(TemporalType.TIMESTAMP)
	private Date creation = new Date();
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastConnect;
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "folder_id")
	private Folder root;
	
	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
	private Set<Role> roles;
	
	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "user_files_s", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "file_id"))
	private Set<File> sharedFiles;
	
	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "user_folders_s", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "folder_id"))
	private Set<Folder> sharedFolders;
	
//	@ManyToMany(cascade = CascadeType.ALL)
//	@JoinTable(name = "user_notif", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "notif_id"))
//	private Set<Notification> notifys;

	public String encrypt() {
		return Encryptor.encrypt(new Long(id).toString());
	}
	
	public long getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public int getActive() {
		return active;
	}

	public void setActive(int active) {
		this.active = active;
	}

	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	public Set<File> getSharedFiles() {
		return sharedFiles;
	}

	public void setSharedFiles(Set<File> sharedFiles) {
		this.sharedFiles = sharedFiles;
	}

	public Set<Folder> getSharedFolders() {
		return sharedFolders;
	}

	public void setSharedFolders(Set<Folder> sharedFolders) {
		this.sharedFolders = sharedFolders;
	}

//	public Set<Notification> getNotifys() {
//		return notifys;
//	}
//
//	public void setNotifys(Set<Notification> notifys) {
//		this.notifys = notifys;
//	}

	public Folder getRoot() {
		return root;
	}

	public void setRoot(Folder root) {
		this.root = root;
	}

	public Date getCreation() {
		return creation;
	}

	public void setCreation(Date creation) {
		this.creation = creation;
	}

	public Date getLastConnect() {
		return lastConnect;
	}

	public void setLastConnect(Date lastConnect) {
		this.lastConnect = lastConnect;
	}

	public boolean isAdmin() {
		for (Role role : roles) {
			if(role.isAdmin())
				return true;
		}
		return false;
	}
	
	public String toString() {
		return this.getName();
	}

}
