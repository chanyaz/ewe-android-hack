package com.expedia.bookings.fragment;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BookingResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelSearch;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.WalletUtils;
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
				HotelSearch search = Db.getHotelSearch();
				String userId = null;
				String tripId = null;
				Long tuid = null;

				if (search.getCreateTripResponse() != null) {
					tripId = search.getCreateTripResponse().getTripId();
					userId = search.getCreateTripResponse().getUserId();
				}

				if (Db.getUser() != null) {
					tuid = Db.getUser().getPrimaryTraveler().getTuid();
				}

				Rate selectedRate = search.getBookingRate();
				BookingResponse response = services.reservation(search.getSearchParams(), search.getSelectedProperty(),
						selectedRate, Db.getBillingInfo(), tripId, userId, tuid);

				notifyWalletTransactionStatus(response);

				return response;
			}
		};
	}

	@Override
	public Class<BookingResponse> getResponseClass() {
		return BookingResponse.class;
	}

	// FullWalletFragment

	@Override
	protected FullWalletRequest getFullWalletRequest() {
		HotelSearch search = Db.getHotelSearch();

		Property property = search.getSelectedProperty();
		Rate originalRate = search.getSelectedRate();
		Rate couponRate = search.getCouponRate();
		Money total = couponRate == null ? originalRate.getDisplayTotalPrice() : couponRate.getDisplayTotalPrice();

		FullWalletRequest.Builder walletRequestBuilder = FullWalletRequest.newBuilder();
		walletRequestBuilder.setGoogleTransactionId(getGoogleWalletTransactionId());

		Cart.Builder cartBuilder = Cart.newBuilder();

		// Total
		cartBuilder.setCurrencyCode(total.getCurrency());
		cartBuilder.setTotalPrice(WalletUtils.formatAmount(total));

		// Base rate
		cartBuilder.addLineItem(WalletUtils.createLineItem(originalRate.getNightlyRateTotal(), property.getName(),
				LineItem.Role.REGULAR));

		// Discount
		if (couponRate != null) {
			Money discount = new Money(couponRate.getTotalPriceAdjustments());
			discount.negate();
			cartBuilder.addLineItem(WalletUtils.createLineItem(discount, property.getName(), LineItem.Role.REGULAR));
		}

		// Taxes & Fees
		if (originalRate.getTotalSurcharge() != null && !originalRate.getTotalSurcharge().isZero()) {
			cartBuilder.addLineItem(WalletUtils.createLineItem(originalRate.getTotalSurcharge(),
					getString(R.string.taxes_and_fees), LineItem.Role.TAX));
		}

		// Extra guest fees
		if (originalRate.getExtraGuestFee() != null && !originalRate.getExtraGuestFee().isZero()) {
			cartBuilder.addLineItem(WalletUtils.createLineItem(originalRate.getExtraGuestFee(),
					getString(R.string.extra_guest_charge), LineItem.Role.TAX));
		}

		walletRequestBuilder.setCart(cartBuilder.build());
		return walletRequestBuilder.build();
	}

}
