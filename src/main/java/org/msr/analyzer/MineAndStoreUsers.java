package org.msr.analyzer;

import
 java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bson.Document;
import org.json.JSONObject;
import org.msr.analyzer.github.GithubUser;
import org.msr.analyzer.github.GithubUserMiner;
import org.msr.controller.Constants;
import org.msr.mongodblayer.MongoDBLayer;

import com.mongodb.Mongo;
import com.mongodb.client.MongoCollection;
import com.mongodb.util.JSON;

public class MineAndStoreUsers {
	private String owner;
	private String repo;
	private boolean isWriteIfExists;
	private boolean isSkipWriting;
	private UsersFromDBExtracter udb;
	
	public Collection<GithubUser> getSetOfUniqueUsers() {
		return udb.getSetOfUniqueUsers();
	}


	public MineAndStoreUsers(String owner, String repo, boolean isWriteIfExists) {

		this.owner = owner;
		this.repo = repo;
		this.isWriteIfExists = isWriteIfExists;
		this.isSkipWriting = false;
	}

	public void setUsersFromDBExtracter(UsersFromDBExtracter udb) {
		this.udb = udb;
	}
	public void extractMineAndWrite() {
		UsersFromDBExtracter udb = new UsersFromDBExtracter(owner, repo);
		udb.extractUsersFromDB();
		mineUserInfoFromGithub();
		writeUsersToDB();
	}

	public void mineUserInfoFromGithub(){
		System.out.println("Retrieving users from Github...");
		//Check if the Mongo collection already exists
		String collectionName = MongoDBLayer.getCollectionName(owner, repo, Constants.ENDPOINT_USERS);
		if (!this.isWriteIfExists && MongoDBLayer.getInstance().isExists(collectionName)) {
			this.isSkipWriting = true;
			System.out.println("Skipping.. " +collectionName + " already exists in DB... So, not mining");
			return;
		}
		
		for (GithubUser gu : udb.getSetOfUniqueUsers() ) {
			GithubUserMiner guMiner = new GithubUserMiner(gu.getLogin());
			GithubUser userFromGithub = guMiner.getUserFromGithub();
			if (userFromGithub != null) {
				gu.merge(userFromGithub);
			}
			gu.updateIsMicrosoft();
			
		}
		
	}

	public void writeUsersToDB() {
		System.out.println("Writing users to DB");
		//Check if the Mongo collection already exists
		String collectionName = MongoDBLayer.getCollectionName(owner, repo, Constants.ENDPOINT_USERS);
		if (this.isSkipWriting || (!this.isWriteIfExists && MongoDBLayer.getInstance().isExists(collectionName))) {
			System.out.println("Skipping.. " +collectionName + " already exists in DB... So, not Writing");
			return;
		}
		
		//Get the list of document objects
		List<Document> docList = new ArrayList<Document>();
		for (GithubUser gu: this.udb.getSetOfUniqueUsers()) {
			JSONObject jo = new JSONObject(gu);
			docList.add(Document.parse(jo.toString()));
		}
		
		
		//Writing all users one by one to the DB
		MongoDBLayer.getInstance().insertDocuments(collectionName,docList);
	}
}
