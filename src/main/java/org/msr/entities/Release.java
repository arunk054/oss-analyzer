package org.msr.entities;

import java.util.Date;
import java.util.List;

public class Release {

		public int[] downloadCount ;
		public List<Date> dateList;
		//A particular Release may have multiple assets - so this download count is an array
		//TODO add the date of release
		public Release(List<Integer> countList, List<Date> dateList) {
			// TODO Auto-generated constructor stub
			
			this.downloadCount = new int[countList.size()];
			int i = 0;
			for(int x: countList) {
				downloadCount[i++] = x;
			}
			this.dateList = dateList;
		}
		public int getTotalDownloadCount() {
			int count = 0;
			for (int d: downloadCount){
				count+=d;
			}
			return count;
		}
		
		public Date getFirstDate() {
			try {
				Date d = dateList.get(0);
				return d;
			} catch (Exception e) {
				return null;
			}
			
			
		}
	
}
