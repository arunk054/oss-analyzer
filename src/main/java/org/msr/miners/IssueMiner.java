package org.msr.miners;

import org.msr.invokers.Configurations;

public class IssueMiner extends TimeBasedMiner {

	protected static String ENDPOINT_NAME = "issues";
	protected static  String PARAMS = "state=all";
	
	public IssueMiner(String owner, String repo, boolean isWriteIfCollectionExists) {
		super(owner, repo,ENDPOINT_NAME,PARAMS, isWriteIfCollectionExists, Configurations.NUM_YEARS_OF_DATA);
	}
}
