package org.msr.analyzer.github;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.msr.analyzer.Configurations;
import org.msr.analyzer.Constants;


public class GithubRateLimitInvoker {

	private String endpointURL;
	private Map<String, List<String>> headers;

	public GithubRateLimitInvoker(int indexOfClient) {
		String params = "client_id=" + Configurations.CLIENT_IDS[indexOfClient] + "&" + "client_secret=" + Configurations.CLIENT_SECRETS[indexOfClient];
		this.endpointURL  = Constants.GITHUB_API_URL+"rate_limit"+"?"+params;
	}
	
	public void invokeRateLimitCheck() {
		GithubAPICore gAPI = new GithubAPICore(this.endpointURL, "rate_limit");
		gAPI.invokeEndpointUntilEnd(true, true);
		headers= gAPI.getCurrentHeaders();
	}
	public boolean isRateLimitExceeded(){
		for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
			String key = entry.getKey();
			
			if ("X-RateLimit-Remaining".equalsIgnoreCase(key)) {
				List<String> listVal = entry.getValue();
				if (listVal != null && listVal.size()>0){
					String val  = listVal.get(0);
					try {
						long remaining = Long.parseLong(val);
						if (remaining > Constants.RATE_LIMIT_MIN_THRESHOLD) {
							return false;
						} else {
							return true;
						}
					} catch (NumberFormatException e) {
						System.out.println("Exception in parsing rate limit header  - remaining limit"+e);
						return false;
					}
				}
				return false;
			}
		}
		//did not find anything so no rate limit exceeded
		return false;
	}
	
	public long getNextResetTime(){
		for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
			String key = entry.getKey();
			
			if ("X-RateLimit-Reset".equalsIgnoreCase(key)) {
				List<String> listVal = entry.getValue();
				if (listVal != null && listVal.size()>0){
					String val  = listVal.get(0);
					try {
						return Long.parseLong(val);
					} catch (NumberFormatException e) {
						System.out.println("Exception in parsing rate limit header Reset time "+e);
						return -1;
					}
				}
				return -1;
			}
		}
		return -1;
	}
	
	public int getRemainingRateLimit(){
		for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
			String key = entry.getKey();
			
			if ("X-RateLimit-Remaining".equalsIgnoreCase(key)) {
				List<String> listVal = entry.getValue();
				if (listVal != null && listVal.size()>0){
					String val  = listVal.get(0);
					try {
						return Integer.parseInt(val);
					} catch (NumberFormatException e) {
						System.out.println("Exception in parsing rate limit header Reset time "+e);
						return -1;
					}
				}
				return -1;
			}
		}
		return -1;
	}
}
