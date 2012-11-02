package com.expedia.bookings.data;

/**
 * Class to back a destination on the launch screen in the Hotels ListView container in the event that user has disabled
 * location services. Consists of a city name and a pretty image url taken from a hotel located in that city.
 */
public class HotelDestination {

	private String mCity;
	private String mImgUrl;

	public HotelDestination setCity(String city) {
		mCity = city;
		return this;
	}

	public String getCity() {
		return mCity;
	}

	public HotelDestination setImgUrl(String url) {
		mImgUrl = url;
		return this;
	}

	public String getImgUrl() {
		return mImgUrl;
	}

}
