package com.expedia.bookings.test.utils;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;

import com.expedia.bookings.test.tests.pageModels.common.BillingAddressScreen;
import com.expedia.bookings.test.tests.pageModels.common.CVVEntryScreen;
import com.expedia.bookings.test.tests.pageModels.common.CardInfoScreen;
import com.expedia.bookings.test.tests.pageModels.common.CommonCheckoutScreen;
import com.expedia.bookings.test.tests.pageModels.common.CommonPaymentMethodScreen;
import com.expedia.bookings.test.tests.pageModels.common.CommonSelectTravelerScreen;
import com.expedia.bookings.test.tests.pageModels.common.CommonTravelerInformationScreen;
import com.expedia.bookings.test.tests.pageModels.common.FindItineraryScreen;
import com.expedia.bookings.test.tests.pageModels.common.LaunchScreen;
import com.expedia.bookings.test.tests.pageModels.common.LogInScreen;
import com.expedia.bookings.test.tests.pageModels.common.ScreenActions;
import com.expedia.bookings.test.tests.pageModels.common.SettingsScreen;
import com.expedia.bookings.test.tests.pageModels.common.SweepstakesScreen;
import com.expedia.bookings.test.tests.pageModels.common.TripsScreen;

public class TestDriver extends ScreenActions {

	private SweepstakesScreen mSweepstakesScreen;
	private LaunchScreen mLaunchScreen;
	private TripsScreen mTripsScreen;
	private FindItineraryScreen mFindItinerary;
	private LogInScreen mLogInScreen;
	private SettingsScreen mSettingsScreen;
	private CommonCheckoutScreen mCommonCheckout;
	private CommonPaymentMethodScreen mCommonPaymentMethod;
	private CommonSelectTravelerScreen mSelectTravelerScreen;
	private CommonTravelerInformationScreen mTravelerInformationScreen;
	private BillingAddressScreen mBillingAddressScreen;
	private CardInfoScreen mCardInfoScreen;
	private CVVEntryScreen mCVVEntryScreen;

	public TestDriver(Instrumentation instrumentation, Activity activity, Resources res, TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	public SweepstakesScreen sweepstakesScreen() {
		if (mSweepstakesScreen == null) {
			mSweepstakesScreen = new SweepstakesScreen(mInstrumentation, getCurrentActivity(), mRes, mPreferences);
		}
		return mSweepstakesScreen;
	}

	public LaunchScreen launchScreen() {
		if (mLaunchScreen == null) {
			mLaunchScreen = new LaunchScreen(mInstrumentation, getCurrentActivity(), mRes, mPreferences);
		}
		return mLaunchScreen;
	}

	public TripsScreen tripsScreen() {
		if (mTripsScreen == null) {
			mTripsScreen = new TripsScreen(mInstrumentation, getCurrentActivity(), mRes, mPreferences);
		}
		return mTripsScreen;
	}

	public FindItineraryScreen findItinerary() {
		if (mFindItinerary == null) {
			mFindItinerary = new FindItineraryScreen(mInstrumentation, getCurrentActivity(), mRes, mPreferences);
		}
		return mFindItinerary;
	}

	public LogInScreen logInScreen() {
		if (mLogInScreen == null) {
			mLogInScreen = new LogInScreen(mInstrumentation, getCurrentActivity(), mRes, mPreferences);
		}
		return mLogInScreen;
	}

	public SettingsScreen settingsScreen() {
		if (mSettingsScreen == null) {
			mSettingsScreen = new SettingsScreen(mInstrumentation, getCurrentActivity(), mRes, mPreferences);
		}
		return mSettingsScreen;
	}

	public CommonCheckoutScreen commonCheckout() {
		if (mCommonCheckout == null) {
			mCommonCheckout = new CommonCheckoutScreen(mInstrumentation, getCurrentActivity(), mRes, mPreferences);
		}
		return mCommonCheckout;
	}

	public CommonSelectTravelerScreen selectTravelerScreen() {
		if (mSelectTravelerScreen == null) {
			mSelectTravelerScreen = new CommonSelectTravelerScreen(mInstrumentation, getCurrentActivity(),
					mRes, mPreferences);
		}
		return mSelectTravelerScreen;
	}

	public CommonTravelerInformationScreen travelerInformationScreen() {
		if (mTravelerInformationScreen == null) {
			mTravelerInformationScreen = new CommonTravelerInformationScreen(mInstrumentation, getCurrentActivity(),
					mRes, mPreferences);
		}
		return mTravelerInformationScreen;
	}

	public CommonPaymentMethodScreen commonPaymentMethodScreen() {
		if (mCommonPaymentMethod == null) {
			mCommonPaymentMethod = new CommonPaymentMethodScreen(mInstrumentation, getCurrentActivity(), mRes,
					mPreferences);
		}
		return mCommonPaymentMethod;
	}

	public BillingAddressScreen billingAddressScreen() {
		if (mBillingAddressScreen == null) {
			mBillingAddressScreen = new BillingAddressScreen(mInstrumentation, getCurrentActivity(), mRes, mPreferences);
		}
		return mBillingAddressScreen;
	}

	public CardInfoScreen cardInfoScreen() {
		if (mCardInfoScreen == null) {
			mCardInfoScreen = new CardInfoScreen(mInstrumentation, getCurrentActivity(), mRes, mPreferences);
		}
		return mCardInfoScreen;
	}

	public CVVEntryScreen cvvEntryScreen() {
		if (mCVVEntryScreen == null) {
			mCVVEntryScreen = new CVVEntryScreen(mInstrumentation, getCurrentActivity(), mRes, mPreferences);
		}
		return mCVVEntryScreen;
	}
}
