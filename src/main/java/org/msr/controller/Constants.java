package org.msr.controller;

import org.msr.miners.RepositoryMiner;
import org.msr.miners.StatsCommitActivityMiner;
import org.msr.miners.StatsContributorsMiner;

public class Constants {
	public static final String ENDPOINT_COMMITS = "commits";
	public static final String ENDPOINT_COMMITS_COMMITTER = "commits_committer";
	public static final String ENDPOINT_COMMITS_AUTHOR = "commits_author";
	public static final String ENDPOINT_STARS = "stargazers";
	public static final String ENDPOINT_FORKS = "forks";
	public static final String ENDPOINT_SUBSCRIBERS = "subscribers";
	public static final String ENDPOINT_RELEASES = "releases";
	public static final String ENDPOINT_CONTRIBUTORS = "contributors";
	public static final String ENDPOINT_ISSUES = "issues";
	public static final String ENDPOINT_ISSUES_COMMENTS = "issues/comments";
	public static final String ENDPOINT_ISSUES_AUTHOR = "issues_author";
	public static final String ENDPOINT_ISSUES_ASSIGNEE = "issues_assignee";

	public static final String ENDPOINT_ISSUES_COMMENTS_PR = "issues/comments_PR";
	public static final String ENDPOINT_ISSUES_AUTHOR_PR = "issues_author_PR";
	public static final String ENDPOINT_ISSUES_ASSIGNEE_PR = "issues_assignee_PR";

	public static final String ENDPOINT_USERS = "users";
	
	//Skipping stats/participation as we dont need 
	public static String[] ALL_ENDPOINTS_ARR = {RepositoryMiner.ENDPOINT_NAME,Constants.ENDPOINT_CONTRIBUTORS,Constants.ENDPOINT_RELEASES,
			Constants.ENDPOINT_ISSUES,Constants.ENDPOINT_ISSUES_COMMENTS,Constants.ENDPOINT_SUBSCRIBERS,Constants.ENDPOINT_STARS,
			Constants.ENDPOINT_FORKS,StatsContributorsMiner.ENDPOINT_NAME,StatsCommitActivityMiner.ENDPOINT_NAME, Constants.ENDPOINT_COMMITS};

	public static final String COLLECTION_REPO_LIST_ = "Repo_List";
	public static final String COLLECTION_SETTINGS = "Settings";
	
	public static final String GITHUB_API_URL = "https://api.github.com/";

	public static final int MAX_GITHUB_API_RETRIES=10;
	public static int RATE_LIMIT_MIN_THRESHOLD = 100;
	public static final String CONTINUE = "CONTINUE";
	public static final String DO_NOT_CONTINUE = "DO_NOT_CONTINUE";
	public static final String[] LINK_REL_VALID_HEADERS = {"next","last"};
	
	public static final String OWNER_REPO_SEPARATOR = "__R__";
	public static final String REPO_ENDPOINT_SEPARATOR = "__E__";

	public static final int MAX_PAGE_LIMIT = 1200;

	public static final int MAX_RETRIES_PER_REPO = 10;
	public static final String MONGODB_DATA_DIR = "/data/db";//"C:\\data\\db";
	

}
