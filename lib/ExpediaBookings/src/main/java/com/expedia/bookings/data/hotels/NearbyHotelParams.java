package com.expedia.bookings.data.hotels;

public class NearbyHotelParams {

	public final String latitude;
	public final String longitude;
	public final String guestCount;
	public final String checkInDate;
	public final String checkOutDate;
	public final String sortOrder;

	public NearbyHotelParams(String latitude, String longitude, String guestCount, String checkInDate, String checkOutDate, String sortOrder) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.guestCount = guestCount;
		this.checkInDate = checkInDate;
		this.checkOutDate = checkOutDate;
		this.sortOrder = sortOrder;
	}
}
