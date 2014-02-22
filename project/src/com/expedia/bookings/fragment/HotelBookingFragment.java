package com.expedia.bookings.fragment;

import java.math.BigDecimal;

import android.support.v4.app.DialogFragment;
import android.text.TextUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BookingResponse;
import com.expedia.bookings.data.CreateTripResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelProductResponse;
import com.expedia.bookings.data.HotelSearch;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.RateBreakdown;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.dialog.HotelErrorDialog;
import com.expedia.bookings.dialog.HotelPriceChangeDialog;
import com.expedia.bookings.fragment.RetryErrorDialogFragment.RetryErrorDialogFragmentListener;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.WalletUtils;
import com.google.android.gms.wallet.FullWalletRequest;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.app.SimpleDialogFragment;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.SettingUtils;

/**
 * This is an View-less Fragment which performs a hotel booking.
 * <p/>
 * It is separated into its own Fragment so that it can use the lifecycle on its own (and
 * can be derived from a Fragment, which will help with Google Wallet compatibility)
 */
public class HotelBookingFragment extends BookingFragment<BookingResponse> implements RetryErrorDialogFragmentListener {

	public static final String TAG = HotelBookingFragment.class.toString();

	public static final String KEY_DOWNLOAD_BOOKING = "com.expedia.bookings.hotel.checkout";
	public static final String KEY_DOWNLOAD_HOTEL_PRODUCT_RESPONSE = "KEY_DOWNLOAD_HOTEL_PRODUCT_RESPONSE";
	public static final String KEY_DOWNLOAD_CREATE_TRIP = "KEY_DOWNLOAD_CREATE_TRIP";
	public static final String KEY_DOWNLOAD_APPLY_COUPON = "KEY_DOWNLOAD_APPLY_COUPON";

	private static final String RETRY_CREATE_TRIP_DIALOG = "RETRY_CREATE_TRIP_DIALOG";
	private static final String HOTEL_OFFER_ERROR_DIALOG = "HOTEL_OFFER_ERROR_DIALOG";

	private HotelProductSuccessListener mHotelProductSuccessListener;

	private CreateTripDownloadStatusListener mCreateTripDownloadStatusListener;

	private CouponDownloadStatusListener mCouponDownloadStatusListener;
	private String mCouponCode;
	private boolean mApplyCouponInitiated;
	private boolean mRemoveCouponInitiated;

