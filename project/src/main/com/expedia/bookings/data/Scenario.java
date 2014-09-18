package com.expedia.bookings.data;

public class Scenario {

	private String mName;
	private String mUrl;

	public Scenario(String name, String url) {
		mName = name;
		mUrl = url;
	}

	public String getUrl() {
		return mUrl;
	}

	public String getName() {
		return mName;
	}

}
