package com.expedia.bookings.data;

import org.joda.time.LocalDate;
import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.utils.GsonUtil;
import com.expedia.bookings.utils.JodaUtils;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONable;

public class RateBreakdown implements JSONable {
	private Money mAmount;
	private LocalDate mDate;

	public Money getAmount() {
		return mAmount;
	}

	public void setAmount(Money amount) {
		this.mAmount = amount;
	}

	public LocalDate getDate() {
		return mDate;
	}

	public void setDate(LocalDate date) {
		this.mDate = date;
	}

	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			GsonUtil.putForJsonable(obj, "amount", mAmount);
			JodaUtils.putLocalDateInJson(obj, "localDate", mDate);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert RateBreakdown object to JSON.", e);
			return null;
		}
	}

	public boolean fromJson(JSONObject obj) {
		mAmount = GsonUtil.getForJsonable(obj, "amount", Money.class);
		mDate = JodaUtils.getLocalDateFromJson(obj, "localDate");

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