	private boolean mCheckoutInitiated;

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
		if (bd.isDownloading(KEY_DOWNLOAD_CREATE_TRIP)) {
			bd.registerDownloadCallback(KEY_DOWNLOAD_CREATE_TRIP, mCreateTripCallback);
		}
		if (bd.isDownloading(KEY_DOWNLOAD_APPLY_COUPON)) {
			bd.registerDownloadCallback(KEY_DOWNLOAD_APPLY_COUPON, mCouponCallback);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (getActivity().isFinishing()) {
			bd.cancelDownload(KEY_DOWNLOAD_HOTEL_PRODUCT_RESPONSE);
			bd.cancelDownload(KEY_DOWNLOAD_CREATE_TRIP);
			bd.cancelDownload(KEY_DOWNLOAD_APPLY_COUPON);
		}
		else {
			bd.unregisterDownloadCallback(KEY_DOWNLOAD_HOTEL_PRODUCT_RESPONSE);
			bd.unregisterDownloadCallback(KEY_DOWNLOAD_CREATE_TRIP);
			bd.unregisterDownloadCallback(KEY_DOWNLOAD_APPLY_COUPON);
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

	/////////////////////////////////////////////////////
	///// Create Trip service related

	public void startCreateTripForCheckout() {
		mCheckoutInitiated = true;
		startCreateTripDownload();
	}

	private void startCreateTripDownload() {
		// Let's cancel download if already running.
		cancelCreateTripDownload();
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.startDownload(KEY_DOWNLOAD_CREATE_TRIP, mCreateTripDownload, mCreateTripCallback);
	}

	private void cancelCreateTripDownload() {
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(KEY_DOWNLOAD_CREATE_TRIP)) {
			bd.cancelDownload(KEY_DOWNLOAD_CREATE_TRIP);
		}
	}

	private final Download<CreateTripResponse> mCreateTripDownload = new Download<CreateTripResponse>() {
		@Override
		public CreateTripResponse doDownload() {
			ExpediaServices services = new ExpediaServices(getActivity());
			BackgroundDownloader.getInstance().addDownloadListener(KEY_DOWNLOAD_CREATE_TRIP, services);
			return services
				.createTrip(Db.getHotelSearch().getSearchParams(), Db.getHotelSearch().getSelectedProperty());
		}
	};

	private final OnDownloadComplete<CreateTripResponse> mCreateTripCallback = new OnDownloadComplete<CreateTripResponse>() {
		@Override
		public void onDownload(CreateTripResponse response) {
			if (response == null) {
				showRetryErrorDialog();
			}
			else if (response.hasErrors()) {
				handleCreateTripError(response);
			}
			else {
				onCreateTripCallSuccess(response);
			}

		}

	};

	private void onCreateTripCallSuccess(CreateTripResponse response) {
		Db.getHotelSearch().setCreateTripResponse(response);
		if (mCreateTripDownloadStatusListener != null) {
			mCreateTripDownloadStatusListener.onCreateTripSuccess();
		}
		if (mApplyCouponInitiated) {
			mApplyCouponInitiated = false;
			startApplyCouponDownloader(mCouponCode);
		}
		else if (mRemoveCouponInitiated) {
			mCouponCode = null;
			mRemoveCouponInitiated = false;
			Db.getHotelSearch().setCouponApplied(false);
			OmnitureTracking.trackHotelCouponRemoved(getActivity());
			mCouponDownloadStatusListener.onPostRemove(response.getNewRate());
		}
		else if (mCheckoutInitiated) {
			mCheckoutInitiated = false;
			doBooking();
		}
	}

	// Error handling
	private void handleCreateTripError(CreateTripResponse response) {
		ServerError firstError = response.getErrors().get(0);

		switch (firstError.getErrorCode()) {
		case TRIP_SERVICE_ERROR:
			// Let's show a retry dialog here.
		case INVALID_INPUT:
			/*
			 * Since we are only sending [productKey, roomInfoFields] params to the service, don't think users have control over the input.
			 * Hence for now let's show a retry dialog here too (after a chat with API team)
			 */
		default: {
			showRetryErrorDialog();
			break;
		}
		}
	}

	private void showRetryErrorDialog() {
		DialogFragment df = new RetryErrorDialogFragment();
		df.show(getChildFragmentManager(), RETRY_CREATE_TRIP_DIALOG);
	}

	/**
	 * CreateTrip download status listener.
	 * Implement this listener if you want to add more functionality.
	 */
	public interface CreateTripDownloadStatusListener {
		public void onCreateTripSuccess();

		public void onCreateTripError(CreateTripResponse response);

		public void onCreateTripRetry();

		public void onCreateTripRetryCancel();
	}

	public void addCreateTripDownloadStatusListener(CreateTripDownloadStatusListener listener) {
		mCreateTripDownloadStatusListener = listener;
	}

	///////////// Retry CreateTrip call dialog handlers
	@Override
	public void onRetryError() {
		//Restart calls again depending on the status
		if (mApplyCouponInitiated) {
			applyCoupon(mCouponCode);
		}
		else if (mRemoveCouponInitiated) {
			clearCoupon();
		}
		else if (mCheckoutInitiated) {
			startCreateTripForCheckout();
		}
	}

	@Override
	public void onCancelError() {
		//On cancelling the retry dialog do appropriately
		if (mApplyCouponInitiated || mRemoveCouponInitiated) {
			cancelCoupon();
			mCouponDownloadStatusListener.onCouponCancel();
		}
		else {
			mCreateTripDownloadStatusListener.onCreateTripRetryCancel();
		}
	}

	/////////////////////////////////////////////////////
	///// Coupon service related

	/**
	 * This method initiates the coupon application process during checkout.
	 * This fragment handles all the callbacks, errors and retries. 
	 * Add {@link CouponDownloadStatusListener} to listen to coupon download status updates.
	 * As of now coupons are applied for Hotels only.
	 * @param couponCode
	 */
	public void applyCoupon(String couponCode) {
		mApplyCouponInitiated = true;
		mRemoveCouponInitiated = false;
		mCouponCode = couponCode;
		startCreateTripDownload();
	}

	/**
	 * This method initiates the coupon removal process during checkout.
	 * Add {@link CouponDownloadStatusListener} to listen to coupon download status updates.
	 */
	public void clearCoupon() {
		mApplyCouponInitiated = false;
		mRemoveCouponInitiated = true;
		cancelApplyCouponDownloader();
		startCreateTripDownload();
	}

	/**
	 * This method cancels an ongoing coupon application/removal calls.
	 */
	public void cancelCoupon() {
		mApplyCouponInitiated = false;
		mRemoveCouponInitiated = false;
		cancelCreateTripDownload();
		cancelApplyCouponDownloader();
	}

	private void startApplyCouponDownloader(String couponCode) {
		mCouponCode = couponCode;
		// Let's cancel download if already running.
		cancelHotelProductDownload();
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.startDownload(KEY_DOWNLOAD_APPLY_COUPON, mCouponDownload, mCouponCallback);
	}

	private void cancelApplyCouponDownloader() {
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(KEY_DOWNLOAD_APPLY_COUPON)) {
			bd.cancelDownload(KEY_DOWNLOAD_APPLY_COUPON);
		}
	}

