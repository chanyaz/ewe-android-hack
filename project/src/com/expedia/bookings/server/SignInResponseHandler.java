package com.expedia.bookings.server;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.data.User;
import com.mobiata.android.Log;
import com.mobiata.android.net.JsonResponseHandler;

public class SignInResponseHandler extends JsonResponseHandler<SignInResponse> {

	private Context mContext;

	public SignInResponseHandler(Context context) {
		mContext = context;
	}

	@Override
	public SignInResponse handleJson(JSONObject response) {
		SignInResponse signInResponse = new SignInResponse();
		try {
			// Check for errors
			signInResponse.addErrors(ParserUtils.parseErrors(mContext, response));
			signInResponse.setSuccess(response.optBoolean("success"));

			if (signInResponse.isSuccess()) {
				User user = new User();
				signInResponse.setUser(user);

				user.setEmail(response.optString("email", null));
				user.setFirstName(response.optString("firstName", null));
				user.setLastName(response.optString("lastName", null));
			}
		}
		catch (JSONException e) {
			Log.e("Could not parse JSON availability response.", e);
			return null;
		}

		return signInResponse;
	}
}
