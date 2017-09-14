package org.msr.miners;

import org.msr.dblayer.DBInterface;
import org.msr.invokers.StatusChecker;

public class RepositoryMiner extends GenericDataMiner {

	public static final String ENDPOINT_NAME = "";
	protected static final String PARAMS = "";
	
	public RepositoryMiner(String owner, String repo, boolean isWriteIfCollectionExists) {
		super(owner, repo,ENDPOINT_NAME,PARAMS, isWriteIfCollectionExists);
	}
	public boolean invokeAndWrite(DBInterface databaseController,StatusChecker st) {
		return super.invokeAndWrite(databaseController, true,st);
	}
	
}
