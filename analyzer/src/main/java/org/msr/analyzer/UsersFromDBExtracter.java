package org.msr.analyzer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bson.Document;
import org.json.JSONObject;
import org.msr.analyzer.datamodel.MongoDBLayer;
import org.msr.analyzer.github.GithubUser;
import org.msr.analyzer.github.GithubUserMiner;

import com.mongodb.Mongo;
import com.mongodb.client.MongoCollection;
import com.mongodb.util.JSON;

public class UsersFromDBExtracter {

	private String owner;
	private String repo;
	private Collection<GithubUser> setOfUniqueUsers;

	public Collection<GithubUser> getSetOfUniqueUsers() {
		return setOfUniqueUsers;
	}


	public UsersFromDBExtracter(String owner, String repo) {

		this.owner = owner;
		this.repo = repo;
	}


	public void extractUsersFromDB() {
		this.setOfUniqueUsers = MongoDBLayer.getInstance().getUniqueUsers(owner, repo);
		System.out.println("Unique users retrieved from DB = "+this.setOfUniqueUsers.size());
	}
}
