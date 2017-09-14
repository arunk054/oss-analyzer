package org.msr.analyzer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.msr.analyzer.datamodel.Commit;
import org.msr.analyzer.datamodel.DownloadsExtracter;
import org.msr.analyzer.datamodel.MongoDBLayer;
import org.msr.analyzer.datamodel.Release;
import org.msr.analyzer.datamodel.Repository;
import org.msr.analyzer.github.GithubUser;


public class LaunchStats2 {

	public static void main(String[] args) {
		List<Repository> listToMine = new ArrayList<Repository>();
		listToMine.add(new Repository("Microsoft", "CNTK"));
		
		for(Repository repoObj: listToMine) {
			performAll(repoObj.getOwner(),repoObj.getRepo());	
		}		
		MongoDBLayer.getInstance().closeConnection();
	}

	private static void performAll(String owner,String repo) {
		
		System.out.println(" Repository: "+owner+"/"+repo);
		
		
		MineAndStoreUsers mineUsers = new MineAndStoreUsers(owner, repo, Configurations.IS_WRITE_IF_COLLECTION_EXISTS);
		mineUsers.extractMineAndWrite();
		Map<String, GithubUser> allUsersMap = MongoDBLayer.getInstance().getAllUsersFromDB(owner,repo);
		
		for (GithubUser gu: allUsersMap.values()) {
			gu.updateIsMicrosoft();
		}
		
		System.out.println(owner+"/"+repo);

		System.out.println("==== Printing the dates ==== ");
		//0 is MS and 1 is nonMS
		int yearStart = 2014;
		int yearEnd = 2016;
		int monthIncrement = 6;

		String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
		for (int i = yearStart; i <= yearEnd; i++) {
			for (int j = 0; j <= 12-monthIncrement; j+=monthIncrement) {
				System.out.println(months[j]+"-"+months[j+monthIncrement-1]+" " +i);
			}
		}
		
		Collection<Commit> commits = MongoDBLayer.getInstance().getCommitsDate(owner, repo);
		for (int i = yearStart; i <= yearEnd; i++) {
			for (int j = 0; j <= 12-monthIncrement; j+=monthIncrement) {
				int[] arr = getCommitCounts(commits,j,j+monthIncrement,i,allUsersMap);
				System.out.println(arr[0]+" "+arr[1]);
			}
		}
		
		System.out.println();
	}

	private static int[] getCommitCounts(Collection<Commit> commits, int month1, int month2, int year, Map<String, GithubUser> allUsersMap) {
		int[] arr = new int[2];
		for (Commit commit: commits) {
			if (commit.isInRange(month1, month2, year)) {
				String login = commit.getGithubUser().getLogin();
				GithubUser userInDB = allUsersMap.get(login);
				if (userInDB == null){
					System.out.println("ERROR: USER NOT FOUND***");
					continue;
				}
				if (userInDB.checkIsAffiliatedToMicrosoft())
				{
					arr[0]++;
				} else {
					arr[1]++;
				}
			}
		}
		return arr;
	}
}
