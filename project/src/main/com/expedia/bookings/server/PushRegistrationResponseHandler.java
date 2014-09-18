package com.expedia.bookings.server;

import org.json.JSONObject;

import android.content.Context;

import com.expedia.bookings.data.PushNotificationRegistrationResponse;
import com.mobiata.android.Log;

public class PushRegistrationResponseHandler extends JsonResponseHandler<PushNotificationRegistrationResponse> {

	public PushRegistrationResponseHandler(Context context) {
	}

	@Override
	public PushNotificationRegistrationResponse handleJson(JSONObject response) {
		PushNotificationRegistrationResponse pushRegResp = new PushNotificationRegistrationResponse();
		Log.d("PushRegistrationResponseHandler handleJson(): " + response.toString());

		try {
			String status = response.optString("status", "fail");
			pushRegResp.setSuccess(status.equalsIgnoreCase("success"));
		}
		catch (Exception ex) {
			Log.e("Exception checking PushNotificationRegistrationResponse status", ex);
		}

		return pushRegResp;
	}

}
