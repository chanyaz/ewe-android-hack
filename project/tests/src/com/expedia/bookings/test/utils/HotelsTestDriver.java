package com.expedia.bookings.test.utils;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;

import com.expedia.bookings.test.tests.pageModels.hotels.HotelsCheckoutScreen;
import com.expedia.bookings.test.tests.pageModels.hotels.HotelsConfirmationScreen;
import com.expedia.bookings.test.tests.pageModels.hotels.HotelsDetailsScreen;
import com.expedia.bookings.test.tests.pageModels.hotels.HotelsReviewsScreen;
import com.expedia.bookings.test.tests.pageModels.hotels.HotelsRoomsRatesScreen;
import com.expedia.bookings.test.tests.pageModels.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.tests.pageModels.hotels.HotelsTermsAndConditionsScreen;
import com.expedia.bookings.test.tests.pageModels.hotels.HotelsTravelerInfoScreen;

public class HotelsTestDriver extends TestDriver {

	private HotelsSearchScreen mHotelsSearchScreen;
	private HotelsDetailsScreen mHotelsDetailsScreen;
	private HotelsReviewsScreen mHotelsReviewsScreen;
	private HotelsRoomsRatesScreen mHotelsRoomsRatesScreen;
	private HotelsCheckoutScreen mHotelsCheckoutScreen;
	private HotelsConfirmationScreen mHotelsConfirmationScreen;
	private HotelsTermsAndConditionsScreen mHotelsTOSScreen;
	private HotelsTravelerInfoScreen mHotelsTravelerInfoScreen;

	public HotelsTestDriver(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	public HotelsSearchScreen hotelsSearchScreen() {
		if (mHotelsSearchScreen == null) {
			mHotelsSearchScreen = new HotelsSearchScreen(mInstrumentation, getCurrentActivity(), mRes, mPreferences);
		}
		return mHotelsSearchScreen;
	}

	public HotelsDetailsScreen hotelsDetailsScreen() {
		if (mHotelsDetailsScreen == null) {
			mHotelsDetailsScreen = new HotelsDetailsScreen(mInstrumentation, getCurrentActivity(), mRes, mPreferences);
		}
		return mHotelsDetailsScreen;
	}

	public HotelsReviewsScreen hotelsReviewsScreen() {
		if (mHotelsReviewsScreen == null) {
			mHotelsReviewsScreen = new HotelsReviewsScreen(mInstrumentation, getCurrentActivity(), mRes, mPreferences);
		}
		return mHotelsReviewsScreen;
	}

	public HotelsRoomsRatesScreen hotelsRoomsRatesScreen() {
		if (mHotelsRoomsRatesScreen == null) {
			mHotelsRoomsRatesScreen = new HotelsRoomsRatesScreen(mInstrumentation, getCurrentActivity(), mRes,
					mPreferences);
		}
		return mHotelsRoomsRatesScreen;
	}

	public HotelsCheckoutScreen hotelsCheckoutScreen() {
		if (mHotelsCheckoutScreen == null) {
			mHotelsCheckoutScreen = new HotelsCheckoutScreen(mInstrumentation, getCurrentActivity(), mRes, mPreferences);
		}
		return mHotelsCheckoutScreen;
	}

	public HotelsTermsAndConditionsScreen hotelsTermsAndConditionsScreen() {
		if (mHotelsTOSScreen == null) {
			mHotelsTOSScreen = new HotelsTermsAndConditionsScreen(mInstrumentation, getCurrentActivity(), mRes,
					mPreferences);
		}

		return mHotelsTOSScreen;
	}

	public HotelsConfirmationScreen hotelsConfirmationScreen() {
		if (mHotelsConfirmationScreen == null) {
			mHotelsConfirmationScreen = new HotelsConfirmationScreen(mInstrumentation, getCurrentActivity(), mRes,
					mPreferences);
		}
		return mHotelsConfirmationScreen;
	}

	public HotelsTravelerInfoScreen travelerInformationScreen() {
		if (mHotelsTravelerInfoScreen == null) {
			mHotelsTravelerInfoScreen = new HotelsTravelerInfoScreen(mInstrumentation, getCurrentActivity(),
					mRes, mPreferences);
		}
		return mHotelsTravelerInfoScreen;
	}

}
