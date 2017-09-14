package org.msr.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.CharBuffer;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.msr.controller.Constants;
import org.msr.invokers.Configurations;
import org.msr.invokers.StatusChecker;
import org.msr.miners.WrapperRepoMiner;

/*
 * This class does the basic API invocation 
 */
public class GithubAPICore {

	// Construct the URL

	// Each one runs in a separate thread and writes the records to the DB
	// The thread never ends until it gets all records for this API call

	// Invoke the API and get all the records
	// For each invocation check for rate limit
	// Get rate limit end time and create a wait till then
	// If starting request, then get the rate limit of the next account from the
	// array of client id and secret
	// Dont forget to change the header accordingly - Have an array of headers
	// as well.

	private String endpointURL;

	public String getEndpointName() {
		return endpointName;
	}

	public JSONArray getResponseJSONArray() {
		return responseJSONArray;
	}

	public Map<String, List<String>> getCurrentHeaders() {
		return currentHeaders;
	}

	public boolean isCompletedResponse() {
		return isCompletedResponse;
	}

	private String endpointName;
	private JSONArray responseJSONArray;
	// the headers of the last request call.
	private Map<String, List<String>> currentHeaders;
	private boolean isCompletedResponse;

	// endpointSuffix contains the name of repo and the endpointName, and the
	// URL params
	// We need this second param to keep track of which collection in DB should
	// we put this into
	// we might remove this endpointName in future
	public GithubAPICore(String endpointURL, String endpointName) {
		// System.out.println("Creating API Invocation for "+ endpointURL);
		this.endpointName = endpointName;
		this.endpointURL = endpointURL;
		this.responseJSONArray = new JSONArray();
		this.isCompletedResponse = false;
	}

	public boolean invokeEndpointUntilEnd(boolean isNonArrayResponse,
			StatusChecker statusChecker) {
		return invokeEndpointUntilEnd(isNonArrayResponse, false, statusChecker);
	}

	public boolean invokeEndpointUntilEnd(boolean isNonArrayResponse,
			boolean skipRateLimit, StatusChecker statusChecker) {

		int pageCounter = 1;
		int retries = 0;
		int maxPage = -1;
		do {
			long nextInvocationAt = 0;
			if (statusChecker != null && statusChecker.isStopRequested()) {
				System.out.println("Stop requested by user...");
				return false;
			}
			while (!skipRateLimit && nextInvocationAt != -1) {
				nextInvocationAt = rateLimitInvokeAt();
				// Just sleep this thread until the next invocation
				if (nextInvocationAt != -1) {
					System.out
							.println("Rate Limit exceeded so sleeping for (seconds): "
									+ nextInvocationAt);
					WrapperRepoMiner
							.setTextToProgressLogger("Rate Limit exceeded so sleeping for (seconds): "
									+ nextInvocationAt);
					try {

						Thread.sleep(nextInvocationAt * (long) 1000);
						System.out.println("Coming out of sleep...");
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						System.out.println("Sleep interrupted.. trying again");
					}
				}
			}
			String newURL = this.endpointURL;
			if (!isNonArrayResponse)
				newURL += getPageCounterParam(pageCounter);

			// indesx: 0 is response, and 1 is the header
			Response response = null;
			try {
				// if (!skipRateLimit)
				// System.out.println("Invoking Endpoint URL: "+newURL);
				response = getResponse(newURL);
				// System.out.println("Got response : Page: "+pageCounter);
			} catch (IOException e) {

				System.out.println("Error receiving response for "
						+ endpointName + " : " + e.getMessage() + ": "
						+ e.toString());
				retries++;
				System.out.println("Retry " + retries + " Max retries : "
						+ Constants.MAX_GITHUB_API_RETRIES);
				if (retries <= Constants.MAX_GITHUB_API_RETRIES) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
					}
					continue;
				} else {
					System.out.println("Exiting : Max retries exceeded");
					return false;
				}
			}
			currentHeaders = response.getHeaders();

			if (isNonArrayResponse) {
				responseJSONArray.put(new JSONObject(response.getResponse()));
				break;
			} else {
				JSONArray curResponse = new JSONArray(response.getResponse());

				// Add response to existing jsonArray
				for (Iterator<Object> iterator = curResponse.iterator(); iterator
						.hasNext();) {
					responseJSONArray.put(iterator.next());
				}
			}
			if (maxPage == -1) {
				maxPage = getLinkHeaderLastPage(response.getHeaders());
				System.out.println("Maximum num of pages for : " + endpointName
						+ " is " + maxPage);
				// This is a hack because even if github api gives the last page
				// it does not work if it is thousands.
				if (maxPage > Constants.MAX_PAGE_LIMIT) {
					maxPage = Constants.MAX_PAGE_LIMIT;
					System.out
							.println("**Maximum num of pages after Github hack : is "
									+ maxPage);
				}
			}

