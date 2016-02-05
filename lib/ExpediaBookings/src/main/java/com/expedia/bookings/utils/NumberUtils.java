package com.expedia.bookings.utils;

import java.math.BigDecimal;

public class NumberUtils {

	public static Double parseDoubleSafe(String str) {
		if (Strings.isEmpty(str)) {
			return null;
		}

		try {
			return Double.parseDouble(str);
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	public static int getPercentagePaidWithPointsForOmniture(BigDecimal fraction, BigDecimal total) {
		if (total.equals(BigDecimal.ZERO)) {
			throw new IllegalArgumentException("Total cannot be zero while calculating percentage");
		}
		return fraction.divide(total, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100")).intValue();
	}
}
