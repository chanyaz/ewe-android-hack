package com.expedia.bookings.activity;

import java.math.BigDecimal;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.FlightPaymentOptionsActivity.YoYoPosition;
import com.expedia.bookings.bitmaps.BitmapDrawable;
import com.expedia.bookings.bitmaps.L2ImageCache;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightCheckoutResponse;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.TripBucket;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.fragment.BookingInProgressDialogFragment;
import com.expedia.bookings.fragment.CVVEntryFragment;
import com.expedia.bookings.fragment.CVVEntryFragment.CVVEntryFragmentListener;
import com.expedia.bookings.fragment.FlightBookingFragment;
import com.expedia.bookings.fragment.SimpleCallbackDialogFragment;
import com.expedia.bookings.fragment.WalletFragment;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.Akeakamai;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.SettingUtils;
import com.squareup.otto.Subscribe;

public class FlightBookingActivity extends FragmentActivity implements CVVEntryFragmentListener {

	private static final String STATE_CVV_ERROR_MODE = "STATE_CVV_ERROR_MODE";

	private Context mContext;

	private CVVEntryFragment mCVVEntryFragment;
	private BookingInProgressDialogFragment mProgressFragment;
	private FlightBookingFragment mBookingFragment;
	private ViewGroup mActionBarTextView;
	private ImageView mBgImageView;

	private boolean mCvvErrorModeEnabled;

	// To make up for a lack of FLAG_ACTIVITY_CLEAR_TASK in older Android versions
	private ActivityKillReceiver mKillReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// When resuming this activity after a background kill, just finish() back to FlightTripOverviewActivity. We
		// don't have enough information to proceed and crash will happen. FTOA has nice handling of reloading state
		// from disk or sending user back to FlightSearch at the very least.
		if (Db.getFlightSearch().getSearchResponse() == null) {
			finish();
		}

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

		setContentView(R.layout.activity_flight_booking);

		mBgImageView = Ui.findView(this, R.id.background_bg_view);
		Point portrait = Ui.getPortraitScreenSize(this);
		final String code = Db.getTripBucket().getFlight().getFlightSearchParams().getArrivalLocation().getDestinationId();
		final String url = new Akeakamai(Images.getFlightDestination(code)) //
			.resizeExactly(portrait.x, portrait.y) //
			.build();

		Bitmap bitmap = L2ImageCache.sDestination.getImage(url, true /*blurred*/, true /*checkDisk*/);
		if (bitmap != null) {
			onBitmapLoaded(bitmap);
		}
		else {
			onBitmapLoadFailed();
		}

		mActionBarTextView = Ui.inflate(this, R.layout.actionbar_cvv, null);

		setupActionBar(false);

		mCVVEntryFragment = Ui.findSupportFragment(this, CVVEntryFragment.TAG);
		mProgressFragment = Ui.findSupportFragment(this, BookingInProgressDialogFragment.TAG);
		mBookingFragment = Ui.findSupportFragment(this, FlightBookingFragment.TAG);

		if (savedInstanceState == null || mBookingFragment == null) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

			mBookingFragment = new FlightBookingFragment();
			ft.add(mBookingFragment, FlightBookingFragment.TAG);

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

			// If the debug setting is made to fake a price change, then fake the current price (by changing it)
			if (!AndroidUtils.isRelease(mContext)) {
				FlightTrip trip = Db.getTripBucket().getFlight().getFlightTrip();

				BigDecimal fakePriceChange = getFakePriceChangeAmount();
				BigDecimal fakeObFees = getFakeObFeesAmount();

				// We change the total price we're sending to the server, so that it
				// thinks there has been a price change.  Note that you can't have the
				// fake price change == -fake ob fees, or else we'll end up with the
				// correct price and it'll just book.  :P
				trip.getTotalFare().add(fakePriceChange);
				trip.getTotalFare().subtract(fakeObFees);
			}
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		if (shouldBail()) {
			return;
		}

