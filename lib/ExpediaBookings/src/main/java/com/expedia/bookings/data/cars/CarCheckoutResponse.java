package com.expedia.bookings.data.cars;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.TripInfo;

public class CarCheckoutResponse extends BaseCarResponse {
	public String activityId;
	public TripInfo newTrip;
	public String orderId;
	public Money totalChargesPrice; // TODO: API needs to include currency code in this object.

	//TODO: nuke these when the comment above is addressed.
	public String currencyCode;
	public String totalCharges;

}
