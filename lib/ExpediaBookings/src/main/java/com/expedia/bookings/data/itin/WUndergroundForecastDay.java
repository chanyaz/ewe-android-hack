package com.expedia.bookings.data.itin;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(strict = false, name = "forecastday")
public class WUndergroundForecastDay {

	@Element(name = "condition")
	private String condition;

	@Element(name = "date")
	private WUndergroundForecastDate date;

	@Element(name = "max_temp")
	private WUndergroundForecastTemp maxTemp;

	@Element(name = "min_temp")
	private WUndergroundForecastTemp minTemp;

	public String getCondition() {
		return condition;
	}

	public WUndergroundForecastDate getDate() {
		return date;
	}

	public WUndergroundForecastTemp getMaxTemp() {
		return maxTemp;
	}

	public WUndergroundForecastTemp getMinTemp() {
		return minTemp;
	}

	@Override
	public String toString() {
		return "WUndergroundForecastDay{" +
			"condition='" + condition + '\'' +
			", date=" + date +
			", maxTemp=" + maxTemp +
			", minTemp=" + minTemp +
			'}';
	}
}
