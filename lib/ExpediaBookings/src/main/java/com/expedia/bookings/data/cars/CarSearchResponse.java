package com.expedia.bookings.data.cars;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

public class CarSearchResponse extends com.expedia.bookings.data.BaseApiResponse {
	public DateTime pickupTime;
	public DateTime dropOffTime;
	public final List<SearchCarOffer> offers = new ArrayList<>();

	public boolean hasProductKey(String productKey) {
		for (SearchCarOffer offer : offers) {
			if (offer.hasProductKey(productKey)) {
				return true;
			}
		}
		return false;
	}

	public SearchCarOffer getProductKeyResponse(String productKey) {
		for (SearchCarOffer offer : offers) {
			if (offer.hasProductKey(productKey)) {
				return offer;
			}
		}
		return null;
	}
}
