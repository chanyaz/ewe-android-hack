package com.expedia.bookings.data.cars;

import com.expedia.bookings.utils.Strings;

public class SearchCarOffer extends BaseCarOffer {
	public SearchCarFare fare;
	public boolean isToggled = false;

	public boolean hasProductKey(String productKey) {
		if (Strings.equals(this.productKey, productKey)) {
			return true;
		}
		return false;
	}
}
