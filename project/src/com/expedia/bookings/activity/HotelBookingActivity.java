package com.expedia.bookings.activity;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.BookingResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.fragment.BookingInProgressDialogFragment;
import com.expedia.bookings.fragment.CVVEntryFragment;
import com.expedia.bookings.fragment.CVVEntryFragment.CVVEntryFragmentListener;
import com.expedia.bookings.fragment.FlightUnavailableDialogFragment;
import com.expedia.bookings.fragment.PriceChangeDialogFragment.PriceChangeDialogFragmentListener;
import com.expedia.bookings.fragment.SimpleCallbackDialogFragment;
import com.expedia.bookings.fragment.SimpleCallbackDialogFragment.SimpleCallbackDialogFragmentListener;
import com.expedia.bookings.fragment.UnhandledErrorDialogFragment;
import com.expedia.bookings.fragment.UnhandledErrorDialogFragment.UnhandledErrorDialogFragmentListener;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.SocialUtils;

public class HotelBookingActivity extends SherlockFragmentActivity implements CVVEntryFragmentListener,
		PriceChangeDialogFragmentListener, SimpleCallbackDialogFragmentListener, UnhandledErrorDialogFragmentListener {

	public static final String BOOKING_DOWNLOAD_KEY = "com.expedia.bookings.hotel.checkout";

	private static final String STATE_CVV_ERROR_MODE = "STATE_CVV_ERROR_MODE";

	private static final int DIALOG_CALLBACK_INVALID_CC = 1;
	private static final int DIALOG_CALLBACK_INVALID_PAYMENT = 2;
	private static final int DIALOG_CALLBACK_INVALID_PHONENUMBER = 3;
	private static final int DIALOG_CALLBACK_INVALID_POSTALCODE = 4;

	private Context mContext;

	private CVVEntryFragment mCVVEntryFragment;
	private BookingInProgressDialogFragment mProgressFragment;

	private boolean mCvvErrorModeEnabled;

	// To make up for a lack of FLAG_ACTIVITY_CLEAR_TASK in older Android versions
	private ActivityKillReceiver mKillReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mKillReceiver = new ActivityKillReceiver(this);
		mKillReceiver.onCreate();

		mContext = this;

		if (savedInstanceState != null) {
			mCvvErrorModeEnabled = savedInstanceState.getBoolean(STATE_CVV_ERROR_MODE);
		}

		setContentView(R.layout.activity_hotel_booking);
		setTitle(R.string.title_complete_booking);

		mCVVEntryFragment = Ui.findSupportFragment(this, CVVEntryFragment.TAG);
		mProgressFragment = Ui.findSupportFragment(this, BookingInProgressDialogFragment.TAG);

		if (savedInstanceState == null) {
			mCVVEntryFragment = CVVEntryFragment.newInstance(this, Db.getBillingInfo());

			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.add(R.id.cvv_frame, mCVVEntryFragment, CVVEntryFragment.TAG);
			ft.commit();
		}

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	protected void onStart() {
		super.onStart();
		OmnitureTracking.trackPageLoadHotelsCheckoutPaymentCid(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		setCvvErrorMode(mCvvErrorModeEnabled);

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(BOOKING_DOWNLOAD_KEY)) {
			bd.registerDownloadCallback(BOOKING_DOWNLOAD_KEY, mCallback);
		}

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

		BackgroundDownloader.getInstance().unregisterDownloadCallback(BOOKING_DOWNLOAD_KEY);

		OmnitureTracking.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mKillReceiver != null) {
			mKillReceiver.onDestroy();
		}
	}

	@Override
	public void onBackPressed() {
		// F1053: Do not let user go back when we are mid-download
		if (!BackgroundDownloader.getInstance().isDownloading(BOOKING_DOWNLOAD_KEY)) {
			super.onBackPressed();
		}
	}

	private void launchConfirmationActivity() {
		startActivity(new Intent(this, ConfirmationFragmentActivity.class));

		// Destroy the activity backstack
		NavUtils.sendKillActivityBroadcast(this);
	}

	private void launchHotelPaymentCreditCardFragment() {
		Intent intent = new Intent(mContext, HotelPaymentOptionsActivity.class);
		intent.putExtra(HotelPaymentOptionsActivity.STATE_TAG_MODE,
				HotelPaymentOptionsActivity.YoYoMode.YOYO.name());
		intent.putExtra(HotelPaymentOptionsActivity.STATE_TAG_DEST,
				HotelPaymentOptionsActivity.YoYoPosition.CREDITCARD.name());

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
			traveler.getPrimaryPhoneNumber().setAreaCode(null);
		}
		Db.getWorkingTravelerManager().setWorkingTravelerAndBase(Db.getTravelers().get(0));
		Db.getWorkingTravelerManager().setAttemptToLoadFromDisk(false);

		startActivity(intent);
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

	//////////////////////////////////////////////////////////////////////////
	// Invalid CVV modes

	public void setCvvErrorMode(boolean enabled) {
		mCvvErrorModeEnabled = enabled;

		// Set header bg
		int bgResId = (enabled) ? R.drawable.bg_flight_action_bar_top_red : R.drawable.bg_action_bar;

		ActionBar ab = getSupportActionBar();
		ab.setBackgroundDrawable(getResources().getDrawable(bgResId));

		// Set the new title
		int titleResId = (enabled) ? R.string.title_invalid_security_code : R.string.title_complete_booking;
		ab.setTitle(titleResId);

		// Pass this along to the fragment
		mCVVEntryFragment.setCvvErrorMode(enabled);
	}

	//////////////////////////////////////////////////////////////////////////
	// Booking downloads

	private void doBooking() {
		setCvvErrorMode(false);

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (!bd.isDownloading(BOOKING_DOWNLOAD_KEY)) {
			// Clear current results (if any)
			Db.setFlightCheckout(null);

			mProgressFragment = new BookingInProgressDialogFragment();
			mProgressFragment.show(getSupportFragmentManager(), BookingInProgressDialogFragment.TAG);

			bd.startDownload(BOOKING_DOWNLOAD_KEY, mDownload, mCallback);
		}
	}

	private Download<BookingResponse> mDownload = new Download<BookingResponse>() {
		@Override
		public BookingResponse doDownload() {
			ExpediaServices services = new ExpediaServices(mContext);
			String userId = null;
			String tripId = null;
			Long tuid = null;

			if (Db.getCreateTripResponse() != null) {
				tripId = Db.getCreateTripResponse().getTripId();
				userId = Db.getCreateTripResponse().getUserId();
			}

			if (Db.getUser() != null) {
				tuid = Db.getUser().getPrimaryTraveler().getTuid();
			}

			return services.reservation(Db.getSearchParams(), Db.getSelectedProperty(), Db.getSelectedRate(),
					Db.getBillingInfo(), tripId, userId, tuid);
		}
	};

	private OnDownloadComplete<BookingResponse> mCallback = new OnDownloadComplete<BookingResponse>() {
		@Override
		public void onDownload(BookingResponse results) {
			mProgressFragment.dismiss();

			Db.setBookingResponse(results);
			setCvvErrorMode(false);

			if (results == null) {
				DialogFragment df = UnhandledErrorDialogFragment.newInstance(null);
				df.show(getSupportFragmentManager(), "noResultsErrorDialog");

				OmnitureTracking.trackErrorPage(mContext, "ReservationRequestFailed");
			}
			else if (!results.isSuccess() && !results.succeededWithErrors()) {
				handleErrorResponse(results);
			}
			else {
				AdTracker.trackHotelBooked();
				launchConfirmationActivity();
			}

			if (Db.getCreateTripResponse() != null) {
				Db.setCouponDiscountRate(Db.getCreateTripResponse().getNewRate());
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Error handling code
	//
	// Split out just for ease-of-reading

	public void handleErrorResponse(BookingResponse response) {
		List<ServerError> errors = response.getErrors();

		boolean hasCVVError = false;
		boolean hasExpirationDateError = false;
		boolean hasCreditCardNumberError = false;
		boolean hasPhoneError = false;
		boolean hasPostalCodeError = false;

		// Log all errors, in case we need to see them
		for (int a = 0; a < errors.size(); a++) {
			ServerError error = errors.get(a);
			Log.v("SERVER ERROR " + a + ": " + error.toJson().toString());

			String field = error.getExtra("field");
			if (TextUtils.isEmpty(field)) {
				continue;
			}

			if (field.equals("creditCardNumber")) {
				hasCreditCardNumberError = true;
			}
			else if (field.equals("expirationDate")) {
				hasExpirationDateError = true;
			}
			else if (field.equals("cvv")) {
				hasCVVError = true;
			}
			else if (field.equals("phone")) {
				hasPhoneError = true;
			}
			else if (field.equals("postalCode")) {
				hasPostalCodeError = true;
			}
		}

		// We make the assumption that if we get an error we can handle in a special manner
		// that it will be the first one.  If there are multiple errors, we assume right
		// now that it will require a generic response.
		ServerError firstError = errors.get(0);

		// Check for special errors; return if we handled it
		switch (firstError.getErrorCode()) {
		case BOOKING_FAILED: {
			if (firstError.getDiagnosticFullText().contains("INVALID_CCNUMBER")) {
				DialogFragment frag = SimpleCallbackDialogFragment.newInstance(null,
						getString(R.string.error_invalid_card_number), getString(android.R.string.ok),
						DIALOG_CALLBACK_INVALID_CC);
				frag.show(getSupportFragmentManager(), "badCcNumberDialog");
				return;
			}
			break;
		}
		case INVALID_INPUT: {
			if (hasCreditCardNumberError && hasExpirationDateError && hasCVVError) {
				DialogFragment frag = SimpleCallbackDialogFragment.newInstance(null,
						getString(R.string.e3_error_checkout_payment_failed), getString(android.R.string.ok),
						DIALOG_CALLBACK_INVALID_PAYMENT);
				frag.show(getSupportFragmentManager(), "badPaymentDialog");
				return;
			}
			else if (hasCVVError) {
				setCvvErrorMode(true);
				mProgressFragment.dismiss();
				return;
			}
			else if (hasCreditCardNumberError) {
				DialogFragment frag = SimpleCallbackDialogFragment.newInstance(null,
						getString(R.string.error_invalid_card_number), getString(android.R.string.ok),
						DIALOG_CALLBACK_INVALID_CC);
				frag.show(getSupportFragmentManager(), "badCcNumberDialog");
				return;
			}
			else if (hasPhoneError) {
				DialogFragment frag = SimpleCallbackDialogFragment.newInstance(null,
						getString(R.string.ean_error_invalid_phone_number), getString(android.R.string.ok),
						DIALOG_CALLBACK_INVALID_PHONENUMBER);
				frag.show(getSupportFragmentManager(), "badPhoneNumberDialog");
				return;
			}
			else if (hasPostalCodeError) {
				DialogFragment frag = SimpleCallbackDialogFragment.newInstance(null,
						getString(R.string.invalid_postal_code), getString(android.R.string.ok),
						DIALOG_CALLBACK_INVALID_POSTALCODE);
				frag.show(getSupportFragmentManager(), "badPostalCodeDialog");
				return;
			}
			break;
		}
		case TRIP_ALREADY_BOOKED:
			// If the trip was already booked, just act like everything is hunky-dory
			launchConfirmationActivity();
			return;
		case SESSION_TIMEOUT:
			showUnavailableErrorDialog();
			return;
		default:
			break;
		}

		// At this point, we haven't handled the error - use a generic response
		DialogFragment df = UnhandledErrorDialogFragment.newInstance("");
		df.show(getSupportFragmentManager(), "unhandledErrorDialog");
	}

	private void showUnavailableErrorDialog() {
		boolean isPlural = (Db.getFlightSearch().getSearchParams().getQueryLegCount() != 1);
		FlightUnavailableDialogFragment df = FlightUnavailableDialogFragment.newInstance(isPlural);
		df.show(getSupportFragmentManager(), "unavailableErrorDialog");
	}

	//////////////////////////////////////////////////////////////////////////
	// CVVEntryFragmentListener

	@Override
	public void onBook(String cvv) {
		Db.getBillingInfo().setSecurityCode(cvv);

		doBooking();
	}

	//////////////////////////////////////////////////////////////////////////
	// PriceChangeDialogFragmentListener

	@Override
	public void onAcceptPriceChange() {
		doBooking();
	}

	@Override
	public void onCancelPriceChange() {
		finish();
	}

	//////////////////////////////////////////////////////////////////////////
	// SimpleCallbackDialogFragmentListener

	@Override
	public void onSimpleDialogClick(int callbackId) {
		switch (callbackId) {
		case DIALOG_CALLBACK_INVALID_CC:
		case DIALOG_CALLBACK_INVALID_POSTALCODE:
		case DIALOG_CALLBACK_INVALID_PAYMENT:
			launchHotelPaymentCreditCardFragment();
			finish();
			break;
		case DIALOG_CALLBACK_INVALID_PHONENUMBER:
			launchHotelTravelerPhoneNumberFragment();
			finish();
			break;
		}
	}

	@Override
	public void onSimpleDialogCancel(int callbackId) {
		// Do nothing; we'll just let the user sit on the CVV screen
	}

	//////////////////////////////////////////////////////////////////////////
	// UnhandledErrorDialogFragmentListener

	@Override
	public void onRetryUnhandledException() {
		doBooking();
	}

	@Override
	public void onCallCustomerSupport() {
		SocialUtils.call(this, PointOfSale.getPointOfSale().getSupportPhoneNumber());
	}

	@Override
	public void onCancelUnhandledException() {
		// Do nothing; we'll just let the user sit on the CVV screen
	}
}
