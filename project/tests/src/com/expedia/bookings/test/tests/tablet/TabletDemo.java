package com.expedia.bookings.test.tests.tablet;

import org.joda.time.LocalDate;

import android.annotation.SuppressLint;

import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.tests.pageModels.tablet.SearchResults;
import com.expedia.bookings.test.tests.pageModels.tablet.SearchScreen;
import com.expedia.bookings.test.utils.CustomActivityInstrumentationTestCase;

public class TabletDemo extends CustomActivityInstrumentationTestCase<SearchActivity> {

	@SuppressLint("NewApi")
	public TabletDemo() {
		super(SearchActivity.class);
		SearchScreen.registerSuggestionResource();
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		// Espresso will not launch our activity for us, we must launch it via getActivity().
		getActivity();
	}

	@Override
	public void tearDown() throws Exception {
		SearchScreen.unregisterSuggestionResource();
	}

	public void testOne() throws InterruptedException {
		SearchScreen.clickToStartSearch();
		SearchScreen.clickDestinationEditText();
		SearchScreen.typeInDestinationEditText("Detroit, MI");
		SearchScreen.clickInListWithText("Detroit, MI");
		SearchScreen.clickOriginEditText();
		SearchScreen.typeInOriginEditText("San Francisco, CA");
		SearchScreen.clickInListWithText("San Francisco, CA");
		LocalDate now = LocalDate.now();
		SearchScreen.clickDate(now, now.plusDays(2));
		SearchScreen.clickSearchButton();
		Thread.sleep(30000);
		SearchResults.swipeUpHotelList();
		Thread.sleep(3000);
	}
}
