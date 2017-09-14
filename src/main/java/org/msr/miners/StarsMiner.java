package org.msr.miners;

public class StarsMiner extends GenericDataMiner {

	protected static final String ENDPOINT_NAME = "stargazers";
	protected static final String PARAMS = "";
	
	
	public StarsMiner(String owner, String repo, boolean isWriteIfCollectionExists) {
		super(owner, repo,ENDPOINT_NAME,PARAMS, isWriteIfCollectionExists);
	}
}
