package com.expedia.bookings.activity;

import java.util.Arrays;
import java.util.GregorianCalendar;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightCheckoutResponse;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.server.ExpediaServices;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.util.Ui;

// This is just for testing booking using data from the app.  It is
// not intended to be at all like the final booking activity (should
// use fragments, not just use all pre-filled data, etc.)
public class FlightBookingActivity extends SherlockFragmentActivity {

	private static final String DOWNLOAD_KEY = "com.expedia.bookings.flight.checkout";

	private TextView mTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_flight_booking);

		mTextView = Ui.findView(this, R.id.text);

		Button button = Ui.findView(this, R.id.button);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mTextView.setText("Request in progress...");

				BackgroundDownloader bd = BackgroundDownloader.getInstance();
				bd.cancelDownload(DOWNLOAD_KEY);
				bd.startDownload(DOWNLOAD_KEY, mDownload, mCallback);
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
			ExpediaServices services = new ExpediaServices(FlightBookingActivity.this);
			BackgroundDownloader.getInstance().addDownloadListener(DOWNLOAD_KEY, services);

			// Create a dummy BillingInfo for now
			BillingInfo billingInfo = new BillingInfo();
			billingInfo.setFirstName("Mary");
			billingInfo.setLastName("Poppins");
			billingInfo.setTelephone("222-222-2222");
			billingInfo.setTelephoneCountry("US");
			billingInfo.setTelephoneCountryCode("1");
			billingInfo.setEmail("dan@mobiata.com");

			Location location = new Location();
			location.setStreetAddress(Arrays.asList(new String[] { "17 Cherry Tree Lane" }));
			location.setCity("London");
			location.setStateCode("LH");
			location.setPostalCode("SW151AA");
			location.setCountryCode("GBR");
			billingInfo.setLocation(location);

			billingInfo.setNumber("4111111111111111");
			billingInfo.setExpirationDate(new GregorianCalendar(2016, 1, 1));
			billingInfo.setSecurityCode("123");

			return services.flightCheckout(Db.getFlightSearch().getSelectedFlightTrip().getProductKey(),
					billingInfo, 0);
		}
	};

	private OnDownloadComplete<FlightCheckoutResponse> mCallback = new OnDownloadComplete<FlightCheckoutResponse>() {
		@Override
		public void onDownload(FlightCheckoutResponse results) {
			if (results == null) {
				mTextView.setText("NULL RESPONSE!");
			}
			else if (results.hasErrors()) {
				mTextView.setText(results.getErrors().get(0).getPresentableMessage(FlightBookingActivity.this));
			}
			else {
				mTextView.setText("SUCCESS!");
			}
		}
	};
}
