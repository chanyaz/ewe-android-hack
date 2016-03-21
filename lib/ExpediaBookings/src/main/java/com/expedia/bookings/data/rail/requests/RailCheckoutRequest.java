package com.expedia.bookings.data.rail.requests;

import java.util.List;

import com.expedia.bookings.data.payment.PaymentInfo;
import com.expedia.bookings.data.payment.Traveler;
import com.expedia.bookings.data.payment.TripDetails;

public class RailCheckoutRequest {

	public List<Traveler> travelers;
	public TripDetails tripDetails;
	public PaymentInfo paymentInfo;
}
