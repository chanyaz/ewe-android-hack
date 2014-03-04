package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightCheckoutResponse;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Itinerary;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.WalletUtils;
import com.google.android.gms.wallet.FullWalletRequest;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;

public class FlightBookingFragment extends BookingFragment<FlightCheckoutResponse> {

	public static final String TAG = FlightBookingFragment.class.toString();

	private static final String DOWNLOAD_KEY = "com.expedia.bookings.flight.checkout";

	// BookingFragment

	@Override
	public String getBookingDownloadKey() {
		return DOWNLOAD_KEY;
	}

	@Override
	public Download<FlightCheckoutResponse> getBookingDownload() {
		return new Download<FlightCheckoutResponse>() {
			@Override
			public FlightCheckoutResponse doDownload() {
				Context context = getActivity();
				ExpediaServices services = new ExpediaServices(context);
				BackgroundDownloader.getInstance().addDownloadListener(DOWNLOAD_KEY, services);

				BillingInfo billingInfo = Db.getBillingInfo();
				FlightTrip trip = Db.getFlightSearch().getSelectedFlightTrip();
				Itinerary itinerary = Db.getItinerary(trip.getItineraryNumber());

				//So at this point, billing info has the correct email address, but the api considers the email
				//address of the first traveler the top priority. We dont want to change the email information
				//of our stored travelers, so we make a copy of the first traveler, and alter its email address.
				List<Traveler> travelers = new ArrayList<Traveler>(Db.getTravelers());
				if (travelers != null && travelers.size() > 0) {
					Traveler trav = new Traveler();
					trav.fromJson(travelers.get(0).toJson());
					trav.setEmail(billingInfo.getEmail());
					travelers.set(0, trav);
				}

				return services.flightCheckout(trip, itinerary, billingInfo, travelers, 0);
			}
		};
	}

	@Override
	public Class<FlightCheckoutResponse> getBookingResponseClass() {
		return FlightCheckoutResponse.class;
	}

	// FullWalletFragment

	@Override
	protected FullWalletRequest getFullWalletRequest() {
		FullWalletRequest.Builder walletRequestBuilder = FullWalletRequest.newBuilder();
		walletRequestBuilder.setGoogleTransactionId(getGoogleWalletTransactionId());
		walletRequestBuilder.setCart(WalletUtils.buildFlightCart(getActivity()));
		return walletRequestBuilder.build();
	}

	@Override
	public void doBookingPrep() {
		// No pre-booking check or prep required for flights. Ignore
	}
}
