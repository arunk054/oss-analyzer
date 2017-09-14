package org.msr.miners;

public class ReleasesMiner extends GenericDataMiner {

	protected static final String ENDPOINT_NAME = "releases";
	protected static final String PARAMS = "";
	
	
	public ReleasesMiner(String owner, String repo, boolean isWriteIfCollectionExists) {
		super(owner, repo,ENDPOINT_NAME,PARAMS, isWriteIfCollectionExists);
	}
}
