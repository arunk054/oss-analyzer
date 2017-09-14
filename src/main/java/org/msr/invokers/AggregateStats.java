package org.msr.invokers;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.print.attribute.SetOfIntegerSyntax;

import org.msr.analyzer.UsersFromDBExtracter;
import org.msr.analyzer.github.GithubUser;
import org.msr.entities.Commit;
import org.msr.entities.DownloadsExtracter;
import org.msr.entities.Issue;
import org.msr.entities.Release;
import org.msr.entities.Repository;
import org.msr.mongodblayer.IssuesExtracter;
import org.msr.mongodblayer.MongoDBLayer;

public class AggregateStats {

	
	private String owner;
	private String repo;
	private Date startDate;
	private Date lastCommitDate;
	private List<Release> releaseList;
	private int usersMS;
	private int usersNonMS;
	private int users;
	private int numContributorsNonMS;
	private int numContributorsMS;
	private int numContributors;
	private Collection<Commit> commits;
	private int msContributions;
	private int totalContributions;
	private int nonMSContributions;
	private Collection<Issue> listOfIssues;
	private int pullReqs;
	private int openPRs;
	private int openBugs;
	private int bugs;
	private int issuesReportedByMS;
	private int stars;
	private int watchers;
	private int forks;
	private long msLinesAdded;
	private long nonMSLinesAdded;
	public String getOwner() {
		return owner;
	}
	public String getRepo() {
		return repo;
	}
	public Date getStartDate() {
		return startDate;
	}
	public Date getLastCommitDate() {
		return lastCommitDate;
	}
	public List<Release> getReleaseList() {
		return releaseList;
	}
	public int getUsersMS() {
		return usersMS;
	}
	public int getUsersNonMS() {
		return usersNonMS;
	}
	public int getUsers() {
		return users;
	}
	public int getNumContributorsNonMS() {
		return numContributorsNonMS;
	}
	public int getNumContributorsMS() {
		return numContributorsMS;
	}
	public int getNumContributors() {
		return numContributors;
	}
	public Collection<Commit> getCommits() {
		return commits;
	}
	public int getMsContributions() {
		return msContributions;
	}
	public int getTotalContributions() {
		return totalContributions;
	}
	public int getNonMSContributions() {
		return nonMSContributions;
	}
	public Collection<Issue> getListOfIssues() {
		return listOfIssues;
	}
	public int getPullReqs() {
		return pullReqs;
	}
	public int getOpenPRs() {
		return openPRs;
	}
	public int getOpenBugs() {
		return openBugs;
	}
	public int getBugs() {
		return bugs;
	}
	public int getIssuesReportedByMS() {
		return issuesReportedByMS;
	}
	public int getStars() {
		return stars;
	}
	public int getWatchers() {
		return watchers;
	}
	public int getForks() {
		return forks;
	}
	public long getMsLinesAdded() {
		return msLinesAdded;
	}
	public long getNonMSLinesAdded() {
		return nonMSLinesAdded;
	}
	public int getPullReqsReportedByMS() {
		return pullReqsReportedByMS;
	}
	public int getOpenPRsByMicrosoft() {
		return openPRsByMicrosoft;
	}

	private int pullReqsReportedByMS;
	private int openPRsByMicrosoft;
	private long ageInYears;

	public String getKey(){
		return this.owner+"/"+this.repo;
	}
	public AggregateStats(String owner, String repo) {
		this.owner = owner;
		this.repo = repo;
	}
	
