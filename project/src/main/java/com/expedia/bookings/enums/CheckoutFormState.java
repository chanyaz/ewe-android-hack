package com.expedia.bookings.enums;

public enum CheckoutFormState {
	OVERVIEW,
	EDIT_TRAVELER,
	EDIT_PAYMENT,
	;

	public boolean isOpen() {
		return this != OVERVIEW;
	}
}
