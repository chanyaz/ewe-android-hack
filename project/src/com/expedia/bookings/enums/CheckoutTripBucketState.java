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
			return CheckoutTripBucketState.HIDDEN;
		default:
			return CheckoutTripBucketState.SHOWING;
		}
	}

	public static CheckoutTripBucketState transmogrify(CheckoutFormState state) {
		switch(state) {
		case OVERVIEW:
			return CheckoutTripBucketState.SHOWING;
		default:
			return CheckoutTripBucketState.HIDDEN;
		}
	}
}
