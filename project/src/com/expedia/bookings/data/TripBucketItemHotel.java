package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;

public class TripBucketItemHotel extends TripBucketItem {

	HotelSearch mHotelSearch;
	HotelAvailability mAvailability;
	Rate mRate;
	Rate mOldRate;

	Rate mCouponRate;
	boolean mIsCouponApplied;

	CreateTripResponse mCreateTripResponse;
	HotelBookingResponse mBookingResponse;

	public TripBucketItemHotel() {
		// ignore
	}

	public TripBucketItemHotel(HotelSearch hotelSearch, Rate rate, HotelAvailability availability) {
		mHotelSearch = hotelSearch.generateForTripBucket();
		mAvailability = availability.clone();
		mRate = rate.clone();
	}

	@Override
	public LineOfBusiness getLineOfBusiness() {
		return LineOfBusiness.HOTELS;
	}

	public Property getProperty() {
		return mHotelSearch.getSelectedProperty();
	}

	public Rate getRate() {
		if (mIsCouponApplied) {
			return mCouponRate;
		}
		else {
			return mRate;
		}
	}

	public Rate getRateNoCoupon() {
		return mRate;
	}

	public Rate getOldRate() {
		return mOldRate;
	}

	public HotelAvailability getHotelAvailability() {
		return mAvailability;
	}

	public HotelSearch getHotelSearch() {
		return mHotelSearch;
	}

	public HotelSearchParams getHotelSearchParams() {
		return mHotelSearch.getSearchParams();
	}

	public void setNewRate(Rate rate) {
		mOldRate = mRate;
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

	public Rate getCouponRate() {
		return mCouponRate;
	}

	public void setCreateTripResponse(CreateTripResponse response) {
		mCreateTripResponse = response;
	}

	public CreateTripResponse getCreateTripResponse() {
		return mCreateTripResponse;
	}

	public HotelBookingResponse getBookingResponse() {
		return mBookingResponse;
	}

	public void setBookingResponse(HotelBookingResponse bookingResponse) {
		mBookingResponse = bookingResponse;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = super.toJson();
			JSONUtils.putJSONable(obj, "hotelSearch", mHotelSearch);
			JSONUtils.putJSONable(obj, "rate", mRate);
			JSONUtils.putJSONable(obj, "oldRate", mOldRate);
			obj.put("type", "hotel");
			obj.put("couponApplied", mIsCouponApplied);
			JSONUtils.putJSONable(obj, "couponRate", mCouponRate);
			JSONUtils.putJSONable(obj, "availability", mAvailability);
			JSONUtils.putJSONable(obj, "createTripResponse", mCreateTripResponse);
			JSONUtils.putJSONable(obj, "bookingResponse", mBookingResponse);
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
		mRate = JSONUtils.getJSONable(obj, "rate", Rate.class);
		mOldRate = JSONUtils.getJSONable(obj, "oldRate", Rate.class);
		mIsCouponApplied = obj.optBoolean("couponApplied");
		mCouponRate = JSONUtils.getJSONable(obj, "couponRate", Rate.class);
		mAvailability = JSONUtils.getJSONable(obj, "availability", HotelAvailability.class);
		mCreateTripResponse = JSONUtils.getJSONable(obj, "createTripResponse", CreateTripResponse.class);
		mBookingResponse = JSONUtils.getJSONable(obj, "bookingResponse", HotelBookingResponse.class);
		return true;
	}
}
