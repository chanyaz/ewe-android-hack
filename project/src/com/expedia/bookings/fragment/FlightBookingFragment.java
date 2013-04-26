package com.expedia.bookings.fragment;

import android.content.Context;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightCheckoutResponse;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Itinerary;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.server.ExpediaServices;
import com.google.android.gms.wallet.Cart;
import com.google.android.gms.wallet.FullWalletRequest;
import com.google.android.gms.wallet.LineItem;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;

public class FlightBookingFragment extends BookingFragment<FlightCheckoutResponse> {

	public static final String TAG = FlightBookingFragment.class.toString();

	private static final String DOWNLOAD_KEY = "com.expedia.bookings.flight.checkout";

	// BookingFragment

	@Override
	public String getDownloadKey() {
		return DOWNLOAD_KEY;
	}

	@Override
	public Download<FlightCheckoutResponse> getDownload() {
		return new Download<FlightCheckoutResponse>() {
			@Override
			public FlightCheckoutResponse doDownload() {
				Context context = getActivity();
				ExpediaServices services = new ExpediaServices(context);
				BackgroundDownloader.getInstance().addDownloadListener(DOWNLOAD_KEY, services);

				BillingInfo billingInfo = Db.getBillingInfo();

				FlightTrip trip = Db.getFlightSearch().getSelectedFlightTrip();
				Itinerary itinerary = Db.getItinerary(trip.getItineraryNumber());

				return services.flightCheckout(trip, itinerary, billingInfo, Db.getTravelers(), 0);
			}
		};
	}

	@Override
	public Class<FlightCheckoutResponse> getResponseClass() {
		return FlightCheckoutResponse.class;
	}

	// FullWalletFragment

	@Override
	protected FullWalletRequest getFullWalletRequest() {
		FlightTrip trip = Db.getFlightSearch().getSelectedFlightTrip();
		FlightLeg firstLeg = trip.getLeg(0);
		Money totalBeforeTax = trip.getBaseFare();
		Money totalAfterTax = trip.getTotalFare();

		Money surcharges = new Money();
		surcharges.setCurrency(totalAfterTax.getCurrency());
		surcharges.add(trip.getFees());
		surcharges.add(trip.getTaxes());
		surcharges.add(trip.getOnlineBookingFeesAmount());

		FullWalletRequest.Builder walletRequestBuilder = FullWalletRequest.newBuilder();
		walletRequestBuilder.setGoogleTransactionId(getGoogleWalletTransactionId());

		Cart.Builder cartBuilder = Cart.newBuilder();
		cartBuilder.setCurrencyCode(totalAfterTax.getCurrency());
		cartBuilder.setTotalPrice(totalAfterTax.getAmount().toPlainString());

		LineItem.Builder beforeTaxBuilder = LineItem.newBuilder();
		beforeTaxBuilder.setCurrencyCode(totalBeforeTax.getCurrency());
		beforeTaxBuilder.setDescription(getString(R.string.path_template, firstLeg.getFirstWaypoint().mAirportCode,
				firstLeg.getLastWaypoint().mAirportCode));
		beforeTaxBuilder.setRole(LineItem.Role.REGULAR);
		beforeTaxBuilder.setTotalPrice(totalBeforeTax.getAmount().toPlainString());
		cartBuilder.addLineItem(beforeTaxBuilder.build());

		LineItem.Builder taxesBuilder = LineItem.newBuilder();
		taxesBuilder.setCurrencyCode(surcharges.getCurrency());
		taxesBuilder.setDescription(getString(R.string.taxes_and_fees));
		taxesBuilder.setRole(LineItem.Role.TAX);
		taxesBuilder.setTotalPrice(surcharges.getAmount().toPlainString());
		cartBuilder.addLineItem(taxesBuilder.build());

		walletRequestBuilder.setCart(cartBuilder.build());
		return walletRequestBuilder.build();
	}
}
