package org.msr.miners;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class TimeBasedMiner extends GenericDataMiner{

	public TimeBasedMiner(String owner, String repo, String endpointName, String params,
			boolean isWriteIfCollectionExists, double timeInYears) {
		//((params.isEmpty())?"":"&")+ TimeBasedMiner.getTimeParamSince(timeInYears)
		super(owner, repo, endpointName, params, isWriteIfCollectionExists);
	}
	
	//gets the time in years, returns including the & prefix for convenience
	public static String getTimeParamSince(double timeInYears) {

		long currentTimeMilis = System.currentTimeMillis();
		long millisSince  = (long)(timeInYears*getMillisPerYear());
		
		long millisTimeAtSince = currentTimeMilis - millisSince;
		
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		TimeZone tz = TimeZone.getTimeZone("GMT");
		df.setTimeZone(tz);
		String dateAsISO = df.format(new Date(millisTimeAtSince));
		System.out.println(dateAsISO);
		return "since="+dateAsISO;
	}

	private static long getMillisPerYear() {
		
		return 365L * 24 * 3600 * 1000;
	}
//	
//	public static void main(String[] args) {
//		System.out.println(TimeBasedMiner.getTimeParamSince(0.5));
//	}

}
