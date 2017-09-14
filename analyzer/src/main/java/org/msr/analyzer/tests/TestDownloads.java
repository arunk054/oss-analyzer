package org.msr.analyzer.tests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.msr.analyzer.Configurations;
import org.msr.analyzer.MineAndStoreUsers;
import org.msr.analyzer.datamodel.DownloadsExtracter;
import org.msr.analyzer.datamodel.MongoDBLayer;
import org.msr.analyzer.datamodel.Release;
import org.msr.analyzer.datamodel.Repository;
import org.msr.analyzer.github.GithubUser;


public class TestDownloads {

	public static void main(String[] args) {
		List<Repository> listToMine = new ArrayList<Repository>();
		listToMine.add(new Repository("FStarLang", "FStar"));
		listToMine.add(new Repository("Z3Prover", "z3"));
		listToMine.add(new Repository("Microsoft", "CNTK"));
		listToMine.add(new Repository("Microsoft", "automatic-graph-layout"));
		listToMine.add(new Repository("Kinect", "RoomAliveToolkit"));
		listToMine.add(new Repository("Microsoft", "Ironclad"));
		
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
		System.out.println("Releases "+releases.size());
		int totalDownloads=0;
		for (Release release: releases ){
			for (int curCount: release.downloadCount) {
				totalDownloads+=curCount;
			}
		}
		System.out.println("Total downloads: " + totalDownloads);		
		Map<String, GithubUser> userMap= new HashMap<String, GithubUser>();
		//Get MS contributors
		Collection<GithubUser> usersInCommits = MongoDBLayer.getInstance().getUniqueUsersFromCommits(owner, repo);
		MongoDBLayer.getInstance().addNewSetOfUsers(userMap, usersInCommits);
		
		//Get all user objects from DB
		Map<String, GithubUser> allUsersMap = MongoDBLayer.getInstance().getAllUsersFromDB(owner,repo);
		
		int nonMSContributors = 0;
		int msContributors=0;
		//Go through each user and count number of MS contributors and non-MS contribuitors
		for (GithubUser contributor: userMap.values()) {
			GithubUser userInDB = allUsersMap.get(contributor.getLogin());
			if (userInDB == null)
				continue;
			boolean isMS = userInDB.checkIsAffiliatedToMicrosoft();
			if (isMS) {
				msContributors++;
			} else {
				
				nonMSContributors ++;
			}
		}
		System.out.println("Ms and Non MS contributors : "+ msContributors + " "+ nonMSContributors);
		
	}
}
