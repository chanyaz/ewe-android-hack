package com.expedia.bookings.data.rail.responses;

import java.util.List;

import com.expedia.bookings.data.Money;

public class RailTicketDeliveryOption {
	public final RailCreateTripResponse.RailTicketDeliveryOptionToken ticketDeliveryOptionToken;
	public String ticketDeliveryOptionCategoryCode;
	public String ticketDeliveryDescription;
	public final Money ticketDeliveryFee;
	public List<String> ticketDeliveryCountryCodeList;
	public boolean departureStation;
}
