package com.expedia.bookings.activity;

import java.math.BigDecimal;
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
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightCheckoutResponse;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Itinerary;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;
import com.expedia.bookings.fragment.BookingInProgressDialogFragment;
import com.expedia.bookings.fragment.CVVEntryFragment;
import com.expedia.bookings.fragment.CVVEntryFragment.CVVEntryFragmentListener;
import com.expedia.bookings.fragment.FlightUnavailableDialogFragment;
import com.expedia.bookings.fragment.PriceChangeDialogFragment;
import com.expedia.bookings.fragment.PriceChangeDialogFragment.PriceChangeDialogFragmentListener;
import com.expedia.bookings.fragment.SimpleCallbackDialogFragment;
import com.expedia.bookings.fragment.SimpleCallbackDialogFragment.SimpleCallbackDialogFragmentListener;
import com.expedia.bookings.fragment.UnhandledErrorDialogFragment;
import com.expedia.bookings.fragment.UnhandledErrorDialogFragment.UnhandledErrorDialogFragmentListener;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.SupportUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.SettingUtils;

public class FlightBookingActivity extends SherlockFragmentActivity implements CVVEntryFragmentListener,
		PriceChangeDialogFragmentListener, SimpleCallbackDialogFragmentListener, UnhandledErrorDialogFragmentListener {

	private static final String DOWNLOAD_KEY = "com.expedia.bookings.flight.checkout";

	private static final String STATE_CVV_ERROR_MODE = "STATE_CVV_ERROR_MODE";

	private static final int DIALOG_CALLBACK_INVALID_CC = 1;
	private static final int DIALOG_CALLBACK_EXPIRED_CC = 2;

	private Context mContext;

	private CVVEntryFragment mCVVEntryFragment;
	private BookingInProgressDialogFragment mProgressFragment;

	private boolean mCvvErrorModeEnabled;

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

		setContentView(R.layout.activity_flight_booking);

		setTitle(R.string.title_complete_booking);

		mCVVEntryFragment = Ui.findSupportFragment(this, CVVEntryFragment.TAG);
		mProgressFragment = Ui.findSupportFragment(this, BookingInProgressDialogFragment.TAG);

		if (savedInstanceState == null) {
			// Determine the data displayed on the CVVEntryFragment
			BillingInfo billingInfo = Db.getBillingInfo();
			StoredCreditCard cc = billingInfo.getStoredCard();

			String personName;
			String cardName;
			if (cc != null) {
				Traveler traveler = Db.getTravelers().get(0);
				personName = traveler.getFirstName() + " " + traveler.getLastName();

				cardName = cc.getDescription();
			}
			else {
				personName = billingInfo.getNameOnCard();

				String ccNumber = billingInfo.getNumber();
				cardName = getString(R.string.card_ending_TEMPLATE, ccNumber.substring(ccNumber.length() - 4));
			}

			mCVVEntryFragment = CVVEntryFragment.newInstance(personName, cardName);

			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.add(R.id.cvv_frame, mCVVEntryFragment, CVVEntryFragment.TAG);
			ft.commit();

			// If the debug setting is made to fake a price change, then fake the current price 
			if (!AndroidUtils.isRelease(mContext)) {
				String val = SettingUtils.get(mContext,
						getString(R.string.preference_flight_fake_price_change),
						getString(R.string.preference_flight_fake_price_change_default));
				Db.getFlightSearch().getSelectedFlightTrip().getTotalFare().add(new BigDecimal(val));
			}
		}

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	protected void onStart() {
		super.onStart();
		OmnitureTracking.trackPageLoadFlightCheckoutPaymentCid(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		setCvvErrorMode(mCvvErrorModeEnabled);

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(DOWNLOAD_KEY)) {
			bd.registerDownloadCallback(DOWNLOAD_KEY, mCallback);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean(STATE_CVV_ERROR_MODE, mCvvErrorModeEnabled);
	}

	@Override
	protected void onPause() {
		super.onPause();

		BackgroundDownloader.getInstance().unregisterDownloadCallback(DOWNLOAD_KEY);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		mKillReceiver.onDestroy();
	}

	private void launchConfirmationActivity() {
		startActivity(new Intent(this, FlightConfirmationActivity.class));

		// Destroy the activity backstack
		NavUtils.sendKillActivityBroadcast(this);
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
		int bgResId = (enabled) ? R.drawable.bg_flight_action_bar_top_red : R.drawable.bg_action_bar_flight_top;

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
		if (!bd.isDownloading(DOWNLOAD_KEY)) {
			// Clear current results (if any)
			Db.setFlightCheckout(null);

			mProgressFragment = new BookingInProgressDialogFragment();
			mProgressFragment.show(getSupportFragmentManager(), BookingInProgressDialogFragment.TAG);

			bd.startDownload(DOWNLOAD_KEY, mDownload, mCallback);
		}
	}

	private Download<FlightCheckoutResponse> mDownload = new Download<FlightCheckoutResponse>() {
		@Override
		public FlightCheckoutResponse doDownload() {
			ExpediaServices services = new ExpediaServices(mContext);
			BackgroundDownloader.getInstance().addDownloadListener(DOWNLOAD_KEY, services);

			//TODO: This block shouldn't happen. Currently the mocks pair phone number with travelers, but the BillingInfo object contains phone info.
			//We need to wait on API updates to either A) set phone number as a billing phone number or B) take a bunch of per traveler phone numbers
			BillingInfo billingInfo = Db.getBillingInfo();
			Traveler traveler = Db.getTravelers().get(0);
			billingInfo.setTelephone(traveler.getPhoneNumber());
			billingInfo.setTelephoneCountryCode(traveler.getPhoneCountryCode());

			//TODO: This also shouldn't happen, we should expect billingInfo to have a valid email address at this point...
			if (TextUtils.isEmpty(billingInfo.getEmail())
					|| (User.isLoggedIn(FlightBookingActivity.this) && Db.getUser() != null
							&& Db.getUser().getPrimaryTraveler() != null
							&& !TextUtils.isEmpty(Db.getUser().getPrimaryTraveler().getEmail()) && Db.getUser()
							.getPrimaryTraveler().getEmail().compareToIgnoreCase(billingInfo.getEmail()) != 0)) {
				String email = traveler.getEmail();
				if (TextUtils.isEmpty(email)) {
					email = Db.getUser().getPrimaryTraveler().getEmail();
				}
				billingInfo.setEmail(email);
			}

			FlightTrip trip = Db.getFlightSearch().getSelectedFlightTrip();
			Itinerary itinerary = Db.getItinerary(trip.getItineraryNumber());

			return services.flightCheckout(trip, itinerary, billingInfo, Db.getTravelers(), 0);
		}
	};

	private OnDownloadComplete<FlightCheckoutResponse> mCallback = new OnDownloadComplete<FlightCheckoutResponse>() {
		@Override
		public void onDownload(FlightCheckoutResponse results) {
			mProgressFragment.dismiss();

			Db.setFlightCheckout(results);

			if (results == null) {
				DialogFragment df = UnhandledErrorDialogFragment.newInstance(null);
				df.show(getSupportFragmentManager(), "noResultsErrorDialog");
			}
			else if (results.hasErrors()) {
				handleErrorResponse(results);
			}
			else {
				launchConfirmationActivity();
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Error handling code
	//
	// Split out just for ease-of-reading

	public void handleErrorResponse(FlightCheckoutResponse response) {
		List<ServerError> errors = response.getErrors();

		// Log all errors, in case we need to see them
		for (int a = 0; a < errors.size(); a++) {
			Log.v("SERVER ERROR " + a + ": " + errors.get(a).toJson().toString());
		}

		// We make the assumption that if we get an error we can handle in a special manner
		// that it will be the first one.  If there are multiple errors, we assume right
		// now that it will require a generic response.
		ServerError firstError = errors.get(0);

		// Check for special errors; return if we handled it
		switch (firstError.getErrorCode()) {
		case PRICE_CHANGE:
			FlightTrip currentOffer = Db.getFlightSearch().getSelectedFlightTrip();
			FlightTrip newOffer = response.getNewOffer();
			PriceChangeDialogFragment fragment = PriceChangeDialogFragment.newInstance(currentOffer, newOffer);
			fragment.show(getSupportFragmentManager(), PriceChangeDialogFragment.TAG);

			OmnitureTracking.trackErrorPageLoadFlightPriceChangeTicket(mContext);
			return;
		case PAYMENT_FAILED:
			String field = firstError.getExtra("field");

			// Handle each type of failure differently
			if ("cvv".equals(field)) {
				setCvvErrorMode(true);

				OmnitureTracking.trackErrorPageLoadFlightIncorrectCVV(mContext);
				return;
			}
			else if ("creditCardNumber".equals(field)) {
				DialogFragment frag = SimpleCallbackDialogFragment.newInstance(null,
						getString(R.string.error_invalid_card_number), getString(android.R.string.ok),
						DIALOG_CALLBACK_INVALID_CC);
				frag.show(getSupportFragmentManager(), "badCcNumberDialog");
				return;
			}
			else if ("expirationDate".equals(field)) {
				DialogFragment frag = SimpleCallbackDialogFragment.newInstance(null,
						getString(R.string.error_expired_payment), getString(android.R.string.ok),
						DIALOG_CALLBACK_EXPIRED_CC);
				frag.show(getSupportFragmentManager(), "expiredCcDialog");
				return;
			}

			OmnitureTracking.trackErrorPageLoadFlightPaymentFailed(mContext);
			break;
		case TRIP_ALREADY_BOOKED:
			// If the trip was already booked, just act like everything is hunky-dory
			launchConfirmationActivity();
			return;
		case FLIGHT_SOLD_OUT:
			showUnavailableErrorDialog();
			OmnitureTracking.trackErrorPageLoadFlightSoldOut(mContext);
			return;
		case SESSION_TIMEOUT:
			showUnavailableErrorDialog();
			OmnitureTracking.trackErrorPageLoadFlightSearchExpired(mContext);
			return;
		default:
			break;
		}

		// At this point, we haven't handled the error - use a generic response
		DialogFragment df = UnhandledErrorDialogFragment.newInstance(Db.getFlightSearch().getSelectedFlightTrip()
				.getItineraryNumber());
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
		case DIALOG_CALLBACK_EXPIRED_CC:
			// For now, do the same thing - leave this activity
			finish();
			break;
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// UnhandledErrorDialogFragmentListener

	@Override
	public void onRetryUnhandledException() {
		doBooking();
	}

	@Override
	public void onCallCustomerSupport() {
		SocialUtils.call(this, SupportUtils.getFlightSupportNumber(this));
	}

	@Override
	public void onCancelUnhandledException() {
		finish();
	}
}
