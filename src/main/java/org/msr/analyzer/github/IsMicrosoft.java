package org.msr.analyzer.github;

public enum IsMicrosoft {
	YES,
	RELEASE_MANAGER,//Mostly treat this as a NO category.
	HAS_COMMIT_ACCESS,//Identify this as a close collaborator
	NO,
	UNKNOWN, 
}
