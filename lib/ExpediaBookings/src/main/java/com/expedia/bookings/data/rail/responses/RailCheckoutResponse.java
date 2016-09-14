package com.expedia.bookings.data.rail.responses;

import com.expedia.bookings.data.BaseApiResponse;

public class RailCheckoutResponse extends BaseApiResponse {
	public String orderId;
	public RailNewTrip newTrip;

	public class RailNewTrip {
		public String itineraryNumber;
		public String travelRecordLocator;
		public String tripId;
	}
}
