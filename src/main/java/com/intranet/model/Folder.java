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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "folder")
public class Folder {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "folder_id")
	private int id;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "path")
	private String path;

	@Column(name = "creation", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date creation;

	@Column(name = "lastUpdate", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastUpdate;
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User owner;

	@OneToMany(cascade = CascadeType.REMOVE)
	@JoinTable(name = "folder_file", joinColumns = @JoinColumn(name = "folder_id"), inverseJoinColumns = @JoinColumn(name = "file_id"))
	private Set<File> files;
	
	@PrePersist
	protected void onCreate() {
		lastUpdate = creation = new Date();
	}

	@PreUpdate
	protected void onUpdate() {
		lastUpdate = new Date();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Set<File> getFiles() {
		return files;
	}

	public void setFiles(Set<File> files) {
		this.files = files;
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
	
}
