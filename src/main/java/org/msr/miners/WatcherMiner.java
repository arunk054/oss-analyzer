package org.msr.miners;

public class WatcherMiner extends GenericDataMiner {

	protected static final String ENDPOINT_NAME = "subscribers";
	protected static final String PARAMS = "";
	
	
	public WatcherMiner(String owner, String repo, boolean isWriteIfCollectionExists) {
		super(owner, repo,ENDPOINT_NAME,PARAMS, isWriteIfCollectionExists);
	}
}
