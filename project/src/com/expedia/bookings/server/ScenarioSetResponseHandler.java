package com.expedia.bookings.server;

import java.io.IOException;

import com.expedia.bookings.data.ScenarioSetResponse;
import com.squareup.okhttp.Response;

public class ScenarioSetResponseHandler implements ResponseHandler<ScenarioSetResponse> {

	@Override
	public ScenarioSetResponse handleResponse(Response response) throws IOException {
		ScenarioSetResponse result = new ScenarioSetResponse();
		result.setSuccess(response != null && response.code() == 200);
		return result;
	}
}
