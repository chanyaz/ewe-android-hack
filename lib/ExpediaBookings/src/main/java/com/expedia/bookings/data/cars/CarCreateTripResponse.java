package com.expedia.bookings.data.cars;

import java.util.List;

import com.expedia.bookings.data.ValidPayment;

public class CarCreateTripResponse extends com.expedia.bookings.data.BaseApiResponse {
	public CreateTripCarOffer carProduct;
	public String originalPrice;
	public String itineraryNumber;
	public String tripId;
	public List<ValidPayment> validFormsOfPayment;

	// Injected after receiving response; required for communicating price change
	public SearchCarOffer searchCarOffer;
}
