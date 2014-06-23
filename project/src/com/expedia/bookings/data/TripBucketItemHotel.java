package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;

/**
 * Store just enough information to populate Db.getHotelSearch() with the chosen room.
 *
 * @author doug
 */
public class TripBucketItemHotel extends TripBucketItem {

	Property mProperty;
	Rate mRate;
	HotelAvailability mAvailability;
	Rate mCouponRate;
	HotelSearchParams mSearchParams;
	boolean mIsCouponApplied;

	public TripBucketItemHotel() {

	}

	public TripBucketItemHotel(Property property, Rate rate, HotelSearchParams searchParams, HotelAvailability availability) {
		mProperty = property;
		mAvailability = availability;
		mRate = rate;
		mSearchParams = searchParams.clone();
	}

	@Override
	public LineOfBusiness getLineOfBusiness() {
		return LineOfBusiness.HOTELS;
	}

	public Property getProperty() {
		return mProperty;
	}

	public Rate getRate() {
		if (mIsCouponApplied) {
			return mCouponRate;
		}
		else {
			return mRate;
		}
	}

	public HotelAvailability getHotelAvailability() {
		return mAvailability;
	}

	public HotelSearchParams getHotelSearchParams() {
		return mSearchParams;
	}

	public void setNewRate(Rate rate) {
		mRate = rate;
		setHasPriceChanged(true);
	}

	public boolean isCouponApplied() {
		return mIsCouponApplied;
	}

	public void setIsCouponApplied(boolean isCouponApplied) {
		mIsCouponApplied = isCouponApplied;
	}

	public void setCouponRate(Rate couponRate) {
		mCouponRate = couponRate;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = super.toJson();
			JSONUtils.putJSONable(obj, "property", mProperty);
			JSONUtils.putJSONable(obj, "rate", mRate);
			JSONUtils.putJSONable(obj, "searchParams", mSearchParams);
			obj.put("type", "hotel");
			obj.put("couponApplied", mIsCouponApplied);
			JSONUtils.putJSONable(obj, "couponRate", mCouponRate);
			JSONUtils.putJSONable(obj, "availability", mAvailability);
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
		mProperty = JSONUtils.getJSONable(obj, "property", Property.class);
		mRate = JSONUtils.getJSONable(obj, "rate", Rate.class);
		mIsCouponApplied = obj.optBoolean("couponApplied");
		mCouponRate = JSONUtils.getJSONable(obj, "couponRate", Rate.class);
		mAvailability = JSONUtils.getJSONable(obj, "availability", HotelAvailability.class);
		mSearchParams = JSONUtils.getJSONable(obj, "searchParams", HotelSearchParams.class);
		return true;
	}
}
