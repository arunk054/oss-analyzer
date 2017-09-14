package org.msr.gui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.msr.analyzer.github.GithubUser;
import org.msr.entities.Commit;
import org.msr.entities.DownloadsExtracter;
import org.msr.entities.Issue;
import org.msr.entities.Release;
import org.msr.entities.Repository;
import org.msr.invokers.AggregateStats;
import org.msr.mongodblayer.IssuesExtracter;
import org.msr.mongodblayer.MongoDBLayer;

import com.mongodb.client.model.Aggregates;

public class TrendBuilder {

	private Integer interval;
	private String owner;
	private String repo;
	private Date startDate;
	private Date lastCommitDate;
	//Does not contain the startdate so the first element is the time of startDate+Interval
	private ArrayList<Date> timeList;
	private List<Release> releaseList;
	private int[] downloads;
	private int[] totalContributions;
	private int[] nonMSContributions;
	private int[] msContributions;
	private long[] msLinesAddedArr;
	private long[] nonMSLinesAddedArr;
	private int[] pullReqs;
	private int[] openPRs;
	private int[] openBugs;
	private int[] bugs;
	private int[] reportedByMS;
	private int[] PRReportedByMS;
	private int[] openPRsByMicrosoft;
	private long[] totalLinesAddedArr;
	private int[] totalIssues;
	private int[] releases;
	
	public TrendBuilder(Integer choice, String owner, String repo) {
		this.owner = owner;
		this.repo = repo;
		interval = choice;
		computeTimeStamps();
	}
	
	public StringBuilder getFormattedOutput() {
		StringBuilder sb = new StringBuilder();
		
		//Set the repo Name
		sb.append("\"Repository:\" "+owner+"/"+repo+"\n");
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		sb.append("\"Start Date:\" "+dateFormat.format(startDate)+"\n");
		sb.append("\"Current Date:\" "+dateFormat.format(new Date())+"\n\n");
		sb.append("\"Time Periods:\" "+getIntervalsAsString()+"\n");
		sb.append("\"Releases:\" "+getDataAsString(this.releases)+"\n");
		sb.append("\"Downloads:\" "+getDataAsString(this.downloads)+"\n");
		
		sb.append("\"Total Contributions:\" "+getDataAsString(totalContributions)+"\n");
		sb.append("\"Total Contributions by Microsoft:\" "+getDataAsString(msContributions)+"\n");
		sb.append("\"Total Contributions OUTSIDE Microsoft:\" "+getDataAsString(nonMSContributions)+"\n");

		sb.append("\"Total Lines of Code Added:\" "+getDataAsString(totalLinesAddedArr)+"\n");
		sb.append("\"Total Lines of Code Added by Microsoft:\" "+getDataAsString(msLinesAddedArr)+"\n");
		sb.append("\"Total Lines of Code Added OUTSIDE Microsoft:\" "+getDataAsString(nonMSLinesAddedArr)+"\n");
		
	
		sb.append("\"Total Issues:\" "+getDataAsString(totalIssues)+"\n");
		sb.append("\"Total Issues by Microsoft:\" "+getDataAsString(reportedByMS)+"\n");
		sb.append("\"Total Issues OUTSIDE Microsoft:\" "+getDataAsString(getDiffArr(totalIssues,reportedByMS))+"\n");
		
		sb.append("\"Total Pull Requests:\" "+getDataAsString(pullReqs)+"\n");
		sb.append("\"Total Pull Requests by Microsoft:\" "+getDataAsString(PRReportedByMS)+"\n");
		sb.append("\"Total Pull Requests OUTSIDE Microsoft:\" "+getDataAsString(getDiffArr(pullReqs,PRReportedByMS))+"\n");
		
		sb.append("\"Open Pull Requests:\" "+getDataAsString(openPRs)+"\n");
		sb.append("\"Open Pull Requests by Microsoft:\" "+getDataAsString(openPRsByMicrosoft)+"\n");
		sb.append("\"Open Pull Requests OUTSIDE Microsoft:\" "+getDataAsString(getDiffArr(openPRs,openPRsByMicrosoft))+"\n");
		return sb;
	}

	
	public StringBuilder getOutputForAnalysis() {
		StringBuilder sb = new StringBuilder();
		
		//Set the repo Name
		sb.append("\"Repository:\" "+owner+"/"+repo+"\n");
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		sb.append("\"Start Date:\" "+dateFormat.format(startDate)+"\n");
		sb.append("\"Time Periods:\" "+getIntervalsAsString()+"\n");
		sb.append("\"Total Contributions:\" "+getDataAsString(totalContributions)+"\n");
		sb.append("\"Total Contributions by Microsoft:\" "+getDataAsString(msContributions)+"\n");
		sb.append("\"Total Contributions OUTSIDE Microsoft:\" "+getDataAsString(nonMSContributions)+"\n");

		return sb;
	}

