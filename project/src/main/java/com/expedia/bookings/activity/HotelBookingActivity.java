package com.expedia.bookings.activity;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.HotelBookingResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Response;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.fragment.BookingInProgressDialogFragment;
import com.expedia.bookings.fragment.CVVEntryFragment;
import com.expedia.bookings.fragment.CVVEntryFragment.CVVEntryFragmentListener;
import com.expedia.bookings.fragment.HotelBookingFragment;
import com.expedia.bookings.fragment.SimpleCallbackDialogFragment;
import com.expedia.bookings.fragment.UnhandledErrorDialogFragment;
import com.expedia.bookings.fragment.WalletFragment;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.otto.Events.CreateTripDownloadRetry;
import com.expedia.bookings.otto.Events.CreateTripDownloadRetryCancel;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.SocialUtils;
import com.squareup.otto.Subscribe;

public class HotelBookingActivity extends FragmentActivity implements CVVEntryFragmentListener {

	private static final String STATE_CVV_ERROR_MODE = "STATE_CVV_ERROR_MODE";

	private Context mContext;

	private CVVEntryFragment mCVVEntryFragment;
	private BookingInProgressDialogFragment mProgressFragment;
	private HotelBookingFragment mBookingFragment;
	private ViewGroup mActionBarTextView;

	private boolean mCvvErrorModeEnabled;

