package org.msr.miners;

import java.util.ArrayList;
import java.util.List;

import org.msr.dblayer.DBInterface;
import org.msr.entities.Repository;
import org.msr.invokers.DownloadInvokeThread;
import org.msr.invokers.ProgressLogger;
import org.msr.invokers.StatusChecker;

/*
 * Invokes all repo miners for a given repo
 */
public class WrapperRepoMiner {

	
	private String repo;
	private String owner;
	private boolean isWriteIfCollectionExists;
	private List<GenericDataMiner> genericDataMiners;

	private static ProgressLogger progressLogger = null;
	
	
	public static boolean setTextToProgressLogger(String s) {
		if (progressLogger != null) {
			progressLogger.setText(s);
			return true;
		}
		return false;
	}
	
	public static void setProgressLogger(ProgressLogger progressLogger) {
		WrapperRepoMiner.progressLogger = progressLogger;
	}

	public WrapperRepoMiner(String owner, String repo) {
		this(owner,repo,false);
	}
	public WrapperRepoMiner(String owner, String repo, boolean isWriteIfCollectionExists) {
		this.owner = owner;
		this.repo=repo;
		this.isWriteIfCollectionExists = isWriteIfCollectionExists;
		this.genericDataMiners = getListOfDataMiners();
	}
	//Ideally we want to return status
	public void invokeAllMiners(DBInterface databaseController, StatusChecker statusChecker) {
			for (GenericDataMiner dataMiner: genericDataMiners) {
				if (statusChecker != null && statusChecker.isStopRequested()) {
					return;
				}
				dataMiner.invokeAndWrite(databaseController,statusChecker);
			}
	}	
	
	private List<GenericDataMiner> getListOfDataMiners() {
		List<GenericDataMiner> list = new ArrayList<GenericDataMiner>();
		list.add(new RepositoryMiner(this.owner,this.repo,this.isWriteIfCollectionExists));
		list.add(new ContributorsMiner(this.owner,this.repo,this.isWriteIfCollectionExists));
		
		list.add(new ReleasesMiner(this.owner,this.repo,this.isWriteIfCollectionExists));
		
		list.add(new IssueMiner(this.owner,this.repo,this.isWriteIfCollectionExists));
		list.add(new IssueCommentsMiner(this.owner,this.repo,this.isWriteIfCollectionExists));
		
		list.add(new WatcherMiner(this.owner,this.repo,this.isWriteIfCollectionExists));
		list.add(new StarsMiner(this.owner,this.repo,this.isWriteIfCollectionExists));
		list.add(new ForksMiner(this.owner,this.repo,this.isWriteIfCollectionExists));
		
		list.add(new StatsContributorsMiner(this.owner,this.repo,this.isWriteIfCollectionExists));
		list.add(new StatsCommitActivityMiner(this.owner,this.repo,this.isWriteIfCollectionExists));
		//Skip for now since difference between owner and rest is not very useful
		//list.add(new StatsParticipationMiner(this.owner,this.repo,this.isWriteIfCollectionExists));

		//The is big one
		list.add(new CommitsMiner(this.owner,this.repo,this.isWriteIfCollectionExists));
		
		return list;
	}

	public boolean isAllEndPointsMined(DBInterface databaseController) {
		//Go through each miner
		for (GenericDataMiner dataMiner: genericDataMiners) {
			if (!dataMiner.isCollectionExists(databaseController))
				return false;
		}
		return true;
	}
}

