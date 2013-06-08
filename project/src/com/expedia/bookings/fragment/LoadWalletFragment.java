package com.expedia.bookings.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.utils.WalletUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.wallet.MaskedWallet;
import com.google.android.gms.wallet.MaskedWalletRequest;
import com.google.android.gms.wallet.WalletConstants;
import com.mobiata.android.Log;

/**
 * This is a version of WalletFragment that is designed for the initial load of the MaskedWallet
 * (and has a "book with Google Wallet" button on the screen).
 */
public abstract class LoadWalletFragment extends WalletFragment {

	private static final String INSTANCE_CHECK_PRE_AUTH = "INSTANCE_CHECK_PRE_AUTH";
	private static final String INSTANCE_LOADED_MASKED_WALLET = "INSTANCE_LOADED_MASKED_WALLET";

	protected boolean mCheckPreAuth = true;
	protected boolean mCheckedPreAuth;
	protected boolean mIsUserPreAuthorized;

	// Whether we've previously loaded a masked wallet in this Fragment
	//
	// Should not be used to determine if a masked wallet is loaded - this
	// is actually used to determine if we should reload it (in case the
	// masked wallet was cleared for some reason).
	protected boolean mLoadedMaskedWallet;

	// Abstractions

	/**
	 * @return the estimated cost of the transaction
	 */
	protected abstract Money getEstimatedTotal();

	/**
	 * @return the flags used in WalletUtils.buildMaskedWalletRequest()
	 */
	protected abstract int getMaskedWalletBuilderFlags();

	/**
	 * This is called once we have a MaskedWallet loaded and in Db.
	 */
	protected abstract void onMaskedWalletFullyLoaded(boolean fromPreauth);

	/**
	 * This indicates that something has changed with the state of Google Wallet,
	 * so we should check to see if we need to change how any Views using it
	 * might be affected.
	 */
	protected abstract void updateWalletViewVisibilities();

	// Useful methods

	/**
	 * Call this when the user wants to buy with Google Wallet (e.g., presses the button)
	 */
	protected void buyWithGoogleWallet() {
		if (mGoogleWalletDisabled) {
			updateWalletViewVisibilities();
			displayGoogleWalletUnavailableToast();
		}
		else if (Db.getMaskedWallet() != null) {
			onMaskedWalletFullyLoaded(false);
		}
		else if (mConnectionResult != null) {
			resolveUnsuccessfulConnectionResult();
		}
		else {
			// Still waiting for the WalletClient to connect so wait for
			// mWalletClient.loadMaskedWallet() to be called from onConnected()
			mProgressDialog.show();
			mHandleMaskedWalletWhenReady = true;
		}
	}

	/**
	 * We want the WalletButton visible when:
	 *
	 * 1. Google wallet is enabled
	 * 2. The stored credit card is not a Google Wallet card
	 * @return
	 */
	protected boolean showWalletButton() {
		StoredCreditCard scc = Db.getBillingInfo().getStoredCard();
		boolean storedCardIsGoogleWallet = scc != null && scc.isGoogleWallet();
		return !mGoogleWalletDisabled && !storedCardIsGoogleWallet;
	}

	/**
	 * We consider Google Wallet loading if:
	 *
	 * 1. Google Wallet is enabled (otherwise it never loads)
	 * 2. The masked wallet is already loaded (at which point obviously it's good)
	 * 3. The WalletClient is not yet connected
	 * 4. We have not yet checked for pre-authorization
	 * 5. The user is pre-authorized but has no masked wallet yet
	 */
	protected boolean isWalletLoading() {
		MaskedWallet maskedWallet = Db.getMaskedWallet();

		return !mGoogleWalletDisabled
				&& maskedWallet == null
				&& (!mWalletClient.isConnected()
						|| !mCheckedPreAuth
						|| mIsUserPreAuthorized);
	}

	private MaskedWalletRequest buildMaskedWalletRequest() {
		return WalletUtils.buildMaskedWalletRequest(getActivity(), getEstimatedTotal(), getMaskedWalletBuilderFlags());
	}

	private void onMaskedWalletReceived(MaskedWallet wallet, boolean fromPreauth) {
		WalletUtils.logWallet(wallet);

		Db.setMaskedWallet(wallet);
		mLoadedMaskedWallet = true;

		onMaskedWalletFullyLoaded(fromPreauth);
	}

