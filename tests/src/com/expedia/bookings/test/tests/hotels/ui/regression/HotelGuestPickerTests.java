package com.expedia.bookings.test.tests.hotels.ui.regression;

import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.utils.HotelsTestDriver;
import com.expedia.bookings.test.utils.HotelsUserData;
import com.expedia.bookings.test.utils.TestPreferences;

import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;
import android.util.DisplayMetrics;
import android.widget.TextView;

public class HotelGuestPickerTests extends ActivityInstrumentationTestCase2<SearchActivity> {

	private static final String TAG = "Hotel Search Regression";

	private Resources mRes;
	DisplayMetrics mMetric;
	private HotelsTestDriver mDriver;
	private HotelsUserData mUser;
	private TestPreferences mPreferences;

	public HotelGuestPickerTests() {
		super(SearchActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		mRes = getActivity().getBaseContext().getResources();
		mPreferences = new TestPreferences();
		mPreferences.setRotationPermission(false);
		mPreferences.setScreenshotPermission(false);
		mDriver = new HotelsTestDriver(getInstrumentation(), getActivity(), mRes, mPreferences);
		mUser = new HotelsUserData();
		mUser.setHotelCityToRandomUSCity();
		try {
			mDriver.sweepstakesScreen().clickNoThanksButton();
		}
		catch (Throwable e) {
			mDriver.enterLog(TAG, "No sweepstakes activity to interact with!");
		}
	}

	// verify that the guest number picker's text views
	// show the expected text when children and adults
	// are incremented and decremented
	public void testPickerTextViews() {
		mDriver.launchScreen().launchHotels();
		mDriver.delay(1);
		mDriver.hotelsSearchScreen().clickOnGuestsButton();

		TextView adultPickerLowerTextView = mDriver.hotelsSearchScreen().guestPicker()
				.pickerLowerTextView(mDriver.hotelsSearchScreen().guestPicker().adultPicker());
		TextView childPickerLowerTextView = mDriver.hotelsSearchScreen().guestPicker()
				.pickerLowerTextView(mDriver.hotelsSearchScreen().guestPicker().childrenPicker());
		TextView adultPickerHigherTextView = mDriver.hotelsSearchScreen().guestPicker()
				.pickerHigherTextView(mDriver.hotelsSearchScreen().guestPicker().adultPicker());
		TextView childPickerHigherTextView = mDriver.hotelsSearchScreen().guestPicker()
				.pickerHigherTextView(mDriver.hotelsSearchScreen().guestPicker().childrenPicker());
		TextView adultPickerCurrentTextView = mDriver.hotelsSearchScreen().guestPicker()
				.pickerCurrentTextView(mDriver.hotelsSearchScreen().guestPicker().adultPicker());
		TextView childPickerCurrentTextView = mDriver.hotelsSearchScreen().guestPicker()
				.pickerCurrentTextView(mDriver.hotelsSearchScreen().guestPicker().childrenPicker());

		assertEquals((String) adultPickerLowerTextView.getText(), "");
		assertEquals((String) childPickerLowerTextView.getText(), "");

		int adultCount = 1;
		int childCount = 0;
		final int adultMax = 6;
		final int childMax = 4;

		String adultCurrentTextViewValue;
		String adultHigherTextViewValue;
		String adultLowerTextViewValue;

		for (int i = 1; i < adultMax; i++) {
			adultCurrentTextViewValue = (String) adultPickerCurrentTextView.getText();
			assertEquals(mDriver.hotelsSearchScreen().guestPicker()
					.adultPickerStringPlural(adultCount), adultCurrentTextViewValue);

			adultHigherTextViewValue = (String) adultPickerHigherTextView.getText();
			assertEquals(mDriver.hotelsSearchScreen().guestPicker()
					.adultPickerStringPlural(adultCount + 1), adultHigherTextViewValue);

			mDriver.hotelsSearchScreen().guestPicker().clickIncrementAdultsButton();
			adultCount++;

			adultLowerTextViewValue = (String) adultPickerLowerTextView.getText();
			assertEquals(mDriver.hotelsSearchScreen().guestPicker()
					.adultPickerStringPlural(adultCount - 1), adultLowerTextViewValue);
		}

		for (int i = 6; i > 0; i--) {
			mDriver.hotelsSearchScreen().guestPicker().clickDecrementAdultsButton();
		}

		String childCurrentTextViewValue;
		String childHigherTextViewValue;
		String childLowerTextViewValue;

		for (int i = 0; i < childMax; i++) {
			childHigherTextViewValue = (String) childPickerHigherTextView.getText();
			assertEquals(mDriver.hotelsSearchScreen().guestPicker()
					.childPickerStringPlural(childCount + 1), childHigherTextViewValue);

			childCurrentTextViewValue = (String) childPickerCurrentTextView.getText();
			assertEquals(mDriver.hotelsSearchScreen().guestPicker()
					.childPickerStringPlural(childCount), childCurrentTextViewValue);

			mDriver.hotelsSearchScreen().guestPicker().clickIncrementChildrenButton();
			childCount++;

			childLowerTextViewValue = (String) childPickerLowerTextView.getText();
			assertEquals(mDriver.hotelsSearchScreen().guestPicker()
					.childPickerStringPlural(childCount - 1), childLowerTextViewValue);
		}
	}

	public void testChildSelectorAppearing() {
		final int childMax = 4;
		mDriver.launchScreen().launchHotels();
		mDriver.delay(1);
		mDriver.hotelsSearchScreen().clickOnGuestsButton();
		assertFalse(mDriver.searchText(mDriver.hotelsSearchScreen().guestPicker().selectChildAgePlural(1)));

		for (int i = 1; i <= childMax; i++) {
			mDriver.hotelsSearchScreen().guestPicker().clickIncrementChildrenButton();
			assertTrue(mDriver.searchText(mDriver.hotelsSearchScreen().guestPicker().selectChildAgePlural(i)));
		}
	}

	@Override
	protected void tearDown() throws Exception {
		mDriver.enterLog(TAG, "tearing down...");
		mDriver.finishOpenedActivities();
	}
}
