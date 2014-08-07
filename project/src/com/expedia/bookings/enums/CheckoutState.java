package com.expedia.bookings.enums;

public enum CheckoutState {
	OVERVIEW,
	FORM_OPEN,
	READY_FOR_CHECKOUT,
	CVV,
	BOOKING,
	BOOKING_UNAVAILABLE,
	CONFIRMATION,
	;

	public static boolean shouldShowPriceChange(CheckoutState state) {
		return !(state == READY_FOR_CHECKOUT || state == CVV || state == BOOKING || state == CONFIRMATION);
	}
}
