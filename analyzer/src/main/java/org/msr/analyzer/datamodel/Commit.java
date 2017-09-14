package org.msr.analyzer.datamodel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.msr.analyzer.github.GithubUser;

public class Commit implements Comparable<Commit>{

	private GithubUser githubUser;
	public GithubUser getGithubUser() {
		return githubUser;
	}

	public void setGithubUser(GithubUser githubUser) {
		this.githubUser = githubUser;
	}

	private Date date;
	private int year;
	private int month;
	
	public Commit(GithubUser gu, Date date) {
		this.githubUser = gu;
		this.date = date;
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		this.month = cal.get(Calendar.MONTH);
		this.year = cal.get(Calendar.YEAR);
	}

	public int compareTo(Commit otherCommit) {
		return this.date.compareTo(otherCommit.date);	
	}

	//month1 is inclusive and month2 is not basically [month1,month2)
	public boolean isInRange(int month1, int month2, int year) {
		if (this.year == year && this.month>= month1 && this.month<month2) {
			return true;
		}
		return false;
	}
	
	
}
