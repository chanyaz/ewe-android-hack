package com.expedia.bookings.data.itin;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(strict = false)
public class WUndergroundCurrentConditions {

	@Element(name = "conditions_full")
	private String conditionsFull;

	@Element(name = "icon")
	private String icon;

	public String getConditionsFull() {
		return conditionsFull;
	}

	public String getIcon() {
		return icon;
	}

	@Override
	public String toString() {
		return "Current Condition = " + conditionsFull;
	}
}
