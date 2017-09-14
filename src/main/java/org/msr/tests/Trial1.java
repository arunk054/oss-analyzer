package org.msr.tests;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.msr.entities.Repository;
import org.msr.invokers.InvokeWrapper;

public class Trial1 {

	
	public static void main(String[] args) {
		//Which repositories do you want
		List<Repository> listToMine = new ArrayList<Repository>();
		listToMine.add(new Repository("FStarLang", "FStar"));
//		listToMine.add(new Repository("Z3Prover", "z3"));
//		listToMine.add(new Repository("Microsoft", "dafny"));
//		listToMine.add(new Repository("Microsoft", "CNTK"));
//		listToMine.add(new Repository("Microsoft", "automatic-graph-layout"));
//		listToMine.add(new Repository("Kinect", "RoomAliveToolkit"));
//		listToMine.add(new Repository("Microsoft", "Ironclad"));
//		listToMine.add(new Repository("codalab", "codalab-competitions"));
//		listToMine.add(new Repository("AutomataDotNet", "Automata"));
//		listToMine.add(new Repository("Microsoft", "TSS.MSR"));
//		listToMine.add(new Repository("MicrosoftTranslator", "DocumentTranslator"));
//		listToMine.add(new Repository("TabularLang", "CoreTabular"));
//		listToMine.add(new Repository("JoinPatterns", "scalablejoins"));
//		listToMine.add(new Repository("predictionmachines", "InteractiveDataDisplay"));
		listToMine.add(new Repository("smaillet-ms", "netmf-interpreter"));
//		listToMine.add(new Repository("leanprover", "lean"));
//		listToMine.add(new Repository("dotnet", "orleans"));
//		listToMine.add(new Repository("Microsoft", "ChakraCore"));
//		listToMine.add(new Repository("dotnet", "roslyn"));
		
		
		//Create the Invoke Wrapper



		InvokeWrapper invoker = new InvokeWrapper(listToMine);
		
		double[] timePerRepo = invoker.startMiningAllRepos();
		double totalTimeTaken = 0;
		for (int i = 0; i < timePerRepo.length; i++) {
			totalTimeTaken+=timePerRepo[i];
			System.out.println("Time taken in Minutes for Repo: "+listToMine.get(i)+ " = "+ timePerRepo[i]);
		}
		System.out.println("Total time taken in Minutes: "+totalTimeTaken);
		
	}
}
