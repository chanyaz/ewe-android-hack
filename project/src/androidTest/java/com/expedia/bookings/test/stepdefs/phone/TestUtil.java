package com.expedia.bookings.test.stepdefs.phone;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nullable;

import org.hamcrest.Matcher;
import org.joda.time.LocalDate;

import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewAssertion;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.expedia.bookings.test.stepdefs.phone.model.ApiRequestData;
import com.expedia.bookings.widget.flights.FlightListAdapter;

import junit.framework.Assert;

import static org.hamcrest.MatcherAssert.assertThat;

public class TestUtil {
	public static Map<String, String> dataSet;
	public static Map<String, String> storeDataAtRuntime = new HashMap();

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
		String stDateStr = getDateInMMMdd(startDays);
		String endDateStr = getDateInMMMdd(endDays);
		return (stDateStr + " - " + endDateStr);
	}

	public static String getFormattedGuestString(int guestCount) {
		String guestStr = Integer.toString(guestCount);
		guestStr += ((guestCount > 1) ? " Guests" : " Guest");
		return guestStr;
	}

	public static LocalDate getDateFromOffset(int offset) {
		return LocalDate.now()
			.plusDays(offset);
	}

	public static String getDayFromDate(LocalDate date) {
		return String.valueOf(date.getDayOfMonth());
	}

	public static String getMonthFromDate(LocalDate date) {
		return String.valueOf(getMonth(date.getMonthOfYear()));
	}

	public static String getYearFromDate(LocalDate date) {
		return String.valueOf(date.getYear());
	}

	public static String getMonth(int month) {
		switch (month) {
		case 1:
			return "Jan";
		case 2:
			return "Feb";
		case 3:
			return "Mar";
		case 4:
			return "Apr";
		case 5:
			return "May";
		case 6:
			return "Jun";
		case 7:
			return "Jul";
		case 8:
			return "Aug";
		case 9:
			return "Sep";
		case 10:
			return "Oct";
		case 11:
			return "Nov";
		default:
			return "Dec";
		}

	}

	public static void validateRequestParams(Map<String, String> expParameters, ApiRequestData apiRequestData) {
		for (Map.Entry<String, String> entry : expParameters.entrySet()) {
			Assert.assertEquals(entry.getValue(), apiRequestData.getQueryParams().get(entry.getKey()).get(0));
		}
	}

	public static String getDateInMMMdd(String days) {
		LocalDate startDate = LocalDate.now().plusDays(Integer.parseInt(days));
		Format dateFormatter = new SimpleDateFormat("MMM d", Locale.US);
		return dateFormatter.format(startDate.toDate());
	}

	public static String getDateRangeInMMMdd(String range) {
		String dateString = getDateInMMMdd(range.split(" - ")[0]) + " - " + getDateInMMMdd(range.split(" - ")[1]);
		return dateString;
	}
}
