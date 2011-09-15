package com.expedia.bookings.data;

public class PropertyInfoResponse extends Response {
	private PropertyInfo mPropertyInfo;

	public void setPropertyInfo(PropertyInfo propertyInfo) {
		mPropertyInfo = propertyInfo;
	}

	public PropertyInfo getPropertyInfo() {
		return mPropertyInfo;
	}
}