	private final Download<CreateTripResponse> mCouponDownload = new Download<CreateTripResponse>() {
		@Override
		public CreateTripResponse doDownload() {
			ExpediaServices services = new ExpediaServices(getActivity());
			BackgroundDownloader.getInstance().addDownloadListener(KEY_DOWNLOAD_APPLY_COUPON, services);
			return services.applyCoupon(mCouponCode, Db.getHotelSearch().getSearchParams(), Db
					.getHotelSearch().getSelectedProperty());
		}
	};

	private final OnDownloadComplete<CreateTripResponse> mCouponCallback = new OnDownloadComplete<CreateTripResponse>() {
		@Override
		public void onDownload(CreateTripResponse response) {
			mApplyCouponInitiated = false;
			// Don't execute if we were killed before finishing
			if (!isAdded()) {
				return;
			}

			if (mCouponDownloadStatusListener != null) {
				if (response == null) {
					Log.w("Failed to apply coupon code (null response): " + mCouponCode);

					DialogFragment df = SimpleDialogFragment.newInstance(null, getString(R.string.coupon_error_no_code));
					df.show(getChildFragmentManager(), "couponError");

					mCouponDownloadStatusListener.onFinishHandleWalletError();
				}
				else if (response.hasErrors()) {
					Log.w("Failed to apply coupon code (server errors): " + mCouponCode);

					DialogFragment df = SimpleDialogFragment.newInstance(null, getString(R.string.coupon_error_no_code));
					df.show(getChildFragmentManager(), "couponError");

					mCouponDownloadStatusListener.onFinishHandleWalletError();
				}
				else {
					Log.i("Applied coupon code: " + mCouponCode);

					Db.getHotelSearch().setCouponApplied(true);
					Db.getHotelSearch().setCreateTripResponse(response);

					OmnitureTracking.trackHotelCouponApplied(getActivity(), mCouponCode);
				}

				// Regardless of what happened, let's refresh the data
				mCouponDownloadStatusListener.onPostApply(response.getNewRate());
			}
			else {
				Log.w("Coupon download completed. Please add a CouponDownloadListener to get handle for all the onDownload finish methods.");
			}
		}
	};

	/**
	 * Coupon download response status listener.
	 */
	public interface CouponDownloadStatusListener {
		public void onFinishHandleWalletError();

		public void onPostApply(Rate rate);

		public void onPostRemove(Rate rate);

		public void onCouponCancel();
	}

	public void addCouponDownloadStatusListener(CouponDownloadStatusListener listener) {
		mCouponDownloadStatusListener = listener;
	}
}
