package com.expedia.bookings.data.lx;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.TripInfo;
import com.expedia.bookings.data.BaseApiResponse;

public class LXCheckoutResponse extends BaseApiResponse {
	public final String tripId;
	public final TripInfo newTrip;
	public final String orderId;
	public Money totalChargesPrice;
	public final String currencyCode;
	public final String totalCharges;
	public final Money newTotalPrice;
	public final LXProduct lxProduct;

	// Injected after receiving response; required for communicating price change
	public final Money originalPrice;
}
