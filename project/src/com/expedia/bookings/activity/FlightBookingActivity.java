package com.expedia.bookings.activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightCheckoutResponse;
import com.expedia.bookings.data.FlightPassenger;
import com.expedia.bookings.section.ISectionEditable.SectionChangeListener;
import com.expedia.bookings.section.SectionBillingInfo;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.widget.NavigationButton;
import com.expedia.bookings.widget.NavigationDropdownAdapter;
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

		final Button button = Ui.findView(this, R.id.button);
		button.setEnabled(false);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mTextView.setText("Request in progress...");

				BackgroundDownloader bd = BackgroundDownloader.getInstance();
				bd.cancelDownload(DOWNLOAD_KEY);
				bd.startDownload(DOWNLOAD_KEY, mDownload, mCallback);
			}
		});

		final SectionBillingInfo ccSecCode = Ui.findView(this, R.id.edit_creditcard_security_code);
		ccSecCode.bind(Db.getBillingInfo());
		ccSecCode.addChangeListener(new SectionChangeListener() {
			@Override
			public void onChange() {
				button.setEnabled(ccSecCode.hasValidInput());
			}
		});

		//Actionbar
		ActionBar actionBar = this.getSupportActionBar();
		NavigationButton nb = NavigationButton.createNewInstanceAndAttach(this, R.drawable.icon, actionBar);
		nb.setDropdownAdapter(new NavigationDropdownAdapter(this));
		nb.setTitle(getTitle());
		

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

			//TODO: This block shouldn't happen. Currently the mocks pair phone number with travelers, but the BillingInfo object contains phone info.
			//We need to wait on API updates to either A) set phone number as a billing phone number or B) take a bunch of per traveler phone numbers
			BillingInfo billingInfo = Db.getBillingInfo();
			FlightPassenger passenger = Db.getFlightPassengers().get(0);
			billingInfo.setTelephone(passenger.getPhoneNumber());
			billingInfo.setTelephoneCountryCode(passenger.getPhoneCountryCode());

			return services.flightCheckout(Db.getFlightSearch().getSelectedFlightTrip().getProductKey(),
					billingInfo, Db.getFlightPassengers(), 0);
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
