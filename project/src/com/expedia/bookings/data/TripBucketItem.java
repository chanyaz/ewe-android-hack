package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import android.view.View;

import com.expedia.bookings.enums.TripBucketItemState;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

/**
 * @author doug
 */
public abstract class TripBucketItem implements JSONable {

	public abstract LineOfBusiness getLineOfBusiness();
	private TripBucketItemState mState;

	public TripBucketItemState getState() {
		return mState;
	}

	public void setState(TripBucketItemState state) {
		mState = state;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			JSONUtils.putEnum(obj, "state", mState);
			return obj;
		}
		catch (JSONException e) {
			Log.e("TripBucketItem toJson() failed", e);
		}
		return null;
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mState = JSONUtils.getEnum(obj, "state", TripBucketItemState.class);
		return true;
	}

}
