package org.msr.entities;

import java.util.Date;

public class Repository {

	private Date startDate, lastPushDate;
	
	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getLastPushDate() {
		return lastPushDate;
	}

	public void setLastPushDate(Date lastPushDate) {
		this.lastPushDate = lastPushDate;
	}

	private String repo;
	public String getRepo() {
		return repo;
	}

	public void setRepo(String repo) {
		this.repo = repo;
	}

	public String getOwner() {
		return owner;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.owner+"/"+this.repo;
	}
	
	public void setOwner(String owner) {
		this.owner = owner;
	}

	private String owner;
	private boolean isWriteIfCollectionExists;


	public Repository(String owner, String repo, boolean isWriteIfCollectionExists ) {
		this.owner = owner;
		this.repo = repo;
		this.isWriteIfCollectionExists = isWriteIfCollectionExists ;
	}

	
	public Repository(String owner, String repo) {
		this.owner = owner;
		this.repo = repo;
		this.isWriteIfCollectionExists = false;
	}

	public boolean isWriteIfCollectionExists() {
		// TODO Auto-generated method stub
		return isWriteIfCollectionExists;
	}
}
