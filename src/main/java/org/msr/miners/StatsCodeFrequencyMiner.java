package org.msr.miners;

public class StatsCodeFrequencyMiner extends GenericDataMiner {

	protected static final String ENDPOINT_NAME = "stats/code_frequency";
	protected static final String PARAMS = "";
	
	//https://developer.github.com/v3/repos/statistics/#get-the-number-of-additions-and-deletions-per-week
	
	public StatsCodeFrequencyMiner(String owner, String repo, boolean isWriteIfCollectionExists) {
		super(owner, repo,ENDPOINT_NAME,PARAMS, isWriteIfCollectionExists);
	}
}
