package com.expedia.bookings.fragment;

import com.expedia.bookings.data.BookingResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelSearch;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.WalletUtils;
import com.google.android.gms.wallet.FullWalletRequest;
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
	public String getBookingDownloadKey() {
		return BOOKING_DOWNLOAD_KEY;
	}

	@Override
	public Download<BookingResponse> getBookingDownload() {
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
	public Class<BookingResponse> getBookingResponseClass() {
		return BookingResponse.class;
	}

	// FullWalletFragment

	@Override
	protected FullWalletRequest getFullWalletRequest() {
		FullWalletRequest.Builder walletRequestBuilder = FullWalletRequest.newBuilder();
		walletRequestBuilder.setGoogleTransactionId(getGoogleWalletTransactionId());
		walletRequestBuilder.setCart(WalletUtils.buildHotelCart(getActivity()));
		return walletRequestBuilder.build();
	}

}
