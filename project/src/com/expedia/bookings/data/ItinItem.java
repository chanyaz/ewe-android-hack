package com.expedia.bookings.data;

public class ItinItem {

	public enum ItinItemType {
		FLIGHT, HOTEL, CAR, CRUISE
	}

	private ItinItemType mType;

	public ItinItem() {

	}

	public ItinItemType getItinType() {
		return mType;
	}

	public void setItinType(ItinItemType type) {
		mType = type;
	}

}
