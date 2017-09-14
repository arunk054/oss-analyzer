package org.msr.miners;

public class ForksMiner extends GenericDataMiner {

	protected static final String ENDPOINT_NAME = "forks";
	protected static final String PARAMS = "";
	
	
	public ForksMiner(String owner, String repo, boolean isWriteIfCollectionExists) {
		super(owner, repo,ENDPOINT_NAME,PARAMS, isWriteIfCollectionExists);
	}
}
