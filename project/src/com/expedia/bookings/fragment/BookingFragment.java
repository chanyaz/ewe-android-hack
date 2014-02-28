package com.expedia.bookings.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Response;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.fragment.HotelBookingFragment.HotelBookingState;
import com.expedia.bookings.utils.WalletUtils;
import com.google.android.gms.wallet.FullWallet;
import com.google.android.gms.wallet.MaskedWallet;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.util.Ui;

public abstract class BookingFragment<T extends Response> extends FullWalletFragment {

	private BookingFragmentListener mListener;

	private String mDownloadKey;

	// Sometimes we want to display dialogs but can't yet; in that case, defer until onResume()
	private boolean mCanModifyFragmentStack;

	private boolean mDoBookingOnResume;

	// If we need to defer handling till later
	private int mGoogleWalletErrorCode;

	private HotelBookingFragment mHotelBookingFragment;

	//////////////////////////////////////////////////////////////////////////
	// Abstractions/overrideables related to only booking

	public abstract String getBookingDownloadKey();

	public abstract Download<T> getBookingDownload();

	public abstract Class<T> getBookingResponseClass();

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Ensure a consistent download key; only grab it once
		mDownloadKey = getBookingDownloadKey();

		mHotelBookingFragment = com.expedia.bookings.utils.Ui.findSupportFragment(this, HotelBookingFragment.TAG);

		if (mHotelBookingFragment == null) {
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			mHotelBookingFragment = new HotelBookingFragment();
			ft.add(mHotelBookingFragment, HotelBookingFragment.TAG);
			ft.commit();
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mListener = Ui.findFragmentListener(this, BookingFragmentListener.class, false);
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
			mHotelBookingFragment.startDownload(HotelBookingState.CHECKOUT);
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

			bd.startDownload(mDownloadKey, getBookingDownload(), mCallback);
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
			response = getBookingResponseClass().newInstance();
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
