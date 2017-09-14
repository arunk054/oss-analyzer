package org.msr.gui;

import java.util.ArrayList;
import java.util.List;

import org.msr.invokers.RepoElement;
import org.msr.mongodblayer.MongoDBLayer;

//Static methods
public class DBOperationsFromUI {

	
	public static List<RepoElement> getCompleteRepos(RepoElement[] repos){
		List<RepoElement> returnList = new ArrayList<RepoElement>();
		for (RepoElement repoElem: repos) {
			boolean isComplete = MongoDBLayer.getInstance().isDownloadComplete(repoElem.getOwner(), repoElem.getRepo());
			if (isComplete){
				returnList.add(repoElem);
			}
		}
		return returnList;
	}
	
}
