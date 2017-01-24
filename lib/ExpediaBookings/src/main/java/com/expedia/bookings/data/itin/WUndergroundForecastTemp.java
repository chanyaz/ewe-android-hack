package com.expedia.bookings.data.itin;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(strict = false)
public class WUndergroundForecastTemp {

	@Element(name = "fahrenheit")
	private int fahrenheit;

	@Element(name = "celsius")
	private int celsius;

	public int getCelsius() {
		return celsius;
	}

	public int getFahrenheit() {
		return fahrenheit;
	}

	@Override
	public String toString() {
		return "WUndergroundForecastTemp{" +
			"celsius=" + celsius +
			", fahrenheit=" + fahrenheit +
			'}';
	}
}
