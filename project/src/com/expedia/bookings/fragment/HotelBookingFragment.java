package com.expedia.bookings.fragment;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BookingResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.server.ExpediaServices;
import com.google.android.gms.wallet.Cart;
import com.google.android.gms.wallet.FullWalletRequest;
import com.google.android.gms.wallet.LineItem;
import com.mobiata.android.BackgroundDownloader.Download;

/**
 * This is an View-less Fragment which performs a hotel booking.
 * 
 * It is separated into its own Fragment so that it can use the lifecycle on its own (and
 * can be derived from a Fragment, which will help with Google Wallet compatibility)
 */
public class HotelBookingFragment extends BookingFragment<BookingResponse> {

	public static final String TAG = HotelBookingFragment.class.toString();

	public static final String BOOKING_DOWNLOAD_KEY = "com.expedia.bookings.hotel.checkout";

	// BookingFragment

	@Override
	public String getDownloadKey() {
		return BOOKING_DOWNLOAD_KEY;
	}

	@Override
	public Download<BookingResponse> getDownload() {
		return new Download<BookingResponse>() {
			@Override
			public BookingResponse doDownload() {
				ExpediaServices services = new ExpediaServices(getActivity());
				String userId = null;
				String tripId = null;
				Long tuid = null;

				if (Db.getCreateTripResponse() != null) {
					tripId = Db.getCreateTripResponse().getTripId();
					userId = Db.getCreateTripResponse().getUserId();
				}

				if (Db.getUser() != null) {
					tuid = Db.getUser().getPrimaryTraveler().getTuid();
				}

				BookingResponse response = services.reservation(Db.getSearchParams(), Db.getSelectedProperty(),
						Db.getSelectedRate(), Db.getBillingInfo(), tripId, userId, tuid);

				notifyWalletTransactionStatus(response);

				return response;
			}
		};
	}

	// FullWalletFragment

	@Override
	protected FullWalletRequest getFullWalletRequest() {
		Property property = Db.getSelectedProperty();
		Rate rate = Db.getSelectedRate();
		Money totalBeforeTax = rate.getTotalAmountBeforeTax();
		Money surcharges = rate.getTotalSurcharge();
		Money totalAfterTax = rate.getTotalAmountAfterTax();

		FullWalletRequest.Builder walletRequestBuilder = FullWalletRequest.newBuilder();
		walletRequestBuilder.setGoogleTransactionId(getGoogleWalletTransactionId());

		Cart.Builder cartBuilder = Cart.newBuilder();
		cartBuilder.setCurrencyCode(totalAfterTax.getCurrency());
		cartBuilder.setTotalPrice(totalAfterTax.getAmount().toPlainString());

		LineItem.Builder beforeTaxBuilder = LineItem.newBuilder();
		beforeTaxBuilder.setCurrencyCode(totalBeforeTax.getCurrency());
		beforeTaxBuilder.setDescription(property.getName());
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
