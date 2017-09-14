package org.msr.analyzer.datamodel;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.Document;
import org.msr.analyzer.Configurations;
import org.msr.analyzer.Constants;
import org.msr.analyzer.github.GithubUser;

import com.mongodb.Block;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class MongoDBLayer {
	private String databaseName;
	private MongoDatabase db;
	private MongoClient mongoClient;
	private static MongoDBLayer currentInstance;

	//just single instance for now.
	private MongoDBLayer() {
		this.databaseName = Configurations.CURRENT_DB_NAME;
		this.mongoClient = new MongoClient( Configurations.DB_SERVER_HOST, Configurations.DB_SERVER_PORT );
		this.db = mongoClient.getDatabase(this.databaseName);
	}

	public static MongoDBLayer getInstance() {
		if (currentInstance != null )
			return currentInstance;
		currentInstance = new MongoDBLayer();
		return currentInstance;
	}

	public void closeConnection() {
		this.mongoClient.close();
	}

	public Collection<GithubUser> getUniqueUsers(String owner, String repo) {
		Map<String,GithubUser> mapOfUsers = new HashMap<String, GithubUser>();
		Collection<GithubUser> curSet = null;

		curSet  = getUniqueUsersFromStars(owner,repo);
		addNewSetOfUsers(mapOfUsers,curSet);

		curSet = getUniqueUsersFromForks(owner, repo);
		addNewSetOfUsers(mapOfUsers,curSet);

		curSet = getUniqueUsersFromSubscribers(owner, repo);
		addNewSetOfUsers(mapOfUsers,curSet);

		curSet = getUniqueUsersFromReleases(owner, repo);
		addNewSetOfUsers(mapOfUsers,curSet);

		curSet = getUniqueUsersFromContributors(owner, repo);
		addNewSetOfUsers(mapOfUsers,curSet);

		curSet = getUniqueUsersFromCommits(owner, repo);
		addNewSetOfUsers(mapOfUsers,curSet);

		curSet = getUniqueUsersFromIssues(owner, repo);
		addNewSetOfUsers(mapOfUsers,curSet);

		curSet = getUniqueUsersFromIssuesComments(owner, repo);
		addNewSetOfUsers(mapOfUsers,curSet);

		return mapOfUsers.values();
	}
	public void addNewSetOfUsers(Map<String, GithubUser> mapOfUsers, Collection<GithubUser> curSet) {
		//Get unique users from Stars


		GithubUser existingUser = null;
		for (GithubUser curUser: curSet) {
			if ((existingUser = mapOfUsers.get(curUser.getLogin())) != null) {
				existingUser.mergeSets(curUser);
			} else {
				mapOfUsers.put(curUser.getLogin(), curUser);
			}
		}
	}

	private Collection<GithubUser> getUniqueUsersFromStars(String owner, String repo) {

		final List<GithubUser> setOfUsers = new ArrayList<GithubUser>();
		//get the collectionName
		final String collectionName = getCollectionName(owner,repo,Constants.ENDPOINT_STARS); 

		//Read each record one by one and extract the username and any other relevant information
		MongoCollection<Document> collection = db.getCollection(collectionName);
		FindIterable<Document> iterable = collection.find();


		iterable.forEach(new Block<Document>() {
			public void apply(Document document) {
				String user;
				try {
					user = document.getString("login");
					GithubUser gu = new GithubUser(user);
					gu.addActivity(Constants.ENDPOINT_STARS);
					setOfUsers.add(gu);
					//System.out.println(user);
				} catch (Exception e) {
					//Just skip
					System.out.println("Error reading user login from "+collectionName);
				}
			}
		});
		return setOfUsers;
	}

	private Collection<GithubUser> getUniqueUsersFromIssuesComments(String owner, String repo) {

		final List<GithubUser> setOfUsers = new ArrayList<GithubUser>();
		//get the collectionName
		final String collectionName = getCollectionName(owner,repo,Constants.ENDPOINT_ISSUES_COMMENTS); 

		//Read each record one by one and extract the username and any other relevant information
		MongoCollection<Document> collection = db.getCollection(collectionName);
		FindIterable<Document> iterable = collection.find();


		iterable.forEach(new Block<Document>() {
			public void apply(Document document) {
				boolean isPullRequest = isPullRequestComment(document);
				String user;
				try {
					Document userDoc = (Document) document.get("user");
					user = userDoc.getString("login");
					GithubUser gu = new GithubUser(user);
					gu.addActivity((isPullRequest)?Constants.ENDPOINT_ISSUES_COMMENTS_PR:Constants.ENDPOINT_ISSUES_COMMENTS);
					setOfUsers.add(gu);
					//System.out.println(user);
				} catch (Exception e) {
					//Just skip
					System.out.println("Error reading user login from "+collectionName);
				}
			}


		});
		return setOfUsers;
	}
	public Collection<GithubUser> getUniqueUsersFromIssues(String owner, String repo) {

		final List<GithubUser> listOfUsers = new ArrayList<GithubUser>();
		//get the collectionName
		final String collectionName = getCollectionName(owner,repo,Constants.ENDPOINT_ISSUES); 

		//Read each record one by one and extract the username and any other relevant information
		MongoCollection<Document> collection = db.getCollection(collectionName);
		FindIterable<Document> iterable = collection.find();


		iterable.forEach(new Block<Document>() {
			public void apply(Document document) {
				String user;
				boolean isPullRequest = MongoDBLayer.isPullRequestIssue(document);
				try {
					Document userDoc = (Document) document.get("user");
					user = userDoc.getString("login");
					GithubUser gu = new GithubUser(user);
					gu.addActivity((isPullRequest)?Constants.ENDPOINT_ISSUES_AUTHOR_PR:Constants.ENDPOINT_ISSUES_AUTHOR);
					listOfUsers.add(gu);
					//System.out.println(user);
				} catch (Exception e) {
					//Just skip
					System.out.println("Error reading user login author from "+collectionName);
				}

				//extract the assignees
				try {
					Document userDoc = (Document) document.get("assignee");
					user = userDoc.getString("login");
					GithubUser gu = new GithubUser(user);
					gu.addActivity((isPullRequest)?Constants.ENDPOINT_ISSUES_ASSIGNEE_PR:Constants.ENDPOINT_ISSUES_ASSIGNEE);
					listOfUsers.add(gu);
				} catch (Exception e) {
					//Just skip
					//System.out.println("Error reading assignee from "+collectionName);
				}
				try {
					List<Document> userDocList = (List<Document>)document.get("assignees");
					for (Document userDoc: userDocList) {
						user = userDoc.getString("login");
						GithubUser gu = new GithubUser(user);
						gu.addActivity(Constants.ENDPOINT_ISSUES_ASSIGNEE);
						listOfUsers.add(gu);						
					}
				} catch (Exception e) {
					//Just skip
					//System.out.println("Error reading assignees from "+collectionName);
				}


			}

		});
		return listOfUsers;
	}

	public static boolean isPullRequestComment(Document document) {
		try {
			String url = document.getString("html_url");
			if (url.contains("/pull/")) {
				return true;
			}
		} catch (Exception e) {
		}
		return false;
	}

	public static boolean isPullRequestIssue(Document document) {
		try {
			Document pr = null;
			if ((pr=(Document) document.get("pull_request")) != null && pr.getString("url")!=null) {
				return true;
			}
		} catch (Exception e) {

		}
		return false;
	}

	private Collection<GithubUser> getUniqueUsersFromForks(String owner, String repo) {

		final List<GithubUser> setOfUsers = new ArrayList<GithubUser>();
		//get the collectionName
		final String collectionName = getCollectionName(owner,repo,Constants.ENDPOINT_FORKS); 

		//Read each record one by one and extract the username and any other relevant information
		MongoCollection<Document> collection = db.getCollection(collectionName);
		FindIterable<Document> iterable = collection.find();


		iterable.forEach(new Block<Document>() {
			public void apply(Document document) {
				String user;
				try {
					Document ownerDoc = (Document) document.get("owner");
					user = ownerDoc.getString("login");
					GithubUser gu = new GithubUser(user);
					gu.addActivity(Constants.ENDPOINT_FORKS);
					setOfUsers.add(gu);
					//System.out.println(user);
				} catch (Exception e) {
					//Just skip
					System.out.println("Error reading user login from "+collectionName);
				}
			}
		});
		return setOfUsers;
	}

	private Collection<GithubUser> getUniqueUsersFromSubscribers(String owner, String repo) {

		final List<GithubUser> setOfUsers = new ArrayList<GithubUser>();
		//get the collectionName
		final String collectionName = getCollectionName(owner,repo,Constants.ENDPOINT_SUBSCRIBERS); 

		//Read each record one by one and extract the username and any other relevant information
		MongoCollection<Document> collection = db.getCollection(collectionName);
		FindIterable<Document> iterable = collection.find();


		iterable.forEach(new Block<Document>() {
			public void apply(Document document) {
				String user;
				try {
					user = document.getString("login");
					GithubUser gu = new GithubUser(user);
					gu.addActivity(Constants.ENDPOINT_SUBSCRIBERS);
					setOfUsers.add(gu);
					//System.out.println(user);
				} catch (Exception e) {
					//Just skip
					System.out.println("Error reading user login from "+collectionName);
				}
			}
		});
		return setOfUsers;
	}

	public Collection<GithubUser> getUniqueUsersFromCommits(String owner, String repo) {

		final List<GithubUser> setOfUsers = new ArrayList<GithubUser>();
		//get the collectionName
		final String collectionName = getCollectionName(owner,repo,Constants.ENDPOINT_COMMITS); 

		//Read each record one by one and extract the username and any other relevant information
		MongoCollection<Document> collection = db.getCollection(collectionName);
		FindIterable<Document> iterable = collection.find();


		iterable.forEach(new Block<Document>() {
			public void apply(Document document) {

				GithubUser authorGU = null,committerGU = null;
				//for debugging
				Document authorDoc=null;
				try {
					authorDoc = (Document) document.get("author");
					String user = authorDoc.getString("login");
					authorGU = new GithubUser(user);
					authorGU.addActivity(Constants.ENDPOINT_COMMITS_AUTHOR);
				} catch (Exception e) {
					//Just skip
					//System.err.println("Error reading author login from "+collectionName + " "+e);
				}

				try {
					Document committerDoc = (Document) document.get("committer");
					String user = committerDoc.getString("login");
					committerGU = new GithubUser(user);
					committerGU.addActivity(Constants.ENDPOINT_COMMITS_COMMITTER);
				} catch (Exception e) {
					//Just skip
					//System.err.println("Error reading committer login from "+collectionName + " "+e);
				}

				try {
					Document commitDoc = (Document) document.get("commit");
					authorDoc = (Document) commitDoc.get("author");
					String email = authorDoc.getString("email");
					authorGU.addEmail(email);
				} catch (Exception e) {
					//System.out.println("Error reading author email from "+collectionName + " : " + e);
				}
				try {
					Document commitDoc = (Document) document.get("commit");
					Document committerDoc = (Document) commitDoc.get("committer");
					String email = committerDoc.getString("email");
					committerGU.addEmail(email);
				} catch (Exception e) {
					//System.out.println("Error reading committer email from "+collectionName+ " : " + e);
				}

				if (authorGU!=null)
					setOfUsers.add(authorGU);
				if (committerGU!=null)
					setOfUsers.add(committerGU);
			}
		});
		return setOfUsers;
	}

	public Collection<Commit> getCommitsDate(String owner, String repo) {

		final List<Commit> listOfCommits = new ArrayList<Commit>();

		//get the collectionName
		final String collectionName = getCollectionName(owner,repo,Constants.ENDPOINT_COMMITS); 

		//Read each record one by one and extract the username and any other relevant information
		MongoCollection<Document> collection = db.getCollection(collectionName);
		FindIterable<Document> iterable = collection.find();


		iterable.forEach(new Block<Document>() {
			public void apply(Document document) {
				GithubUser authorGU = null,committerGU = null;
				//for debugging
				Document authorDoc=null;
				try {
					authorDoc = (Document) document.get("author");
					String user = authorDoc.getString("login");
					authorGU = new GithubUser(user);
					authorGU.addActivity(Constants.ENDPOINT_COMMITS_AUTHOR);
				} catch (Exception e) {
					//Just skip
					//System.err.println("Error reading author login from "+collectionName + " "+e);
				}

				try {
					Document committerDoc = (Document) document.get("committer");
					String user = committerDoc.getString("login");
					committerGU = new GithubUser(user);
					committerGU.addActivity(Constants.ENDPOINT_COMMITS_COMMITTER);
				} catch (Exception e) {
					//Just skip
					//System.err.println("Error reading committer login from "+collectionName + " "+e);
				}


				String dateStr = null;
				Date returnDate = null;
				DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");


				try {
					if (authorGU != null) {
						//System.out.println(authorGU.getLogin());
						authorDoc = (Document) document.get("commit");
						authorDoc = (Document) authorDoc.get("author");
						dateStr = authorDoc.getString("date");
						
						try {
							returnDate = formatter.parse(dateStr);
							listOfCommits.add(new Commit(authorGU,returnDate));
							return;
						} catch (ParseException e) {

						}

					}

				} catch (Exception e) {
					//Just skip
					//System.err.println("Error reading author login from "+collectionName + " "+e);
				}
				

				try {
					if (committerGU!=null) {
						
						authorDoc = (Document) document.get("commit");
						authorDoc = (Document) authorDoc.get("comitter");
						dateStr = authorDoc.getString("date");
						
						try {
							returnDate = formatter.parse(dateStr);
							listOfCommits.add(new Commit(committerGU,returnDate));
							return;
						} catch (ParseException e) {

						}
					}

				} catch (Exception e) {
					//Just skip
					//System.err.println("Error reading author login from "+collectionName + " "+e);
				}
			}
		});
		return listOfCommits;
	}


	private Collection<GithubUser> getUniqueUsersFromContributors(String owner, String repo) {

		final List<GithubUser> setOfUsers = new ArrayList<GithubUser>();
		//get the collectionName
		final String collectionName = getCollectionName(owner,repo,Constants.ENDPOINT_CONTRIBUTORS); 

		//Read each record one by one and extract the username and any other relevant information
		MongoCollection<Document> collection = db.getCollection(collectionName);
		FindIterable<Document> iterable = collection.find();


		iterable.forEach(new Block<Document>() {
			public void apply(Document document) {
				String user;
				try {
					user = document.getString("login");
					GithubUser gu = new GithubUser(user);
					gu.addActivity(Constants.ENDPOINT_CONTRIBUTORS);
					setOfUsers.add(gu);
					//System.out.println(user);
				} catch (Exception e) {
					//Just skip
					System.out.println("Error reading user login from "+collectionName);
				}
			}
		});
		return setOfUsers;
	}

	private Collection<GithubUser> getUniqueUsersFromReleases(String owner, String repo) {

		final List<GithubUser> setOfUsers = new ArrayList<GithubUser>();
		//get the collectionName
		final String collectionName = getCollectionName(owner,repo,Constants.ENDPOINT_RELEASES); 

		//Read each record one by one and extract the username and any other relevant information
		MongoCollection<Document> collection = db.getCollection(collectionName);
		FindIterable<Document> iterable = collection.find();


		iterable.forEach(new Block<Document>() {
			public void apply(Document document) {
				String user;
				try {
					Document authorDoc = (Document) document.get("author");
					user = authorDoc.getString("login");
					GithubUser gu = new GithubUser(user);
					gu.addActivity(Constants.ENDPOINT_RELEASES);
					setOfUsers.add(gu);
					//System.out.println(user);
				} catch (Exception e) {
					//Just skip
					System.out.println("Error reading user login from "+collectionName);
				}

				try {
					List<Document> assetArr = (List<Document>) document.get("assets");
					for (Document assedDoc: assetArr) {
						Document uploaderDoc = (Document) assedDoc.get("uploader");
						user = uploaderDoc.getString("login");
						GithubUser gu = new GithubUser(user);
						gu.addActivity(Constants.ENDPOINT_RELEASES);
						setOfUsers.add(gu);

					}
				} catch (Exception e) {
					//Just skip
					System.out.println("Error reading user login from assets in "+collectionName);
				}

			}
		});
		return setOfUsers;
	}

	public static String getCollectionName(String owner, String repo, String endpointName) {
		return owner+Constants.OWNER_REPO_SEPARATOR+repo+Constants.REPO_ENDPOINT_SEPARATOR+endpointName;
	}


	public boolean isExists(String collectionName) {
		MongoCollection<Document> table = db.getCollection(collectionName);
		return (table.count()>0)?true:false;
	}

	public void insertDocuments(String collectionName, List<Document> documents) {
		this.db.getCollection(collectionName).insertMany(documents);
	}

	public List<Document> getDocuments(String collectionName) {
		FindIterable<Document> iterable = db.getCollection(collectionName).find();

		final List<Document> returnList = new ArrayList<Document>();
		iterable.forEach(new Block<Document>() {
			public void apply(Document document) {

				try {
					returnList.add(document);

				} catch (Exception e) {
					//Just skip
				}
			}
		});
		return returnList;

	}

	public Map<String, GithubUser> getAllUsersFromDB(String owner, String repo) {
		String collectionName = MongoDBLayer.getCollectionName(owner, repo, Constants.ENDPOINT_USERS);
		FindIterable<Document> iterable = db.getCollection(collectionName).find();

		final Map<String,GithubUser> returnMap = new HashMap<String, GithubUser>();
		iterable.forEach(new Block<Document>() {
			public void apply(Document document) {
				try {
					GithubUser curUser = GithubUser.getObjectWithCompany(document);
					returnMap.put(curUser.getLogin(), curUser);
				} catch (Exception e) {
					//Just skip
				}
			}
		});
		return returnMap;
	}

	public Repository getRepository(String owner, String repo) {
		String collectionName = getCollectionName(owner, repo, "");
		List<Document> docs = getDocuments(collectionName);
		if (docs.isEmpty())
			return null;
		Document repoDoc = docs.get(0);
		String dateStr = repoDoc.getString("pushed_at");
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		Date pushDate = null;

		try {
			pushDate  = formatter.parse(dateStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Date startDate = null;
		dateStr = repoDoc.getString("created_at");
		try {
			startDate = formatter.parse(dateStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Repository repoObj = new Repository(owner, repo);
		repoObj.setLastPushDate(pushDate);
		repoObj.setStartDate(startDate);
		return repoObj;
	}

	public Collection<GithubUser> getListOfIssueReporters(String owner, String repo) {

		final List<GithubUser> listOfUsers = new ArrayList<GithubUser>();
		//get the collectionName
		final String collectionName = getCollectionName(owner,repo,Constants.ENDPOINT_ISSUES); 

		//Read each record one by one and extract the username and any other relevant information
		MongoCollection<Document> collection = db.getCollection(collectionName);
		FindIterable<Document> iterable = collection.find();


		iterable.forEach(new Block<Document>() {
			public void apply(Document document) {
				String user;
				boolean isPullRequest = MongoDBLayer.isPullRequestIssue(document);
				try {
					Document userDoc = (Document) document.get("user");
					user = userDoc.getString("login");
					GithubUser gu = new GithubUser(user);
					gu.addActivity((isPullRequest)?Constants.ENDPOINT_ISSUES_AUTHOR_PR:Constants.ENDPOINT_ISSUES_AUTHOR);
					listOfUsers.add(gu);
					//System.out.println(user);
				} catch (Exception e) {
					//Just skip
					System.out.println("Error reading user login author from "+collectionName);
				}
				
			}
		});
		return listOfUsers;
	}

}
