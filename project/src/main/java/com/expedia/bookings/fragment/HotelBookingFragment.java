package com.expedia.bookings.fragment;

import java.math.BigDecimal;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.AirAttach;
import com.expedia.bookings.data.CreateTripResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelAvailability;
import com.expedia.bookings.data.HotelBookingResponse;
import com.expedia.bookings.data.HotelProductResponse;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.RateBreakdown;
import com.expedia.bookings.data.Response;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.TripBucketItemHotel;
import com.expedia.bookings.dialog.HotelErrorDialog;
import com.expedia.bookings.dialog.HotelPriceChangeDialog;
import com.expedia.bookings.fragment.RetryErrorDialogFragment.RetryErrorDialogFragmentListener;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.Ui;
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
public class HotelBookingFragment extends BookingFragment<HotelBookingResponse> implements RetryErrorDialogFragmentListener {

	public static final String TAG = HotelBookingFragment.class.toString();

	public static final String KEY_DOWNLOAD_BOOKING = "com.expedia.bookings.hotel.checkout";
	public static final String KEY_DOWNLOAD_HOTEL_PRODUCT_RESPONSE = "KEY_DOWNLOAD_HOTEL_PRODUCT_RESPONSE";
	public static final String KEY_DOWNLOAD_CREATE_TRIP = "KEY_DOWNLOAD_HOTEL_CREATE_TRIP";
	public static final String KEY_DOWNLOAD_APPLY_COUPON = "KEY_DOWNLOAD_HOTEL_APPLY_COUPON";
	public static final String KEY_DOWNLOAD_REMOVE_COUPON = "KEY_DOWNLOAD_REMOVE_COUPON";

	private static final String RETRY_CREATE_TRIP_DIALOG = "RETRY_HOTELCREATE_TRIP_DIALOG";
	private static final String HOTEL_OFFER_ERROR_DIALOG = "HOTEL_OFFER_ERROR_DIALOG";
	private static final String HOTEL_PRODUCT_RATEUP_DIALOG = "HOTEL_PRODUCT_RATEUP_DIALOG";

	private static final String INSTANCE_HOTELBOOKING_STATE = "INSTANCE_HOTELBOOKING_STATE";

	private String mCouponCode;

	private HotelBookingState mState = HotelBookingState.DEFAULT;

	public enum HotelBookingState {
		DEFAULT,
		HOTEL_PRODUCT,
		CREATE_TRIP,
		COUPON_APPLY,
		COUPON_REMOVE,
		COUPON_REPLACE,
		CHECKOUT
	}

	public static HotelBookingFragment newInstance() {
		return new HotelBookingFragment();
	}

	// BookingFragment

	@Override
	public String getBookingDownloadKey() {
		return KEY_DOWNLOAD_BOOKING;
	}

	@Override
	public void clearBookingResponse() {
		Db.getTripBucket().getHotel().setBookingResponse(null);
	}

	@Override
	public Download<HotelBookingResponse> getBookingDownload() {
		return new Download<HotelBookingResponse>() {
			@Override
			public HotelBookingResponse doDownload() {
				ExpediaServices services = new ExpediaServices(getActivity());
				TripBucketItemHotel hotel = Db.getTripBucket().getHotel();

				String userId = null;
				String tripId = null;
				String tealeafId = null;
				Long tuid = null;

				if (hotel.getCreateTripResponse() != null) {
					tripId = hotel.getCreateTripResponse().getTripId();
					userId = hotel.getCreateTripResponse().getUserId();
					tealeafId = hotel.getCreateTripResponse().getTealeafId();
				}

				if (Db.getUser() != null) {
					tuid = Db.getUser().getPrimaryTraveler().getTuid();
				}

				Rate selectedRate = hotel.getRate();
				HotelBookingResponse response = services.reservation(hotel.getHotelSearchParams(), hotel.getProperty(),
					selectedRate, Db.getBillingInfo(), tripId, userId, tuid, tealeafId);

				notifyWalletTransactionStatus(response);

				return response;
			}
		};
	}

