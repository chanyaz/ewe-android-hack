package com.expedia.bookings.widget;

import org.json.JSONObject;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;

import com.expedia.bookings.data.Rate;
import com.mobiata.android.json.JSONUtils;

public class RowRoomRateLayout extends LinearLayout {

	// The Rate associated with this row and its children
	private Rate mRate;

	public RowRoomRateLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		Bundle bundle = new Bundle();
		bundle.putParcelable("super", superState);
		if (mRate != null) {
			JSONUtils.putJSONable(bundle, "rate", mRate);
		}
		return bundle;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		// Boilerplate code so parent classes can restore state
		if (!(state instanceof Bundle)) {
			super.onRestoreInstanceState(state);
			return;
		}

		Bundle bundle = (Bundle) state;
		super.onRestoreInstanceState(bundle.getParcelable("super"));
		if (bundle.containsKey("rate")) {
			mRate = JSONUtils.getJSONable(bundle, "rate", Rate.class);
		}
		else {
			mRate = null;
		}
	}

	public void setRate(Rate rate) {
		mRate = rate;
	}

	public Rate getRate() {
		return mRate;
	}
}
