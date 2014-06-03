package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

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

	private boolean mHasPriceChanged;

	// Boolean check to indicate if this bucket item is actively being viewed/selected.
	private boolean mIsSelected;

	public boolean hasPriceChanged() {
		return mHasPriceChanged;
	}

	public void setHasPriceChanged(boolean priceChanged) {
		this.mHasPriceChanged = priceChanged;
	}

	public TripBucketItemState getState() {
		if (mState == null) {
			return TripBucketItemState.DEFAULT;
		}
		else {
			return mState;
		}
	}

	public void setState(TripBucketItemState state) {
		mState = state;
	}

	public boolean isSelected() {
		return mIsSelected;
	}

	public void setSelected(boolean isSelected) {
		this.mIsSelected = isSelected;
	}
//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			JSONUtils.putEnum(obj, "state", mState);
			obj.put("isSelected", mIsSelected);
			obj.put("hasPriceChanged", mHasPriceChanged);
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
		mIsSelected = obj.optBoolean("isSelected");
		mHasPriceChanged = obj.optBoolean("hasPriceChanged");
		return true;
	}

}
