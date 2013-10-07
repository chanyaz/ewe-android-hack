package com.expedia.bookings.test.tests.localization;

import java.util.Locale;

import ErrorsAndExceptions.OutOfPOSException;
import android.content.res.Configuration;
import android.os.Environment;
import android.util.Log;

import com.expedia.bookings.activity.LaunchActivity;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.data.trips.TripComponent;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.test.utils.CustomActivityInstrumentationTestCase;
import com.expedia.bookings.test.utils.ScreenshotMethodInterface;
import com.expedia.bookings.test.utils.ScreenshotSweepRunnerUtils;
import com.expedia.bookings.widget.ItinListView;

public class ItinScreenshotSweepFromList extends CustomActivityInstrumentationTestCase<LaunchActivity> {
	private static final String TAG = ItinScreenshotSweepFromList.class.getSimpleName();
	private static final String LOCALE_LIST_LOCATION =
			Environment.getExternalStorageDirectory().getPath() + "/locales_list.txt";

	public ItinScreenshotSweepFromList() {
		super(LaunchActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		mPreferences.setScreenshotPermission(true);
		mPreferences.setRotationPermission(false);
		mUser.setBookingServer("(Stubbed)");
	}

	private class Runner implements ScreenshotMethodInterface {
		@Override
		public void execute() throws Exception {
			mDriver.delay();
			mDriver.launchScreen().openMenuDropDown();
			if (mDriver.searchText(mDriver.launchScreen().settingsString())) {
				mDriver.launchScreen().pressSettings();
			}
			else {
				mDriver.clickInList(0);
			}
			mDriver.settingsScreen().clickSelectAPIString();
			mDriver.delay();
			mDriver.clickOnText(mUser.getBookingServer());
			mDriver.delay(1);
			mDriver.goBack();

			// Pull locales/points of sale from the internal list,
			// set the locale, get the stubbed itins, and iterate through them
			// in order to grab one of each type.
			for (int i = 0; i < 20; i++) {
				mDriver.setScreenshotCount(0);
				Locale testingLocale = mDriver.mLocaleUtils.selectNextLocaleFromInternalList(LOCALE_LIST_LOCATION);
				mDriver.enterLog(TAG, "Starting sweep of " + testingLocale.toString());
				mDriver.delay();

				mDriver.launchScreen().openMenuDropDown();
				if (mDriver.searchText(mDriver.launchScreen().settingsString())) {
					mDriver.launchScreen().pressSettings();
				}
				else {
					mDriver.clickInList(0);
				}
				mDriver.settingsScreen().clickCountryString();
				mDriver.settingsScreen().selectPOSFromLocale(testingLocale);
				mDriver.delay(1);
				mDriver.goBack();
				mDriver.launchScreen().swipeToTripsScreen();
				mDriver.tripsScreen().clickOnLogInButton();
				mDriver.logInScreen().typeTextEmailEditText(mUser.getLoginEmail());
				mDriver.logInScreen().typeTextPasswordEditText(mUser.getLoginPassword());
				mDriver.logInScreen().clickOnLoginButton();
				mDriver.delay();
				mDriver.waitForStringToBeGone(mDriver.tripsScreen().fetchingYourItineraries(), 300);

				ItinListView itinListView = mDriver.tripsScreen().itineraryListView();
				int cardCount = itinListView.getCount();
				for (int j = 0; j < TripComponent.Type.values().length; j++) {
					mDriver.scrollToTop();
					mDriver.delay();
					Type currentType = TripComponent.Type.values()[j];
					for (int k = 0; k < cardCount; k++) {
						ItinCardData data = itinListView.getItinCardData(k);
						if (data != null) {
							if (data.getTripComponentType().equals(currentType)) {
								itinListView.smoothScrollToPosition(k);
								mDriver.delay();
								mDriver.screenshot(currentType.name() + " card from list");
								if (!data.getTripComponentType().equals(Type.CRUISE)) {
									int visibleLowerBoundIndex = itinListView.getFirstVisiblePosition();
									mDriver.clickOnView(itinListView.getChildAt(k - visibleLowerBoundIndex));
									mDriver.screenshot(currentType.name() + " card expanded");
									mDriver.goBack();
									mDriver.delay(1);
									break;
								}
							}
						}
					}
				}
				mDriver.tripsScreen().openMenuDropDown();
				mDriver.delay();
				if (mDriver.searchText(mDriver.launchScreen().logOutString())) {
					mDriver.launchScreen().pressLogOut();
				}
				else {
					mDriver.clickInList(0);
				}
				mDriver.launchScreen().pressLogOut();
				mDriver.delay();
				mDriver.tripsScreen().swipeToLaunchScreen();
			}
		}
	}

	public void testMethod() throws Exception {
		Runner runner = new Runner();
		ScreenshotSweepRunnerUtils.run(runner, mRes);
	}

	@Override
	protected void tearDown() throws Exception {
		mDriver.enterLog(TAG, "tearing down...");
		mDriver.finishOpenedActivities();
	}
}
