
package com.intranet.model;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.Transient;

@Entity
@Table(name = "user")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "user_id")
	private int id;
	
	@Column(name = "email")
	private String email;
	
	@Column(name = "password")
	@Length(min = 5)
	@Transient
	private String password;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "last_name")
	private String lastName;
	
	@Column(name = "active")
	private int active;
	
	@ManyToMany(cascade = CascadeType.REMOVE)
	@JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
	private Set<Role> roles;
	
	@ManyToMany(cascade = CascadeType.REMOVE)
	@JoinTable(name = "user_file", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "file_id"))
	private Set<File> files;
	
	@ManyToMany(cascade = CascadeType.REMOVE)
	@JoinTable(name = "user_folder", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "folder_id"))
	private Set<Folder> folders;
	
	@ManyToMany(cascade = CascadeType.REMOVE)
	@JoinTable(name = "sharedFiles", joinColumns = @JoinColumn(name = "file_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
	private Set<File> sharedFiles;
	
	@ManyToMany(cascade = CascadeType.REMOVE)
	@JoinTable(name = "sharedFolders", joinColumns = @JoinColumn(name = "folder_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
	private Set<Folder> sharedFolders;

	public int getId() {
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

	public Set<File> getFiles() {
		return files;
	}

	public void setFiles(Set<File> files) {
		this.files = files;
	}

	public Set<Folder> getFolders() {
		return folders;
	}

	public void setFolder(Set<Folder> folders) {
		this.folders = folders;
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

	public void setFolders(Set<Folder> folders) {
		this.folders = folders;
	}

}
