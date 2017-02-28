package com.expedia.bookings.test.stepdefs.phone.flights;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;


public class TestUtilFlights {
	public static Map<String, String> dataSet;

	public static int convertFlightDepartureTimeToInteger(String time) throws ParseException {

		String formattedTime = formatTime(time);

		int hours = Integer.parseInt(formattedTime.split(":")[0]);
		int mins = Integer.parseInt(formattedTime.split(":")[1]);

		return ((hours * 60) + mins);
	}

	public static int convertArrivalTimeToInteger(String time) throws ParseException {

		String formattedTime = formatTime(time);

		int hours = Integer.parseInt(formattedTime.split(":")[0]);
		int mins = Integer.parseInt(formattedTime.split(":")[1]);

		if (time.contains("+1d")) {
			hours = hours + 24;
		}
		else if (time.contains("+2d")) {
			hours = hours + 48;
		}
		else if (time.contains("-1d")) {
			hours = hours - 24;
		}
		else if (time.contains("-2d")) {
			hours = hours - 48;
		}

		return ((hours * 60) + mins);
	}

	public static int convertDurationToInteger(String durationAtPosition) {
		int hours, mins;
		if (durationAtPosition.contains("h")) {
			hours = Integer.parseInt(durationAtPosition.split("h ")[0]);
			mins = Integer.parseInt(durationAtPosition.split("h ")[1].split("m")[0]);
		}
		else {
			hours = 0;
			mins = Integer.parseInt(durationAtPosition.split("m")[0]);
		}

		return ((hours * 60) + mins);
	}

	private static String formatTime(String time) throws ParseException {
		SimpleDateFormat targetFormat = new SimpleDateFormat("HH:mm", Locale.US);
		SimpleDateFormat currentFormat = new SimpleDateFormat("hh:mm", Locale.US);

		String formattedTime;
		int index = time.indexOf(":");
		if (time.contains("am") || time.contains("pm")) {
			time = time.substring(0, index + 6);

			Date currentTime = currentFormat.parse(time);
			formattedTime = targetFormat.format(currentTime);
		}
		else {
			formattedTime = time.substring(0, index + 3);
		}

		return formattedTime;
	}


}
