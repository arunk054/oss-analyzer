package org.msr.invokers;

public class Configurations {

	
	public static String CURRENT_DB_NAME = "MSR_Outreach_Github_DB";
	public static String DB_SERVER_HOST = "localhost";
	public static int DB_SERVER_PORT = 27017;
	
	//Index0 is arunkaly ID, index1 is arunkiiitb
	public static String[] CLIENT_IDS = {"dummy","dumm"};
	public static String[] CLIENT_SECRETS = {"dummy","dumm"};
	public static int CURRENT_CLIENT_INDEX = 1;
	
	//TimeStamp since we want to collect data => Applicable for all miners that extend TimeBasedMiners. The API should support the since param
	public static  double NUM_YEARS_OF_DATA=5;
}
