package org.msr.miners;

public class StatsContributorsMiner extends GenericDataMiner {

	public static final String ENDPOINT_NAME = "stats/contributors";
	protected static final String PARAMS = "";
	
	//https://developer.github.com/v3/repos/statistics/#get-contributors-list-with-additions-deletions-and-commit-counts
	
	public StatsContributorsMiner(String owner, String repo, boolean isWriteIfCollectionExists) {
		super(owner, repo,ENDPOINT_NAME,PARAMS, isWriteIfCollectionExists);
	}
}
