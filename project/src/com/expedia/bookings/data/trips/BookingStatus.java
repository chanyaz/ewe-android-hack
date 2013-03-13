package com.expedia.bookings.data.trips;

public enum BookingStatus {
	SAVED,
	PENDING,
	BOOKED,
	CANCELLED;

	/**
	 * We don't want to show all BookingStatuses in the app; check here
	 * to see if you want to view them.
	 */
	public static boolean filterOut(BookingStatus bookingStatus) {
		return bookingStatus == CANCELLED || bookingStatus == SAVED;
	}
}
