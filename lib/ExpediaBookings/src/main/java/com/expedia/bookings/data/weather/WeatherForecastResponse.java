package com.expedia.bookings.data.weather;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class WeatherForecastResponse {
	@SerializedName("DailyForecasts")
	public List<DailyForecast> dailyForecasts;

	public static class DailyForecast {
		@SerializedName("Date")
		public String date;
		@SerializedName("Temperature")
		public Temperatures temperature;
		@SerializedName("Day")
		public Forecast day;
		@SerializedName("Night")
		public Forecast night;

		public static class Temperatures {
			@SerializedName("Minimum")
			public Temperature minimum;
			@SerializedName("Maximum")
			public Temperature maximum;

			public static class Temperature {
				@SerializedName("Value")
				public int value;
				@SerializedName("Unit")
				public String unit;
			}
		}

		public static class Forecast {
			@SerializedName("Icon")
			public int iconNumber;
			@SerializedName("IconPhrase")
			public String iconPhrase;
		}
	}
}