	// Lifecycle

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mCheckPreAuth = savedInstanceState.getBoolean(INSTANCE_CHECK_PRE_AUTH, true);
			mLoadedMaskedWallet = savedInstanceState.getBoolean(INSTANCE_LOADED_MASKED_WALLET, false);
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		// If the MaskedWallet used to exist but has since been cleared, we want
		// to reset the state of this fragment (and unbind all wallet data)
		if (mLoadedMaskedWallet && Db.getMaskedWallet() == null) {
			Log.i(WalletUtils.TAG, "Masked wallet *was* loaded, but is now null; resetting LoadWalletFragment");

			mCheckPreAuth = true;
			mCheckedPreAuth = false;
			mIsUserPreAuthorized = false;

			Db.clearGoogleWallet();
			WalletUtils.unbindAllWalletDataFromBillingInfo(Db.getBillingInfo());
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean(INSTANCE_CHECK_PRE_AUTH, mCheckPreAuth);
		outState.putBoolean(INSTANCE_LOADED_MASKED_WALLET, mLoadedMaskedWallet);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case REQUEST_CODE_RESOLVE_ERR:
			// Call connect regardless of success or failure.
			// If the result was success, the connect should succeed
			// If the result was not success, this should get a new connection result
			mWalletClient.connect();
			break;
		case REQUEST_CODE_RESOLVE_LOAD_MASKED_WALLET:
			switch (resultCode) {
			case Activity.RESULT_OK:
				onMaskedWalletReceived((MaskedWallet) data.getParcelableExtra(WalletConstants.EXTRA_MASKED_WALLET),
						false);
				break;
			case Activity.RESULT_CANCELED:
				if (mWalletClient != null && mWalletClient.isConnected()) {
					Log.w("Masked wallet request: received RESULT_CANCELED");
					// Need to load a new ConnectionResult (even if user canceled)
					// in case they want to try it again
					mWalletClient.loadMaskedWallet(buildMaskedWalletRequest(), this);
				}
				break;
			default:
				int errorCode = data.getIntExtra(WalletConstants.EXTRA_ERROR_CODE, -1);
				handleError(errorCode);
				break;
			}
			break;
		}
	}

	// WalletFragment

	@Override
	protected void handleError(int errorCode) {
		super.handleError(errorCode);

		switch (errorCode) {
		case WalletConstants.ERROR_CODE_BUYER_CANCELLED:
			// Fetch a new ConnectionResult by calling loadMaskedWallet() again.
			// This is necessary because ConnectionResults cannot be reused
			mWalletClient.loadMaskedWallet(buildMaskedWalletRequest(), this);
			break;
		case WalletConstants.ERROR_CODE_SPENDING_LIMIT_EXCEEDED:
			mGoogleWalletDisabled = true;
			Toast.makeText(getActivity(), getString(R.string.spending_limit_exceeded), Toast.LENGTH_LONG).show();
			break;
		default:
			// Unrecoverable error
			mGoogleWalletDisabled = true;
			displayGoogleWalletUnavailableToast();
			break;
		}

		updateWalletViewVisibilities();
	}

	// ConnectionCallbacks

	@Override
	public void onConnected(Bundle connectionHint) {
		super.onConnected(connectionHint);

		// Check that we're not going to go over the transaction limit; if we are, shut it all down
		if (!WalletUtils.offerGoogleWallet(getEstimatedTotal())) {
			mGoogleWalletDisabled = true;
		}

		// Don't re-request the masked wallet if we already have it
		if (!mGoogleWalletDisabled && Db.getMaskedWallet() == null) {
			if (mCheckPreAuth) {
				mWalletClient.checkForPreAuthorization(this);
			}
			else {
				// For state purposes, act like we checked it (and we're just not pre-authed)
				mCheckedPreAuth = true;
			}

			// Immediately start requesting the wallet (even if we don't have product back yet)
			// We can just ballpark the final cost for the masked wallet.
			mWalletClient.loadMaskedWallet(buildMaskedWalletRequest(), this);
		}

		updateWalletViewVisibilities();
	}

	@Override
	public void onDisconnected() {
		super.onDisconnected();

		updateWalletViewVisibilities();
	}

	// OnPreAuthorizationDeterminedListener

	@Override
	public void onPreAuthorizationDetermined(ConnectionResult status, boolean isUserPreAuthorized) {
		super.onPreAuthorizationDetermined(status, isUserPreAuthorized);

		mCheckedPreAuth = true;
		mIsUserPreAuthorized = isUserPreAuthorized;

		updateWalletViewVisibilities();
	}

	// OnMaskedWalletLoadedListener

	@Override
	public void onMaskedWalletLoaded(ConnectionResult status, MaskedWallet wallet) {
		super.onMaskedWalletLoaded(status, wallet);

		if (status.isSuccess()) {
			// User has pre-authorized the app
			Log.i(WalletUtils.TAG, "User has pre-authorized app with Wallet before, automatically binding data...");
			onMaskedWalletReceived(wallet, true);
		}
		else if (status.hasResolution()) {
			mConnectionResult = status;
			mRequestCode = REQUEST_CODE_RESOLVE_LOAD_MASKED_WALLET;

			// If we require resolution, do not check for preauth in the future;
			// it will just be a waste of time.
			mCheckPreAuth = false;

			if (mHandleMaskedWalletWhenReady) {
				mProgressDialog.dismiss();

				resolveUnsuccessfulConnectionResult();
			}
			else if (mIsUserPreAuthorized) {
				// We thought the user was pre-authed, but some problem came up; make the user press button
				mIsUserPreAuthorized = false;
				updateWalletViewVisibilities();
			}
		}

		// It no longer matters if we're pre-authed, since we have a wallet (or a resolution thereof)
		mIsUserPreAuthorized = false;
	}
}
