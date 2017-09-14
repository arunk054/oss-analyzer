package org.msr.invokers;

import java.util.ArrayList;
import java.util.List;

import org.msr.controller.Constants;
import org.msr.dblayer.DBInterface;
import org.msr.entities.Repository;
import org.msr.miners.GenericDataMiner;
import org.msr.miners.IssueMiner;
import org.msr.miners.RepositoryMiner;
import org.msr.miners.WatcherMiner;
import org.msr.miners.WrapperRepoMiner;
import org.msr.mongodblayer.MongoDBLayer;

public class InvokeWrapper {

	private List<Repository> repositoriesToMine;

	
	public InvokeWrapper(List<Repository> repositoriesToMine) {
		// TODO Auto-generated constructor stub
		this.repositoriesToMine = repositoriesToMine;
	}
	
	public double[] startMiningAllRepos() {
		DBInterface databaseController = MongoDBLayer.getInstance();
		double[] timeForEachRepoInSeconds = new double[repositoriesToMine.size()];
		int i = 0;
		for (Repository repo: repositoriesToMine) {
			System.out.println();
			System.out.println("Mining: "+repo);
			long beforeTime = System.currentTimeMillis();
			WrapperRepoMiner wrapperRepoMiner = new WrapperRepoMiner(repo.getOwner(), repo.getRepo(), repo.isWriteIfCollectionExists());
			int retry = 0;
			do {
				wrapperRepoMiner.invokeAllMiners(databaseController,null);
				//check if all endpoints have been sucessfully mined
				retry++;
			}while(retry < Constants.MAX_RETRIES_PER_REPO && !wrapperRepoMiner.isAllEndPointsMined(databaseController));
			timeForEachRepoInSeconds[i++] = ((System.currentTimeMillis()-beforeTime)/(1000D*60));
			
		}
		databaseController.closeDB();
		return timeForEachRepoInSeconds;
	}


	
}
