package org.msr.analyzer.datamodel;

import java.util.List;

public class Release {

		public int[] downloadCount ;
		
		//A particular Release may have multiple assets - so this download count is an array
		//TODO add the date of release
		public Release(List<Integer> countList) {
			// TODO Auto-generated constructor stub
			
			this.downloadCount = new int[countList.size()];
			int i = 0;
			for(int x: countList) {
				downloadCount[i++] = x;
			}
		}
	
}
