package com.expedia.bookings.fragment;

import android.os.Bundle;

import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Response;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.WalletUtils;
import com.google.android.gms.wallet.FullWallet;
import com.google.android.gms.wallet.MaskedWallet;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;

public abstract class BookingFragment<T extends Response> extends FullWalletFragment {

	private String mDownloadKey;

	// Sometimes we want to display dialogs but can't yet; in that case, defer until onResume()
	private boolean mCanModifyFragmentStack;

	private boolean mDoBookingOnResume;

	// If we need to defer handling till later
	private int mGoogleWalletErrorCode;

	//////////////////////////////////////////////////////////////////////////
	// Abstractions/overrideables related to only booking

	public abstract String getBookingDownloadKey();

	public abstract Download<T> getBookingDownload();

	public abstract Class<T> getBookingResponseClass();

	// Use this method if we need to gather/prepare more information before calling booking download.
	public abstract void doBookingPrep();

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Ensure a consistent download key; only grab it once
		mDownloadKey = getBookingDownloadKey();

	}

	@Override
	public void onResume() {
		super.onResume();
		// Register on Otto bus
		Events.register(this);
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
		// UnRegister on Otto bus
		Events.unregister(this);
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
				startBookingProcess();
			}
		}

	}

	private void startBookingProcess() {
		// Post event to Otto bus
		Events.post(new Events.BookingDownloadStarted());
		if ((this instanceof HotelBookingFragment) && Db.getHotelSearch().getCreateTripResponse() == null) {
			doBookingPrep();
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
			Events.post(new Events.BookingDownloadResponse(results));
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

		startBookingProcess();
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
		Events.post(new Events.BookingDownloadResponse(response));
	}

}
