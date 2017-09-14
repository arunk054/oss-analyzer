package org.msr.analyzer.tests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;
import org.msr.analyzer.Configurations;
import org.msr.analyzer.MineAndStoreUsers;
import org.msr.analyzer.datamodel.MongoDBLayer;
import org.msr.analyzer.github.GithubUser;
import org.msr.analyzer.github.GithubUserMiner;
import org.msr.analyzer.github.IsMicrosoft;

public class TestMineUniqueUsers {

	public static void main(String[] args) {
		String owner = "Z3Prover";
		String repo = "z3";
		
		
		
		MineAndStoreUsers mineUsers = new MineAndStoreUsers(owner, repo, Configurations.IS_WRITE_IF_COLLECTION_EXISTS);

		mineUsers.extractMineAndWrite();
		MongoDBLayer.getInstance().closeConnection();
		
		
		
		int moreThan1 = 0, none = 0;
		int nullCompany = 0;
		int yesMicrosoft = 0, noMicrosoft = 0 , unknownMicrosoft = 0;
		int releaseMicrosoft = 0,hasCommitAccess=0;		
		List<GithubUser> moreList = new ArrayList<GithubUser>(); 
		for (GithubUser gu : mineUsers.getSetOfUniqueUsers()) {
			
			gu.updateIsMicrosoft();
			
			JSONObject jo = new JSONObject(gu);
			//System.out.println(gu);
			if (gu.getEmails().size() > 1) {
				moreThan1++;
				moreList.add(gu);
			} else if (gu.getEmails().size() == 0) {
				none++;
			} 
			if (gu.getCompany().isEmpty()) {
				nullCompany++;
			}
			
			if(gu.nonBeanGetisMicrosoft()==IsMicrosoft.YES) {
				yesMicrosoft++;
			} else if (gu.nonBeanGetisMicrosoft() == IsMicrosoft.NO){
				noMicrosoft++;
			} else if (gu.nonBeanGetisMicrosoft() == IsMicrosoft.RELEASE_MANAGER) {
				releaseMicrosoft++;
			} else if (gu.nonBeanGetisMicrosoft() == IsMicrosoft.HAS_COMMIT_ACCESS) {
				hasCommitAccess++;
			} else {
				unknownMicrosoft++;
			}
		}
		//How many users with more than one email
		System.out.println("Total "+ mineUsers.getSetOfUniqueUsers().size());
		System.out.println(" more than 1 = "+ moreThan1);
		System.out.println(" none = "+ none);
		System.out.println("Null companies = "+nullCompany);
		
		System.out.println();
		System.out.println("yes no unknown "+ yesMicrosoft+ " "+noMicrosoft + " "+releaseMicrosoft+ " " + hasCommitAccess+ " " +unknownMicrosoft);
		
		
	}
}
