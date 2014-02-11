package com.expedia.bookings.activity;

import java.math.BigDecimal;
import java.util.List;

import org.joda.time.DateTime;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.ImageView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.FlightPaymentOptionsActivity.YoYoPosition;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightCheckoutResponse;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Response;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.ServerError.ErrorCode;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.fragment.BookingFragment.BookingFragmentListener;
import com.expedia.bookings.fragment.BookingInProgressDialogFragment;
import com.expedia.bookings.fragment.CVVEntryFragment;
import com.expedia.bookings.fragment.CVVEntryFragment.CVVEntryFragmentListener;
import com.expedia.bookings.fragment.FlightBookingFragment;
import com.expedia.bookings.fragment.FlightUnavailableDialogFragment;
import com.expedia.bookings.fragment.PriceChangeDialogFragment;
import com.expedia.bookings.fragment.PriceChangeDialogFragment.PriceChangeDialogFragmentListener;
import com.expedia.bookings.fragment.SimpleCallbackDialogFragment;
import com.expedia.bookings.fragment.SimpleCallbackDialogFragment.SimpleCallbackDialogFragmentListener;
import com.expedia.bookings.fragment.UnhandledErrorDialogFragment;
import com.expedia.bookings.fragment.UnhandledErrorDialogFragment.UnhandledErrorDialogFragmentListener;
import com.expedia.bookings.fragment.WalletFragment;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.SettingUtils;

