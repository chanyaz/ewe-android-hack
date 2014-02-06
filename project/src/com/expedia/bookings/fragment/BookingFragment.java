package com.expedia.bookings.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

import com.expedia.bookings.data.CreateTripResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Response;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.WalletUtils;
import com.google.android.gms.wallet.FullWallet;
import com.google.android.gms.wallet.MaskedWallet;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.util.Ui;

public abstract class BookingFragment<T extends Response> extends FullWalletFragment {

	private static final String RETRY_CREATETRIP_DIALOG = "RETRY_CREATETRIP_DIALOG";

	private BookingFragmentListener mListener;

	private String mDownloadKey;
	private static final String KEY_CREATE_TRIP = "KEY_CREATE_TRIP";

	// Sometimes we want to display dialogs but can't yet; in that case, defer until onResume()
	private boolean mCanModifyFragmentStack;

	private boolean mDoBookingOnResume;

	// If we need to defer handling till later
	private int mGoogleWalletErrorCode;

	//////////////////////////////////////////////////////////////////////////
	// Abstractions/overrideables

	public abstract String getDownloadKey();

	public abstract Download<T> getDownload();

	public abstract Class<T> getResponseClass();

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Ensure a consistent download key; only grab it once
		mDownloadKey = getDownloadKey();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mListener = Ui.findFragmentListener(this, BookingFragmentListener.class);
	}

	@Override
	public void onResume() {
		super.onResume();

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(mDownloadKey)) {
			bd.registerDownloadCallback(mDownloadKey, mCallback);
		}
		else if (mDoBookingOnResume) {
			doBooking();
		}

		mCanModifyFragmentStack = true;
		if (mGoogleWalletErrorCode != 0) {
			handleError(mGoogleWalletErrorCode);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		mCanModifyFragmentStack = false;
	}

	@Override
	public void onPause() {
		super.onPause();

		BackgroundDownloader.getInstance().unregisterDownloadCallback(mDownloadKey);

		// When we leave this Fragment, we want to unbind any data we might have gotten
		// from the FullWallet; otherwise the user can see/edit this data later.
		if (getActivity().isFinishing() && willBookViaGoogleWallet()) {
			WalletUtils.unbindFullWalletDataFromBillingInfo(Db.getBillingInfo());
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Booking download

	public boolean isBooking() {
		return BackgroundDownloader.getInstance().isDownloading(mDownloadKey);
	}

	public void doBooking() {
		if (willBookViaGoogleWallet()) {
			confirmBookingWithGoogleWallet();
		}
		else {
			if (!isAdded()) {
				mDoBookingOnResume = true;
			}
			else {
				startBooking();
			}
		}
	}

	/**
	 * Since both flight and hotel fragments use this and both have a different path to checkout,
	 * let's send them on their respective ways.
	 */
	private void startBooking() {
		if (this instanceof FlightBookingFragment) {
			mListener.onStartBooking();
			startBookingDownload();
		}
		else if (this instanceof HotelBookingFragment) {
			startHotelBooking();
		}
	}

	private void startHotelBooking() {
		mListener.onStartBooking();
		/*
		 *  CheckoutV2 requires us to do a create call before making the checkout call.
		 *  Check to see if create has been called before. If a coupon has been added/removed,
		 *  this call will already be made and a new tripId and productKey will be obtained.
		 *  In that case just start the checkout else call create.
		 */
		if (Db.getHotelSearch().getCreateTripResponse() == null) {
			BackgroundDownloader bd = BackgroundDownloader.getInstance();
			bd.startDownload(KEY_CREATE_TRIP, mCreateTripDownload, mCreateTripCallback);
		}
		else {
			startBookingDownload();
		}
	}

	private void startBookingDownload() {
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (!bd.isDownloading(mDownloadKey)) {
			// Clear current results (if any)
			Db.setBookingResponse(null);

			bd.startDownload(mDownloadKey, getDownload(), mCallback);
		}
	}

	private OnDownloadComplete<T> mCallback = new OnDownloadComplete<T>() {
		@Override
		public void onDownload(T results) {
			mListener.onBookingResponse(results);
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Google Wallet

	public boolean willBookViaGoogleWallet() {
		StoredCreditCard scc = Db.getBillingInfo().getStoredCard();
		return scc != null && scc.isGoogleWallet();
	}

	// FullWalletFragment

	@Override
	protected String getGoogleWalletTransactionId() {
		MaskedWallet maskedWallet = Db.getMaskedWallet();
		if (maskedWallet == null) {
			throw new RuntimeException("Tried to retrieve the full wallet without having a valid masked wallet first.");
		}
		return maskedWallet.getGoogleTransactionId();
	}

	@Override
	protected void onFullWalletLoaded(FullWallet wallet) {
		WalletUtils.bindWalletToBillingInfo(wallet, Db.getBillingInfo());

		startBooking();
	}

	private final Download<CreateTripResponse> mCreateTripDownload = new Download<CreateTripResponse>() {
		@Override
		public CreateTripResponse doDownload() {
			ExpediaServices services = new ExpediaServices(getActivity());
			BackgroundDownloader.getInstance().addDownloadListener(KEY_CREATE_TRIP, services);
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
				Db.getHotelSearch().setCreateTripResponse(response);
				startBookingDownload();
			}

		}
	};

	// Error handling

	private void handleCreateTripError(CreateTripResponse response) {
		ServerError firstError = response.getErrors().get(0);

		switch (firstError.getErrorCode()) {
		case TRIP_SERVICE_UNKNOWN_ERROR:
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
		df.show(((FragmentActivity) getActivity()).getSupportFragmentManager(), RETRY_CREATETRIP_DIALOG);
	}

	@Override
	protected void handleError(int errorCode) {
		super.handleError(errorCode);

		if (mCanModifyFragmentStack) {
			mGoogleWalletErrorCode = 0;
			simulateError(errorCode);
		}
		else {
			mGoogleWalletErrorCode = errorCode;
		}
	}

	private void simulateError(int errorCode) {
		T response;
		try {
			response = getResponseClass().newInstance();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

		ServerError error = new ServerError();
		error.setCode("GOOGLE_WALLET_ERROR");
		response.addError(error);
		mListener.onBookingResponse(response);
	}

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface BookingFragmentListener {
		public void onStartBooking();

		public void onBookingResponse(Response results);
	}
}
