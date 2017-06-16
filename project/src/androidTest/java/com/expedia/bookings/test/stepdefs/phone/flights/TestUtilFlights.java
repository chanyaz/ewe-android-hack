package com.expedia.bookings.test.stepdefs.phone.flights;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nullable;

import org.hamcrest.Matcher;

import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewAssertion;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.expedia.bookings.test.stepdefs.phone.CommonSteps;
import com.expedia.bookings.widget.flights.FlightListAdapter;

import junit.framework.Assert;

import static org.hamcrest.MatcherAssert.assertThat;

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

	public static ViewAssertion assertFlightResultsListFor(final Matcher<View> matcher) {
		return new ViewAssertion() {

			@Override
			public void check(@Nullable View view, @Nullable NoMatchingViewException e) {

				RecyclerView recyclerView = (RecyclerView) view;
				FlightListAdapter adapter = (FlightListAdapter) recyclerView.getAdapter();
				int itemCount = adapter.getItemCount() - adapter.adjustPosition();

				for (int position = 1; position <= itemCount; position++) {
					RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForPosition(position);
					View viewAtPosition = viewHolder.itemView;
					assertThat(viewAtPosition, matcher);
				}
			}
		};
	}

	public static ViewAssertion assertFlightsResultsListSizeEquals(final int size) {
		return new ViewAssertion() {
			@Override
			public void check(View view, NoMatchingViewException noView) {
				RecyclerView recyclerView = (RecyclerView) view;
				FlightListAdapter adapter = (FlightListAdapter) recyclerView.getAdapter();
				int itemCount = adapter.getItemCount() - adapter.adjustPosition();
				Assert.assertEquals(itemCount, size);
			}
		};
	}

	public static String getFormattedDateString(String startDays, String endDays) {
		String stDateStr = CommonSteps.getDateInMMMdd(startDays);
		String endDateStr = CommonSteps.getDateInMMMdd(endDays);
		return (stDateStr + " - " + endDateStr);
	}

	public static String getFormattedGuestString(int guestCount) {
		String guestStr = Integer.toString(guestCount);
		guestStr += ((guestCount > 1) ? " Guests" : " Guest");
		return guestStr;
	}
}
