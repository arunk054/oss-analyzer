package org.msr.analyzer.github;

import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.msr.analyzer.Configurations;
import org.msr.analyzer.Constants;
import org.msr.analyzer.github.GithubAPICore;


public class GithubUserMiner {

	public static final String ENDPOINT_NAME = "users";
	private String login;

	//returns the details of a user using the Github API
	public GithubUserMiner(String login) {
		this.login=login;	
	}
	
	public GithubUser getUserFromGithub() {
		String baseParams = "client_id=" + Configurations.CLIENT_IDS[Configurations.CURRENT_CLIENT_INDEX] + "&" + "client_secret=" + Configurations.CLIENT_SECRETS[Configurations.CURRENT_CLIENT_INDEX];
		String endpointURL = Constants.GITHUB_API_URL+GithubUserMiner.ENDPOINT_NAME+"/"+this.login+"?"+baseParams;

		//invoke the API
		GithubAPICore ghAPI = new GithubAPICore(endpointURL, GithubUserMiner.ENDPOINT_NAME);
		boolean returnVal = ghAPI.invokeEndpointUntilEnd(true);
		if (!returnVal) {
			System.out.println("ERROR invoking users api for "+login);
			return null;
		}
		JSONArray ja = ghAPI.getResponseJSONArray();
		if (ja.length() == 0) {
			System.out.println("ERROR empty object for "+login);
			return null;
		}
		JSONObject userJson = (JSONObject) ja.get(0);
		String company = "",name="", location = "";
		int followers = 0, following = 0;
		Set<String> emailSet = new HashSet<String>(); 
		try {
			company = (String) userJson.get("company");
		}catch (Exception e) {
			
		}
		
		try {
			name = (String) userJson.get("name");
		}catch (Exception e) {
			
		}
		
		try {
			location = (String) userJson.get("location");
		}catch (Exception e) {
			
		}
		
		try {
			String emailStr = (String) userJson.get("email");
			if (emailStr!=null && !emailStr.isEmpty()) {
				String[] emails = emailStr.split("[,;]");
				for (String email : emails) {
					emailSet.add(email);
				}
			}
		}catch (Exception e) {
			
		}
		try {
			followers = (int) userJson.getInt("followers");
		}catch (Exception e) {
			
		}

		try {
			following = (int) userJson.getInt("following");
		}catch (Exception e) {
			
		}
		GithubUser gu = new GithubUser(login,name,company,location);
		gu.addEmails(emailSet);
		gu.setFollowers(followers);
		gu.setFollowing(following);
		
		return gu;
	}
}
