package com.expedia.bookings.server;

import org.json.JSONObject;

import android.content.Context;

import com.expedia.bookings.data.BackgroundImageResponse;
import com.expedia.bookings.data.ServerError.ApiMethod;
import com.mobiata.android.Log;
import com.mobiata.android.net.JsonResponseHandler;

public class BackgroundImageResponseHandler extends JsonResponseHandler<BackgroundImageResponse> {
	private Context mContext;

	public BackgroundImageResponseHandler(Context context) {
		mContext = context;
	}

	@Override
	public BackgroundImageResponse handleJson(JSONObject response) {
		BackgroundImageResponse resp = new BackgroundImageResponse();
		ParserUtils.logActivityId(response);
		try {
			resp.fromJson(response);
			resp.addErrors(ParserUtils.parseErrors(mContext, ApiMethod.BACKGROUND_IMAGE, response));
		}
		catch (Exception e) {
			Log.e("Could not parse flight checkout response", e);
			return null;
		}

		return resp;
	}
}
