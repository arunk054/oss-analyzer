package org.msr.miners;

import org.msr.invokers.Configurations;

public class CommitsMiner extends TimeBasedMiner {

	protected static String ENDPOINT_NAME = "commits";
	protected static  String PARAMS = "";
	
	public CommitsMiner(String owner, String repo, boolean isWriteIfCollectionExists) {
		super(owner, repo,ENDPOINT_NAME,PARAMS, isWriteIfCollectionExists, Configurations.NUM_YEARS_OF_DATA);
	}
}
