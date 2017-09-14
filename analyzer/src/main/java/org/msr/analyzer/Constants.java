package org.msr.analyzer;

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
	
	public static final String OWNER_REPO_SEPARATOR = "__R__";
	public static final String REPO_ENDPOINT_SEPARATOR = "__E__";
	public static final int MAX_GITHUB_API_RETRIES = 10;

	public static final String GITHUB_API_URL = "https://api.github.com/";
	public static int RATE_LIMIT_MIN_THRESHOLD = 10;
	public static final String CONTINUE = "CONTINUE";
	public static final String DO_NOT_CONTINUE = "DO_NOT_CONTINUE";
	public static final String[] LINK_REL_VALID_HEADERS = {"next","last"};
	

}
