package com.expedia.bookings.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

	public static List<Integer> getRandomNumberList(int size) {
		List<Integer> numberList = new ArrayList<>();
		for (int index = 0; index < size; index++) {
			numberList.add(index);
		}
		Collections.shuffle(numberList);
		return numberList;
	}

	public static float round(float number, int precision) {
		float shiftNumberByPlaces = (float) Math.pow(10, precision);
		return Math.round(number * shiftNumberByPlaces) / shiftNumberByPlaces;
	}
}
