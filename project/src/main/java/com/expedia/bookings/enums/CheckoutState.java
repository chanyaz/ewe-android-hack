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
		return !(state == CVV || state == BOOKING || state == CONFIRMATION);
	}

	/**
	 * If the CheckoutState is one of these states, it means we have already validated checkout information
	 * and have allowed the user to move forward in states.
	 */
	public boolean validCheckoutInfoHasBeenEntered() {
		return this == CVV || this == BOOKING || this == CONFIRMATION;
	}

	public boolean shouldShowConfirmation() {
		return this == CONFIRMATION;
	}

}
