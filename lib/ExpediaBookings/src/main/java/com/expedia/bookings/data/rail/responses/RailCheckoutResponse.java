package com.expedia.bookings.data.rail.responses;

import com.expedia.bookings.data.BaseApiResponse;

public class RailCheckoutResponse extends BaseApiResponse {
	public final String orderId;
	public final RailNewTrip newTrip;
	public final String currencyCode;
	public final String totalCharges;
	public final RailDomainProduct railDomainProduct;

	public static class RailNewTrip {
		public final String itineraryNumber;
		public String travelRecordLocator;
		public String tripId;
	}
}
