package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONable;

public class UserLoyaltyMembershipInformation implements JSONable {

	private double loyaltyPointsAvailable; // Orbucks can return decimal values here (hence double)
	private double loyaltyPointsPending; // and here
	private String bookingCurrency;
	private boolean isAllowedToShopWithPoints = false;
	private MonetaryValue loyaltyMonetaryValue;
	private boolean isLoyaltyMembershipActive;
	private LoyaltyMembershipTier loyaltyMembershipTier = LoyaltyMembershipTier.NONE;
	private TierCredits currentTierCredits;
	private TierCredits reqUpgradeCredits;

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

	public MonetaryValue getLoyaltyMonetaryValue() {
		return loyaltyMonetaryValue;
	}

	public void setLoyaltyMonetaryValue(MonetaryValue monetaryValue) {
		loyaltyMonetaryValue = monetaryValue;
	}

	public boolean isLoyaltyMembershipActive() {
		return isLoyaltyMembershipActive;
	}

	public LoyaltyMembershipTier getLoyaltyMembershipTier() {
		return loyaltyMembershipTier;
	}

	public TierCredits getCurrentTierCredits() {
		return currentTierCredits;
	}

	public TierCredits getReqUpgradeCredits() {
		return reqUpgradeCredits;
	}

	public void setLoyaltyPointsPending(double loyaltyPointsPending) {
		this.loyaltyPointsPending = loyaltyPointsPending;
	}

	public void setLoyaltyMembershipActive(boolean loyaltyMembershipActive) {
		isLoyaltyMembershipActive = loyaltyMembershipActive;
	}

	public void setLoyaltyMembershipTier(
		LoyaltyMembershipTier loyaltyMembershipTier) {
		this.loyaltyMembershipTier = loyaltyMembershipTier;
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
			obj.putOpt("membershipTierName", loyaltyMembershipTier.toApiValue());
			obj.putOpt("currentTierCredits", currentTierCredits.toJson());
			obj.putOpt("reqUpgradeCredits", reqUpgradeCredits.toJson());
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
		isLoyaltyMembershipActive = obj.optBoolean("loyaltyMemebershipActive", false); // yes, the key is misspelt by the API
		String membershipTierName = obj.optString("membershipTierName", null);
		loyaltyMembershipTier = LoyaltyMembershipTier.fromApiValue(membershipTierName);
		if (obj.has("loyaltyMonetaryValue")) { // the old api response doesn't return loyaltyMonetaryValue
			// TODO - we can remove this check after 1st May 2016 (API will have been released to production)
			loyaltyMonetaryValue = new MonetaryValue(obj.optJSONObject("loyaltyMonetaryValue"));
		}
		else {
			loyaltyMonetaryValue = new MonetaryValue(new Money("0.0", bookingCurrency));
		}
		if (obj.has("currentTierCredits")) {
			currentTierCredits = new TierCredits(obj.optJSONObject("currentTierCredits"));
		}
		if (obj.has("reqUpgradeCredits")) {
			reqUpgradeCredits = new TierCredits(obj.optJSONObject("reqUpgradeCredits"));
		}

		return true;
	}

	public static class TierCredits implements JSONable {

		private MonetaryValue amount;
		private int hotelNights;
		private String tierName;

		public TierCredits(JSONObject obj) {
			fromJson(obj);
		}

		public Money getAmount() {
			return this.amount;
		}

		public int getHotelNights() {
			return this.hotelNights;
		}

		public String getTierName() {
			return this.tierName;
		}

		@Override
		public JSONObject toJson() {
			JSONObject obj = new JSONObject();
			try {
				obj.putOpt("amount", this.amount.toJson());
				obj.put("hotelNights", this.hotelNights);
				obj.put("tierName", this.tierName);
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
			this.amount = new MonetaryValue(obj.optJSONObject("amount"));
			this.hotelNights = Integer.parseInt(obj.optString("hotelNights", ""));
			this.tierName = obj.optString("tierName", "");
			return true;
		}
	}

	public static class MonetaryValue extends Money implements JSONable {

		private String apiFormattedPrice;

		public MonetaryValue(JSONObject obj) {
			fromJson(obj);
		}

		public MonetaryValue(Money money) {
			super(money.amount, money.currencyCode);
		}

		public void setApiFormattedPrice(String apiFormattedPrice) {
			this.apiFormattedPrice = apiFormattedPrice;
		}

		@Override
		public String getFormattedMoney() {
			return apiFormattedPrice;
		}

		@Override
		public JSONObject toJson() {
			try {
				JSONObject obj = new JSONObject();
				obj.put("amount", this.getAmount());
				obj.put("currencyCode", this.getCurrency());
				obj.put("formattedPrice", this.apiFormattedPrice);
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
			this.apiFormattedPrice = obj.optString("formattedPrice", "");
			return true;
		}
	}
}
