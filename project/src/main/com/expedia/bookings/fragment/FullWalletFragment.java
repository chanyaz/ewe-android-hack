package com.expedia.bookings.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;

import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Response;
import com.expedia.bookings.utils.WalletUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.wallet.FullWallet;
import com.google.android.gms.wallet.FullWalletRequest;
import com.google.android.gms.wallet.MaskedWallet;
import com.google.android.gms.wallet.NotifyTransactionStatusRequest;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import com.mobiata.android.Log;

/**
 * This is a version of WalletFragment that is designed for the final load of
 * a FullWallet.  For use when intent to book is confirmed.
 */
public abstract class FullWalletFragment extends WalletFragment {

	private static final String INSTANCE_FULL_WALLET = "INSTANCE_FULL_WALLET";

	// Once we have a FullWallet, we should assume we've started booking
	private FullWallet mFullWallet;

	// Abstractions

	protected abstract FullWalletRequest getFullWalletRequest();

	protected abstract String getGoogleWalletTransactionId();

	protected abstract void onFullWalletLoaded(FullWallet wallet);

	// Useful methods

	// Call this when you are sure that you want to book via Google Wallet
	protected void confirmBookingWithGoogleWallet() {
		if (!isGoogleWalletEnabled()) {
			displayGoogleWalletUnavailableToast();
			handleUnrecoverableGoogleWalletError(WalletConstants.ERROR_CODE_UNKNOWN);
		}
		else if (mFullWallet != null) {
			onFullWalletLoaded(mFullWallet);
		}
		else if (mWalletClient != null && mWalletClient.isConnected()) {
			requestFullWallet();
		}
		else {
			mHandleFullWalletWhenReady = true;
		}
	}

	protected void notifyWalletTransactionStatus(Response response) {
		// We assume that if there is no full wallet, we didn't book via Wallet
		if (mFullWallet != null) {
			int status = WalletUtils.getStatus(response);
			if (status != 0) {
				NotifyTransactionStatusRequest.Builder notifyBuilder = NotifyTransactionStatusRequest.newBuilder();
				notifyBuilder.setGoogleTransactionId(mFullWallet.getGoogleTransactionId());
				notifyBuilder.setStatus(status);

				if (mWalletClient.isConnected()) {
					Wallet.notifyTransactionStatus(mWalletClient, notifyBuilder.build());
				}
				else {
					Log.w("Somehow Google Wallet disconnected before we could notify them of transaction status!");
				}
			}
		}
	}

	private void requestFullWallet() {
		Log.i(WalletUtils.TAG, "Attempting to retrieve full wallet for booking...");

		String googleWalletTransactionId = getGoogleWalletTransactionId();

		FullWalletRequest fwRequest;
		if (WalletUtils.tryToCreateCvvChallenge(getActivity())) {
			fwRequest = WalletUtils.buildCvvChallengeRequest(googleWalletTransactionId);
		}
		else {
			fwRequest = getFullWalletRequest();
		}

		// Load the full wallet
		Wallet.loadFullWallet(mWalletClient, fwRequest, REQUEST_CODE_RESOLVE_LOAD_FULL_WALLET);
	}

	private void onFullWalletReceived(FullWallet wallet) {
		mFullWallet = wallet;

		// Clear out the Wallet data from before, now that we have
		// a full wallet (which invalidates previous data)
		Db.setMaskedWallet(null);

		onFullWalletLoaded(wallet);
	}

	// Lifecycle

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mFullWallet = savedInstanceState.getParcelable(INSTANCE_FULL_WALLET);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putParcelable(INSTANCE_FULL_WALLET, mFullWallet);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		int errorCode = -1;
		if (data != null) {
			errorCode = data.getIntExtra(WalletConstants.EXTRA_ERROR_CODE, -1);
		}

		switch (requestCode) {
		case REQUEST_CODE_RESOLVE_ERR:
			if (resultCode == Activity.RESULT_OK) {
				mWalletClient.connect();
			}
			else {
				handleUnrecoverableGoogleWalletError(errorCode);
			}
			break;
		case REQUEST_CODE_RESOLVE_LOAD_FULL_WALLET:
			switch (resultCode) {
			case Activity.RESULT_OK:
				if (data.hasExtra(WalletConstants.EXTRA_FULL_WALLET)) {
					onFullWalletReceived((FullWallet) data.getParcelableExtra(WalletConstants.EXTRA_FULL_WALLET));
				}
				else if (data.hasExtra(WalletConstants.EXTRA_MASKED_WALLET)) {
					Db.setMaskedWallet((MaskedWallet) data.getParcelableExtra(WalletConstants.EXTRA_MASKED_WALLET));
					requestFullWallet();
				}
				break;
			case Activity.RESULT_CANCELED:
				Log.w("Full wallet request: received RESULT_CANCELED, quitting out of activity");
				getActivity().finish();
				break;
			default:
				handleError(errorCode);
				break;
			}
			break;
		}
	}

	// ConnectionCallbacks

	@Override
	public void onConnected(Bundle connectionHint) {
		super.onConnected(connectionHint);

		if (mHandleFullWalletWhenReady && mFullWallet == null) {
			requestFullWallet();
		}
	}

	@Override
	public void onFullWalletLoaded(ConnectionResult status, FullWallet wallet) {
		super.onFullWalletLoaded(status, wallet);

		if (status.isSuccess()) {
			// User has pre-authorized the app
			onFullWalletReceived(wallet);
		}
		else if (status.hasResolution()) {
			try {
				status.startResolutionForResult(getActivity(), REQUEST_CODE_RESOLVE_LOAD_FULL_WALLET);
			}
			catch (SendIntentException e) {
				// Retry loading the full wallet
				mProgressDialog.show();
				mHandleFullWalletWhenReady = true;
				requestFullWallet();
			}
		}
		else {
			Log.e("FullWalletFragment.onFullWalletLoaded() - !status.isSuccess() && !status.hasResolution(). Setting ERROR_CODE_UNKNOWN");
			handleError(WalletConstants.ERROR_CODE_UNKNOWN);
		}
	}
}
