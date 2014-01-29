package com.expedia.bookings.server;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;

import com.expedia.bookings.data.ScenarioSetResponse;

public class ScenarioSetResponseHandler implements ResponseHandler<ScenarioSetResponse> {

	@Override
	public ScenarioSetResponse handleResponse(HttpResponse response) throws IOException {
		ScenarioSetResponse result = new ScenarioSetResponse();
		result.setSuccess(response != null && response.getStatusLine().getStatusCode() == 200);
		return result;
	}
}
