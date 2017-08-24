package com.expedia.bookings.data.rail.responses;

import org.joda.time.DateTime;

public class RailDateTime implements Comparable<RailDateTime> {
	public final String raw;
	public Long epochSeconds;
	public String localized;
	public String localizedShortDate;

	public DateTime toDateTime() {
		return DateTime.parse(raw);
	}

	@Override
	public int compareTo(RailDateTime another) {
		if (this != null && another != null && this.epochSeconds != null && another.epochSeconds != null) {
			if (this.epochSeconds > another.epochSeconds) {
				return 1;
			}
			else if (this.epochSeconds < another.epochSeconds) {
				return -1;
			}
			else {
				return 0;
			}
		}
		else {
			return 0;
		}
	}
}
