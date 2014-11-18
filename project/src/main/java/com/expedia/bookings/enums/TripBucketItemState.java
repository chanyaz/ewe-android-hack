package com.expedia.bookings.enums;

/**
 * A brief explanation of states, as of 5/19/2014:
 * DEFAULT - Currently this is same as SHOWING_CHECKOUT_BUTTON state.
 * DISABLED - For the 2nd item in the trip bucket during the CheckoutState.BOOKING
 * 	we make the "Book" button invisible, so users can't book until checkout is completed.
 * SHOWING_CHECKOUT_BUTTON - The name and duration should be center aligned
 *  with text gravity center.
 * BOOKING_UNAVAILABLE - Same as SHOWING_CHECKOUT_BUTTON, except the text is changed.
 * SHOWING_PRICE_CHANGE - exactly the same as EXPANDED,
 *  with the price change notification shown at the bottom of the expanded view
 * EXPANDED - They should be aligned at the bottom.
 * PURCHASED - Aligned bottom and checkmark above them.
 * CONFIRMATION - same as purchased.
 *
 * Created by jdrotos on 2/5/14.
 */
public enum TripBucketItemState {
	DEFAULT,
	DISABLED,
	BOOKING_UNAVAILABLE,
	EXPIRED,
	PURCHASED,

	// The following states are transient UI states and should never be set
	// in the data model!
	SHOWING_CHECKOUT_BUTTON, // transient
	SHOWING_PRICE_CHANGE, // transient
	SHOWING_AIR_ATTACH_PRICE_CHANGE, // transient
	EXPANDED, // transient
	CONFIRMATION, // transient
}
