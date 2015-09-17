package com.expedia.bookings.data.cars;

import java.util.List;

import com.expedia.bookings.data.Money;

public class CreateTripCarFare extends BaseCarFare {

	public Money totalDueToday;
	public Money totalDueAtPickup;
	public Money grandTotal;
	public List<RateBreakdownItem> priceBreakdownOfTotalDueToday;
	public List<RateBreakdownItem> priceBreakdownOfTotalDueAtPickup;

}
