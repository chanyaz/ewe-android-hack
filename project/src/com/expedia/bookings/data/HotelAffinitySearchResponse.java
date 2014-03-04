package com.expedia.bookings.data;

import java.util.List;

public class HotelAffinitySearchResponse extends Response {

	private List<Property> mProperties;

	public List<Property> getProperties() {
		return mProperties;
	}

	public void setProperties(List<Property> properties) {
		mProperties = properties;
	}

}
