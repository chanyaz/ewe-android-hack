package com.expedia.bookings.enums;

/**
 * A brief explanation of states, as of 5/19/2014:
 * DEFAULT -
 * DISABLED -
 * SHOWING_CHECKOUT_BUTTON - The name and duration should be center aligned
 *   with text gravity center.
 * SHOWING_PRICE_CHANGE - exactly the same as EXPANDED,
 *   with the price change notification shown at the bottom of the expanded view
 * EXPANDED - They should be aligned at the bottom.
 * PURCHASED - Aligned bottom and checkmark above them.
 * CONFIRMATION - same as purchased.
 *
 * Created by jdrotos on 2/5/14.
 */
public enum TripBucketItemState {
	DEFAULT,
	DISABLED,
	SHOWING_CHECKOUT_BUTTON,
	SHOWING_PRICE_CHANGE,
	EXPANDED,
	PURCHASED,
	CONFIRMATION
}
