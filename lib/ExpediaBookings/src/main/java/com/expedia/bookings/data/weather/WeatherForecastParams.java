package com.expedia.bookings.data.weather;

public class WeatherForecastParams {
	public String apiKey;
	public String locationCode;

	public WeatherForecastParams(String apiKey, String locationCode) {
		this.apiKey = apiKey;
		this.locationCode = locationCode;
	}
}