		OmnitureTracking.trackPageLoadFlightCheckoutPaymentCid(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Events.register(this);
		if (shouldBail()) {
			return;
		}

		setCvvErrorMode(mCvvErrorModeEnabled);

		OmnitureTracking.onResume(this);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean(STATE_CVV_ERROR_MODE, mCvvErrorModeEnabled);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Events.unregister(this);
		if (shouldBail()) {
			return;
		}

		OmnitureTracking.onPause();
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

		startActivity(new Intent(this, FlightConfirmationActivity.class));

		// Destroy the activity backstack
		NavUtils.sendKillActivityBroadcast(this);
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

		// Set header bg
		int bgResId = isError
			? R.drawable.bg_flight_action_bar_top_red
			: Ui.obtainThemeResID(this, R.attr.actionBarBackgroundDrawable);

		actionBar.setBackgroundDrawable(getResources().getDrawable(bgResId));

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
	/// Booking events

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
		// #1269: Don't do the invalid CC page jump if we're booking using Google Wallet
		if (mBookingFragment.willBookViaGoogleWallet()) {
			finish();
			return;
		}

		switch (callbackId) {
		case SimpleCallbackDialogFragment.CODE_INVALID_CC:
			//Go to CC number entry page
			Intent gotoCCEntryIntent = new Intent(FlightBookingActivity.this, FlightPaymentOptionsActivity.class);
			if (Db.getBillingInfo() != null && Db.getBillingInfo().hasStoredCard()) {
				gotoCCEntryIntent.putExtra(FlightPaymentOptionsActivity.INTENT_TAG_DEST, YoYoPosition.OPTIONS.name());
			}
			else {
				Db.getWorkingBillingInfoManager().setWorkingBillingInfoAndBase(Db.getBillingInfo());
				gotoCCEntryIntent
						.putExtra(FlightPaymentOptionsActivity.INTENT_TAG_DEST, YoYoPosition.CREDITCARD.name());
			}
			startActivity(gotoCCEntryIntent);
			break;
		case SimpleCallbackDialogFragment.CODE_EXPIRED_CC:
			//Go to CC overview page
			Intent gotoCCOverviewIntent = new Intent(FlightBookingActivity.this, FlightPaymentOptionsActivity.class);
			Db.getWorkingBillingInfoManager().setWorkingBillingInfoAndBase(Db.getBillingInfo());
			gotoCCOverviewIntent.putExtra(FlightPaymentOptionsActivity.INTENT_TAG_DEST, YoYoPosition.OPTIONS.name());
			startActivity(gotoCCOverviewIntent);
			break;
		default:
			// For now, do the same thing - leave this activity
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
	// BirthDateInvalidDialog

	@Subscribe
	public void onInvalidBirthdateEditSearch(Events.BirthDateInvalidEditSearch event) {
		NavUtils.goToFlights(this, true, null, Intent.FLAG_ACTIVITY_CLEAR_TOP);
	}

	@Subscribe
	public void onInvalidBirthdateEditTraveler(Events.BirthDateInvalidEditTraveler event) {
		onBackPressed();
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

	@Subscribe
	public void onStartBooking(Events.BookingDownloadStarted event) {
		showProgressDialog();
	}

	@Subscribe
	public void onBookingResponse(Events.BookingDownloadResponse event) {
		FlightCheckoutResponse response = (FlightCheckoutResponse) event.response;

		dismissProgressDialog();

		// Modify the response to fake online booking fees
		if (!AndroidUtils.isRelease(mContext) && response != null && response.getNewOffer() != null) {
			BigDecimal fakeObFees = getFakeObFeesAmount();
			if (!fakeObFees.equals(BigDecimal.ZERO)) {
				Money amount = new Money(response.getNewOffer().getTotalFare());
				amount.setAmount(fakeObFees);
				response.getNewOffer().setOnlineBookingFeesAmount(amount);
			}
		}

		if (!AndroidUtils.isRelease(mContext) && response != null &&
			SettingUtils.get(this, R.string.preference_force_passenger_category_error, false)) {
			ServerError passengerCategoryError = new ServerError();
			passengerCategoryError.setCode("INVALID_INPUT");
			passengerCategoryError.addExtra("field", "mainPassenger.birthDate");
			response.addErrorToFront(passengerCategoryError);
		}

		Db.getTripBucket().getFlight().setCheckoutResponse(response);
		// TODO save TripBucket to disk for better persistence?

		if (response == null || response.hasErrors()) {
			mBookingFragment.handleBookingErrorResponse(response);
		}
		else {
			AdTracker.trackFlightBooked();
			launchConfirmationActivity();

			OmnitureTracking.trackPageLoadFlightCheckoutConfirmation(mContext);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Debug code

	private BigDecimal getFakePriceChangeAmount() {
		String amount = SettingUtils.get(mContext,
				getString(R.string.preference_fake_flight_price_change),
				getString(R.string.preference_fake_price_change_default));
		return new BigDecimal(amount);
	}

	private BigDecimal getFakeObFeesAmount() {
		String amount = SettingUtils.get(mContext,
				getString(R.string.preference_flight_fake_obfees),
				getString(R.string.preference_flight_fake_obfees_default));
		return new BigDecimal(amount);
	}

	public void onBitmapLoaded(Bitmap bitmap) {
		BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
		mBgImageView.setImageDrawable(drawable);
	}

	public void onBitmapLoadFailed() {
		Bitmap bitmap = L2ImageCache.sDestination.getImage(getResources(), R.drawable.default_flights_background, true /*blurred*/);
		onBitmapLoaded(bitmap);
	}
}
