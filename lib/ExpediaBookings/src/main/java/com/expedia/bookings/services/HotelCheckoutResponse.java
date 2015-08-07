package com.expedia.bookings.services;

public class HotelCheckoutResponse {

	public CheckoutResponse checkoutResponse;

	public static class BookingResponse {

		public String itineraryNumber;
	}

	public static class CheckoutResponse {

		public BookingResponse bookingResponse;
	}

}
