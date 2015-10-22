package com.expedia.bookings.services;

import com.expedia.bookings.data.cars.BaseApiResponse;
import com.expedia.bookings.data.hotels.HotelCreateTripResponse;
import com.expedia.bookings.data.hotels.HotelOffersResponse;

public class HotelCheckoutResponse extends BaseApiResponse {

	public CheckoutResponse checkoutResponse;
	public String orderId;
	public String currencyCode;
	public String totalCharges;

	public static class BookingResponse {

		public String itineraryNumber;
		public String email;
		public String tripId;
		public String supplierType;
		public String travelRecordLocator;
	}

	public static class CheckoutResponse {

		public BookingResponse bookingResponse;
		public ProductResponse productResponse;
		public PriceChangeResponse jsonPriceChangeResponse;
	}

	public static class ProductResponse {
		public String checkInDate;
		public String checkOutDate;
		public String hotelId;
		public String localizedHotelName;
		public String hotelAddress;
		public String hotelCity;
		public String hotelStateProvince;
		public HotelOffersResponse.HotelRoomResponse hotelRoomResponse;
		public String hotelCountry;
		public String regionId;
		public String bigImageUrl;
	}

	public static class PriceChangeResponse {
		public HotelCreateTripResponse.HotelProductResponse oldProduct;
		public HotelCreateTripResponse.HotelProductResponse newProduct;
	}
}
