package com.expedia.bookings.data;

import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONable;

public class UserLoyaltyMembershipInformation implements JSONable {

	private double loyaltyPointsAvailable; // Orbucks can return decimal values here (hence double)
	private double loyaltyPointsPending; // and here
	private String bookingCurrency;
	private boolean isAllowedToShopWithPoints = false;
	private LoyaltyMonetaryValueObject loyaltyMonetaryValue;
	private boolean isLoyaltyMembershipActive;
	private LoyaltyMembershipTier loyaltyMembershipTier = LoyaltyMembershipTier.NONE;

	public double getLoyaltyPointsAvailable() {
		return loyaltyPointsAvailable;
	}

	public double getLoyaltyPointsPending() {
		return loyaltyPointsPending;
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

	public Money getLoyaltyMonetaryValue() {
		return loyaltyMonetaryValue;
	}

	public boolean isLoyaltyMembershipActive() {
		return isLoyaltyMembershipActive;
	}

	public LoyaltyMembershipTier getLoyaltyMembershipTier() {
		return loyaltyMembershipTier;
	}

	@Override
	public JSONObject toJson() {
		JSONObject obj = new JSONObject();

		try {
			obj.putOpt("loyaltyPointsAvailable", loyaltyPointsAvailable);
			obj.putOpt("loyaltyPointsPending", loyaltyPointsPending);
			obj.putOpt("bookingCurrency", bookingCurrency);
			obj.putOpt("isAllowedToShopWithPoints", isAllowedToShopWithPoints);
			if (loyaltyMonetaryValue != null) {
				obj.putOpt("loyaltyMonetaryValue", loyaltyMonetaryValue.toJson());
			}
			obj.putOpt("loyaltyMemebershipActive", isLoyaltyMembershipActive);
			obj.putOpt("membershipTierName", loyaltyMembershipTier.name());
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
		loyaltyPointsPending = obj.optDouble("loyaltyPointsPending", 0);
		bookingCurrency = obj.optString("bookingCurrency", null);
		isAllowedToShopWithPoints = obj.optBoolean("isAllowedToShopWithPoints", false);
		loyaltyMonetaryValue = new LoyaltyMonetaryValueObject(obj.optJSONObject("loyaltyMonetaryValue"));
		isLoyaltyMembershipActive = obj.optBoolean("loyaltyMemebershipActive", false); // yes, the key is misspelt by the API
		String membershipTierName = obj.optString("membershipTierName", "NONE").toUpperCase(Locale.US);
		loyaltyMembershipTier = LoyaltyMembershipTier.valueOf(membershipTierName);

		return true;
	}

	class LoyaltyMonetaryValueObject extends Money implements JSONable {

		public LoyaltyMonetaryValueObject(JSONObject obj) {
			fromJson(obj);
		}

		@Override
		public JSONObject toJson() {
			try {
				JSONObject obj = new JSONObject();
				obj.put("amount", this.getAmount());
				obj.put("currencyCode", this.getCurrency());
				return obj;
			}
			catch (JSONException e) {
				throw new RuntimeException(e); // cannot recover from this
			}
		}

		@Override
		public boolean fromJson(JSONObject obj) {
			if (obj == null) {
				return false;
			}
			this.setAmount(obj.optString("amount", ""));
			this.setCurrency(obj.optString("currencyCode", ""));
			return true;
		}
	}

	public enum LoyaltyMembershipTier {
		NONE, BLUE, SILVER, GOLD;

		public boolean isGoldOrSilver() {
			return this == SILVER || this == GOLD;
		}
	}
}
