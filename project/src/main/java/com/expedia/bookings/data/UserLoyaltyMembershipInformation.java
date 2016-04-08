package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONable;

public class UserLoyaltyMembershipInformation implements JSONable {

	private double loyaltyPointsAvailable;
	private String bookingCurrency;
	private boolean isAllowedToShopWithPoints;

	public double getLoyaltyPointsAvailable() {
		return loyaltyPointsAvailable;
	}

	public String getBookingCurrency() {
		return bookingCurrency;
	}

	public boolean isAllowedToShopWithPoints() {
		return isAllowedToShopWithPoints;
	}

	public void setLoyaltyPointsAvailable(double loyaltyPointsAvailable) {
		this.loyaltyPointsAvailable = loyaltyPointsAvailable;
	}

	public void setBookingCurrency(String bookingCurrency) {
		this.bookingCurrency = bookingCurrency;
	}

	public void setAllowedToShopWithPoints(boolean allowedToShopWithPoints) {
		isAllowedToShopWithPoints = allowedToShopWithPoints;
	}

	@Override
	public JSONObject toJson() {
		JSONObject obj = new JSONObject();

		try {
			obj.putOpt("loyaltyPointsAvailable", loyaltyPointsAvailable);
			obj.putOpt("bookingCurrency", bookingCurrency);
			obj.putOpt("isAllowedToShopWithPoints", isAllowedToShopWithPoints);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert UserLoyaltyMembershipInformation to JSON", e);
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		loyaltyPointsAvailable = obj.optDouble("loyaltyPointsAvailable", 0);
		bookingCurrency = obj.optString("bookingCurrency", null);
		isAllowedToShopWithPoints = obj.optBoolean("isAllowedToShopWithPoints", false);
		return true;
	}
}
