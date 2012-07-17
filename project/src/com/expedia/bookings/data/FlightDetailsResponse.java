package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.json.JSONUtils;

public class FlightDetailsResponse extends Response {

	private FlightTrip mOffer;
	private FlightTrip mOldOffer;
	private Money mPriceChangeAmount;

	private boolean mIsChangeAllowed;
	private boolean mIsEnrouteChangeAllowed;
	private boolean mIsEnrouteRefundAllowed;
	private boolean mIsRefundable;
	private Money mChangePenaltyAmount;

	public boolean hasPriceChanged() {
		return mPriceChangeAmount != null;
	}

	public void setOffer(FlightTrip flightTrip) {
		mOffer = flightTrip;
	}

	public FlightTrip getOffer() {
		return mOffer;
	}

	public void setOldOffer(FlightTrip flightTrip) {
		mOldOffer = flightTrip;
	}

	public FlightTrip getOldOffer() {
		return mOldOffer;
	}

	public void setPriceChangeAmount(Money priceChangeAmount) {
		mPriceChangeAmount = priceChangeAmount;
	}

	public Money getPriceChangeAmount() {
		return mPriceChangeAmount;
	}

	public boolean isIsChangeAllowed() {
		return mIsChangeAllowed;
	}

	public void setIsChangeAllowed(boolean isChangeAllowed) {
		mIsChangeAllowed = isChangeAllowed;
	}

	public boolean isIsEnrouteChangeAllowed() {
		return mIsEnrouteChangeAllowed;
	}

	public void setIsEnrouteChangeAllowed(boolean isEnrouteChangeAllowed) {
		mIsEnrouteChangeAllowed = isEnrouteChangeAllowed;
	}

	public boolean isIsEnrouteRefundAllowed() {
		return mIsEnrouteRefundAllowed;
	}

	public void setIsEnrouteRefundAllowed(boolean isEnrouteRefundAllowed) {
		mIsEnrouteRefundAllowed = isEnrouteRefundAllowed;
	}

	public boolean isIsRefundable() {
		return mIsRefundable;
	}

	public void setIsRefundable(boolean isRefundable) {
		mIsRefundable = isRefundable;
	}

	public Money getChangePenaltyAmount() {
		return mChangePenaltyAmount;
	}

	public void setChangePenaltyAmount(Money changePenaltyAmount) {
		mChangePenaltyAmount = changePenaltyAmount;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		JSONObject obj = super.toJson();
		if (obj == null) {
			return null;
		}

		try {
			JSONUtils.putJSONable(obj, "offer", mOffer);
			JSONUtils.putJSONable(obj, "oldOffer", mOldOffer);
			JSONUtils.putJSONable(obj, "priceChangeAmount", mPriceChangeAmount);
			obj.putOpt("isChangeAllowed", mIsChangeAllowed);
			obj.putOpt("isEnrouteChangeAllowed", mIsEnrouteChangeAllowed);
			obj.putOpt("isEnrouteRefundAllowed", mIsEnrouteRefundAllowed);
			obj.putOpt("isRefundable", mIsRefundable);
			JSONUtils.putJSONable(obj, "changePenaltyAmount", mChangePenaltyAmount);
			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		super.fromJson(obj);
		mOffer = JSONUtils.getJSONable(obj, "offer", FlightTrip.class);
		mOldOffer = JSONUtils.getJSONable(obj, "oldOffer", FlightTrip.class);
		mPriceChangeAmount = JSONUtils.getJSONable(obj, "priceChangeAmount", Money.class);
		mIsChangeAllowed = obj.optBoolean("isChangeAllowed");
		mIsEnrouteChangeAllowed = obj.optBoolean("isEnrouteChangeAllowed");
		mIsEnrouteRefundAllowed = obj.optBoolean("isEnrouteRefundAllowed");
		mIsRefundable = obj.optBoolean("isRefundable");
		mChangePenaltyAmount = JSONUtils.getJSONable(obj, "changePenaltyAmount", Money.class);
		return true;
	}
}
