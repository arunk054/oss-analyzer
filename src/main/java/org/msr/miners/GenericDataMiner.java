package org.msr.miners;

import org.msr.controller.Constants;
import org.msr.controller.GithubAPICore;
import org.msr.dblayer.DBInterface;
import org.msr.invokers.Configurations;
import org.msr.invokers.StatusChecker;

//super class of all miners
public class GenericDataMiner {
	


	public static final String BASE_REPO_URL = Constants.GITHUB_API_URL+"repos/";
	protected String endpointURL;
	protected String params;  
	protected String endpointName;
	protected String repo;
	protected String owner;
	private boolean isWriteIfCollectionExists;
	private GithubAPICore ghAPI;
	
	public GenericDataMiner(String owner, String repo, String endpointName, String params, boolean isWriteIfCollectionExists) {
		this.owner = owner;
		this.repo = repo;
		String baseParams = "client_id=" + Configurations.CLIENT_IDS[Configurations.CURRENT_CLIENT_INDEX] + "&" + "client_secret=" + Configurations.CLIENT_SECRETS[Configurations.CURRENT_CLIENT_INDEX];
		if (!params.isEmpty())
			baseParams+="&"+params;
		this.endpointName = endpointName;
		this.isWriteIfCollectionExists = isWriteIfCollectionExists;
		this.endpointURL = BASE_REPO_URL+owner+"/"+repo+((this.endpointName.isEmpty())?"":"/"+this.endpointName)+"?"+baseParams;	
	}
	
	public boolean invokeAPI(boolean isNonArrayResponse, StatusChecker statusChecker) {
		WrapperRepoMiner.setTextToProgressLogger("Downloading : "+endpointName + " for: "+owner+"/"+repo);
		ghAPI = new GithubAPICore(this.endpointURL, this.endpointName);
		return ghAPI.invokeEndpointUntilEnd(isNonArrayResponse,statusChecker);
	}
	
	public boolean writeToDB(DBInterface databaseController) {
		WrapperRepoMiner.setTextToProgressLogger("Writing to DB : "+endpointName + " for: "+owner+"/"+repo);
		boolean returnVal = databaseController.writeRecords(ghAPI.getResponseJSONArray(), getCollectionName(), this.isWriteIfCollectionExists);
		return returnVal;
	}
	public boolean isCollectionExists(DBInterface databaseController) {
		return databaseController.isExists(this.getCollectionName());
	}
	private String getCollectionName() {
		return this.owner+Constants.OWNER_REPO_SEPARATOR+this.repo+Constants.REPO_ENDPOINT_SEPARATOR+this.endpointName;
	}

	public boolean invokeAndWrite(DBInterface databaseController, boolean isNonArrayResponse, StatusChecker statusChecker) {
		//Check if we should invoke this or not
		if (!this.isWriteIfCollectionExists && databaseController.isExists(this.getCollectionName())) {
			WrapperRepoMiner.setTextToProgressLogger("Skipping : "+endpointName + " already exists!");
			return false;
		}
		
		if ((invokeAPI(isNonArrayResponse,statusChecker))) {
			return writeToDB(databaseController);
			
		} else {
			System.out.println("ERROR: API: "+this.owner+"/"+this.repo+"/"+this.endpointName);
			WrapperRepoMiner.setTextToProgressLogger("ERROR: API: "+this.owner+"/"+this.repo+"/"+this.endpointName);
			return false;
		}
		
	}

	public boolean invokeAndWrite(DBInterface databaseController, StatusChecker statusChecker) {
		return invokeAndWrite(databaseController, false, statusChecker);
	}
	
}
