package com.expedia.bookings.utils;

import android.location.Address;

public class SearchUtils {

	// Determines whether an Address is an exact location (e.g., a particular street address)
	public static boolean isExactLocation(Address address) {
		return address != null && address.getThoroughfare() != null;
	}
}
