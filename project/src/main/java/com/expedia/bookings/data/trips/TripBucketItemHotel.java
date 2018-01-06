package com.expedia.bookings.data.trips;

import com.expedia.bookings.data.HotelAvailability;
import com.expedia.bookings.data.HotelBookingResponse;
import com.expedia.bookings.data.HotelSearch;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Rate;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class TripBucketItemHotel extends TripBucketItem {

	private HotelSearch mHotelSearch;
	private HotelAvailability mAvailability;
	private Rate mRate;
	private Rate mOldRate;

	private Rate mCouponRate;
	private boolean mIsCouponApplied;

	private HotelBookingResponse mBookingResponse;

	TripBucketItemHotel() {
		// ignore
	}

	TripBucketItemHotel(HotelSearch hotelSearch, Rate rate, HotelAvailability availability) {
		mHotelSearch = hotelSearch.generateForTripBucket();
		mAvailability = availability.clone();
		mRate = rate.clone();
	}

	@Override
	public LineOfBusiness getLineOfBusiness() {
		return LineOfBusiness.HOTELS;
	}

	public Rate getRate() {
		if (mIsCouponApplied) {
			return mCouponRate;
		}
		else {
			return mRate;
		}
	}

	public HotelSearch getHotelSearch() {
		return mHotelSearch;
	}

	public HotelSearchParams getHotelSearchParams() {
		return mHotelSearch.getSearchParams();
	}

	public HotelBookingResponse getBookingResponse() {
		return mBookingResponse;
	}

	public void setBookingResponse(HotelBookingResponse bookingResponse) {
		mBookingResponse = bookingResponse;
	}

	public void clearCheckoutData() {
		mCouponRate = null;
		mIsCouponApplied = false;
		mBookingResponse = null;
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
		mBookingResponse = JSONUtils.getJSONable(obj, "bookingResponse", HotelBookingResponse.class);
		return true;
	}
}
