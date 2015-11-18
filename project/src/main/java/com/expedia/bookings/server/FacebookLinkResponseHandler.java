package com.expedia.bookings.server;

import org.json.JSONObject;

import android.content.Context;

import com.expedia.bookings.data.FacebookLinkResponse;
import com.expedia.bookings.data.FacebookLinkResponse.FacebookLinkResponseCode;
import com.mobiata.android.Log;

// TODO: this is legacy code, it should be replaced AccountLib ASAP

public class FacebookLinkResponseHandler extends JsonResponseHandler<FacebookLinkResponse> {

	public FacebookLinkResponseHandler(Context context) {
	}

	@Override
	public FacebookLinkResponse handleJson(JSONObject response) {
		FacebookLinkResponse fbLinkResponse = new FacebookLinkResponse();
		ParserUtils.logActivityId(response);

		try {
			String respStatus = response.getString("status");
			FacebookLinkResponseCode respCode = FacebookLinkResponseCode.valueOf(respStatus);
			fbLinkResponse.setFacebookLinkResponseCode(respCode);
		}
		catch (Exception ex) {
			Log.e("Exception parsing response", ex);
		}
		return fbLinkResponse;
	}

}
