package com.expedia.bookings.data.cars;

import com.expedia.bookings.utils.Strings;

public class SearchCarOffer extends BaseCarOffer {
	public SearchCarFare fare;
	public boolean isToggled = false;

	public boolean hasProductKey(String productKey) {
		return Strings.equals(this.productKey, productKey);
	}
}
