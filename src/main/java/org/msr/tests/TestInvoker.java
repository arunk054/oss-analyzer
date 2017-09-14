package org.msr.tests;

import org.msr.controller.Constants;
import org.msr.controller.GithubAPICore;
import org.msr.controller.GithubRateLimitInvoker;
import org.msr.dblayer.DBInterface;
import org.msr.invokers.Configurations;
import org.msr.mongodblayer.MongoDBLayer;

public class TestInvoker {

	public static void main(String[] args) {
		String endPointName = "issues";
		String owner = "FStarLang";
		String repo = "FStar";
		String url  = Constants.GITHUB_API_URL +"repos/"+ owner + "/" + repo + "/"+endPointName+"?state=all" + "&client_id=" + Configurations.CLIENT_IDS[Configurations.CURRENT_CLIENT_INDEX] + "&" + "client_secret=" + Configurations.CLIENT_SECRETS[Configurations.CURRENT_CLIENT_INDEX];
		GithubAPICore ghAPI = new GithubAPICore(url, endPointName);
		ghAPI.invokeEndpointUntilEnd(false, false,null);
		System.out.println("Total records " +ghAPI.getResponseJSONArray().length());
		System.out.println("record 1 : "+ghAPI.getResponseJSONArray().get(0));
		
		//Now write to DB
		DBInterface databaseController = MongoDBLayer.getInstance();
		databaseController.writeRecords(ghAPI.getResponseJSONArray(), "fstar_issues", false);
		databaseController.closeDB();
		
	}
}
