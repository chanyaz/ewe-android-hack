package com.expedia.bookings.server;

import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.expedia.bookings.data.ServerError.ApiMethod;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.TravelerCommitResponse;
import com.mobiata.android.Log;

public class TravelerCommitResponseHandler extends JsonResponseHandler<TravelerCommitResponse> {

	private final Traveler mTraveler;
	private final Context mContext;

	public TravelerCommitResponseHandler(Context context, Traveler trav) {
		mContext = context;
		mTraveler = trav;
	}

	@Override
	public TravelerCommitResponse handleJson(JSONObject response) {
		TravelerCommitResponse travUpdateResponse = new TravelerCommitResponse();
		ParserUtils.logActivityId(response);
		try {
			travUpdateResponse.fromJson(response);
			travUpdateResponse.addErrors(ParserUtils.parseErrors(ApiMethod.COMMIT_TRAVELER, response));
			if (!mTraveler.hasTuid() && travUpdateResponse.isSucceeded()
					&& !TextUtils.isEmpty(travUpdateResponse.getTuid())) {
				mTraveler.setTuid(Long.valueOf(travUpdateResponse.getTuid()));
			}
		}
		catch (Exception e) {
			Log.e("Could not parse flight checkout response", e);
			return null;
		}

		return travUpdateResponse;
	}
}
