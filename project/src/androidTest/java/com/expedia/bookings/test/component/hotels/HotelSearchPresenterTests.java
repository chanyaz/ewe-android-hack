package com.expedia.bookings.test.component.hotels;

import org.joda.time.LocalDate;
import org.junit.Rule;
import org.junit.Test;

import com.expedia.bookings.R;
import com.expedia.bookings.test.rules.ExpediaMockWebServerRule;
import com.expedia.bookings.test.rules.PlaygroundRule;
import com.expedia.bookings.utils.DateUtils;

import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class HotelSearchPresenterTests {

	@Rule
	public final PlaygroundRule playground = new PlaygroundRule(R.layout.widget_hotel_search_params);
	@Rule
	public final ExpediaMockWebServerRule server = new ExpediaMockWebServerRule();

	@Test
	public void testDateButtonTextPopulation() throws Throwable {
		// Open calendar
		HotelViewModel.selectDateButton().check(matches(withText(R.string.select_dates)));

		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);

		// Select start date
		HotelViewModel.selectDates(startDate, null);
		HotelViewModel.selectDateButton().check(matches(withText(DateUtils.localDateToMMMd(startDate))));

		// Select end date
		HotelViewModel.selectDates(startDate, endDate);
		String expected = DateUtils.localDateToMMMd(startDate) + " to " + DateUtils.localDateToMMMd(endDate);
		HotelViewModel.selectDateButton().check(matches(withText(expected)));
	}
}
