package com.expedia.bookings.data.rail.responses;

import com.expedia.bookings.data.Money;

public abstract class BaseRailOffer {
	public Money totalPrice;
	public String railOfferToken;
	//TODO Add price breakdown
}
