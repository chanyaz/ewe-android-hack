package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.joda.time.LocalDate;
import org.json.JSONObject;

import com.mobiata.android.json.JSONable;

public class FlightHistogram implements JSONable, Comparable<FlightHistogram> {

	private LocalDate mKeyDate;
	private int mCount;
	private float mTotalPrice;
	private double mMinPrice;
	private float mMaxPrice;
	private HashMap<String, FlightHistogram> mReturnFlightDateHistograms;

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

	/**
	 * This is a HashMap of DateString to ReturnHistogram e.g. "2014-07-01" -> FlightHistogram
	 *
	 * @param returnFlightDatePrices
	 */
	public void setReturnFlightDateHistograms(HashMap<String, FlightHistogram> returnFlightDatePrices) {
		mReturnFlightDateHistograms = returnFlightDatePrices;
	}

	/**
	 * Thus returns a sorted list of FlightHistogram's for return flights based on this
	 * FlightHistogram. It purposely returns an empty list if no data is available. Hopefully
	 * the GDE data will get better.
	 *
	 * @return a non-null ArrayList
	 */
	public ArrayList<FlightHistogram> getReturnFlightDateHistograms() {
		if (mReturnFlightDateHistograms == null) {
			return new ArrayList<>();
		}
		ArrayList<FlightHistogram> grams = new ArrayList<>(mReturnFlightDateHistograms.values());
		Collections.sort(grams);
		return grams;
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
