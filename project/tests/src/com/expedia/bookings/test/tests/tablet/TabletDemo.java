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
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		// Espresso will not launch our activity for us, we must launch it via getActivity().
		getActivity();
	}

	public void testOne() throws InterruptedException {
		Thread.sleep(1000);
		SearchScreen.clickToStartSearch();
		Thread.sleep(3000);
		SearchScreen.clickDestinationEditText();
		Thread.sleep(3000);
		SearchScreen.typeInDestinationEditText("Detroit, MI");
		Thread.sleep(1000);
		SearchScreen.clickInListWithText("Detroit, MI");
		SearchScreen.clickOriginEditText();
		SearchScreen.typeInOriginEditText("San Francisco, CA");
		Thread.sleep(1000);
		SearchScreen.clickInListWithText("San Francisco, CA");
		Thread.sleep(3000);
		LocalDate ld1 = LocalDate.now();
		SearchScreen.clickDate(ld1, ld1.plusDays(2));
		SearchScreen.clickSearchButton();
		Thread.sleep(30000);
		SearchResults.swipeUpHotelList();
		Thread.sleep(3000);
	}
}
