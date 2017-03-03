package com.expedia.bookings.data;

import java.util.List;

public class RoomUpgradeOffersResponse {

	public UpgradeOffers upgradeOffers;

	public static class UpgradeOffers {
		public List<HotelRoomResponse> roomOffers;
	}

	public static class HotelRoomResponse {
		public String productKey;
	}
}
