package org.msr.tests;

import java.util.ArrayList;
import java.util.Collection;

import java.util.List;

import org.msr.analyzer.UsersFromDBExtracter;
import org.msr.analyzer.github.GithubUser;
import org.msr.entities.DownloadsExtracter;
import org.msr.entities.Release;
import org.msr.entities.Repository;
import org.msr.mongodblayer.MongoDBLayer;


public class TestDownloads {

	public static void main(String[] args) {
		List<Repository> listToMine = new ArrayList<Repository>();
		listToMine.add(new Repository("FStarLang", "FStar"));
		//listToMine.add(new Repository("Z3Prover", "z3"));
		//listToMine.add(new Repository("Microsoft", "CNTK"));
		//listToMine.add(new Repository("Microsoft", "automatic-graph-layout"));
		//listToMine.add(new Repository("Kinect", "RoomAliveToolkit"));
		//listToMine.add(new Repository("Microsoft", "Ironclad"));
		listToMine.add(new Repository("smaillet-ms", "netmf-interpreter"));
		
		for(Repository repoObj: listToMine) {
			performAll(repoObj.getOwner(),repoObj.getRepo());	
		}		
		MongoDBLayer.getInstance().closeConnection();
	}

	private static void performAll(String owner,String repo) {
		
		System.out.println(" Repository: "+owner+"/"+repo);
		
		
		UsersFromDBExtracter udb = new UsersFromDBExtracter(owner, repo);
		udb.extractUsersFromDB();
		
		
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
		//Get MS contributors
		Collection<GithubUser> usersInCommits = MongoDBLayer.getInstance().getUniqueUsersFromCommits(owner, repo);
		System.out.println("Unique users from Commits : "+usersInCommits.size());
		
		int nonMSContributors = 0;
		int msContributors=0;
		//Go through each user and count number of MS contributors and non-MS contribuitors
		for (GithubUser contributor:usersInCommits) {
			
			boolean isMS = contributor.checkIsAffiliatedToMicrosoft();
			if (isMS) {
				msContributors++;
			} else {
				
				nonMSContributors ++;
			}
		}
		System.out.println("Ms and Non MS contributors : "+ msContributors + " "+ nonMSContributors);
		
	}
}
