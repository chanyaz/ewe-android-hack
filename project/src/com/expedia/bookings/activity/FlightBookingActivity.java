package com.expedia.bookings.activity;

import java.math.BigDecimal;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;

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
import com.expedia.bookings.fragment.BlurredBackgroundFragment;
import com.expedia.bookings.fragment.BookingInProgressDialogFragment;
import com.expedia.bookings.fragment.CVVEntryFragment;
import com.expedia.bookings.fragment.CVVEntryFragment.CVVEntryFragmentListener;
import com.expedia.bookings.fragment.PriceChangeDialogFragment;
import com.expedia.bookings.fragment.PriceChangeDialogFragment.PriceChangeDialogFragmentListener;
import com.expedia.bookings.fragment.SimpleSupportDialogFragment;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.SettingUtils;

// This is just for testing booking using data from the app.  It is
// not intended to be at all like the final booking activity (should
// use fragments, not just use all pre-filled data, etc.)
public class FlightBookingActivity extends SherlockFragmentActivity implements CVVEntryFragmentListener,
		PriceChangeDialogFragmentListener {

	private static final String DOWNLOAD_KEY = "com.expedia.bookings.flight.checkout";

	private Context mContext;

	private BlurredBackgroundFragment mBgFragment;
	private CVVEntryFragment mCVVEntryFragment;
	private BookingInProgressDialogFragment mProgressFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;

		setContentView(R.layout.activity_flight_booking);

		setTitle(R.string.title_complete_booking);

		mBgFragment = Ui.findSupportFragment(this, BlurredBackgroundFragment.TAG);
		mCVVEntryFragment = Ui.findSupportFragment(this, CVVEntryFragment.TAG);
		mProgressFragment = Ui.findSupportFragment(this, BookingInProgressDialogFragment.TAG);

		if (savedInstanceState == null) {
			mBgFragment = new BlurredBackgroundFragment();

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
			ft.add(R.id.bg_frame, mBgFragment, BlurredBackgroundFragment.TAG);
			ft.add(R.id.cvv_frame, mCVVEntryFragment, CVVEntryFragment.TAG);
			ft.commit();
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

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(DOWNLOAD_KEY)) {
			bd.registerDownloadCallback(DOWNLOAD_KEY, mCallback);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		BackgroundDownloader.getInstance().unregisterDownloadCallback(DOWNLOAD_KEY);
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
	// Booking downloads

	private void doBooking() {
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (!bd.isDownloading(DOWNLOAD_KEY)) {
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

			// If the debug setting is made to fake a price change, then fake the current price 
			if (!AndroidUtils.isRelease(mContext)) {
				String val = SettingUtils.get(mContext,
						getString(R.string.preference_flight_fake_price_change),
						getString(R.string.preference_flight_fake_price_change_default));
				trip.getTotalFare().add(new BigDecimal(val));
			}

			return services.flightCheckout(trip, itinerary, billingInfo, Db.getTravelers(), 0);
		}
	};

	private OnDownloadComplete<FlightCheckoutResponse> mCallback = new OnDownloadComplete<FlightCheckoutResponse>() {
		@Override
		public void onDownload(FlightCheckoutResponse results) {
			mProgressFragment.dismiss();

			Db.setFlightCheckout(results);

			if (results == null) {
				SimpleSupportDialogFragment sdf = SimpleSupportDialogFragment.newInstance(
						getString(R.string.error_booking_title), "LOCALIZE: Did not get any response from the server!");
				sdf.show(getSupportFragmentManager(), "noResultsErrorDialog");
			}
			else if (results.hasErrors()) {
				handleErrorResponse(results);
			}
			else {
				// Launch the conf page
				startActivity(new Intent(mContext, FlightConfirmationActivity.class));
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Error handling code
	//
	// Split out just for ease-of-reading

	public void handleErrorResponse(FlightCheckoutResponse response) {
		boolean handledError = true;
		List<ServerError> errors = response.getErrors();
		if (errors.size() == 1) {
			ServerError error = errors.get(0);

			switch (error.getErrorCode()) {
			case PRICE_CHANGE:
				FlightTrip currentOffer = Db.getFlightSearch().getSelectedFlightTrip();
				FlightTrip newOffer = response.getNewOffer();
				PriceChangeDialogFragment fragment = PriceChangeDialogFragment.newInstance(currentOffer, newOffer);
				fragment.show(getSupportFragmentManager(), PriceChangeDialogFragment.TAG);
				break;
			default:
				handledError = false;
				break;
			}
		}

		// For unhandled errors, use this default dialog
		if (!handledError) {
			StringBuilder sb = new StringBuilder();

			sb.append("TEMP ERROR: Response came back with errors:");

			for (ServerError error : response.getErrors()) {
				sb.append("\n\n");
				sb.append(error.getPresentableMessage(mContext));
			}

			SimpleSupportDialogFragment sdf = SimpleSupportDialogFragment.newInstance(
					getString(R.string.error_booking_title), sb.toString());
			sdf.show(getSupportFragmentManager(), "multipleErrorsDialog");
		}
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
}
