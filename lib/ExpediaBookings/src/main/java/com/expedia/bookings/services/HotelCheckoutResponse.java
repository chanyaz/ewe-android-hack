package com.expedia.bookings.services;

import java.util.List;

import com.expedia.bookings.data.BaseApiResponse;
import com.expedia.bookings.data.hotels.HotelCreateTripResponse;
import com.expedia.bookings.data.hotels.HotelOffersResponse;
import com.expedia.bookings.data.payment.PointsDetails;
import com.expedia.bookings.data.payment.UserPaymentPreferences;
import com.expedia.bookings.utils.Strings;

public class HotelCheckoutResponse extends BaseApiResponse {

	public final CheckoutResponse checkoutResponse;
	public final String orderId;
	public final String currencyCode;
	public final String totalCharges;
	public final List<PointsDetails> pointsDetails;
	public final UserPaymentPreferences userPreferencePoints;

	public static class BookingResponse {

		public final String itineraryNumber;
		public String email;
		public String tripId;
		public final String supplierType;
		public final String travelRecordLocator;
	}

	public static class CheckoutResponse {

		public final BookingResponse bookingResponse;
		public final ProductResponse productResponse;
		public final PriceChangeResponse jsonPriceChangeResponse;
	}

	public static class ProductResponse {
		public final String checkInDate;
		public final String checkOutDate;
		public final String hotelId;
		public final String hotelName;
		public final String localizedHotelName;
		public String hotelAddress;
		public final String hotelCity;
		public String hotelStateProvince;
		public final HotelOffersResponse.HotelRoomResponse hotelRoomResponse;
		public String hotelCountry;
		public String regionId;
		public String bigImageUrl;

		public String getHotelName() {
			if (Strings.isEmpty(localizedHotelName)) {
				return hotelName;
			}
			return localizedHotelName;
		}
	}

	public static class PriceChangeResponse {
		public final HotelCreateTripResponse.HotelProductResponse oldProduct;
		public final HotelCreateTripResponse.HotelProductResponse newProduct;
	}
}
