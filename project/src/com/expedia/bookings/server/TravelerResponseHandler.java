package com.expedia.bookings.server;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.data.TravelerInfoResponse;
import com.expedia.bookings.data.User;
import com.mobiata.android.Log;
import com.mobiata.android.net.JsonResponseHandler;

public class TravelerResponseHandler extends JsonResponseHandler<TravelerInfoResponse> {

	private Context mContext;

	public TravelerResponseHandler(Context context) {
		mContext = context;
	}

	@Override
	public TravelerInfoResponse handleJson(JSONObject response) {
		TravelerInfoResponse resp = new TravelerInfoResponse();
		try {
//			// Check for errors
			resp.addErrors(ParserUtils.parseErrors(mContext, ServerError.ApiMethod.PROFILE, response));
			resp.setSuccess(response.optBoolean("success"));
			
			if (resp.isSuccess()) {
				Traveler traveler = new Traveler();
				traveler.fromJson(response);
				resp.setTraveler(traveler);
			}
		}
		catch (Exception e) {
			Log.e("Could not parse JSON Traveler response.", e);
			return null;
		}

		return resp;
	}
}