	//arr1 - arr2
	private int[] getDiffArr(int[] arr1, int[] arr2) {
		int[] returnArr = new int[arr1.length];
		for (int i = 0; i < arr1.length;++i) {
			returnArr[i] = arr1[i]-arr2[i];
		}
		return returnArr;
	}
	//sorry for duplicating the code just so much in a hurry
	private String getDataAsString(long[] values) {
		StringBuilder sb = new StringBuilder();
		//sb.append("0 ");
		for (long v: values) {
			sb.append(v+ " ");
		}
		return sb.toString();
	}
	private String getDataAsString(int[] values) {
		StringBuilder sb = new StringBuilder();
		//sb.append("0 ");
		for (int v: values) {
			sb.append(v+ " ");
		}
		return sb.toString();
	}
	private String getIntervalsAsString() {

		String[] monthArr = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
		StringBuilder sb = new StringBuilder();
		Calendar cal = Calendar.getInstance();
		//cal.setTime(startDate);
		//start with start date
		//sb.append("'"+monthArr[cal.get(Calendar.MONTH)]+"-"+cal.get(Calendar.YEAR));
		//sb.append(" ");
		
		for (Date d : timeList) {
			cal.setTime(d);
			sb.append("'"+monthArr[cal.get(Calendar.MONTH)]+"-"+cal.get(Calendar.YEAR));
			sb.append(" ");
		}
		return sb.toString();
	}
	
	public void computeAll() {
		computeDownloads();
		computeCommits();
		computeLinesAdded();
		extractIssues();
		
	}
	
	private void computeAge() {
		Repository repoObj = MongoDBLayer.getInstance().getRepository(owner, repo);
		startDate = repoObj.getStartDate();
		lastCommitDate = repoObj.getLastPushDate();
		
	}
	
	private void computeCommits() {
		Collection<Commit> commits = MongoDBLayer.getInstance().getCommitsDate(owner, repo);
		this.msContributions= new int[timeList.size()];
		this.nonMSContributions = new int[timeList.size()];
		this.totalContributions = new int[timeList.size()];
		for (Commit com: commits) {
			Date d = com.getDate();

			int index = getIndexInTimeList(d);
			if (com.getGithubUser().checkIsAffiliatedToMicrosoft()) {
				msContributions[index]++;
			} else {
				nonMSContributions[index]++;
			}
			totalContributions[index]++;	
		}
		
	}
	
	
	
