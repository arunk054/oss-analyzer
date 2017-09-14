package org.msr.entities;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.msr.controller.Constants;
import org.msr.mongodblayer.MongoDBLayer;

import com.mongodb.client.MongoDatabase;

public class DownloadsExtracter {


	private String owner;
	private String repo;

	public DownloadsExtracter(String owner, String repo) {
		this.owner = owner;
		this.repo = repo;
	}

	public List<Release> getDownloads() {
		String collectionName = MongoDBLayer.getCollectionName(owner, repo, Constants.ENDPOINT_RELEASES);
		List<Release> returnList = new ArrayList<Release>();
		//Get all records from the database
		List<Document> documents = MongoDBLayer.getInstance().getDocuments(collectionName);
		for (Document releaseDoc : documents) {
			try {
				List<Document> assets = (List<Document>) releaseDoc.get("assets");
				List<Integer> countList = new ArrayList<Integer>();
				List<Date> dateList = new ArrayList<Date>();
				for (Document asset: assets) {
					try {
						Object countObj = asset.get("download_count");
						int count = 0;
						if (countObj instanceof Integer) {
							count = ((Integer) countObj).intValue();
						} else if (countObj instanceof Long) {
							count = ((Long) countObj).intValue();
						}
						countList.add(count);
					}  catch (Exception e) {	
					}
					
					String dateStr = asset.getString("created_at");
					DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
					Date createdAtDate = null;

					try {
						createdAtDate  = formatter.parse(dateStr);
					} catch (ParseException e) {
					}
					
					if (createdAtDate == null) {
						dateStr = asset.getString("updated_at");
						formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

						try {
							createdAtDate  = formatter.parse(dateStr);
						} catch (ParseException e) {
						}
					}
					dateList.add(createdAtDate);
					
				}
				returnList.add(new Release(countList, dateList));

			} catch (Exception e) {
				//its going to be an empty list
			}
		}
		return returnList;
	}


}
