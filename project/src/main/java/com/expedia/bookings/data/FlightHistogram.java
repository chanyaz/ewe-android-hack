package com.expedia.bookings.data;


import org.joda.time.LocalDate;
import org.json.JSONObject;

import com.mobiata.android.json.JSONable;

public class FlightHistogram implements JSONable, Comparable<FlightHistogram> {

	private LocalDate mKeyDate;
	private int mCount;
	private Money mMinPrice;
	private Money mMaxPrice;

	public LocalDate getKeyDate() {
		return mKeyDate;
	}

	public void setKeyDate(LocalDate date) {
		mKeyDate = date;
	}

	public int getCount() {
		return mCount;
	}

	public void setCount(int count) {
		mCount = count;
	}

	public Money getMinPrice() {
		return mMinPrice;
	}

	public void setMinPrice(Money minPrice) {
		mMinPrice = minPrice;
	}

	public Money getMaxPrice() {
		return mMaxPrice;
	}

	public void setMaxPrice(Money maxPrice) {
		mMaxPrice = maxPrice;
	}

	// TODO better JSONable implementation once types are set

	@Override
	public JSONObject toJson() {
		JSONObject obj = new JSONObject();

		return obj;
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		return true;
	}


	@Override
	public int compareTo(FlightHistogram another) {
		if (another == null && getKeyDate() != null) {
			return 1;
		}
		else if (getKeyDate() == null) {
			return -1;
		}
		else if (getKeyDate() != null) {
			return getKeyDate().compareTo(another.getKeyDate());
		}
		return 0;
	}
}
