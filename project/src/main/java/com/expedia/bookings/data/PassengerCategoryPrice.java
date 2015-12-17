package com.expedia.bookings.data;

import org.json.JSONObject;

import com.expedia.bookings.enums.PassengerCategory;
import com.expedia.bookings.utils.GsonUtil;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class PassengerCategoryPrice implements Comparable<PassengerCategoryPrice>, JSONable {

	private PassengerCategory mPassengerCategory;
	private Money mTotalPrice;
	private Money mBasePrice;
	private Money mTaxesPrice;

	public PassengerCategoryPrice() {
		//Default constructor, required by some of our JSONUtils functions
	}

	public PassengerCategoryPrice(PassengerCategory passengerCategory, Money totalPrice, Money basePrice,
		Money taxesPrice) {
		mPassengerCategory = passengerCategory;
		mTotalPrice = totalPrice;
		mBasePrice = basePrice;
		mTaxesPrice = taxesPrice;
	}

	// Setters

	public PassengerCategory getPassengerCategory() {
		return mPassengerCategory;
	}

	public Money getTotalPrice() {
		return mTotalPrice;
	}

	public Money getBasePrice() {
		return mBasePrice;
	}

	public Money getTaxes() {
		return mTaxesPrice;
	}

	///////////////////////////////////////////////////////////////////////////
	// Comparable

	// When we sort a Collection of type PassengerCategoryPrice, we want
	// adults (ADULT, SENIOR) always before children (CHILD, ADULT_CHILD)
	// always before infants (INFANT_IN_SEAT, INFANT_IN_LAP)

	@Override
	public int compareTo(PassengerCategoryPrice another) {
		return mPassengerCategory.compareTo(another.getPassengerCategory());
	}


	///////////////////////////////////////////////////////////////////////////
	// JSONable

	private static final String JSON_PASSENGER_CATEGORY = "JSON_PASSENGER_CATEGORY";
	private static final String JSON_TOTAL_PRICE = "JSON_TOTAL_PRICE";
	private static final String JSON_BASE_PRICE = "JSON_BASE_PRICE";
	private static final String JSON_TAXES = "JSON_TAXES";

	@Override
	public JSONObject toJson() {
		JSONObject obj = new JSONObject();
		try {
			JSONUtils.putEnum(obj, JSON_PASSENGER_CATEGORY, mPassengerCategory);
			GsonUtil.putForJsonable(obj, JSON_BASE_PRICE, mBasePrice);
			GsonUtil.putForJsonable(obj, JSON_TOTAL_PRICE, mTotalPrice);
			GsonUtil.putForJsonable(obj, JSON_TAXES, mTaxesPrice);
			return obj;
		}
		catch (Exception ex) {
			Log.e("Exception in PassengerCategoryPrice.toJson", ex);
		}
		return null;
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		try {
			mPassengerCategory = JSONUtils.getEnum(obj, JSON_PASSENGER_CATEGORY, PassengerCategory.class);
			mBasePrice = GsonUtil.getForJsonable(obj, JSON_BASE_PRICE, Money.class);
			mTotalPrice = GsonUtil.getForJsonable(obj, JSON_TOTAL_PRICE, Money.class);
			mTaxesPrice = GsonUtil.getForJsonable(obj, JSON_TAXES, Money.class);
			return true;
		}
		catch (Exception ex) {
			Log.e("Exception in PassengerCategoryPrice.fromJson", ex);
		}
		return false;
	}
}