	public void computeAll() {
		computeAge();
		computeDownloads();
		computeTotalUsers();
		System.out.println("Before computing contributors");
		computeTotalContributors();
		System.out.println("Before computing commits");
		computeCommits();
		System.out.println("Before computing issues");
		extractIssues();
		System.out.println("Before computing lines added");
		computeLinesAdded();
		System.out.println("Finished all");
	}
	public StringBuilder getFormattedOutput() {
		StringBuilder sb = new StringBuilder();
		
		//Set the repo Name
		sb.append("\"Repository:\" "+owner+"/"+repo+"\n");
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		sb.append("\"Start Date:\" "+dateFormat.format(startDate)+"\n");
		sb.append("\"Last Commit Date:\" "+dateFormat.format(lastCommitDate)+"\n");
		sb.append("\"Total Stars / Forks / Watchers:\" "+stars+"  "+ forks + " "+ watchers+"\n");
		sb.append("\"Number of Releases:\" "+releaseList.size()+"\n");
		sb.append("\"Number of Downloads:\" "+getDownloadCount()+"\n");
		sb.append("\"Total Unique Users:\" "+users+"\n");
		sb.append("\"Total Users in Microsoft:\" "+usersMS+"\n");
		sb.append("\"Total Users OUTSIDE Microsoft:\" "+usersNonMS+"\n");
		sb.append("\"Total Contributors:\" "+numContributors+"\n");
		sb.append("\"Total Contributors in Microsoft:\" "+numContributorsMS+"\n");
		sb.append("\"Total Contributors OUTSIDE Microsoft:\" "+numContributorsNonMS+"\n");
		
		sb.append("\"Total Contributions:\" "+totalContributions+"\n");
		sb.append("\"Total Contributions by Microsoft:\" "+msContributions+"\n");
		sb.append("\"Total Contributions OUTSIDE Microsoft:\" "+nonMSContributions+"\n");

		sb.append("\"Total Lines of Code Added:\" "+(msLinesAdded+nonMSLinesAdded)+"\n");
		sb.append("\"Total Lines of Code Added by Microsoft:\" "+msLinesAdded+"\n");
		sb.append("\"Total Lines of Code Added OUTSIDE Microsoft:\" "+nonMSLinesAdded+"\n");
		
	
		sb.append("\"Total Issues:\" "+(bugs+pullReqs)+"\n");
		sb.append("\"Total Issues by Microsoft:\" "+issuesReportedByMS+"\n");
		sb.append("\"Total Issues OUTSIDE Microsoft:\" "+((bugs+pullReqs)-issuesReportedByMS)+"\n");
		
		sb.append("\"Total Pull Requests:\" "+pullReqs+"\n");
		sb.append("\"Total Pull Requests by Microsoft:\" "+pullReqsReportedByMS+"\n");
		sb.append("\"Total Pull Requests OUTSIDE Microsoft:\" "+(pullReqs-pullReqsReportedByMS)+"\n");
		
		sb.append("\"Open Pull Requests:\" "+openPRs+"\n");
		sb.append("\"Open Pull Requests by Microsoft:\" "+openPRsByMicrosoft+"\n");
		sb.append("\"Open Pull Requests OUTSIDE Microsoft:\" "+(openPRs-openPRsByMicrosoft)+"\n");
				
		//Time taken
		sb.append("\"Average Time(hours) to close any Issue:\" "+Math.round(getAverageTimeToClose(false))+"\n");
		sb.append("\"Average Time(hours) to close Pull Requests:\" "+Math.round(getAverageTimeToClose(true))+"\n");

		
		return sb;
	}
	public int getPullReqsOutsideMS(){
		return pullReqs-pullReqsReportedByMS;
	}
	public int getIssuesOutsideMS(){
		return (bugs+pullReqs)-issuesReportedByMS;
	}
	//Average time in hours
	public double getAverageTimeToClose(boolean isPullReq) {
		double totalDiff =0.0;
		int denom = 0;
		
		for (Issue is: listOfIssues) {
			if (is.isOpen() || (isPullReq && isPullReq!=is.isPullRequest()))
				continue;
			
			Date closedAt = is.getClosedAt();
			Date createdAt = is.getCreatedAt();
			if (closedAt != null && createdAt!= null) {
				totalDiff += getDateDiffHours(createdAt,closedAt);
				denom++;
			}
		}
		if (denom!=0) {
			return totalDiff/denom;
		}
		return 0;
	}
	public static double getDateDiffHours(Date date1, Date date2) {
	    long diffInMillies = date2.getTime() - date1.getTime();
	    //return diff as as hours
	    return diffInMillies/(1000D*60*60);
	}
	
	private void computeLinesAdded() {
		//Get the stats/contributors
		List<GithubUser> listOfGU = MongoDBLayer.getInstance().getLinesAdded(owner,repo);
		long msLinesAdded = 0, nonMSLinesAdded=0;
		for (GithubUser gu: listOfGU) {
			if (gu.checkIsAffiliatedToMicrosoft()) {
				msLinesAdded+=gu.getLinesAdded();
			} else {
				nonMSLinesAdded += gu.getLinesAdded();
			}
		}
		this.msLinesAdded = msLinesAdded;
		this.nonMSLinesAdded  = nonMSLinesAdded;
		
	}

	public int getDownloadCountInt() {
		int count = 0;
		for (Release r: releaseList) {
			count+=r.getTotalDownloadCount();
		}
		return count;
	}

