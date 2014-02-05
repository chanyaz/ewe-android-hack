package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import android.view.View;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;

/**
 * @author doug
 */
public class TripBucketItemHotel extends TripBucketItem {

	HotelSearch mHotelSearch;
	Property mProperty;
	Rate mRate;

	public TripBucketItemHotel(HotelSearch hotelSearch, Property property, Rate rate) {
		mHotelSearch = hotelSearch;
		mProperty = property;
		mRate = rate;
	}

	@Override
	public LineOfBusiness getLineOfBusiness() {
		return LineOfBusiness.HOTELS;
	}

	public HotelSearch getHotelSearch() {
		return mHotelSearch;
	}

	public Property getProperty() {
		return mProperty;
	}

	public Rate getRate() {
		return mRate;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = super.toJson();
			JSONUtils.putJSONable(obj, "hotelSearch", mHotelSearch);
			JSONUtils.putJSONable(obj, "property", mProperty);
			JSONUtils.putJSONable(obj, "rate", mRate);
			return obj;
		}
		catch (JSONException e) {
			Log.e("TripBucketItemHotel toJson() failed", e);
		}
		return null;
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		super.fromJson(obj);
		mHotelSearch = JSONUtils.getJSONable(obj, "hotelSearch", HotelSearch.class);
		mProperty = JSONUtils.getJSONable(obj, "property", Property.class);
		mRate = JSONUtils.getJSONable(obj, "rate", Rate.class);
		return true;
	}
}
