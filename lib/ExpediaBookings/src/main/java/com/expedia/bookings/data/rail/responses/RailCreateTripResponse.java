package com.expedia.bookings.data.rail.responses;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.TripResponse;

public class RailCreateTripResponse extends TripResponse {

	public Money totalPrice;
	public List<RailProduct> railsProducts;
	public String offerToken;

	public static class RailProduct {
		public String productToken;
		public Money productPrice;
		public List<RailLegOption> legList;
		public RailDateTime productHoldExpirationDateTime;
		public RailBookingOptions additionalBookingOptions;
		public List<FareBreakdown> fareBreakdown;

		public static class FareBreakdown {
			public Money fare;
			public List<SegmentClass> segmentClass;

			public class SegmentClass {
				public String serviceClass;
				public String fareClass;
			}
		}
	}

	public static class RailBookingOptions {
		public List<TicketDeliveryOption> ticketDeliveryOptions;

		public static class TicketDeliveryOption {
			public String description;
			public String code;
			public String destination;
			public boolean isRefundable;
			public Money fee;
		}
	}

	@NotNull
	@Override
	public Money getTripTotalExcludingFee() {
		return totalPrice;
	}

	@NotNull
	@Override
	public Money tripTotalPayableIncludingFeeIfZeroPayableByPoints() {       //what does it mean???
		throw new UnsupportedOperationException("TripTotalIncludingFee is not implemented for rail");
	}

	@Override
	public boolean isCardDetailsRequiredForBooking() {
		return true;
	}
}