	public String getDownloadCount() {
		int count = 0;
		for (Release r: releaseList) {
			count+=r.getTotalDownloadCount();
		}
		return String.valueOf(count);
	}

	//Age
	public void computeAge() {
		Repository repoObj = MongoDBLayer.getInstance().getRepository(owner, repo);
		startDate = repoObj.getStartDate();
		lastCommitDate = repoObj.getLastPushDate();
		long diffInMillies = lastCommitDate.getTime() - startDate.getTime();
	    this.ageInYears = diffInMillies/(3600*1000*24*365);
	}

	public double getAgeInYearsTweaked() {
		return ageInYears;
	}

	public long getAgeInYears() {
		return ageInYears;
	}
	public void setAgeInYears(long ageInYears) {
		this.ageInYears = ageInYears;
	}
	//Get downloads and releases and the dates of releases
	public void computeDownloads() {
		DownloadsExtracter de = new DownloadsExtracter(owner, repo);
		List<Release> releaseList = de.getDownloads();
		this.releaseList = releaseList;
	}
	
	//Total Users
	public void computeTotalUsers() {
		UsersFromDBExtracter ude = new UsersFromDBExtracter(owner, repo);
		ude.extractUsersFromDB();
		Collection<GithubUser> setOfUniqueUsers = ude.getSetOfUniqueUsers();
		int msCount = 0;
		for (GithubUser gu: setOfUniqueUsers) {
			if (gu.checkIsAffiliatedToMicrosoft()) {
				msCount++;
			}
		}
		this.usersMS = msCount;
		this.usersNonMS = setOfUniqueUsers.size() - msCount;
		this.users = setOfUniqueUsers.size();
		this.stars = MongoDBLayer.getInstance().getStars();
		this.watchers = MongoDBLayer.getInstance().getWathchers();
		this.forks= MongoDBLayer.getInstance().getForks();
	}
	
	
	public void computeTotalContributors() {
		Collection<GithubUser> contributors = MongoDBLayer.getInstance().getUniqueUsersFromContributors(owner, repo);
		Map<String, GithubUser> mapOfUsers = new HashMap<String, GithubUser>();
		MongoDBLayer.getInstance().addNewSetOfUsers(mapOfUsers , contributors);
		MongoDBLayer.getInstance().addNewSetOfUsers(mapOfUsers , MongoDBLayer.getInstance().getUniqueUsersFromCommits(owner, repo));
		int msCount = 0;
		for (GithubUser gu: mapOfUsers.values()) {
			if (gu.checkIsAffiliatedToMicrosoft()) {
				msCount++;
			}
		}
		
		
		this.numContributors = mapOfUsers.size();
		this.numContributorsMS = msCount;
		this.numContributorsNonMS =  mapOfUsers.size() - msCount;
	}
	
	public void computeCommits() {
		this.commits = MongoDBLayer.getInstance().getCommitsDate(owner, repo);
		int msContributions=0, nonMSContributions = 0;;
		for (Commit com: commits) {
			if (com.getGithubUser().checkIsAffiliatedToMicrosoft()) {
				msContributions++;
			} else {
				nonMSContributions++;
			}
		}
		this.totalContributions = this.commits.size();
		this.msContributions = msContributions;
		this.nonMSContributions = nonMSContributions;
	}
	
	public void extractIssues() {
		IssuesExtracter ie = new IssuesExtracter(owner, repo);
		this.listOfIssues = ie.getListOfIssues();
		int pullReqs = 0, openPRs=0, openBugs = 0, bugs = 0, reportedByMS=0,PRReportedByMS=0,openPRsByMicrosoft=0;
		for (Issue is: listOfIssues) {
			if (is.isPullRequest()) {
				pullReqs++;
				if (is.isOpen() ){
					openPRs++;
					if (is.getGu().checkIsAffiliatedToMicrosoft()){
						openPRsByMicrosoft++;
					}
				}
				if (is.getGu().checkIsAffiliatedToMicrosoft()){
					PRReportedByMS++;
				}
			} else {
				bugs++;
				if (is.isOpen() ){
					openBugs++;
				}
			}
			if(is.getGu().checkIsAffiliatedToMicrosoft()) {
				reportedByMS++;
			} 
		}
		this.pullReqs = pullReqs ;
		this.openPRs=openPRs;
		this.openBugs = openBugs ;
		this.bugs = bugs;
		this.issuesReportedByMS=reportedByMS;
		this.pullReqsReportedByMS=PRReportedByMS;
		this.openPRsByMicrosoft=openPRsByMicrosoft;
	}
	
}

