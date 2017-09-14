package org.msr.entities;

import java.util.Date;

import org.msr.analyzer.github.GithubUser;

public class Issue {

	private Date closedAt;
	public Date getClosedAt() {
		return closedAt;
	}

	public void setClosedAt(Date closedAt) {
		this.closedAt = closedAt;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public GithubUser getGu() {
		return gu;
	}

	public void setGu(GithubUser gu) {
		this.gu = gu;
	}

	public boolean isPullRequest() {
		return isPullRequest;
	}

	public void setPullRequest(boolean isPullRequest) {
		this.isPullRequest = isPullRequest;
	}

	private Date createdAt;
	private String state;
	private GithubUser gu;
	private boolean isPullRequest;

	public Issue(boolean isPullRequest, GithubUser gu, String state, Date createdAt, Date closedAt) {
		this.isPullRequest = isPullRequest;
		this.gu = gu;
		this.state = state;
		this.createdAt = createdAt;
		this.closedAt = closedAt;

	}

	public boolean isOpen() {
		
		return ("open".equalsIgnoreCase(state));
	}
}
