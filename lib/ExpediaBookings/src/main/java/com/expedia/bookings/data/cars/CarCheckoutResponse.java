package com.expedia.bookings.data.cars;

import java.util.List;

import com.expedia.bookings.data.Money;

public class CarCheckoutResponse {
	public String activityId;
	public CarTripInfo newTrip;
	public String orderId;
	public Money totalChargesPrice; // TODO: API needs to include currency code in this object.

	//TODO: nuke these when the comment above is addressed.
	public String currencyCode;
	public String totalCharges;

	public List<CarApiError> errors;

	public boolean hasErrors() {
		return errors != null && errors.size() > 0;
	}

	public String printErrors() {
		StringBuilder builder = new StringBuilder();
		for (CarApiError error : errors) {
			builder.append(error.toDebugString());
			builder.append("\n");
		}
		return builder.toString();
	}

}
