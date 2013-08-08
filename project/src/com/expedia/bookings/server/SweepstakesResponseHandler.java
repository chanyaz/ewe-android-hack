package com.expedia.bookings.server;

import org.json.JSONObject;

import com.expedia.bookings.data.SweepstakesResponse;
import com.mobiata.android.net.JsonResponseHandler;

public class SweepstakesResponseHandler extends JsonResponseHandler<SweepstakesResponse> {
	@Override
	public SweepstakesResponse handleJson(JSONObject response) {
		SweepstakesResponse sweepstakesResponse = new SweepstakesResponse();
		sweepstakesResponse.setSweepstakesPromotionEnabled(response.optBoolean("sweepstakesPromotionEnabled", false));

		return sweepstakesResponse;
	}
}
