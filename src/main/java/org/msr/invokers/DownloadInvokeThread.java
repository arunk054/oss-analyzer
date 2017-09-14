package org.msr.invokers;

import org.msr.miners.WrapperRepoMiner;
import org.msr.mongodblayer.MongoDBLayer;

public class DownloadInvokeThread extends Thread implements StatusChecker {

	private ProgressLogger progressLogger;
	private RepoElement[] repoArr;
	private boolean requestStop;

	public DownloadInvokeThread(RepoElement[] repoArr, ProgressLogger progressLogger) {
		this.progressLogger = progressLogger;
		this.repoArr = repoArr;
		this.requestStop = false;
	}

	@Override
	public void run() {
		progressLogger.setRunning(true);
		//Iterate through each repo and check interrupt status after each miner
		for (RepoElement repoElem: repoArr  ){
			WrapperRepoMiner.setTextToProgressLogger("======= Starting Download: "+repoElem.getOwner()+"/"+repoElem.getRepo()+" ===== ");
			WrapperRepoMiner wrapperRepoMiner = new WrapperRepoMiner(repoElem.getOwner(), repoElem.getRepo());
			wrapperRepoMiner.invokeAllMiners(MongoDBLayer.getInstance(),this);
			if (requestStop) {
				WrapperRepoMiner.setTextToProgressLogger("====== Download Interrupted by user.. Done Aborting. =======");
				break;
			}
			WrapperRepoMiner.setTextToProgressLogger("======= Finished Downloading: "+repoElem.getOwner()+"/"+repoElem.getRepo()+" ===== ");
			WrapperRepoMiner.setTextToProgressLogger("");
		}
		progressLogger.setRunning(false);		
	}

	public void stopThread() {
		this.requestStop = true;
		
	}

	@Override
	public boolean isStopRequested() {
		return requestStop;
	}
	
}
