package org.msr.controller;

import java.util.List;
import java.util.Map;

public class Response {

	private String response;
	public String getResponse() {
		return response;
	}

	public Map<String, List<String>> getHeaders() {
		return headers;
	}

	private Map<String, List<String>>  headers;
	
	public Response(String response, Map<String, List<String>> headers) {
		this.response = response;
		this.headers = headers;
	}
}