	// To make up for a lack of FLAG_ACTIVITY_CLEAR_TASK in older Android versions
	private ActivityKillReceiver mKillReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!ExpediaBookingApp.useTabletInterface(this)) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		if (shouldBail()) {
			return;
		}

		mKillReceiver = new ActivityKillReceiver(this);
		mKillReceiver.onCreate();

		mContext = this;

		if (savedInstanceState != null) {
			mCvvErrorModeEnabled = savedInstanceState.getBoolean(STATE_CVV_ERROR_MODE);
		}

		setContentView(R.layout.activity_hotel_booking);
		mActionBarTextView = Ui.inflate(this, R.layout.actionbar_cvv, null);

		setupActionBar(false);

		mCVVEntryFragment = Ui.findSupportFragment(this, CVVEntryFragment.TAG);
		mProgressFragment = Ui.findSupportFragment(this, BookingInProgressDialogFragment.TAG);
		mBookingFragment = Ui.findSupportFragment(this, HotelBookingFragment.TAG);

		if (mBookingFragment == null) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

			mBookingFragment = new HotelBookingFragment();
			ft.add(mBookingFragment, HotelBookingFragment.TAG);

			if (mBookingFragment.willBookViaGoogleWallet()) {
				// Start showing progress dialog; depend on the wallet client connecting for
				// kicking off the reservation
				showProgressDialog();
				mBookingFragment.doBooking();
			}
			else {
				mCVVEntryFragment = CVVEntryFragment.newInstance(Db.getBillingInfo());
				ft.add(R.id.cvv_frame, mCVVEntryFragment, CVVEntryFragment.TAG);
			}

			ft.commit();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		if (shouldBail()) {
			return;
		}

		OmnitureTracking.trackPageLoadHotelsCheckoutPaymentCid();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Events.register(this);

		if (shouldBail()) {
			return;
		}

		setCvvErrorMode(mCvvErrorModeEnabled);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (shouldBail()) {
			return;
		}

		outState.putBoolean(STATE_CVV_ERROR_MODE, mCvvErrorModeEnabled);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Events.unregister(this);

		if (shouldBail()) {
			return;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (shouldBail()) {
			return;
		}

		if (mKillReceiver != null) {
			mKillReceiver.onDestroy();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (WalletFragment.isRequestCodeFromWalletFragment(requestCode)) {
			mBookingFragment.onActivityResult(requestCode, resultCode, data);
		}
		else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public void onBackPressed() {
		// F1053: Do not let user go back when we are mid-download
		if (!mBookingFragment.isBooking()) {
			super.onBackPressed();
		}
	}

	private void launchConfirmationActivity() {
		startActivity(new Intent(this, HotelConfirmationActivity.class));

		// Destroy the activity backstack
		NavUtils.sendKillActivityBroadcast(this);
	}

	private void launchHotelPaymentCreditCardFragment() {
		Intent intent;
		if (Db.getBillingInfo() != null && Db.getBillingInfo().hasStoredCard()) {
			intent = HotelPaymentOptionsActivity.gotoOptionsIntent(mContext);
		}
		else {
			intent = HotelPaymentOptionsActivity.gotoCreditCardEntryIntent(mContext);
		}

		Db.getWorkingBillingInfoManager().setWorkingBillingInfoAndBase(Db.getBillingInfo());

		startActivity(intent);
	}

	private void launchHotelTravelerPhoneNumberFragment() {
		Intent intent = new Intent(mContext, HotelTravelerInfoOptionsActivity.class);
		intent.putExtra(HotelTravelerInfoOptionsActivity.STATE_TAG_MODE,
				HotelPaymentOptionsActivity.YoYoMode.EDIT.name());
		intent.putExtra(HotelTravelerInfoOptionsActivity.STATE_TAG_DEST,
				HotelTravelerInfoOptionsActivity.YoYoPosition.ONE.name());

		Traveler traveler = Db.getTravelers().get(0);
		if (traveler.getPrimaryPhoneNumber() != null) {
			traveler.getPrimaryPhoneNumber().setNumber(null);
		}
		Db.getWorkingTravelerManager().setWorkingTravelerAndBase(Db.getTravelers().get(0));

		startActivity(intent);
	}

	private boolean shouldBail() {
		// #1106: Don't continue to load any part of the
		// activity as we're just about to recreate it
		return !ExpediaBookingApp.useTabletInterface(this) && !getResources().getBoolean(R.bool.portrait);
	}

	//////////////////////////////////////////////////////////////////////////
	// Action bar

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void setupActionBar(boolean isError) {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);

		Drawable actionBarDrawable = isError
			? getResources().getDrawable(R.drawable.bg_flight_action_bar_top_red)
			: Ui.obtainThemeDrawable(this, R.attr.skin_actionBarBackgroundDrawable);

		actionBar.setBackgroundDrawable(actionBarDrawable);

		int titleResId = isError
			? R.string.title_invalid_security_code
			: R.string.title_complete_booking;

		((TextView) mActionBarTextView.findViewById(R.id.title)).setText(titleResId);

		actionBar.setCustomView(mActionBarTextView);
	}

	//////////////////////////////////////////////////////////////////////////
	// Invalid CVV modes

	public void setCvvErrorMode(boolean enabled) {
		// If we're booking with Google Wallet, ignore this entirely
		if (mBookingFragment.willBookViaGoogleWallet()) {
			return;
		}

		mCvvErrorModeEnabled = enabled;

		// Set the new title
		setupActionBar(enabled);

		// Pass this along to the fragment
		mCVVEntryFragment.setCvvErrorMode(enabled);
	}

	//////////////////////////////////////////////////////////////////////////
	// Booking downloads

	private void showProgressDialog() {
		if (mProgressFragment == null) {
			mProgressFragment = new BookingInProgressDialogFragment();
			mProgressFragment.show(getSupportFragmentManager(), BookingInProgressDialogFragment.TAG);
		}
	}

	private void dismissProgressDialog() {
		if (mProgressFragment != null) {
			mProgressFragment.dismiss();
			mProgressFragment = null;
		}
	}

	private void doBooking() {
		setCvvErrorMode(false);

		mBookingFragment.doBooking();
	}

	//////////////////////////////////////////////////////////////////////////
	// CVVEntryFragmentListener

	@Override
	public void onBook(String cvv) {
		Db.getBillingInfo().setSecurityCode(cvv);

		doBooking();
	}

	///////////////////////////////////
	/// Bookings events

	@Subscribe
	public void onBookingResponseErrorCVV(Events.BookingResponseErrorCVV event) {
		setCvvErrorMode(event.setCVVMode);
	}

	@Subscribe
	public void onBookingResponseErrorTripBooked(Events.BookingResponseErrorTripBooked event) {
		launchConfirmationActivity();
	}

	//////////////////////////////////////////////////////////////////////////
	// Price Change events

	@Subscribe
	public void onAcceptPriceChange(Events.PriceChangeDialogAccept event) {
		doBooking();
	}

	@Subscribe
	public void onCancelPriceChange(Events.PriceChangeDialogCancel event) {
		finish();
	}

	//////////////////////////////////////////////////////////////////////////
	// SimpleCallbackDialogFragment

	@Subscribe
	public void onSimpleDialogClick(Events.SimpleCallBackDialogOnClick event) {
		int callbackId = event.callBackId;
		switch (callbackId) {
		case SimpleCallbackDialogFragment.CODE_INVALID_CC:
		case SimpleCallbackDialogFragment.CODE_INVALID_POSTALCODE:
		case SimpleCallbackDialogFragment.CODE_INVALID_PAYMENT:
			// #1269: Don't do the invalid CC page jump if we're booking using Google Wallet
			if (!mBookingFragment.willBookViaGoogleWallet()) {
				launchHotelPaymentCreditCardFragment();
			}

			finish();
			break;
		case SimpleCallbackDialogFragment.CODE_NAME_ONCARD_MISMATCH:
			launchHotelPaymentCreditCardFragment();
			finish();
			break;
		case SimpleCallbackDialogFragment.CODE_INVALID_PHONENUMBER:
			launchHotelTravelerPhoneNumberFragment();
			finish();
			break;
		default:
			finish();
			break;
		}
	}

	@Subscribe
	public void onSimpleDialogCancel(Events.SimpleCallBackDialogOnCancel event) {
		// If we're booking via wallet, back out; otherwise sit on CVV screen
		if (mBookingFragment.willBookViaGoogleWallet()) {
			finish();
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// UnhandledErrorDialogFragment

	@Subscribe
	public void onRetryUnhandledException(Events.UnhandledErrorDialogRetry event) {
		doBooking();
	}

	@Subscribe
	public void onCallCustomerSupport(Events.UnhandledErrorDialogCallCustomerSupport event) {
		SocialUtils.call(this, PointOfSale.getPointOfSale().getSupportPhoneNumberBestForUser(Db.getUser()));
	}

	@Subscribe
	public void onCancelUnhandledException(Events.UnhandledErrorDialogCancel event) {
		// If we're booking via wallet, back out; otherwise sit on CVV screen
		if (mBookingFragment.willBookViaGoogleWallet()) {
			finish();
		}
	}

	///////////////////////////////////
	/// Booking download related

	@Subscribe
	public void onStartBooking(Events.BookingDownloadStarted event) {
		showProgressDialog();
	}

	@Subscribe
	public void onBookingResponse(Events.BookingDownloadResponse event) {
		Response results = event.response;
		HotelBookingResponse response = (HotelBookingResponse) results;
		Property property = Db.getTripBucket().getHotel().getProperty();

		dismissProgressDialog();

		Db.getTripBucket().getHotel().setBookingResponse(response);
		// TODO save trip bucket for better state persistence?
		setCvvErrorMode(false);

		if (results == null) {
			DialogFragment df = UnhandledErrorDialogFragment.newInstance(null);
			df.show(getSupportFragmentManager(), "noResultsErrorDialog");

			OmnitureTracking.trackErrorPage("ReservationRequestFailed");
		}
		else if (!results.isSuccess() && !response.succeededWithErrors()) {
			response.setProperty(property);
			mBookingFragment.handleBookingErrorResponse(response);
		}
		else {
			response.setProperty(property);
			AdTracker.trackHotelBooked();
			launchConfirmationActivity();
		}
	}

	@Subscribe
	public void onCreateTripDownloadRetry(CreateTripDownloadRetry event) {
		mBookingFragment.doBooking();
	}

	@Subscribe
	public void onCreateTripDownloadRetryCancel(CreateTripDownloadRetryCancel event) {
		dismissProgressDialog();
	}
}
