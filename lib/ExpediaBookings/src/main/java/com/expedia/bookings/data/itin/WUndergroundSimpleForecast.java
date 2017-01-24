package com.expedia.bookings.data.itin;

import java.util.List;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(strict = false)
public class WUndergroundSimpleForecast {

	@ElementList(type = WUndergroundForecastDay.class, inline=true)
	private List<WUndergroundForecastDay> forecastDays;

	public List<WUndergroundForecastDay> getForecastDays() {
		return forecastDays;
	}

	@Override
	public String toString() {
		return "WUndergroundSimpleForecast{" +
			"forecastDays=" + forecastDays +
			'}';
	}
}
