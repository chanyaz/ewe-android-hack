package com.expedia.bookings.data.packages;

import java.util.List;

import com.expedia.bookings.data.cars.BaseApiResponse;
import com.expedia.bookings.data.hotels.HotelOffersResponse;

public class PackageOffersResponse extends BaseApiResponse {
	public List<PackageHotelOffers> packageHotelOffers;

	public static class PackageHotelOffers {
		public String packageProductId;
		public HotelOffersResponse.HotelRoomResponse hotelOffer;
	}
}
