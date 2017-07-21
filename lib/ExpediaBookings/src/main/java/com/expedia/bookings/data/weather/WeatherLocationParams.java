package com.expedia.bookings.data.weather;

/**
 * Created by kseo on 7/21/17.
 */

public class WeatherLocationParams {
	public final String apiKey;
	public final String query;

	public WeatherLocationParams(String apiKey, String query) {
		this.apiKey = apiKey;
		this.query = query;
	}
}
