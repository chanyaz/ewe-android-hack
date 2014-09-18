package com.expedia.bookings.data.trips;

import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.data.Activity;
import com.mobiata.android.json.JSONUtils;

public class TripActivity extends TripComponent {

	private Activity mActivity;

	public TripActivity() {
		super(Type.ACTIVITY);
	}

	public void setActivity(Activity activity) {
		mActivity = activity;
	}

	public Activity getActivity() {
		return mActivity;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		JSONObject obj = super.toJson();
		if (obj == null) {
			return null;
		}

		try {
			JSONUtils.putJSONable(obj, "activity", mActivity);
			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		super.fromJson(obj);
		mActivity = JSONUtils.getJSONable(obj, "activity", Activity.class);
		return true;
	}
}
