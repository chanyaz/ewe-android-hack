package com.expedia.bookings.test.tests.hotels.ui.regression;

import java.util.ArrayList;

import android.widget.RadioButton;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.PhoneSearchActivity;
import com.expedia.bookings.test.utils.CustomActivityInstrumentationTestCase;

public class HotelSearchSortTests extends CustomActivityInstrumentationTestCase<PhoneSearchActivity> {

	private static final String TAG = "Hotel Search Sort Tests";

	public HotelSearchSortTests() {
		super(PhoneSearchActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testSearchForCurrentLocationYieldsSortByDistance() throws Exception {
		String currentLocation = getString(R.string.current_location);
		mDriver.hotelsSearchScreen().clickSearchEditText();
		mDriver.hotelsSearchScreen().clickToClearSearchEditText();
		mDriver.clickOnText(currentLocation);
		mDriver.hotelsSearchScreen().clickOnGuestsButton();
		mDriver.hotelsSearchScreen().guestPicker().clickOnSearchButton();
		mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());
		mDriver.hotelsSearchScreen().clickOnSortButton();
		mDriver.delay(1);
		assertTrue(isSelectedSortMethod(mDriver.hotelsSearchScreen().sortMenu().getSortByDistanceString()));
	}

	public void testSearchByAddressYieldsSortByDistance() throws Exception {
		String address = "114 Sansome St. San Francisco CA 94104";
		mDriver.hotelsSearchScreen().clickSearchEditText();
		mDriver.hotelsSearchScreen().clickToClearSearchEditText();
		mDriver.hotelsSearchScreen().enterSearchText(address);
		mDriver.hotelsSearchScreen().clickOnGuestsButton();
		mDriver.hotelsSearchScreen().guestPicker().clickOnSearchButton();
		mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());
		mDriver.hotelsSearchScreen().clickOnSortButton();
		mDriver.delay(1);
		assertTrue(isSelectedSortMethod(mDriver.hotelsSearchScreen().sortMenu().getSortByDistanceString()));
	}

	public void testSearchByPOIYieldsSortByDistance() throws Exception {
		String pointOfInterest = "Washington Monument";
		mDriver.hotelsSearchScreen().clickSearchEditText();
		mDriver.hotelsSearchScreen().clickToClearSearchEditText();
		mDriver.hotelsSearchScreen().enterSearchText(pointOfInterest);
		mDriver.hotelsSearchScreen().clickOnGuestsButton();
		mDriver.hotelsSearchScreen().guestPicker().clickOnSearchButton();
		mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());
		mDriver.hotelsSearchScreen().clickOnSortButton();
		mDriver.delay(1);
		assertTrue(isSelectedSortMethod(mDriver.hotelsSearchScreen().sortMenu().getSortByDistanceString()));
	}

	public void testSearchByNeighborhoodYieldsSortByPopularity() throws Exception {
		String neighborhood = "Fisherman's Wharf, San Francisco";
		mDriver.hotelsSearchScreen().clickSearchEditText();
		mDriver.hotelsSearchScreen().clickToClearSearchEditText();
		mDriver.hotelsSearchScreen().enterSearchText(neighborhood);
		mDriver.hotelsSearchScreen().clickOnGuestsButton();
		mDriver.hotelsSearchScreen().guestPicker().clickOnSearchButton();
		mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());
		mDriver.hotelsSearchScreen().clickOnSortButton();
		mDriver.delay(1);
		assertTrue(isSelectedSortMethod(mDriver.hotelsSearchScreen().sortMenu().getSortByPopularityString()));
	}

	public void testSearchByAiportYieldsSortByPopularity() throws Exception {
		String airport = "SFO";
		mDriver.hotelsSearchScreen().clickSearchEditText();
		mDriver.hotelsSearchScreen().clickToClearSearchEditText();
		mDriver.hotelsSearchScreen().enterSearchText(airport);
		mDriver.hotelsSearchScreen().clickOnGuestsButton();
		mDriver.hotelsSearchScreen().guestPicker().clickOnSearchButton();
		mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());
		mDriver.hotelsSearchScreen().clickOnSortButton();
		mDriver.delay(1);
		assertTrue(isSelectedSortMethod(mDriver.hotelsSearchScreen().sortMenu().getSortByPopularityString()));
	}

	public void testSearchByPostalCodeDefaultsToPopularity() throws Exception {
		String postalCode = "48104";
		mDriver.hotelsSearchScreen().clickSearchEditText();
		mDriver.hotelsSearchScreen().clickToClearSearchEditText();
		mDriver.hotelsSearchScreen().enterSearchText(postalCode);
		mDriver.hotelsSearchScreen().clickOnGuestsButton();
		mDriver.hotelsSearchScreen().guestPicker().clickOnSearchButton();
		mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());
		mDriver.hotelsSearchScreen().clickOnSortButton();
		mDriver.delay(1);
		assertTrue(isSelectedSortMethod(mDriver.hotelsSearchScreen().sortMenu().getSortByPopularityString()));
	}

	// Assert that the RadioButton checked corresponds to
	// the passed menu title
	private boolean isSelectedSortMethod(String sortTitle) {
		int i = 0;
		ArrayList<RadioButton> buttons = mDriver.getCurrentViews(RadioButton.class);
		RadioButton b;
		TextView t;
		while (i < buttons.size()) {
			b = buttons.get(i);
			if (b.isChecked()) {
				t = mDriver.getView(TextView.class, i * 2);
				return t.getText().equals(sortTitle);
			}
			i++;
		}
		return false;
	}
}
