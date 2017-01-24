package com.expedia.bookings.data.itin;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * http://simple.sourceforge.net/download/stream/doc/tutorial/tutorial.php
 */

@Root(name = "location", strict = false)
public class WUndergroundSearchResponse {

	@Element(name = "current_conditions")
	private WUndergroundCurrentConditions currentConditions;

	@Element(name = "simpleforecast")
	private WUndergroundSimpleForecast simpleForecast;

	public WUndergroundCurrentConditions getWUndergroundCurrentConditions() {
		return currentConditions;
	}

	public WUndergroundSimpleForecast getSimpleForecast() {
		return simpleForecast;
	}

	@Override
	public String toString() {
		return "WUndergroundSearchResponse{" +
			"currentConditions=" + currentConditions +
			", simpleForecast=" + simpleForecast +
			'}';
	}
}
