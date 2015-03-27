package com.expedia.bookings.data.lx;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.TripInfo;
import com.expedia.bookings.data.cars.BaseApiResponse;

public class LXCheckoutResponse extends BaseApiResponse {
	public TripInfo newTrip;
	public String orderId;
	public Money totalChargesPrice;
	public String currencyCode;
	public String totalCharges;
}
