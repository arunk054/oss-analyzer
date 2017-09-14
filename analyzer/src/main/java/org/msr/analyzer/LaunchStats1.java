package org.msr.analyzer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.msr.analyzer.datamodel.DownloadsExtracter;
import org.msr.analyzer.datamodel.MongoDBLayer;
import org.msr.analyzer.datamodel.Release;
import org.msr.analyzer.datamodel.Repository;
import org.msr.analyzer.github.GithubUser;


public class LaunchStats1 {

	public static void main(String[] args) {
		List<Repository> listToMine = new ArrayList<Repository>();
		listToMine.add(new Repository("FStarLang", "FStar"));
		listToMine.add(new Repository("Z3Prover", "z3"));
		listToMine.add(new Repository("Microsoft", "dafny"));
		listToMine.add(new Repository("Microsoft", "CNTK"));
		listToMine.add(new Repository("Microsoft", "automatic-graph-layout"));
		listToMine.add(new Repository("Kinect", "RoomAliveToolkit"));
		listToMine.add(new Repository("Microsoft", "Ironclad"));
		listToMine.add(new Repository("codalab", "codalab-competitions"));
		listToMine.add(new Repository("AutomataDotNet", "Automata"));
		listToMine.add(new Repository("Microsoft", "TSS.MSR"));
		listToMine.add(new Repository("MicrosoftTranslator", "DocumentTranslator"));
		listToMine.add(new Repository("TabularLang", "CoreTabular"));
		listToMine.add(new Repository("JoinPatterns", "scalablejoins"));
		listToMine.add(new Repository("predictionmachines", "InteractiveDataDisplay"));
		listToMine.add(new Repository("smaillet-ms", "netmf-interpreter"));
		listToMine.add(new Repository("leanprover", "lean"));
		listToMine.add(new Repository("dotnet", "orleans"));
		listToMine.add(new Repository("Microsoft", "ChakraCore"));
		//listToMine.add(new Repository("dotnet", "roslyn"));
		
		
		for(Repository repoObj: listToMine) {
			performAll(repoObj.getOwner(),repoObj.getRepo());	
		}		
		MongoDBLayer.getInstance().closeConnection();
	}

	private static void performAll(String owner,String repo) {
		
		System.out.println(" Repository: "+owner+"/"+repo);
		
		
		MineAndStoreUsers mineUsers = new MineAndStoreUsers(owner, repo, Configurations.IS_WRITE_IF_COLLECTION_EXISTS);
		mineUsers.extractMineAndWrite();
		
		
		DownloadsExtracter downloadExtracter = new DownloadsExtracter(owner, repo);
		List<Release> releases = downloadExtracter.getDownloads();
		//System.out.println("Releases "+releases.size());
		int totalDownloads=0;
		for (Release release: releases ){
			for (int curCount: release.downloadCount) {
				totalDownloads+=curCount;
			}
		}
		//System.out.println("Total downloads: " + totalDownloads);		
		Map<String, GithubUser> tempUsersMap= new HashMap<String, GithubUser>();
		//Get MS contributors
		Collection<GithubUser> tempUsersList = MongoDBLayer.getInstance().getUniqueUsersFromCommits(owner, repo);
		MongoDBLayer.getInstance().addNewSetOfUsers(tempUsersMap, tempUsersList);
		
		//Get all user objects from DB
		Map<String, GithubUser> allUsersMap = MongoDBLayer.getInstance().getAllUsersFromDB(owner,repo);
		int totalUsers = allUsersMap.size();
		int totalContributors = tempUsersMap.size();
		int nonMSContributors = 0;
		int msContributors=0;
		//Go through each user and count number of MS contributors and non-MS contribuitors
		for (GithubUser contributor: tempUsersMap.values()) {
			GithubUser userInDB = allUsersMap.get(contributor.getLogin());
			if (userInDB == null)
				continue;
			boolean isMS = userInDB.checkIsAffiliatedToMicrosoft();
			if (isMS) {
				msContributors++;
			} else {
				nonMSContributors++;
			}
		}
		//System.out.println("Ms and Non MS contributors : "+ msContributors + " "+ nonMSContributors);
		Repository repoFromDB = MongoDBLayer.getInstance().getRepository(owner, repo);
		Date startDate = repoFromDB.getStartDate();
		Date lastActiveDate = repoFromDB.getLastPushDate();
		

		tempUsersList = MongoDBLayer.getInstance().getListOfIssueReporters(owner, repo);
		MongoDBLayer.getInstance().addNewSetOfUsers(tempUsersMap, tempUsersList);
		int totalIssues = tempUsersList.size();
		int msIssues = 0, nonMSIssues = 0;
		for (GithubUser contributor: tempUsersList) {
			GithubUser userInDB = allUsersMap.get(contributor.getLogin());
			if (userInDB == null)
				continue;
			boolean isMS = userInDB.checkIsAffiliatedToMicrosoft();
			if (isMS) {
				msIssues++;
			} else {
				nonMSIssues++;
			}
		}
		
		//printing out for this repo
		System.out.println();
		System.out.println(owner+"/"+repo);
		SimpleDateFormat df = new SimpleDateFormat("MM/yyyy");;
		
		System.out.println(df.format(startDate) + " "+releases.size()+" "+totalDownloads+" "+totalUsers+" "+totalContributors+" "+nonMSContributors+" "+totalIssues+" "+nonMSIssues+" "+df.format(lastActiveDate));
		System.out.println();
	}
}