			pageCounter++;

			// Even if we have a lot of pages Github limits to about 1300 pages
			// of data that you can parse.
			if (pageCounter > maxPage
					|| Constants.DO_NOT_CONTINUE
							.equals(getLinkHeaderRel(response.getHeaders()))) {
				break;
			}

			// Sleep for a few milli seconds before next API call - just to not
			// overwhelm the Github server
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		} while (true);

		return true;
	}

	private String getPageCounterParam(int pageCounter) {

		return "&page=" + pageCounter;
	}

	private Response getResponse(String url) throws IOException {
		// System.out.println("Getting response for "+url);
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// default is GET
		con.setRequestMethod("GET");

		// add request header
		con.setRequestProperty("User-Agent", "OSS study MSR");

		int responseCode = con.getResponseCode();
		// System.out.println("Response Code : " + responseCode);

		// throw an exception if error code
		if (responseCode != 200) {
			int MAX = 256;
			char[] errorChar = new char[MAX];
			int count = 0;
			if (con.getErrorStream() != null) {
				InputStreamReader ir = new InputStreamReader(
						con.getErrorStream());
				count = ir.read(errorChar, 0, MAX);
			}
			throw new IOException("Error response code " + responseCode + " "
					+ String.valueOf(errorChar, 0, (count <= 0) ? 1 : count));
		}

		// read response
		BufferedReader in = new BufferedReader(new InputStreamReader(
				con.getInputStream()));

		String inputLine;
		StringBuffer responseBuffer = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			responseBuffer.append(inputLine);
		}

		return new Response(responseBuffer.toString(), con.getHeaderFields());

	}

	private String getLinkHeaderRel(Map<String, List<String>> headerFields) {
		for (Map.Entry<String, List<String>> entry : headerFields.entrySet()) {
			if ("Link".equalsIgnoreCase(entry.getKey())) {

				List<String> listVal = entry.getValue();

				if (listVal != null && listVal.size() > 0) {
					String strVal = listVal.get(0);
					int indexOfRel = strVal.indexOf("rel=");
					if (indexOfRel != -1) {
						String strNextRel = strVal.substring(indexOfRel,
								strVal.indexOf(',', indexOfRel));
						if (isValidString(strNextRel,
								Constants.LINK_REL_VALID_HEADERS)) {
							return Constants.CONTINUE;
						}

					}
				}
			}
		}
		return Constants.DO_NOT_CONTINUE;
	}

	private int getLinkHeaderLastPage(Map<String, List<String>> headerFields) {
		for (Map.Entry<String, List<String>> entry : headerFields.entrySet()) {
			try {
				if ("Link".equalsIgnoreCase(entry.getKey())) {
					List<String> listVal = entry.getValue();
					if (listVal != null && listVal.size() > 0) {
						String strVal = listVal.get(0);
						String pageStr = "&page=";
						int indexOfLastPage = strVal.lastIndexOf(pageStr);
						if (indexOfLastPage != -1) {
							String lastPageVal = strVal.substring(
									indexOfLastPage + pageStr.length(),
									strVal.indexOf('>', indexOfLastPage));
							return Integer.parseInt(lastPageVal);
						}
					}
				}
			} catch (Exception e) {
				// do nothing but continue
				System.out.println("error parsing headers: " + e);
				// e.printStackTrace();
			}
		}
		return Constants.MAX_PAGE_LIMIT;
	}

	private boolean isValidString(String strNextRel,
			String[] linkRelValidHeaders) {
		for (int i = 0; i < linkRelValidHeaders.length; i++) {
			if (strNextRel.contains(linkRelValidHeaders[i])) {
				return true;
			}
		}
		return false;
	}

	// returns -1 if no rate limit exceeded, else the time when you should
	// invoke again.
	private long rateLimitInvokeAt() {
		GithubRateLimitInvoker ghRateLimit = new GithubRateLimitInvoker(
				Configurations.CURRENT_CLIENT_INDEX);
		ghRateLimit.invokeRateLimitCheck();
		int remaining = ghRateLimit.getRemainingRateLimit();
		if (remaining < Constants.RATE_LIMIT_MIN_THRESHOLD * 2) {
			System.out.println("Remaining rate limit : " + remaining);
			System.out.println();
		}

		if (!ghRateLimit.isRateLimitExceeded())
			return -1;
		long nextResetTime = ghRateLimit.getNextResetTime();
		if (nextResetTime <= 0) {
			return -1;
		}
		long curTimeSecs = System.currentTimeMillis() / (long) 1000;

		System.out.println("next invoke at: "
				+ new Date(nextResetTime * (long) 1000));
		// Add some buffer wait time
		long waitTime = nextResetTime - curTimeSecs + (long) 5;
		return waitTime;
		// return the wait time in secs.

	}

}
