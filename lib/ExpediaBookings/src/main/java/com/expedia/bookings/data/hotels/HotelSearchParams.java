package com.expedia.bookings.data.hotels;

import org.joda.time.LocalDate;

public class HotelSearchParams {
	public String city;
	public LocalDate checkin;
	public LocalDate checkout;

	public String toServerCheckin() {
		return convert(checkin);
	}

	public String toServerCheckout() {
		return convert(checkout);
	}

	private String convert(LocalDate date) {
		return date.year() + "-" + date.monthOfYear() + "-" + date.dayOfMonth();
	}
}
