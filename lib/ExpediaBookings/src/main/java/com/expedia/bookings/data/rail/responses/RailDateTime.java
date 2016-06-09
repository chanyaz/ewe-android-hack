package com.expedia.bookings.data.rail.responses;

import org.joda.time.DateTime;

public class RailDateTime {
	public String raw;
	public Long epochSeconds;
	public String localized;
	public String localizedShortDate;

	public DateTime toDateTime() {
		return DateTime.parse(raw);
	}
}