	@Override
	public Class<HotelBookingResponse> getBookingResponseClass() {
		return HotelBookingResponse.class;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			mState = (HotelBookingState) savedInstanceState.getSerializable(INSTANCE_HOTELBOOKING_STATE);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(INSTANCE_HOTELBOOKING_STATE, mState);
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
		if (isDownloadingHotelProduct()) {
			bd.registerDownloadCallback(KEY_DOWNLOAD_HOTEL_PRODUCT_RESPONSE, mHotelProductCallback);
		}
		if (isDownloadingCreateTrip()) {
			bd.registerDownloadCallback(KEY_DOWNLOAD_CREATE_TRIP, mCreateTripCallback);
		}
		if (isDownloadingCoupon()) {
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

	@Override
	public void doBookingPrep() {
		if (Db.getTripBucket().getHotel().getCreateTripResponse() == null) {
			throw new RuntimeException("Can't book without a create trip response");
		}

		startBookingDownload();
	}

	public void startDownload(HotelBookingState state) {
		Log.v("HotelBookingFragment startDowload requested for : " + state);
		mState = state;
		switch (state) {
		case HOTEL_PRODUCT:
			startHotelProductDownload();
			break;
		case CREATE_TRIP:
			startCreateTripDownload();
			break;
		case COUPON_APPLY:
			if (mCouponCode == null) {
				throw new RuntimeException("Coupon Code is null or not set. Please call startDownload(HotelBookingState, String) instead and pass the coupon code.");
			}
			else {
				applyCoupon(mCouponCode);
			}
			break;
		case COUPON_REMOVE:
			clearCoupon();
			break;
		case COUPON_REPLACE:
			if (mCouponCode == null) {
				throw new RuntimeException("Coupon Code is null or not set. Please call startDownload(HotelBookingState, String) instead and pass the coupon code.");
			}
			startReplaceCouponDownloadProcess(mCouponCode);
			break;
		case CHECKOUT:
			Events.post(new Events.BookingDownloadStarted());
			if (Db.getTripBucket().getHotel().getCreateTripResponse() == null) {
				throw new RuntimeException("Can't do booking without create trip response");
			}
			else {
				doBooking();
			}
			break;
		default:
			break;
		}
	}

	public void startDownload(HotelBookingState state, String couponCode) {
		if (state == HotelBookingState.COUPON_APPLY) {
			mCouponCode = couponCode;
			startDownload(HotelBookingState.COUPON_APPLY);
		}
		else if (state == HotelBookingState.COUPON_REPLACE) {
			mCouponCode = couponCode;
			startDownload(HotelBookingState.COUPON_REPLACE);
		}
		else {
			startDownload(state);
		}
	}

	public void cancelDownload(HotelBookingState state) {
		Log.v("HotelBookingFragment cancelDownload requested for : " + state);
		mState = state;
		switch (state) {
		case COUPON_APPLY:
			cancelCreateTripDownload();
			cancelApplyCouponDownloader();
			Events.post(new Events.CouponDownloadCancel());
			break;
		case COUPON_REMOVE:
			cancelCreateTripDownload();
			cancelRemoveCouponDownloader();
			break;
		default:
			break;
		}
	}

	/////////////////////////////////////////////////////
	///// Hotel Product service related

	private void startHotelProductDownload() {
		// Let's cancel download if already running.
		cancelHotelProductDownload();
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.startDownload(KEY_DOWNLOAD_HOTEL_PRODUCT_RESPONSE, mHotelProductDownload, mHotelProductCallback);
	}

	private void cancelHotelProductDownload() {
		if (isDownloadingHotelProduct()) {
			BackgroundDownloader.getInstance().cancelDownload(KEY_DOWNLOAD_HOTEL_PRODUCT_RESPONSE);
		}
	}

	public boolean isDownloadingHotelProduct() {
		return BackgroundDownloader.getInstance().isDownloading(KEY_DOWNLOAD_HOTEL_PRODUCT_RESPONSE);
	}

	private final Download<HotelProductResponse> mHotelProductDownload = new Download<HotelProductResponse>() {
		@Override
		public HotelProductResponse doDownload() {
			ExpediaServices services = new ExpediaServices(getActivity());
			BackgroundDownloader.getInstance().addDownloadListener(KEY_DOWNLOAD_HOTEL_PRODUCT_RESPONSE, services);

			Rate selectedRate = Db.getTripBucket().getHotel().getRate();
			HotelSearchParams params = Db.getTripBucket().getHotel().getHotelSearchParams();
			Property property = Db.getTripBucket().getHotel().getProperty();

			return services.hotelProduct(params, property, selectedRate);
		}
	};

	private final OnDownloadComplete<HotelProductResponse> mHotelProductCallback = new OnDownloadComplete<HotelProductResponse>() {
		@Override
		public void onDownload(HotelProductResponse response) {
			if (response == null || response.hasErrors()) {
				handleHotelProductError(response);
			}
			else {
				Rate selectedRate = Db.getTripBucket().getHotel().getRate();
				Rate newRate = response.getRate();

				if (TextUtils.equals(selectedRate.getRateKey(), response.getOriginalProductKey())) {
					onHotelProductSuccess(response, selectedRate, newRate);
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
	 * If we want to add more UI functionality, then Subscribe to Events.HotelProductDownloadSuccess class.
	 */
	private void onHotelProductSuccess(HotelProductResponse response, Rate selectedRate, Rate newRate) {
		Db.getTripBucket().getHotel().setHotelProductResponse(response);
		if (!AndroidUtils.isRelease(getActivity())) {
			String priceChangeString = SettingUtils.get(getActivity(),
				getString(R.string.preference_fake_hotel_price_change),
				getString(R.string.preference_fake_price_change_default));
			BigDecimal priceChange = new BigDecimal(priceChangeString);

			//Update total price
			newRate.getDisplayTotalPrice().add(priceChange);
			newRate.getTotalAmountAfterTax().add(priceChange);

			//Update all nights total and per/night totals
			newRate.getNightlyRateTotal().add(priceChange);
			if (newRate.getRateBreakdownList() != null) {
				BigDecimal numberOfNights = new BigDecimal(newRate.getRateBreakdownList().size());
				BigDecimal perNightChange = priceChange.divide(numberOfNights, BigDecimal.ROUND_UP);
				for (RateBreakdown breakdown : newRate.getRateBreakdownList()) {
					breakdown.getAmount().add(perNightChange);
				}
			}

		}

		int priceChange = selectedRate.compareForPriceChange(newRate);
		if (priceChange != 0) {
			Db.getTripBucket().getHotel().setNewRate(newRate);

			// Let's pop a dialog for phone and post Events.TripPriceChange event for tablet.
			if (!ExpediaBookingApp.useTabletInterface(getActivity())) {
				// The rules for what we show between rooms and rates and checkout as the
				// "total" differ. Thus, we must defer to what the new rate is trying to display.
				Money oldPrice = newRate.getCheckoutPriceType() == Rate.CheckoutPriceType.TOTAL_WITH_MANDATORY_FEES ?
					selectedRate.getTotalPriceWithMandatoryFees() : selectedRate.getDisplayTotalPrice();
				boolean isPriceHigher = priceChange < 0;
				HotelPriceChangeDialog dialog = HotelPriceChangeDialog.newInstance(isPriceHigher,
					oldPrice, newRate.getDisplayTotalPrice());
				dialog.show(getChildFragmentManager(), HOTEL_PRODUCT_RATEUP_DIALOG);
			}
			else {
				Events.post(new Events.HotelProductRateUp(newRate));
			}
		}
		else if (newRate.showResortFeesMessaging()) {
			Db.getTripBucket().getHotel().setNewRate(newRate);
		}

		HotelAvailability availability = Db.getTripBucket().getHotel().getHotelAvailability();
		availability.updateFrom(selectedRate.getRateKey(), response);
		availability.setSelectedRate(newRate);

		Events.post(new Events.HotelProductDownloadSuccess(response));
	}

	private void handleHotelProductError(HotelProductResponse response) {
		if (response == null) {
			Events.post(new Events.CreateTripDownloadError(null));
			showRetryErrorDialog();
		}
		else {
			HotelErrorDialog dialog = HotelErrorDialog.newInstance();
			int messageId = Ui.obtainThemeResID(getActivity(), R.attr.skin_errorHotelOffersHotelServiceFailureString);

			if (response.getErrors() != null) {
				for (ServerError error : response.getErrors()) {
					if (error.getErrorCode() == ServerError.ErrorCode.HOTEL_ROOM_UNAVAILABLE) {
						messageId = R.string.e3_error_hotel_offers_hotel_room_unavailable;
						HotelAvailability availability;

						// Cleanup trip bucket
						availability = Db.getTripBucket().getHotel().getHotelAvailability();
						availability.removeRate(response.getOriginalProductKey());

						// Cleanup search data
						String id = Db.getTripBucket().getHotel().getProperty().getPropertyId();
						availability = Db.getHotelSearch().getAvailability(id);
						if (availability != null) {
							availability.removeRate(response.getOriginalProductKey());
						}

						// Post event for tablets to show the BookingUnavailableFragment
						Events.post(new Events.BookingUnavailable(LineOfBusiness.HOTELS));
					}
					// Handling product key expiration.
					else if (error.getErrorCode() == ServerError.ErrorCode.INVALID_INPUT && error.getExtra("field").equals("productKey")) {
						Events.post(new Events.TripItemExpired(LineOfBusiness.HOTELS));
					}
				}
			}

			// Let's show the error dialog only for phones.
			if (!ExpediaBookingApp.useTabletInterface(getActivity())) {
				dialog.setMessage(messageId);
				dialog.show(getFragmentManager(), HOTEL_OFFER_ERROR_DIALOG);
			}
		}
	}

	/////////////////////////////////////////////////////
	///// Create Trip service related

	private void startCreateTripDownload() {
		// Let's cancel download if already running.
		cancelCreateTripDownload();
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.startDownload(KEY_DOWNLOAD_CREATE_TRIP, mCreateTripDownload, mCreateTripCallback);
	}

	private void cancelCreateTripDownload() {
		if (isDownloadingCreateTrip()) {
			BackgroundDownloader.getInstance().cancelDownload(KEY_DOWNLOAD_CREATE_TRIP);
		}
	}

	public boolean isDownloadingCreateTrip() {
		return BackgroundDownloader.getInstance().isDownloading(KEY_DOWNLOAD_CREATE_TRIP);
	}

	private final Download<CreateTripResponse> mCreateTripDownload = new Download<CreateTripResponse>() {
		@Override
		public CreateTripResponse doDownload() {
			ExpediaServices services = new ExpediaServices(getActivity());
			BackgroundDownloader.getInstance().addDownloadListener(KEY_DOWNLOAD_CREATE_TRIP, services);

			HotelSearchParams params = Db.getTripBucket().getHotel().getHotelSearchParams();
			Property property = Db.getTripBucket().getHotel().getProperty();
			Rate rate = Db.getTripBucket().getHotel().getRate();

			boolean qualifyAirAttach = Db.getTripBucket() != null && Db.getTripBucket().isUserAirAttachQualified();

			return services.createTrip(params, property, rate, qualifyAirAttach);
		}
	};

	private final OnDownloadComplete<CreateTripResponse> mCreateTripCallback = new OnDownloadComplete<CreateTripResponse>() {
		@Override
		public void onDownload(CreateTripResponse response) {
			if (response == null || response.hasErrors()) {
				handleCreateTripError(response);
			}
			else {
				onCreateTripCallSuccess(response);
			}
		}

	};

	private void onCreateTripCallSuccess(CreateTripResponse response) {
		Db.getTripBucket().getHotel().setCreateTripResponse(response);
		Db.getTripBucket().getHotel().addValidPayments(response.getValidPayments());

		Rate originalRate = response.getNewRate();
		Rate airAttachRate = response.getAirAttachRate();
		if (airAttachRate != null && originalRate.compareForPriceChange(airAttachRate) != 0) {
			Db.getTripBucket().getHotel().setNewRate(originalRate);
			Db.getTripBucket().getHotel().setNewRate(airAttachRate);
			Events.post(new Events.HotelProductRateUp(airAttachRate));
		}

		switch (mState) {
		case COUPON_APPLY:
			startApplyCouponDownloader(mCouponCode);
			break;
		case COUPON_REMOVE:
			startRemoveCouponDownloader();
			OmnitureTracking.trackHotelCouponRemoved(getActivity());
			break;
		case CHECKOUT:
			doBooking();
			break;

		default:
			Events.post(new Events.CreateTripDownloadSuccess(response));
			break;
		}
	}

	// Error handling
	private void handleCreateTripError(CreateTripResponse response) {
		if (response == null) {
			Events.post(new Events.CreateTripDownloadError(LineOfBusiness.HOTELS, null));
		}
		else {
			ServerError firstError = response.getErrors().get(0);
			Events.post(new Events.CreateTripDownloadError(LineOfBusiness.HOTELS, firstError));
		}
	}

	private void showRetryErrorDialog() {
		DialogFragment df = new RetryErrorDialogFragment();
		df.show(getChildFragmentManager(), RETRY_CREATE_TRIP_DIALOG);
	}

	///////////// Retry CreateTrip call dialog handlers
	@Override
	public void onRetryError() {
		//Restart calls again depending on the status

		switch (mState) {
		case COUPON_APPLY:
			applyCoupon(mCouponCode);
			break;
		case COUPON_REMOVE:
			clearCoupon();
			break;
		case CHECKOUT:
			startCreateTripDownload();
			break;

		default:
			Events.post(new Events.CreateTripDownloadRetry());
			break;
		}

	}

	@Override
	public void onCancelError() {
		//On cancelling the retry dialog do appropriately
		if (mState == HotelBookingState.COUPON_APPLY || mState == HotelBookingState.COUPON_REMOVE) {
			cancelDownload(mState);
			Events.post(new Events.CouponDownloadCancel());
		}
		else {
			Events.post(new Events.CreateTripDownloadRetryCancel());
		}
	}

	/////////////////////////////////////////////////////
	///// Coupon service related


	/*******************************
	 * Applying coupon
	 *******************************/

	/**
	 * This method initiates the coupon application process during checkout.
	 * This fragment handles all the callbacks, errors and retries.
	 * Add {@link CouponDownloadStatusListener} to listen to coupon download status updates.
	 * As of now coupons are applied for Hotels only.
	 * @param couponCode
	 */
	private void applyCoupon(String couponCode) {
		mCouponCode = couponCode;
		if (Db.getTripBucket().getHotel().getCreateTripResponse() == null) {
			startCreateTripDownload();
		}
		else {
			startApplyCouponDownloader(mCouponCode);
		}
	}

	/**
	 * This method initiates the coupon removal process during checkout.
	 * Add {@link CouponDownloadStatusListener} to listen to coupon download status updates.
	 */

	private void startApplyCouponDownloader(String couponCode) {
		mCouponCode = couponCode;
		// Let's cancel download if already running.
		cancelHotelProductDownload();
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.startDownload(KEY_DOWNLOAD_APPLY_COUPON, mCouponDownload, mCouponCallback);
	}

	private void cancelApplyCouponDownloader() {
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (isDownloadingCoupon()) {
			bd.cancelDownload(KEY_DOWNLOAD_APPLY_COUPON);
		}
	}

	public boolean isDownloadingCoupon() {
		return BackgroundDownloader.getInstance().isDownloading(KEY_DOWNLOAD_APPLY_COUPON);
	}

	private final Download<CreateTripResponse> mCouponDownload = new Download<CreateTripResponse>() {
		@Override
		public CreateTripResponse doDownload() {
			ExpediaServices services = new ExpediaServices(getActivity());
			BackgroundDownloader.getInstance().addDownloadListener(KEY_DOWNLOAD_APPLY_COUPON, services);
			return services.applyCoupon(mCouponCode, Db.getTripBucket().getHotel());
		}
	};

	private final OnDownloadComplete<CreateTripResponse> mCouponCallback = new OnDownloadComplete<CreateTripResponse>() {
		@Override
		public void onDownload(CreateTripResponse response) {
			// Don't execute if we were killed before finishing
			if (!isAdded()) {
				return;
			}
			if (response == null || response.hasErrors()) {
				handleCouponError(response);
			}
			else {
				Log.i("Applied coupon code: " + mCouponCode);
				if (WalletUtils.offerGoogleWalletCoupon(getActivity()) && WalletUtils.isCouponWalletCoupon(mCouponCode)) {
					Db.getTripBucket().getHotel().setIsCouponGoogleWallet(true);
				}
				Db.getTripBucket().getHotel().setIsCouponApplied(true);
				Db.getTripBucket().getHotel().setCreateTripResponse(response);
				Db.getTripBucket().getHotel().setCouponRate(response.getNewRate());
				Db.saveTripBucket(getActivity());

				OmnitureTracking.trackHotelCouponApplied(getActivity(), mCouponCode);
				Events.post(new Events.CouponApplyDownloadSuccess(response.getNewRate()));
			}
		}
	};

	/*******************************
	 * Removing coupon
	 *******************************/

	private void clearCoupon() {
		cancelApplyCouponDownloader();
		startRemoveCouponDownloader();
	}

	private void startRemoveCouponDownloader() {
		cancelHotelProductDownload();
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.startDownload(KEY_DOWNLOAD_REMOVE_COUPON, mRemoveCouponDownload, mRemoveCouponCallback);
	}

	private void cancelRemoveCouponDownloader() {
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (isRemovingCoupon()) {
			bd.cancelDownload(KEY_DOWNLOAD_REMOVE_COUPON);
		}
	}

	public boolean isRemovingCoupon() {
		return BackgroundDownloader.getInstance().isDownloading(KEY_DOWNLOAD_REMOVE_COUPON);
	}

	private final Download<CreateTripResponse> mRemoveCouponDownload = new Download<CreateTripResponse>() {
		@Override
		public CreateTripResponse doDownload() {
			ExpediaServices services = new ExpediaServices(getActivity());
			BackgroundDownloader.getInstance().addDownloadListener(KEY_DOWNLOAD_REMOVE_COUPON, services);
			return services.removeCoupon(Db.getTripBucket().getHotel());
		}
	};

	private final OnDownloadComplete<CreateTripResponse> mRemoveCouponCallback = new OnDownloadComplete<CreateTripResponse>() {
		@Override
		public void onDownload(CreateTripResponse response) {
			if (!isAdded()) {
				return;
			}
			else if (response == null || response.hasErrors()) {
				handleCouponError(response);
			}
			else {
				Log.i("Removed coupon from product");
				Db.getTripBucket().getHotel().setIsCouponApplied(false);
				Db.getTripBucket().getHotel().setCreateTripResponse(response);
				Db.getTripBucket().getHotel().setCouponRate(null);
				Db.saveTripBucket(getActivity());
				OmnitureTracking.trackHotelCouponRemoved(getActivity());
				Events.post(new Events.CouponRemoveDownloadSuccess(response.getNewRate()));
			}
		}
	};

	/*******************************
	 * Replacing coupon
	 * Convenience method for setting an apply coupon call
	 * as the callback to a remove coupon call. Only do this
	 * if you really need to.
	 *******************************/

	private void startReplaceCouponDownloadProcess(String couponCode) {
		mCouponCode = couponCode;
		cancelHotelProductDownload();
		cancelRemoveCouponDownloader();
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.startDownload(KEY_DOWNLOAD_REMOVE_COUPON, mRemoveCouponDownload, mApplyCouponAfterRemovalCallback);
	}


	private final OnDownloadComplete<CreateTripResponse> mApplyCouponAfterRemovalCallback = new OnDownloadComplete<CreateTripResponse>() {
		@Override
		public void onDownload(CreateTripResponse response) {
			if (response == null || response.hasErrors()) {
				handleCouponError(response);
			}
			else {
				applyCoupon(mCouponCode);
			}
		}
	};

	/*
	 * Coupon Error Handling
	 */

	private void handleCouponError(CreateTripResponse response) {
		Log.w("Failed to apply coupon code : " + mCouponCode);

		String errorMessage;
		if (response == null) {
			errorMessage = getString(R.string.coupon_error_service_timeout);
		}
		else {
			ServerError serverError = response.getErrors().get(0);
			errorMessage = serverError.getCouponErrorMessage(getActivity());
			OmnitureTracking.trackHotelCouponFail(getActivity(), mCouponCode, serverError.getCouponErrorType());
		}

		DialogFragment df = SimpleDialogFragment.newInstance(null, errorMessage);
		df.show(getChildFragmentManager(), "couponError");

		Events.post(new Events.CouponDownloadError());
	}

	@Override
	public void handleBookingErrorResponse(Response response) {
		super.handleBookingErrorResponse(response, LineOfBusiness.HOTELS);
	}

}
