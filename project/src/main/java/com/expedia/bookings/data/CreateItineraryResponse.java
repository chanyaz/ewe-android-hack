package com.expedia.bookings.data;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.utils.GsonUtil;
import com.mobiata.android.json.JSONUtils;

public class CreateItineraryResponse extends Response {

	private Itinerary mItinerary;

	private FlightTrip mOffer;

	private List<ValidPayment> mValidPayments;

	private boolean mIsSplitTicket;

	public void setItinerary(Itinerary itinerary) {
		mItinerary = itinerary;
	}

	public Itinerary getItinerary() {
		return mItinerary;
	}

	public void setOffer(FlightTrip offer) {
		mOffer = offer;
	}

	public FlightTrip getOffer() {
		return mOffer;
	}

	public void setValidPayments(List<ValidPayment> payments) {
		mValidPayments = payments;
	}

	public List<ValidPayment> getValidPayments() {
		return mValidPayments;
	}

	public void setIsSplitTicket(boolean isSplitTicket) {
		mIsSplitTicket = isSplitTicket;
	}

	public boolean isSplitTicket() {
		return mIsSplitTicket;
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
			JSONUtils.putJSONable(obj, "itinerary", mItinerary);
			JSONUtils.putJSONable(obj, "offer", mOffer);
			GsonUtil.putListForJsonable(obj, "validPayments", mValidPayments);
			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		super.fromJson(obj);

		mItinerary = JSONUtils.getJSONable(obj, "itinerary", Itinerary.class);
		mOffer = JSONUtils.getJSONable(obj, "offer", FlightTrip.class);
		mValidPayments = GsonUtil.getListForJsonable(obj, "validPayments", ValidPayment.gsonListTypeToken);

		return true;
	}
}
