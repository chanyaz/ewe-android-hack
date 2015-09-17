package com.expedia.bookings.data.cars;

import java.util.List;

import com.expedia.bookings.data.ValidPayment;

public class CarCreateTripResponse extends BaseCarResponse {

	public String activityId;
	public CreateTripCarOffer carProduct;

	public String itineraryNumber;
	public String tripId;
	public List<ValidPayment> validFormsOfPayment;

	// Injected after receiving response; required for communicating price change
	public SearchCarOffer searchCarOffer;

	public boolean hasPriceChange() {
		return hasErrors() && getFirstError().errorCode == CarApiError.Code.PRICE_CHANGE;
	}

}
