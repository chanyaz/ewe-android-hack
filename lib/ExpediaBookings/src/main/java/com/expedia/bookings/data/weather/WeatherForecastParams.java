package com.expedia.bookings.data.weather;

public class WeatherForecastParams {
	public String apiKey;
	public String locationCode;

	public WeatherForecastParams(String locationCode, String apiKey) {
		this.apiKey = apiKey;
		this.locationCode = locationCode;
	}
}