public class FlightBookingActivity extends SherlockFragmentActivity implements CVVEntryFragmentListener,
		PriceChangeDialogFragmentListener, SimpleCallbackDialogFragmentListener, UnhandledErrorDialogFragmentListener,
		BookingFragmentListener {

	private static final String STATE_CVV_ERROR_MODE = "STATE_CVV_ERROR_MODE";

	private static final int DIALOG_CALLBACK_INVALID_CC = 1;
	private static final int DIALOG_CALLBACK_EXPIRED_CC = 2;
	private static final int DIALOG_CALLBACK_MINOR = 3;

	private Context mContext;

	private CVVEntryFragment mCVVEntryFragment;
	private BookingInProgressDialogFragment mProgressFragment;
	private FlightBookingFragment mBookingFragment;

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

		ImageView bgImageView = Ui.findView(this, R.id.background_bg_view);
		bgImageView.setImageBitmap(Db.getBackgroundImage(this, true));

		setTitle(R.string.title_complete_booking);

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
				mCVVEntryFragment = CVVEntryFragment.newInstance(this, Db.getBillingInfo());
				ft.add(R.id.cvv_frame, mCVVEntryFragment, CVVEntryFragment.TAG);
			}

			ft.commit();

			// If the debug setting is made to fake a price change, then fake the current price (by changing it)
			if (!AndroidUtils.isRelease(mContext)) {
				FlightTrip trip = Db.getFlightSearch().getSelectedFlightTrip();

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

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

	//////////////////////////////////////////////////////////////////////////
	// Invalid CVV modes

	public void setCvvErrorMode(boolean enabled) {
		// If we're booking with Google Wallet, ignore this entirely
		if (mBookingFragment.willBookViaGoogleWallet()) {
			return;
		}

		mCvvErrorModeEnabled = enabled;
		// Set header bg
		int bgResId = (enabled) ? R.drawable.bg_flight_action_bar_top_red : Ui.obtainThemeResID(this,R.attr.actionBarBackgroundDrawable);

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

			// If the debug setting is made to fake a price change, then fake the price here too
			// This is sort of a second price change, to help figure out testing when we have obfees and a price change...
			if (!AndroidUtils.isRelease(this)) {
				String val = SettingUtils.get(this,
						getString(R.string.preference_fake_flight_price_change),
						getString(R.string.preference_fake_price_change_default));
				currentOffer.getTotalFare().add(new BigDecimal(val));
				newOffer.getTotalFare().add(new BigDecimal(val));
			}

			PriceChangeDialogFragment fragment = PriceChangeDialogFragment.newInstance(currentOffer, newOffer);
			fragment.show(getSupportFragmentManager(), PriceChangeDialogFragment.TAG);

			OmnitureTracking.trackErrorPageLoadFlightPriceChangeTicket(mContext);
			return;
		case INVALID_INPUT:
		case PAYMENT_FAILED:
			String field = firstError.getExtra("field");

			if (firstError.getErrorCode() == ErrorCode.PAYMENT_FAILED) {
				OmnitureTracking.trackErrorPageLoadFlightPaymentFailed(mContext);
			}

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
			// 1643: Handle an odd API response. This is probably due to the transition
			// to being anble to handle booking tickets for minors. We shouldn't need this in the future.
			else if ("mainFlightPassenger.birthDate".equals(field)) {
				DialogFragment frag = SimpleCallbackDialogFragment.newInstance(null,
						getString(R.string.error_booking_with_minor), getString(android.R.string.ok),
						DIALOG_CALLBACK_MINOR);
				frag.show(getSupportFragmentManager(), "cannotBookWithMinorDialog");
				return;
			}

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
		case CANNOT_BOOK_WITH_MINOR: {
			DialogFragment frag = SimpleCallbackDialogFragment
					.newInstance(null,
							getString(R.string.error_booking_with_minor), getString(android.R.string.ok),
							DIALOG_CALLBACK_MINOR);
			frag.show(getSupportFragmentManager(), "cannotBookWithMinorDialog");
			return;
		}
		case GOOGLE_WALLET_ERROR: {
			DialogFragment frag = SimpleCallbackDialogFragment.newInstance(null,
					getString(R.string.google_wallet_unavailable), getString(android.R.string.ok), 0);
			frag.show(getSupportFragmentManager(), "googleWalletErrorDialog");
			return;
		}
		default:
			OmnitureTracking.trackErrorPageLoadFlightCheckout(mContext);
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
		// #1269: Don't do the invalid CC page jump if we're booking using Google Wallet
		if (mBookingFragment.willBookViaGoogleWallet()) {
			finish();
			return;
		}

		switch (callbackId) {
		case DIALOG_CALLBACK_INVALID_CC:
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
		case DIALOG_CALLBACK_EXPIRED_CC:
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

	@Override
	public void onSimpleDialogCancel(int callbackId) {
		// If we're booking via wallet, back out; otherwise sit on CVV screen
		if (mBookingFragment.willBookViaGoogleWallet()) {
			finish();
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
		SocialUtils.call(this, PointOfSale.getPointOfSale().getSupportPhoneNumberBestForUser(Db.getUser()));
	}

	@Override
	public void onCancelUnhandledException() {
		// If we're booking via wallet, back out; otherwise sit on CVV screen
		if (mBookingFragment.willBookViaGoogleWallet()) {
			finish();
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// BookingFragmentListener

	@Override
	public void onStartBooking() {
		showProgressDialog();
	}

	@Override
	public void onBookingResponse(Response results) {
		FlightCheckoutResponse response = (FlightCheckoutResponse) results;

		dismissProgressDialog();

		// Modify the response to fake online booking fees
		if (!AndroidUtils.isRelease(mContext) && response.getNewOffer() != null) {
			BigDecimal fakeObFees = getFakeObFeesAmount();
			if (!fakeObFees.equals(BigDecimal.ZERO)) {
				Money amount = new Money(response.getNewOffer().getTotalFare());
				amount.setAmount(fakeObFees);
				response.getNewOffer().setOnlineBookingFeesAmount(amount);
			}
		}

		Db.setFlightCheckout(response);

		if (response == null) {
			DialogFragment df = UnhandledErrorDialogFragment.newInstance(null);
			df.show(getSupportFragmentManager(), "noResultsErrorDialog");
		}
		else if (response.hasErrors()) {
			handleErrorResponse(response);
		}
		else {
			// Tracking
			try {
				if (Db.getFlightSearch() != null && Db.getFlightSearch().getSelectedFlightTrip() != null) {
					FlightTrip trip = Db.getFlightSearch().getSelectedFlightTrip();
					int days = 0;
					if (trip.getLegCount() > 0) {
						FlightLeg firstLeg = Db.getFlightSearch().getSelectedFlightTrip().getLeg(0);
						DateTime departureCal = new DateTime(firstLeg.getFirstWaypoint().getMostRelevantDateTime());
						DateTime now = DateTime.now();
						days = JodaUtils.daysBetween(departureCal, now);
						if (days < 0) {
							days = 0;
						}
					}
					Money money = Db.getFlightSearch().getSelectedFlightTrip().getTotalFare();
					String destAirportCode = Db.getFlightSearch().getSearchParams().getArrivalLocation()
							.getDestinationId();
					if (money != null) {
						AdTracker.trackFlightBooked(money.getCurrency(), money.getAmount().doubleValue(), days,
								destAirportCode);
					}
				}
			}
			catch (Exception ex) {
				Log.e("Exception tracking flight checkout", ex);
			}
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
}
