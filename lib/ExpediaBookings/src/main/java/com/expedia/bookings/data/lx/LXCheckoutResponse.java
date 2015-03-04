package com.expedia.bookings.data.lx;

import java.util.List;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.TripInfo;

public class LXCheckoutResponse {
	public String activityId;
	public TripInfo newTrip;
	public String orderId;
	public Money totalChargesPrice;
	public String currencyCode;
	public String totalCharges;
	public List<LXApiError> errors;
}
