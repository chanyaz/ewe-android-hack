package com.expedia.bookings.fragment;

import android.app.Activity;
import android.content.Intent;

import com.expedia.bookings.data.Db;
import com.expedia.bookings.utils.WalletUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.wallet.MaskedWallet;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;

/**
 * This is a version of WalletFragment that is designed for changing the MaskedWallet.
 */
public abstract class ChangeWalletFragment extends WalletFragment {

	// Abstractions

	protected abstract void onMaskedWalletChanged(MaskedWallet maskedWallet);

	protected abstract void onCriticalWalletError();

	// Useful methods

	public void changeMaskedWallet() {
		if (!isGoogleWalletEnabled()) {
			displayGoogleWalletUnavailableToast();
		}
		else if (mConnectionResult != null) {
			resolveUnsuccessfulConnectionResult();
		}
		else {
			MaskedWallet maskedWallet = Db.getMaskedWallet();
			Wallet.changeMaskedWallet(mWalletClient, maskedWallet.getGoogleTransactionId(), maskedWallet.getMerchantTransactionId(), REQUEST_CODE_RESOLVE_CHANGE_MASKED_WALLET);
		}
	}

	// Lifecycle

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		mProgressDialog.hide();

		// Retrieve the error code, if available
		int errorCode = -1;
		if (data != null) {
			errorCode = data.getIntExtra(WalletConstants.EXTRA_ERROR_CODE, -1);
		}

		switch (requestCode) {
		case REQUEST_CODE_RESOLVE_ERR:
			mWalletClient.connect();
			break;
		case REQUEST_CODE_RESOLVE_CHANGE_MASKED_WALLET:
			switch (resultCode) {
			case Activity.RESULT_OK:
				MaskedWallet maskedWallet = data.getParcelableExtra(WalletConstants.EXTRA_MASKED_WALLET);
				Db.setMaskedWallet(maskedWallet);
				WalletUtils.bindWalletToBillingInfo(maskedWallet, Db.getWorkingBillingInfoManager()
						.getWorkingBillingInfo());
				onMaskedWalletChanged(maskedWallet);
				break;
			case Activity.RESULT_CANCELED:
				// Who cares if they canceled?  Just stay as before
				// No action needed, the user did not change their payment method
				break;
			default:
				handleError(errorCode);
			}
			break;
		}
	}

	// WalletFragment

	@Override
	protected void handleError(int errorCode) {
		super.handleError(errorCode);

		// If we get an error trying to change the wallet, kick the user back
		// to the previous page (so they can click the "buy with google wallet"
		// button again if they want to use it) and unbind Google Wallet.
		displayGoogleWalletUnavailableToast();

		WalletUtils.unbindAllWalletDataFromBillingInfo(Db.getWorkingBillingInfoManager().getWorkingBillingInfo());

		onCriticalWalletError();
	}

	// OnMaskedWalletLoadedListener

	@Override
	public void onMaskedWalletLoaded(ConnectionResult status, MaskedWallet wallet) {
		super.onMaskedWalletLoaded(status, wallet);

		mConnectionResult = status;
		mRequestCode = REQUEST_CODE_RESOLVE_CHANGE_MASKED_WALLET;

		// This callback is the result of a call to changeMaskedWallet(), so the result should
		// never be isSuccess() because changeMaskedWallet() should never return a MaskedWallet
		if (status.hasResolution()) {
			mProgressDialog.dismiss();
			resolveUnsuccessfulConnectionResult();
		}
		else {
			// This should never happen, but who knows!
			handleUnrecoverableGoogleWalletError(status.getErrorCode());
		}
	}
}
