package org.msr.miners;

import org.msr.dblayer.DBInterface;
import org.msr.invokers.StatusChecker;

public class StatsParticipationMiner extends GenericDataMiner {

	protected static final String ENDPOINT_NAME = "stats/participation";
	protected static final String PARAMS = "";
	
	//https://developer.github.com/v3/repos/statistics/#get-the-number-of-additions-and-deletions-per-week
	
	public StatsParticipationMiner(String owner, String repo, boolean isWriteIfCollectionExists) {
		super(owner, repo,ENDPOINT_NAME,PARAMS, isWriteIfCollectionExists);
	}
	
	public boolean invokeAndWrite(DBInterface databaseController, StatusChecker st) {
		return super.invokeAndWrite(databaseController, true,st);
	}
	
}
