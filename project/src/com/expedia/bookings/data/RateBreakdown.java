package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class RateBreakdown implements JSONable {
	private Money mAmount;
	private Date mDate;

	public Money getAmount() {
		return mAmount;
	}

	public void setAmount(Money amount) {
		this.mAmount = amount;
	}

	public Date getDate() {
		return mDate;
	}

	public void setDate(Date date) {
		this.mDate = date;
	}

	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			JSONUtils.putJSONable(obj, "amount", mAmount);
			JSONUtils.putJSONable(obj, "date", mDate);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert RateBreakdown object to JSON.", e);
			return null;
		}
	}

	public boolean fromJson(JSONObject obj) {
		mAmount = (Money) JSONUtils.getJSONable(obj, "amount", Money.class);
		mDate = (Date) JSONUtils.getJSONable(obj, "date", Date.class);

		return true;
	}

	@Override
	public String toString() {
		JSONObject obj = toJson();
		try {
			return obj.toString(2);
		}
		catch (JSONException e) {
			return obj.toString();
		}
	}
}
