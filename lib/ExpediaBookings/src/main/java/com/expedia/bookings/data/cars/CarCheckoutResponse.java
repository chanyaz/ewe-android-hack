package com.expedia.bookings.data.cars;

import com.expedia.bookings.data.Money;

public class CarCheckoutResponse {
	public String activityId;
	public CarTripInfo newTrip;
	public String orderId;
	public Money totalChargesPrice; // TODO: API needs to include currency code in this object.

	//TODO: nuke these when the comment above is addressed.
	public String currencyCode;
	public String totalCharges;
}
