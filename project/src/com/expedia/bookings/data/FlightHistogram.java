package com.expedia.bookings.data;

import org.joda.time.LocalDate;
import org.json.JSONObject;

import com.mobiata.android.json.JSONable;

public class FlightHistogram implements JSONable {

	private LocalDate mDate;
	private int mCount;
	private float mTotalPrice;
	private float mMinPrice;
	private float mMaxPrice;

	// TODO priceAsStr is temporary!
	private String mPriceAsStr;

	public LocalDate getDate() {
		return mDate;
	}

	public void setDate(LocalDate date) {
		mDate = date;
	}

	public int getCount() {
		return mCount;
	}

	public void setCount(int count) {
		mCount = count;
	}

	public float getTotalPrice() {
		return mTotalPrice;
	}

	public void setTotalPrice(float totalPrice) {
		mTotalPrice = totalPrice;
	}

	public float getMinPrice() {
		return mMinPrice;
	}

	public void setMinPrice(float minPrice) {
		mMinPrice = minPrice;
	}

	public float getMaxPrice() {
		return mMaxPrice;
	}

	public void setMaxPrice(float maxPrice) {
		mMaxPrice = maxPrice;
	}

	public String getPriceAsStr() {
		return mPriceAsStr;
	}

	public void setPriceAsStr(String price) {
		mPriceAsStr = "$" + price;
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
}
