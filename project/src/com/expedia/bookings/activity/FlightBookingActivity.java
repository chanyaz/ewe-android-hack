package com.expedia.bookings.activity;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightCheckoutResponse;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Itinerary;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;
import com.expedia.bookings.fragment.BookingInProgressDialogFragment;
import com.expedia.bookings.section.ISectionEditable.SectionChangeListener;
import com.expedia.bookings.section.SectionBillingInfo;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.util.ViewUtils;

// This is just for testing booking using data from the app.  It is
// not intended to be at all like the final booking activity (should
// use fragments, not just use all pre-filled data, etc.)
public class FlightBookingActivity extends SherlockFragmentActivity {

	private static final String DOWNLOAD_KEY = "com.expedia.bookings.flight.checkout";

	private Context mContext;

	private TextView mTextView;

	private BookingInProgressDialogFragment mProgressFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;

		setContentView(R.layout.activity_flight_booking);

		mProgressFragment = Ui.findSupportFragment(this, BookingInProgressDialogFragment.TAG);

		mTextView = Ui.findView(this, R.id.text);

		final Button button = Ui.findView(this, R.id.button);
		button.setEnabled(false);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ViewUtils.hideSoftKeyboard(mContext, mTextView);

				mProgressFragment = new BookingInProgressDialogFragment();
				mProgressFragment.show(getSupportFragmentManager(), BookingInProgressDialogFragment.TAG);

				BackgroundDownloader bd = BackgroundDownloader.getInstance();
				bd.cancelDownload(DOWNLOAD_KEY);
				bd.startDownload(DOWNLOAD_KEY, mDownload, mCallback);
			}
		});

		if (savedInstanceState == null && !ExpediaServices.suppressFinalBooking(this)) {
			button.setText("Push to book!");

			mTextView.setText("WARNING!  WARNING!\n\nFlight bookings are NOT being suppressed - "
					+ "a real booking will occur when you hit go!");
		}

		final SectionBillingInfo ccSecCode = Ui.findView(this, R.id.section_edit_creditcard_security_code);
		ccSecCode.bind(Db.getBillingInfo());
		ccSecCode.addChangeListener(new SectionChangeListener() {
			@Override
			public void onChange() {
				button.setEnabled(ccSecCode.hasValidInput());
			}
		});
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

			// Set the email - either from the traveler himself, or from the primary account email
			String email = traveler.getEmail();
			if (TextUtils.isEmpty(email)) {
				User user = Db.getUser();
				if (user != null) {
					email = user.getPrimaryTraveler().getEmail();
				}
			}
			billingInfo.setEmail(email);

			FlightTrip trip = Db.getFlightSearch().getSelectedFlightTrip();
			Itinerary itinerary = Db.getItinerary(trip.getItineraryNumber());
			return services.flightCheckout(trip, itinerary, billingInfo, Db.getTravelers(), 0);
		}
	};

	private OnDownloadComplete<FlightCheckoutResponse> mCallback = new OnDownloadComplete<FlightCheckoutResponse>() {
		@Override
		public void onDownload(FlightCheckoutResponse results) {
			mProgressFragment.dismiss();

			// This is all just temporary display code while we figure out how to do the conf page 
			StringBuilder sb = new StringBuilder();

			if (results == null) {
				sb.append("Did not get any response from the server!");
			}
			else if (results.hasErrors()) {
				sb.append("Response came back with errors:");

				for (ServerError error : results.getErrors()) {
					sb.append("\n\n");
					sb.append(error.getPresentableMessage(mContext));
				}
			}
			else {
				sb.append("Booking success!");
				sb.append("\n\n");
				sb.append("orderId: " + results.getOrderId());

				if (!results.getOrderId().equals("000000")) {
					sb.append("\n\nWARNING: ORDER ID WAS NOT 000000! THIS MEANS THE BOOKING ACTUALLY WENT THROUGH!");
				}
			}

			mTextView.setText(sb.toString());
		}
	};
}
