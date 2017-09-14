package org.msr.mongodblayer;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.msr.analyzer.github.GithubUser;
import org.msr.controller.Constants;
import org.msr.entities.Issue;

import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

public class IssuesExtracter {

	
	
	private String repo;
	private String owner;

	public IssuesExtracter(String owner, String repo) {
		this.owner = owner;
		this.repo = repo;
		
	}
	
	public Collection<Issue> getListOfIssues() {


		//get the collectionName
		final String collectionName = MongoDBLayer.getCollectionName(owner,repo,Constants.ENDPOINT_ISSUES); 

		//Read each record one by one and extract the username and any other relevant information
		MongoCollection<Document> collection = MongoDBLayer.getInstance().getCollection(collectionName);
		FindIterable<Document> iterable = collection.find();
		final Collection<Issue> listOfIssues = new ArrayList<Issue>();

		iterable.forEach(new Block<Document>() {
			public void apply(Document document) {
				String user;
				boolean isPullRequest = MongoDBLayer.isPullRequestIssue(document);
				GithubUser gu =null;
				try {
					Document userDoc = (Document) document.get("user");
					user = userDoc.getString("login");
					gu=new GithubUser(user);
					gu.addActivity((isPullRequest)?Constants.ENDPOINT_ISSUES_AUTHOR_PR:Constants.ENDPOINT_ISSUES_AUTHOR);
					//System.out.println(user);
				} catch (Exception e) {
					//Just skip
					System.out.println("Error reading user login author from "+collectionName);
				}
				String state = "open";
				//Get the state
				try {
					String extractedState = (String) document.get("state");
					if (extractedState!=null && !extractedState.isEmpty()) {
						state = extractedState;
					}
					
				} catch (Exception e) {
				}
				
				//Get the created at date


				Date createdAt = null;

				try {
					String dateStr = document.getString("created_at");
					DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
					createdAt  = formatter.parse(dateStr);
				} catch (Exception e) {
				}
								
				//Get the closed at Date if state is not open
				Date closedAt = null;
				try {
					String dateStr = document.getString("closed_at");
					DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
					closedAt  = formatter.parse(dateStr);
				} catch (Exception e) {
				}
				
				Issue is = new Issue(isPullRequest,gu,state,createdAt,closedAt);

				listOfIssues.add(is);
				//Finally create the issue object including the flag isPullRequest
				

			}

		});
		return listOfIssues;
	}
}
