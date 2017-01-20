package com.expedia.bookings.data.itin;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

public class WUndergroundSearchResponse {

	@Root(name = "location")
	public class Location {
		@Element(name = "current_conditions")
		private CurrentConditions currentConditions;
	}

	public class CurrentConditions {
		@Element(name = "conditions_full")
		private String conditionsFull;

		@Element(name = "icon")
		private String icon;
	}

}