	private void computeLinesAdded() {
		this.msLinesAddedArr = new long[timeList.size()];
		this.nonMSLinesAddedArr = new long[timeList.size()];
		this.totalLinesAddedArr = new long[timeList.size()];
		int i =0;
		long prevTime = 0;
		for (Date d: timeList) {
			//Get the stats/contributors
			List<GithubUser> listOfGU = MongoDBLayer.getInstance().getLinesAdded(owner,repo,d,prevTime);
			prevTime = d.getTime();
			long msLinesAdded = 0, nonMSLinesAdded=0;
			for (GithubUser gu: listOfGU) {
				if (gu.checkIsAffiliatedToMicrosoft()) {
					msLinesAdded+=gu.getLinesAdded();
				} else {
					nonMSLinesAdded += gu.getLinesAdded();
				}
			}
			msLinesAddedArr[i] = msLinesAdded;
			nonMSLinesAddedArr[i] = nonMSLinesAdded;
			this.totalLinesAddedArr[i] = nonMSLinesAdded +msLinesAdded; 
			i++;
		}
		
	}
	
	private void computeDownloads() {
		DownloadsExtracter de = new DownloadsExtracter(owner, repo);
		List<Release> releaseList = de.getDownloads();
		this.releaseList = releaseList;
		this.downloads = new int[timeList.size()];
		this.releases = new int[timeList.size()];
		for (Release release: releaseList) {
			Date d = release.getFirstDate();
			int index = getIndexInTimeList(d);
			downloads[index]+=release.getTotalDownloadCount();
			releases[index]++;
		}
	}
	
	private int getIndexInTimeList(Date givenDate) {
		if (givenDate == null)
			return timeList.size()-1;
		int i = 0;
		for (Date date: timeList) {
			if (givenDate.equals(date) || givenDate.before(date)){
				return i;
			}
			i++;
		}
		//just return the last index
		return timeList.size()-1;
	}

	private void computeTimeStamps() {
		//Get the start date of the project
		computeAge();
		//Get the current Date		
		Date now = new Date();
		
		double diffInHours = AggregateStats.getDateDiffHours(this.startDate,now);
		//Get the diff into Months
		double diffInMonths = diffInHours/(24*30);
		long months = Math.round(diffInMonths);
		this.timeList = new ArrayList<Date>();

		timeList.add(now);
		
		//Then compile the list of dates
		int m = 0;
		for (m = interval; m < months; m+=interval) {
			Date newDate = addMonths(now,-m);
			timeList.add(newDate);
		}

		timeList.add(startDate);
		
		//reverse the list
		int len = timeList.size();
		for (int i = 0, j = len -1; i < j; ++i , --j) {
			Date ithDate = timeList.get(i);
			timeList.set(i, timeList.get(j));
			timeList.set(j, ithDate);
		}
		
	}
	public static Date addMonths(Date date, int months)
	{
	    Calendar cal = Calendar.getInstance();
	    cal.setTime(date);
	    cal.add(Calendar.MONTH, months);   
	    return cal.getTime();
	}
	
	public void extractIssues() {
		IssuesExtracter ie = new IssuesExtracter(owner, repo);
		Collection<Issue> listOfIssues = ie.getListOfIssues();
		this.pullReqs = new int[timeList.size()];
		this.openPRs= new int[timeList.size()];
		this.openBugs = new int[timeList.size()];
		this.bugs = new int[timeList.size()];
		this.reportedByMS= new int[timeList.size()];
		this.PRReportedByMS=new int[timeList.size()]; 
		this.openPRsByMicrosoft=new int[timeList.size()];
		this.totalIssues=new int[timeList.size()];
		
		for (Issue is: listOfIssues) {
			int index = getIndexInTimeList(is.getCreatedAt());
			if (is.isPullRequest()) {
				pullReqs[index]++;
				if (is.isOpen() ){
					openPRs[index]++;
					if (is.getGu().checkIsAffiliatedToMicrosoft()){
						openPRsByMicrosoft[index]++;
					}
				}
				if (is.getGu().checkIsAffiliatedToMicrosoft()){
					PRReportedByMS[index]++;
				}
			} else {
				bugs[index]++;
				if (is.isOpen() ){
					openBugs[index]++;
				}
			}
			this.totalIssues[index]++;
			if(is.getGu().checkIsAffiliatedToMicrosoft()) {
				reportedByMS[index]++;
			} 
		}
	}

}
