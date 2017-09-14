package org.msr.dblayer;

import org.json.JSONArray;

public interface DBInterface {
	
	public boolean writeRecords(JSONArray records, String collectionName, boolean isOverwriteIfExists) ;
	public void closeDB();

	public boolean isExists(String collectionName);
	
}
