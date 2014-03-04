package com.expedia.bookings.test.tests.tablet;

import org.joda.time.LocalDate;

import android.annotation.SuppressLint;

import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.test.tests.pageModels.tablet.SearchResults;
import com.expedia.bookings.test.tests.pageModels.tablet.SearchScreen;
import com.expedia.bookings.test.utils.CustomActivityInstrumentationTestCase;
import com.google.android.apps.common.testing.ui.espresso.Espresso;
import com.google.android.apps.common.testing.ui.espresso.contrib.CountingIdlingResource;
import com.squareup.otto.Subscribe;

public class TabletDemo extends CustomActivityInstrumentationTestCase<SearchActivity> {

	private CountingIdlingResource mSuggestionsIdlingResource;

	@SuppressLint("NewApi")
	public TabletDemo() {
		super(SearchActivity.class);
		Events.register(this);
		mSuggestionsIdlingResource = new CountingIdlingResource("SuggestionResults");
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		Espresso.registerIdlingResources(mSuggestionsIdlingResource);
		// Espresso will not launch our activity for us, we must launch it via getActivity().
		getActivity();
	}

	@Override
	public void tearDown() throws Exception {
		mSuggestionsIdlingResource = null;
	}

	public void testOne() throws InterruptedException {
		SearchScreen.clickToStartSearch();
		SearchScreen.clickDestinationEditText();
		SearchScreen.typeInDestinationEditText("Detroit, MI");
		SearchScreen.clickInListWithText("Detroit, MI");
		SearchScreen.clickOriginEditText();
		SearchScreen.typeInOriginEditText("San Francisco, CA");
		SearchScreen.clickInListWithText("San Francisco, CA");
		LocalDate ld1 = LocalDate.now();
		SearchScreen.clickDate(ld1, ld1.plusDays(2));
		SearchScreen.clickSearchButton();
		Thread.sleep(30000);
		SearchResults.swipeUpHotelList();
		Thread.sleep(3000);
	}

	@Subscribe
	public void on(Events.SuggestionQueryStarted event) {
		mSuggestionsIdlingResource.increment();
	}

	@Subscribe
	public void on(Events.SuggestionResultsDelivered event) {
		mSuggestionsIdlingResource.decrement();
	}
}
