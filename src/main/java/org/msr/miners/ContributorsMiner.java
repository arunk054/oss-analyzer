package org.msr.miners;

public class ContributorsMiner extends GenericDataMiner {

	protected static final String ENDPOINT_NAME = "contributors";
	protected static final String PARAMS = "";
	
	
	public ContributorsMiner(String owner, String repo, boolean isWriteIfCollectionExists) {
		super(owner, repo,ENDPOINT_NAME,PARAMS, isWriteIfCollectionExists);
	}
}
