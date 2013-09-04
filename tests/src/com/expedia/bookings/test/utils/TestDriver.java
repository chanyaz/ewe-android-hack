package com.expedia.bookings.test.utils;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;

import com.expedia.bookings.test.tests.pageModels.common.BillingAddressScreen;
import com.expedia.bookings.test.tests.pageModels.common.CVVEntryScreen;
import com.expedia.bookings.test.tests.pageModels.common.CardInfoScreen;
import com.expedia.bookings.test.tests.pageModels.common.CommonCheckoutScreen;
import com.expedia.bookings.test.tests.pageModels.common.CommonPaymentMethodScreen;
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
	private BillingAddressScreen mBillingAddressScreen;
	private CardInfoScreen mCardInfoScreen;
	private CVVEntryScreen mCVVEntryScreen;

	public TestDriver(Instrumentation instrumentation, Activity activity, Resources res) {
		super(instrumentation, activity, res);
		mSweepstakesScreen = null;
		mLaunchScreen = null;
		mTripsScreen = null;
		mFindItinerary = null;
		mLogInScreen = null;
		mSettingsScreen = null;
		mCommonCheckout = null;
		mCommonPaymentMethod = null;
		mBillingAddressScreen = null;
		mCardInfoScreen = null;
		mCVVEntryScreen = null;
	}

	public SweepstakesScreen sweepstakesScreen() {
		if (mSweepstakesScreen == null) {
			mSweepstakesScreen = new SweepstakesScreen(mInstrumentation, getCurrentActivity(), mRes);
		}
		return mSweepstakesScreen;
	}

	public LaunchScreen launchScreen() {
		if (mLaunchScreen == null) {
			mLaunchScreen = new LaunchScreen(mInstrumentation, getCurrentActivity(), mRes);
		}
		return mLaunchScreen;
	}

	public TripsScreen tripsScreen() {
		if (mTripsScreen == null) {
			mTripsScreen = new TripsScreen(mInstrumentation, getCurrentActivity(), mRes);
		}
		return mTripsScreen;
	}

	public FindItineraryScreen findItinerary() {
		if (mFindItinerary == null) {
			mFindItinerary = new FindItineraryScreen(mInstrumentation, getCurrentActivity(), mRes);
		}
		return mFindItinerary;
	}

	public LogInScreen logInScreen() {
		if (mLogInScreen == null) {
			mLogInScreen = new LogInScreen(mInstrumentation, getCurrentActivity(), mRes);
		}
		return mLogInScreen;
	}

	public SettingsScreen settingsScreen() {
		if (mSettingsScreen == null) {
			mSettingsScreen = new SettingsScreen(mInstrumentation, getCurrentActivity(), mRes);
		}
		return mSettingsScreen;
	}

	public CommonCheckoutScreen commonCheckout() {
		if (mCommonCheckout == null) {
			mCommonCheckout = new CommonCheckoutScreen(mInstrumentation, getCurrentActivity(), mRes);
		}
		return mCommonCheckout;
	}

	public CommonPaymentMethodScreen commonPaymentMethodScreen() {
		if (mCommonPaymentMethod == null) {
			mCommonPaymentMethod = new CommonPaymentMethodScreen(mInstrumentation, getCurrentActivity(), mRes);
		}
		return mCommonPaymentMethod;
	}

	public BillingAddressScreen billingAddressScreen() {
		if (mBillingAddressScreen == null) {
			mBillingAddressScreen = new BillingAddressScreen(mInstrumentation, getCurrentActivity(), mRes);
		}
		return mBillingAddressScreen;
	}

	public CardInfoScreen cardInfoScreen() {
		if (mCardInfoScreen == null) {
			mCardInfoScreen = new CardInfoScreen(mInstrumentation, getCurrentActivity(), mRes);
		}
		return mCardInfoScreen;
	}

	public CVVEntryScreen cvvEntryScreen() {
		if (mCVVEntryScreen == null) {
			mCVVEntryScreen = new CVVEntryScreen(mInstrumentation, getCurrentActivity(), mRes);
		}
		return mCVVEntryScreen;
	}

}
