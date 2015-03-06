package com.expedia.bookings.data.cars;

import java.util.List;

import com.expedia.bookings.data.ValidPayment;

public class CarCreateTripResponse {

	public String activityId;
	public CreateTripCarOffer carProduct;

	public String itineraryNumber;
	public String tripId;
	public List<ValidPayment> validFormsOfPayment;

}
