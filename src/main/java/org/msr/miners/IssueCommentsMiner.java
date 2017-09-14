package org.msr.miners;

import org.msr.invokers.Configurations;

public class IssueCommentsMiner extends TimeBasedMiner {

	protected static String ENDPOINT_NAME = "issues/comments";
	protected static  String PARAMS = "";
	
	public IssueCommentsMiner(String owner, String repo, boolean isWriteIfCollectionExists) {
		super(owner, repo,ENDPOINT_NAME,PARAMS, isWriteIfCollectionExists, Configurations.NUM_YEARS_OF_DATA);
	}
}
