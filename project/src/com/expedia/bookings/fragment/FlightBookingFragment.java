package com.expedia.bookings.fragment;

import android.content.Context;
import android.text.TextUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightCheckoutResponse;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Itinerary;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;
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
