package com.expedia.bookings.data.cars;

public enum RateTerm {
	UNKNOWN,
	HOURLY,
	DAILY,
	WEEKLY,
	WEEKEND,
	MONTHLY,
	TOTAL;

	public static RateTerm toEnum(String value) {
		for (RateTerm rateTerm : values()) {
			if (rateTerm.name().equals(value)) {
				return rateTerm;
			}
		}
		return RateTerm.UNKNOWN;
	}
}
