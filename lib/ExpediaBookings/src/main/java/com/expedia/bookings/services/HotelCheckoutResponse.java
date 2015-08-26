package com.expedia.bookings.services;

public class HotelCheckoutResponse {

	public CheckoutResponse checkoutResponse;

	public static class BookingResponse {

		public String itineraryNumber;
		public String email;
	}

	public static class CheckoutResponse {

		public BookingResponse bookingResponse;
		public ProductResponse productResponse;
	}

	public static class ProductResponse {
		public String checkInDate;
		public String checkOutDate;
		public String hotelId;
		public String localizedHotelName;
		public String hotelAddress;
		public String hotelCity;
		public String hotelStateProvince;
		public String hotelCountry;
		public String bigImageUrl;
	}

}
