package com.expedia.bookings.server;

import org.json.JSONObject;

import android.content.Context;

import com.expedia.bookings.data.ExpediaImageResponse;
import com.expedia.bookings.data.ServerError.ApiMethod;
import com.mobiata.android.Log;

public class ExpediaImageResponseHandler extends JsonResponseHandler<ExpediaImageResponse> {
	private Context mContext;

	public ExpediaImageResponseHandler(Context context) {
		mContext = context;
	}

	@Override
	public ExpediaImageResponse handleJson(JSONObject response) {
		ExpediaImageResponse resp = new ExpediaImageResponse();
		ParserUtils.logActivityId(response);
		try {
			resp.fromJson(response);
			resp.addErrors(ParserUtils.parseErrors(mContext, ApiMethod.BACKGROUND_IMAGE, response));
		}
		catch (Exception e) {
			Log.e("Could not parse Expedia image response", e);
			return null;
		}

		return resp;
	}
}
