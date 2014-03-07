package com.expedia.bookings.server;

import org.json.JSONArray;
import org.json.JSONObject;

import com.expedia.bookings.data.ScenarioResponse;

public class ScenarioResponseHandler extends JsonResponseHandler<ScenarioResponse> {

	@Override
	public ScenarioResponse handleJson(JSONObject response) {
		if (response == null) {
			return null;
		}

		ScenarioResponse scenarioResponse = new ScenarioResponse();

		JSONArray links = response.optJSONArray("stubLinks");
		for (int a = 0; a < links.length(); a++) {
			JSONObject link = links.optJSONObject(a);
			String name = link.optString("stubName");
			String url = link.optString("stubUrl");
			scenarioResponse.addScenario(name, url);
		}

		return scenarioResponse;
	}

}
