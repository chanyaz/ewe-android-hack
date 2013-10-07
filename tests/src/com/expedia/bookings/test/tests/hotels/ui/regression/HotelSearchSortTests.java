package com.expedia.bookings.test.tests.hotels.ui.regression;

import java.util.ArrayList;

import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.PhoneSearchActivity;
import com.expedia.bookings.test.tests.pageModels.hotels.HotelSearchResultRow;
import com.expedia.bookings.test.utils.CustomActivityInstrumentationTestCase;

/*
 * This class' methods exercise and verify app functionality related
 * to hotels search & sort.
 */
public class HotelSearchSortTests extends CustomActivityInstrumentationTestCase<PhoneSearchActivity> {

	private static final String TAG = HotelSearchSortTests.class.getSimpleName();

	public HotelSearchSortTests() {
		super(PhoneSearchActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	private float getCleanFloatFromTextView(TextView t) {
		String str = t.getText().toString();
		str = str.replaceAll("[^\\d.]", "");
		return Float.parseFloat(str);
	}

	private void initiateSearchHelper(String searchDestination) throws Exception {
		mDriver.hotelsSearchScreen().clickSearchEditText();
		mDriver.hotelsSearchScreen().clickToClearSearchEditText();
		mDriver.hotelsSearchScreen().enterSearchText(searchDestination);
		mDriver.hotelsSearchScreen().clickOnGuestsButton();
		mDriver.hotelsSearchScreen().guestPicker().clickOnSearchButton();
		mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());
	}

	public void testSortByPrice() throws Exception {
		mUser.setHotelCityToRandomUSCity();
		mDriver.enterLog(TAG, "Test Sort By Price City: " + mUser.getHotelSearchCity());
		initiateSearchHelper(mUser.getHotelSearchCity());
		if (!mDriver.searchText(mDriver.hotelsSearchScreen().noHotelsAvailableTonight(), 1, false, true)) {
			mDriver.hotelsSearchScreen().clickOnSortButton();
			mDriver.delay(1);
			mDriver.hotelsSearchScreen().sortMenu().clickSortByPriceString();
			mDriver.delay(1);
			int totalHotels = mDriver.hotelsSearchScreen().hotelResultsListView().getCount();
			int hotelsPerScreenHeight = mDriver.hotelsSearchScreen().hotelResultsListView().getChildCount();
			if (totalHotels > 1) {
				View topHotelRow = mDriver.hotelsSearchScreen().hotelResultsListView()
						.getChildAt(1);
				HotelSearchResultRow previousRow = new HotelSearchResultRow(topHotelRow);
				float previousRowPrice = getCleanFloatFromTextView(previousRow.getPriceTextView());

				for (int j = 0; j < totalHotels / hotelsPerScreenHeight; j++) {
					for (int i = 1; i < hotelsPerScreenHeight; i++) {
						View currentHotelRowView = mDriver.hotelsSearchScreen().hotelResultsListView().getChildAt(i);
						HotelSearchResultRow currentRow = new HotelSearchResultRow(currentHotelRowView);
						float currentRowPrice = getCleanFloatFromTextView(currentRow.getPriceTextView());
						mDriver.enterLog(TAG, "RATING " + currentRowPrice + " >= " + previousRowPrice);
						assertTrue(currentRowPrice >= previousRowPrice);
						previousRowPrice = currentRowPrice;
					}
					mDriver.scrollDown();
					hotelsPerScreenHeight = mDriver.hotelsSearchScreen().hotelResultsListView().getChildCount();
				}
			}
		}
	}

	public void testSortByDistance() throws Exception {
		String address = "114 Sansome St. San Francisco, CA 94104";
		mDriver.enterLog(TAG, "Test Sort By Distance - address: " + address);
		initiateSearchHelper(address);
		if (!mDriver.searchText(mDriver.hotelsSearchScreen().noHotelsAvailableTonight(), 1, false, true)) {
			mDriver.hotelsSearchScreen().clickOnSortButton();
			mDriver.delay(1);
			mDriver.hotelsSearchScreen().sortMenu().clickSortByDistanceString();
			mDriver.delay(1);
			int totalHotels = mDriver.hotelsSearchScreen().hotelResultsListView().getCount();
			int hotelsPerScreenHeight = mDriver.hotelsSearchScreen().hotelResultsListView().getChildCount();

			if (totalHotels > 1) {
				View topHotelRow = mDriver.hotelsSearchScreen().hotelResultsListView()
						.getChildAt(1);
				HotelSearchResultRow previousRow = new HotelSearchResultRow(topHotelRow);
				float previousRowDistance = getCleanFloatFromTextView(previousRow.getProximityTextView());
				for (int j = 0; j < totalHotels / hotelsPerScreenHeight; j++) {
					for (int i = 1; i < hotelsPerScreenHeight; i++) {
						View currentHotelRowView = mDriver.hotelsSearchScreen().hotelResultsListView().getChildAt(i);
						HotelSearchResultRow currentRow = new HotelSearchResultRow(currentHotelRowView);
						float currentRowDistance = getCleanFloatFromTextView(currentRow.getProximityTextView());
						mDriver.enterLog(TAG, "DISTANCE " + currentRowDistance + " >= " + previousRowDistance);
						assertTrue(currentRowDistance >= previousRowDistance);
						previousRowDistance = currentRowDistance;
					}
					mDriver.scrollDown();
					hotelsPerScreenHeight = mDriver.hotelsSearchScreen().hotelResultsListView().getChildCount();
				}
			}
		}
	}

	public void testSortByRating() throws Exception {
		mUser.setHotelCityToRandomUSCity();
		mDriver.enterLog(TAG, "Test Sort By Rating City: " + mUser.getHotelSearchCity());
		initiateSearchHelper(mUser.getHotelSearchCity());
		if (!mDriver.searchText(mDriver.hotelsSearchScreen().noHotelsAvailableTonight(), 1, false, true)) {
			mDriver.hotelsSearchScreen().clickOnSortButton();
			mDriver.delay(1);
			mDriver.hotelsSearchScreen().sortMenu().clickSortByUserRatingString();
			mDriver.delay(1);

			final int totalHotels = mDriver.hotelsSearchScreen().hotelResultsListView().getCount() - 1;
			int hotelsPerScreenHeight = mDriver.hotelsSearchScreen().hotelResultsListView().getChildCount();
			if (totalHotels > 1) {
				View topHotelRow = mDriver.hotelsSearchScreen().hotelResultsListView()
						.getChildAt(1);
				HotelSearchResultRow previousRow = new HotelSearchResultRow(topHotelRow);
				float previousRowRating = previousRow.getRatingBar().getRating();
				for (int i = 0; i < totalHotels / hotelsPerScreenHeight; i++) {
					for (int j = 1; j < hotelsPerScreenHeight; j++) {
						View currentHotelRowView = mDriver.hotelsSearchScreen().hotelResultsListView()
								.getChildAt(j);
						HotelSearchResultRow currentRow = new HotelSearchResultRow(currentHotelRowView);
						float currentRowRating = currentRow.getRatingBar().getRating();
						mDriver.enterLog(TAG, "RATING " + previousRowRating + " >= " + currentRowRating);
						assertTrue(previousRowRating >= currentRowRating);
						previousRowRating = currentRowRating;
					}
					mDriver.scrollDown();
					hotelsPerScreenHeight = mDriver.hotelsSearchScreen().hotelResultsListView().getChildCount();
				}
			}
		}
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
		assertTrue(isTheSelectedSortMethod(mDriver.hotelsSearchScreen().sortMenu().getSortByDistanceString()));
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
		assertTrue(isTheSelectedSortMethod(mDriver.hotelsSearchScreen().sortMenu().getSortByDistanceString()));
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
		assertTrue(isTheSelectedSortMethod(mDriver.hotelsSearchScreen().sortMenu().getSortByDistanceString()));
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
		assertTrue(isTheSelectedSortMethod(mDriver.hotelsSearchScreen().sortMenu().getSortByPopularityString()));
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
		assertTrue(isTheSelectedSortMethod(mDriver.hotelsSearchScreen().sortMenu().getSortByPopularityString()));
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
		assertTrue(isTheSelectedSortMethod(mDriver.hotelsSearchScreen().sortMenu().getSortByPopularityString()));
	}

	// Assert that the RadioButton checked corresponds to
	// the passed menu title
	// We have to do it in this hack-y way because
	// the MenuItems do not provide a way to access
	// the radio button.
	private boolean isTheSelectedSortMethod(String sortTitle) {
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

	@Override
	protected void tearDown() throws Exception {
		mDriver.enterLog(TAG, "tearing down...");
		mDriver.finishOpenedActivities();
	}
}
