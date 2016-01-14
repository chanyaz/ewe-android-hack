package com.expedia.bookings.data.packages;

import java.util.List;

import com.expedia.bookings.data.cars.BaseApiResponse;
import com.expedia.bookings.data.hotels.HotelOffersResponse;

public class PackageOffersResponse extends BaseApiResponse {
	public List<PackageHotelOffer> packageHotelOffers;

	public static class PackageHotelOffer {
		public String packageProductId;
		public HotelOffersResponse.HotelRoomResponse hotelOffer;
	}
}
