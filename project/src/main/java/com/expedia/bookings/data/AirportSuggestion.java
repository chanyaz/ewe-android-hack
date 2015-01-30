package com.expedia.bookings.data;

public class AirportSuggestion {
	private int mIcon;
	private String mText1;
	private String mText2;
	private Location mLocation;

	public void setIcon(int icon) {
		mIcon = icon;
	}

	public void setText1(String text1) {
		mText1 = text1;
	}

	public void setText2(String text2) {
		mText2 = text2;
	}

	public void setLocation(Location location) {
		mLocation = location;
	}

	public int getIcon() {
		return mIcon;
	}

	public String getText1() {
		return mText1;
	}

	public String getText2() {
		return mText2;
	}

	public Location getLocation() {
		return mLocation;
	}

}
