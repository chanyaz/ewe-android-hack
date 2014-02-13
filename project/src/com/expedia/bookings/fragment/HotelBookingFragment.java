package com.expedia.bookings.fragment;

import java.math.BigDecimal;

import android.text.TextUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BookingResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelProductResponse;
import com.expedia.bookings.data.HotelSearch;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.RateBreakdown;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.dialog.HotelErrorDialog;
import com.expedia.bookings.dialog.HotelPriceChangeDialog;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.WalletUtils;
import com.google.android.gms.wallet.FullWalletRequest;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.SettingUtils;

/**
 * This is an View-less Fragment which performs a hotel booking.
 *
 * It is separated into its own Fragment so that it can use the lifecycle on its own (and
 * can be derived from a Fragment, which will help with Google Wallet compatibility)
 */
public class HotelBookingFragment extends BookingFragment<BookingResponse> {

	public static final String TAG = HotelBookingFragment.class.toString();

	public static final String KEY_DOWNLOAD_BOOKING = "com.expedia.bookings.hotel.checkout";
	public static final String KEY_DOWNLOAD_HOTEL_PRODUCT_RESPONSE = "KEY_DOWNLOAD_HOTEL_PRODUCT_RESPONSE";

	private static final String HOTEL_OFFER_ERROR_DIALOG = "HOTEL_OFFER_ERROR_DIALOG";

	private HotelProductSuccessListener mHotelProductSuccessListener;

	// BookingFragment

	@Override
	public String getBookingDownloadKey() {
		return KEY_DOWNLOAD_BOOKING;
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

	@Override
	public void onResume() {
		super.onResume();
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(KEY_DOWNLOAD_HOTEL_PRODUCT_RESPONSE)) {
			bd.registerDownloadCallback(KEY_DOWNLOAD_HOTEL_PRODUCT_RESPONSE, mHotelProductCallback);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (getActivity().isFinishing()) {
			bd.cancelDownload(KEY_DOWNLOAD_HOTEL_PRODUCT_RESPONSE);
		}
		else {
			bd.unregisterDownloadCallback(KEY_DOWNLOAD_HOTEL_PRODUCT_RESPONSE);
		}
	}

	/////////////////////////////////////////////////////
	///// Hotel Product service related

	public void startHotelProductDownload() {
		// Let's cancel download if already running.
		cancelHotelProductDownload();
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.startDownload(KEY_DOWNLOAD_HOTEL_PRODUCT_RESPONSE, mHotelProductDownload, mHotelProductCallback);
	}

	public void registerForHotelProductDownload() {
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.registerDownloadCallback(KEY_DOWNLOAD_HOTEL_PRODUCT_RESPONSE, mHotelProductCallback);
	}

	public void cancelHotelProductDownload() {
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(KEY_DOWNLOAD_HOTEL_PRODUCT_RESPONSE)) {
			bd.cancelDownload(KEY_DOWNLOAD_HOTEL_PRODUCT_RESPONSE);
		}
	}

	private final Download<HotelProductResponse> mHotelProductDownload = new Download<HotelProductResponse>() {
		@Override
		public HotelProductResponse doDownload() {
			ExpediaServices services = new ExpediaServices(getActivity());
			BackgroundDownloader.getInstance().addDownloadListener(KEY_DOWNLOAD_HOTEL_PRODUCT_RESPONSE, services);
			Rate selectedRate = Db.getHotelSearch().getSelectedRate();
			return services.hotelProduct(Db.getHotelSearch().getSearchParams(), Db.getHotelSearch()
					.getSelectedProperty(), selectedRate);
		}
	};

	private final OnDownloadComplete<HotelProductResponse> mHotelProductCallback = new OnDownloadComplete<HotelProductResponse>() {
		@Override
		public void onDownload(HotelProductResponse response) {
			if (response == null || response.hasErrors()) {
				handleHotelProductError(response);
			}
			else {
				final String selectedId = Db.getHotelSearch().getSelectedPropertyId();
				Rate selectedRate = Db.getHotelSearch().getSelectedRate();
				Rate newRate = response.getRate();

				if (TextUtils.equals(selectedRate.getRateKey(), response.getOriginalProductKey())) {
					onHotelProductSuccess(response, selectedId, selectedRate, newRate);
				}
				else {
					handleHotelProductError(response);
				}
			}
		}

	};

	/**
	 * This method takes care of all the updating upon hotelProduct download success
	 * which is common for most cases.
	 * If we want to add more UI functionality, then implement the HotelProductSuccessListener.
	 */
	private void onHotelProductSuccess(HotelProductResponse response, final String selectedId, Rate selectedRate,
			Rate newRate) {
		if (!AndroidUtils.isRelease(getActivity())) {
			String val = SettingUtils.get(getActivity(),
					getString(R.string.preference_fake_hotel_price_change),
					getString(R.string.preference_fake_price_change_default));
			BigDecimal bigDecVal = new BigDecimal(val);

			//Update total price
			newRate.getDisplayTotalPrice().add(bigDecVal);

			//Update all nights total and per/night totals
			newRate.getNightlyRateTotal().add(bigDecVal);
			if (newRate.getRateBreakdownList() != null) {
				BigDecimal perNightChange = bigDecVal.divide(new BigDecimal(newRate
						.getRateBreakdownList().size()));
				for (RateBreakdown breakdown : newRate.getRateBreakdownList()) {
					breakdown.getAmount().add(perNightChange);
				}
			}

		}

		int priceChange = selectedRate.compareForPriceChange(newRate);
		if (priceChange != 0) {
			boolean isPriceHigher = priceChange < 0;
			HotelPriceChangeDialog dialog = HotelPriceChangeDialog.newInstance(isPriceHigher,
					selectedRate.getDisplayTotalPrice(), newRate.getDisplayTotalPrice());
			dialog.show(getFragmentManager(), "priceChangeDialog");
		}

		Db.getHotelSearch().getAvailability(selectedId).updateFrom(selectedRate.getRateKey(), response);
		Db.getHotelSearch().getAvailability(selectedId).setSelectedRate(newRate);

		if (mHotelProductSuccessListener != null) {
			mHotelProductSuccessListener.onHotelProductSuccess();
		}
	}

	private void handleHotelProductError(HotelProductResponse response) {
		HotelErrorDialog dialog = HotelErrorDialog.newInstance();
		int messageId = R.string.e3_error_hotel_offers_hotel_service_failure;
		if (response != null && response.getErrors() != null) {
			for (ServerError error : response.getErrors()) {
				if (error.getErrorCode() == ServerError.ErrorCode.HOTEL_ROOM_UNAVAILABLE) {
					String selectedId = Db.getHotelSearch().getSelectedPropertyId();
					messageId = R.string.e3_error_hotel_offers_hotel_room_unavailable;
					Db.getHotelSearch().getAvailability(selectedId).removeRate(response.getOriginalProductKey());
				}
			}
		}

		dialog.setMessage(messageId);
		dialog.show(getFragmentManager(), HOTEL_OFFER_ERROR_DIALOG);
	}

	public void addHotelProductSuccessListener(HotelProductSuccessListener listener) {
		mHotelProductSuccessListener = listener;
	}

	/**
	 * Post hotelProduct download success listener.
	 * Implement this listener if you want to add more functionality.
	 */
	public interface HotelProductSuccessListener {
		public void onHotelProductSuccess();
	}
}
