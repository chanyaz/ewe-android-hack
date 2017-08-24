package com.expedia.bookings.data.cars;

import java.util.List;

import com.expedia.bookings.data.Money;

public class CreateTripCarFare extends BaseCarFare {

	public final Money totalDueToday;
	public final Money totalDueAtPickup;
	public Money grandTotal;
	public final List<RateBreakdownItem> priceBreakdownOfTotalDueToday;
	public final List<RateBreakdownItem> priceBreakdownOfTotalDueAtPickup;

}
