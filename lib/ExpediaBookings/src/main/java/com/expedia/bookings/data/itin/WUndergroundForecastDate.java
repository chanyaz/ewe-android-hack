package com.expedia.bookings.data.itin;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(strict = false)
public class WUndergroundForecastDate {

	@Element(name = "weekday")
	private String weekday;

	public String getWeekday() {
		return weekday;
	}

	@Override
	public String toString() {
		return "WUndergroundForecastDate{" +
			"weekday='" + weekday + '\'' +
			'}';
	}
}
