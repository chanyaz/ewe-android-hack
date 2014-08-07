package com.expedia.bookings.enums;

public enum CheckoutTripBucketState {
	// The open state is for portrait only
	OPEN,

	SHOWING,
	HIDDEN,
	;

	public static CheckoutTripBucketState transmogrify(CheckoutState state) {
		switch(state) {
		case CVV:
		case BOOKING:
		case FORM_OPEN:
			return CheckoutTripBucketState.HIDDEN;
		default:
			return CheckoutTripBucketState.SHOWING;
		}
	}
}
