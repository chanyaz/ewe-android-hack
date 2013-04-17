package com.expedia.bookings.fragment;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightCheckoutResponse;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Itinerary;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;
import com.expedia.bookings.server.ExpediaServices;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;

public class FlightBookingFragment extends Fragment {

	public static final String TAG = FlightBookingFragment.class.toString();

	private static final String DOWNLOAD_KEY = "com.expedia.bookings.flight.checkout";

	private FlightBookingFragmentListener mListener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof FlightBookingFragmentListener)) {
			throw new RuntimeException("FlightBookingFragment Activity must implement listener!");
		}

		mListener = (FlightBookingFragmentListener) activity;
	}

	@Override
	public void onResume() {
		super.onResume();

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(DOWNLOAD_KEY)) {
			bd.registerDownloadCallback(DOWNLOAD_KEY, mCallback);
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		BackgroundDownloader.getInstance().unregisterDownloadCallback(DOWNLOAD_KEY);
	}

	public boolean isBooking() {
		return BackgroundDownloader.getInstance().isDownloading(DOWNLOAD_KEY);
	}

	public void doBooking() {
		startBookingDownload();
	}

	private void startBookingDownload() {
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (!bd.isDownloading(DOWNLOAD_KEY)) {
			mListener.onStartBooking();

			// Clear current results (if any)
			Db.setFlightCheckout(null);

			bd.startDownload(DOWNLOAD_KEY, mDownload, mCallback);
		}
	}

	private Download<FlightCheckoutResponse> mDownload = new Download<FlightCheckoutResponse>() {
		@Override
		public FlightCheckoutResponse doDownload() {
			Context context = getActivity();
			ExpediaServices services = new ExpediaServices(context);
			BackgroundDownloader.getInstance().addDownloadListener(DOWNLOAD_KEY, services);

			//TODO: This block shouldn't happen. Currently the mocks pair phone number with travelers, but the BillingInfo object contains phone info.
			//We need to wait on API updates to either A) set phone number as a billing phone number or B) take a bunch of per traveler phone numbers
			BillingInfo billingInfo = Db.getBillingInfo();
			Traveler traveler = Db.getTravelers().get(0);
			billingInfo.setTelephone(traveler.getPhoneNumber());
			billingInfo.setTelephoneCountryCode(traveler.getPhoneCountryCode());

			//TODO: This also shouldn't happen, we should expect billingInfo to have a valid email address at this point...
			if (TextUtils.isEmpty(billingInfo.getEmail())
					|| (User.isLoggedIn(context) && Db.getUser() != null
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
			mListener.onCheckoutResponse(results);
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface FlightBookingFragmentListener {
		public void onStartBooking();

		public void onCheckoutResponse(FlightCheckoutResponse results);
	}
}
