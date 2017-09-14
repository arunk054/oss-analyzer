package org.msr.analyzer.github;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import java.util.Set;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.msr.analyzer.Constants;

public class GithubUser {

	private static Set<String> listOfTwoFactorLogins;

	private Set<String> activities;

	private IsMicrosoft isMicrosoftEnum;
	private String AffiliatedToMicrosoft;

	

	public String getAffiliatedToMicrosoft() {
		return AffiliatedToMicrosoft;
	}

	public void setAffiliatedToMicrosoft(String affiliatedToMicrosoft) {
		AffiliatedToMicrosoft = affiliatedToMicrosoft;
	}

	public void setIsMicrosoft(IsMicrosoft isMicrosoft) {
		this.isMicrosoftEnum = isMicrosoft;
		this.AffiliatedToMicrosoft = isMicrosoft.toString();
	}

	public Set<String> getActivities() {
		return activities;
	}

	public void addActivity(String activity) {
		this.activities.add(activity);
	}
	public void addActivities(Set<String> activities) {

		this.activities.addAll(activities);
	}

	private String login;
	public String getLogin() {
		return login;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<String> getEmails() {
		return emails;
	}

	public void addEmail(String email) {
		this.emails.add(email); 
	}

	public void addEmails(Collection<? extends String> emails) {
		this.emails.addAll(emails);
	}

	private String name;
	private Set<String> emails;

	private String company;
	private String location;
	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public int getFollowers() {
		return followers;
	}

	public void setFollowers(int followers) {
		this.followers = followers;
	}

	public int getFollowing() {
		return following;
	}

	public void setFollowing(int following) {
		this.following = following;
	}

	private int followers;
	private int following;

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}



	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return login+" : "+name + " : " + company + " : "+ location+ " : "+ getEmailString() + " " + getActivitiesString();
	}
	private String getActivitiesString() {
		StringBuilder sb = new StringBuilder("[");
		for (String s: activities) {
			sb.append(s);
			sb.append(",");
		}
		sb.append("]");
		return sb.toString();
	}

	private String getEmailString() {
		StringBuilder sb = new StringBuilder("[");
		for (String s: emails) {
			sb.append(s);
			sb.append(",");
		}
		sb.append("]");
		return sb.toString();
	}

	public GithubUser(String login, String name, String company, String location) {
		this.login = login;
		this.emails = new HashSet<String>();
		this.company= company;
		this.name = name;
		this.location = location;
		this.followers = 0;
		this.following = 0;
		this.activities = new HashSet<String>();
		this.setIsMicrosoft(isMicrosoftEnum.UNKNOWN);

	}
	public GithubUser(String login) {
		this(login,"","","");		
	}

	@Override
	public boolean equals(Object obj) {
		GithubUser otherUser = (GithubUser) obj;
		return this.login.equals(otherUser.login);
	}

	@Override
	public int hashCode() {
		return this.login.hashCode();
	}

	public void mergeSets(GithubUser userFromGithub) {
		this.addEmails(userFromGithub.getEmails());
		this.addActivities(userFromGithub.activities);

	}
	public void merge(GithubUser userFromGithub) {
		this.addEmails(userFromGithub.getEmails());
		this.addActivities(userFromGithub.activities);
		//we are just overwriting for now.
		this.name = userFromGithub.name;
		this.company = userFromGithub.company;
		this.location = userFromGithub.location;
		//Ideally take average
		this.followers = userFromGithub.followers;
		this.following = userFromGithub.following;

	}

	//Update this user as to whether she is within Microsoft or not or unknown
	public void updateIsMicrosoft() {

		this.setIsMicrosoft(computeIsMicrosoftState());

	}

	private IsMicrosoft computeIsMicrosoftState() {
		
		//First check if the user exists in the Two factor authentication list.
		Set<String> twoFactorLogins = GithubUser.getListOfTwoFactorLogins();
		if (this.login != null && twoFactorLogins.contains(this.login)) {
			System.out.println("User in two factor "+this.login);
			return IsMicrosoft.YES;
		}
		
		
		//Check email addresses
		//valid email domains
		String[] validDomains = {"microsoft","xamarin","xbox" };
		for (String email: this.emails) {
			email = email.toLowerCase();
			for (String validDomain: validDomains) {
				if (email.contains(validDomain)) {
					return IsMicrosoft.YES;
				}
			}
		}
		//Check for company name
		String[] validCompanies = {"microsoft","xamarin","xbox","skype","azure" };
		String curCompany = company.toLowerCase();
		for (String validCompany: validCompanies) {
			if (curCompany.contains(validCompany)) {
				return IsMicrosoft.YES;
			}
		}
		
		//We could check if this person has had commits_committer or a release activity and determine them as a MS employee
		if (this.activities.contains(Constants.ENDPOINT_RELEASES))
			return IsMicrosoft.RELEASE_MANAGER;
		
		if (this.activities.contains(Constants.ENDPOINT_COMMITS_COMMITTER))
			return IsMicrosoft.HAS_COMMIT_ACCESS;
		
		//If company and email are missing then we return as unknown
		if (company.isEmpty() && emails.isEmpty()) {
			return IsMicrosoft.UNKNOWN;
		}
		
		return IsMicrosoft.NO;
	}

	private static Set<String> getListOfTwoFactorLogins() {
		
		if (GithubUser.listOfTwoFactorLogins == null || GithubUser.listOfTwoFactorLogins.isEmpty()) {
			GithubUser.listOfTwoFactorLogins = getTwoFactorLogins();
		}
		return GithubUser.listOfTwoFactorLogins;
	}

	private static Set<String> getTwoFactorLogins() {
		System.out.println("Reading two factor authentication file...");
		//look at the file - people.json in the default directory
		String fileName = "people.json";
		BufferedReader br =null;
		StringBuffer sb = new StringBuffer();
		//empty by default
		Set<String> returnSet = new HashSet<String>();
		try {
			br = 	new BufferedReader(new FileReader(new File(fileName))) ;
			String line = null;
			while ((line=br.readLine())!=null) {
				sb.append(line);
			}
		} catch (FileNotFoundException e) {
			System.out.println("Error reading "+fileName + " "+ e);
			return returnSet ;
		} catch (IOException e) {
			System.out.println("Error reading "+fileName + " "+ e);
			return returnSet;
		}
		try {
			JSONArray jo = new JSONArray(sb.toString());
			int len = jo.length();
			for (int i= 0; i < len; ++i) {
				returnSet.add(jo.getJSONObject(i).getString("ProviderUsername"));
			}
		} catch (JSONException e) {
			System.out.println("ERROR parsing json from "+fileName);
			return returnSet; 
		}
		
		return returnSet;
	}

	public IsMicrosoft nonBeanGetisMicrosoft() {
		return isMicrosoftEnum;
	}

	public static GithubUser getObjectWithCompany(Document document) {
		try {
			String login = document.getString("login");
			String affiliatedToMicrosoft = document.getString("affiliatedToMicrosoft");
			GithubUser gu = new GithubUser(login);
			IsMicrosoft enumMicrosoft = IsMicrosoft.valueOf(affiliatedToMicrosoft);
			gu.setIsMicrosoft(enumMicrosoft);
			return gu;
		} catch (Exception e) {
			//This error not acceptable
			e.printStackTrace();
			return null;
		}
		
	}

	
	public boolean checkIsAffiliatedToMicrosoft() {
		if (isMicrosoftEnum == IsMicrosoft.YES )
			return true;
		return false;
	}
}
