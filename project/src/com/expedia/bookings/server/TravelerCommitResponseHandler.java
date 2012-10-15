package com.expedia.bookings.server;

import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.TravelerCommitResponse;
import com.expedia.bookings.data.ServerError.ApiMethod;
import com.mobiata.android.Log;
import com.mobiata.android.net.JsonResponseHandler;

public class TravelerCommitResponseHandler extends JsonResponseHandler<TravelerCommitResponse> {

	private Traveler mTraveler;
	private Context mContext;

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
			travUpdateResponse.addErrors(ParserUtils.parseErrors(mContext, ApiMethod.COMMIT_TRAVELER, response));
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
