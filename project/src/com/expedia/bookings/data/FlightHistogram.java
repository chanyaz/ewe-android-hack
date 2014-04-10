package com.expedia.bookings.data;

import org.joda.time.LocalDate;
import org.json.JSONObject;

import com.mobiata.android.json.JSONable;

public class FlightHistogram implements JSONable {

	private LocalDate mKeyDate;
	private int mCount;
	private float mTotalPrice;
	private double mMinPrice;
	private float mMaxPrice;

	// TODO priceAsStr is temporary!
	private String mPriceAsStr;

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

	public float getTotalPrice() {
		return mTotalPrice;
	}

	public void setTotalPrice(float totalPrice) {
		mTotalPrice = totalPrice;
	}

	public double getMinPrice() {
		return mMinPrice;
	}

	public void setMinPrice(double minPrice) {
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
