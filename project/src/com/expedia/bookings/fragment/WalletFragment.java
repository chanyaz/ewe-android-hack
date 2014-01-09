package com.expedia.bookings.fragment;

import java.lang.ref.WeakReference;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Toast;

import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.utils.WalletUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.wallet.FullWallet;
import com.google.android.gms.wallet.MaskedWallet;
import com.google.android.gms.wallet.WalletClient;
import com.google.android.gms.wallet.WalletClient.OnFullWalletLoadedListener;
import com.google.android.gms.wallet.WalletClient.OnMaskedWalletLoadedListener;
import com.google.android.gms.wallet.WalletClient.OnPreAuthorizationDeterminedListener;
import com.google.android.gms.wallet.WalletConstants;
import com.mobiata.android.Log;

/**
 * This is a base Fragment for usage with the Google Wallet API.
 *
 * It provides some assistance for common problems.
 *
 * TODO: Improve progress dialog so it's a fragment?  Or possibly leave it up
 * to a listener to determine how loading should be shown?
 */
public class WalletFragment extends Fragment implements ConnectionCallbacks, OnConnectionFailedListener,
		OnPreAuthorizationDeterminedListener, OnMaskedWalletLoadedListener, OnFullWalletLoadedListener {

	/**
	 * Request code used when attempting to resolve issues with connecting to Google Play Services.
	 * Only use this request code when calling {@link ConnectionResult#startResolutionForResult(
	 * android.app.Activity, int)}.
	 */
	public static final int REQUEST_CODE_RESOLVE_ERR = 1000;

	/**
	 * Request code used when attempting to resolve issues with loading a masked wallet
	 * Only use this request code when calling {@link ConnectionResult#startResolutionForResult(
	 * android.app.Activity, int)}.
	 */
	public static final int REQUEST_CODE_RESOLVE_LOAD_MASKED_WALLET = 1001;

	/**
	 * Request code used when attempting to resolve issues with loading a full wallet
	 * Only use this request code when calling {@link ConnectionResult#startResolutionForResult(
	 * android.app.Activity, int)}.
	 */
	public static final int REQUEST_CODE_RESOLVE_LOAD_FULL_WALLET = 1002;

	/**
	 * Request code used when attempting to resolve issues with changing a masked wallet
	 * Only use this request code when calling {@link ConnectionResult#startResolutionForResult(
	 * android.app.Activity, int)}.
	 */
	public static final int REQUEST_CODE_RESOLVE_CHANGE_MASKED_WALLET = 1003;

	private static final String INSTANCE_GOOGLE_WALLET_ENABLED = "INSTANCE_GOOGLE_WALLET_ENABLED";

	protected WalletClient mWalletClient;

	// Whether the user tried to do an action that requires a masked wallet (i.e.: loadMaskedWallet)
	// before a masked wallet was acquired (i.e. still waiting for mWalletClient to connect)
	protected boolean mHandleMaskedWalletWhenReady = false;
	protected boolean mHandleFullWalletWhenReady = false;

	// Allow disabling of all Google Wallet functionality
	private boolean mGoogleWalletEnabled = true;

	// Cached connection result
	protected ConnectionResult mConnectionResult;

	// Activity request code if trying cached connection result for resolution
	protected int mRequestCode;

	// Progress dialog shown whenever Wallet is doing something we need
	// to be uninterrupted for.
	protected ProgressDialog mProgressDialog;

	//////////////////////////////////////////////////////////////////////////
	// Activation

	/**
	 * Disable Google Wallet functionality.
	 * 
	 * You can *only* disable Google Wallet; you cannot re-enable it later.  We've
	 * had too many problems where one accidentally does this.
	 */
	public void disableGoogleWallet() {
		mGoogleWalletEnabled = false;
	}

	public boolean isGoogleWalletEnabled() {
		return mGoogleWalletEnabled;
	}

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Before we do *anything*, check if Google Wallet has been disabled on the POS or in a previous instance
		if (!PointOfSale.getPointOfSale().supportsGoogleWallet() || (savedInstanceState != null
				&& !savedInstanceState.getBoolean(INSTANCE_GOOGLE_WALLET_ENABLED, true))) {
			Log.d("disableGoogleWallet: WalletFragment.onCreate");
			disableGoogleWallet();
		}

		// Set up a wallet client
		Context context = getActivity();
		mWalletClient = new WalletClient(context, WalletUtils.getWalletEnvironment(context), null, this, this);
		mRetryHandler = new RetryHandler(this);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Initialize the progress dialog (in case we need to use it later)
		mProgressDialog = new ProgressDialog(getActivity());
		mProgressDialog.setMessage(getString(R.string.loading));
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				mHandleMaskedWalletWhenReady = false;
				mHandleFullWalletWhenReady = false;
			}
		});
	}

	@Override
	public void onStart() {
		super.onStart();

		// Connect to Google Play Services
		mWalletClient.connect();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean(INSTANCE_GOOGLE_WALLET_ENABLED, mGoogleWalletEnabled);
	}

	@Override
	public void onStop() {
		super.onStop();

		// Disconnect from Google Play Services
		mWalletClient.disconnect();

		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}

		mRetryHandler.removeMessages(MESSAGE_RETRY_CONNECTION);
	}

	//////////////////////////////////////////////////////////////////////////
	// Error handling/resolution resolving code

	protected void handleError(int errorCode) {
		WalletUtils.logError(errorCode);
	}

	protected void handleUnrecoverableGoogleWalletError(int errorCode) {
		WalletUtils.logError(errorCode);

		Log.d("disableGoogleWallet: WalletFragment.handleUnrecoverableGoogleWalletError");
		disableGoogleWallet();
	}

	protected void displayGoogleWalletUnavailableToast() {
		Toast.makeText(getActivity(), R.string.google_wallet_unavailable, Toast.LENGTH_LONG).show();
	}

	public static boolean isRequestCodeFromWalletFragment(int requestCode) {
		return requestCode == REQUEST_CODE_RESOLVE_ERR
				|| requestCode == REQUEST_CODE_RESOLVE_CHANGE_MASKED_WALLET
				|| requestCode == REQUEST_CODE_RESOLVE_LOAD_FULL_WALLET
				|| requestCode == REQUEST_CODE_RESOLVE_LOAD_MASKED_WALLET;
	}

	/**
	 * Helper to try to resolve a result that is not successful.
	 * <p>
	 * If the result has a resolution (i.e. the user must select a payment instrument),
	 * {@link ConnectionResult#startResolutionForResult(Activity, int)} will be called
	 * to allow the user to enter additional input.  Otherwise, if the error is user recoverable
	 * (i.e. the user has an out of date version of Google Play Services installed), an error dialog
	 * provided by
	 * {@link GooglePlayServicesUtil#getErrorDialog(int, Activity, int, OnCancelListener)}. Finally,
	 * if none of the other cases apply, the error will be handled in {@link #handleError(int)}.
	 */
	protected void resolveUnsuccessfulConnectionResult() {
		// Additional user input is needed
		if (mConnectionResult.hasResolution()) {
			try {
				mConnectionResult.startResolutionForResult(getActivity(), mRequestCode);
			}
			catch (SendIntentException e) {
				reconnect();
			}
		}
		else {
			int errorCode = mConnectionResult.getErrorCode();
			if (GooglePlayServicesUtil.isUserRecoverableError(errorCode)) {
				Dialog dialog = GooglePlayServicesUtil.getErrorDialog(errorCode, getActivity(),
						REQUEST_CODE_RESOLVE_ERR, new OnCancelListener() {
							@Override
							public void onCancel(DialogInterface dialog) {
								// Get a new connection result
								mWalletClient.connect();
							}
						});

				// The dialog will either be dismissed, which will invoke the OnCancelListener, or
				// the dialog will be addressed, which will result in a callback to OnActivityResult()
				dialog.show();
			}
			else {
				switch (errorCode) {
				case ConnectionResult.INTERNAL_ERROR:
				case ConnectionResult.NETWORK_ERROR:
					reconnect();
					break;
				default:
					handleError(errorCode);
				}
			}
		}

		// Results are one time use
		mConnectionResult = null;
	}

	//////////////////////////////////////////////////////////////////////////
	// ConnectionCallbacks

	@Override
	public void onConnected(Bundle connectionHint) {
		Log.d(WalletUtils.TAG, "onConnected(" + connectionHint + ")");
	}

	@Override
	public void onDisconnected() {
		Log.d(WalletUtils.TAG, "onDisconnected()");
	}

	//////////////////////////////////////////////////////////////////////////
	// OnConnectionFailedListener

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.w(WalletUtils.TAG, "onConnectionFailed(" + result + ")");

		// Save the result so that it can be processed when the user taps a Google Wallet button
		mConnectionResult = result;
		mRequestCode = REQUEST_CODE_RESOLVE_ERR;

		// Handle the user's tap by dismissing the progress dialog and attempting to resolve the
		// connection result.
		if (mHandleMaskedWalletWhenReady || mHandleFullWalletWhenReady) {
			mProgressDialog.dismiss();

			resolveUnsuccessfulConnectionResult();
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// OnPreAuthorizationDeterminedListener

	@Override
	public void onPreAuthorizationDetermined(ConnectionResult status, boolean isUserPreAuthorized) {
		Log.d(WalletUtils.TAG, "onPreAuthorizationDetermined(" + status + ", " + isUserPreAuthorized + ")");
	}

	//////////////////////////////////////////////////////////////////////////
	// OnMaskedWalletLoadedListener

	@Override
	public void onMaskedWalletLoaded(ConnectionResult status, MaskedWallet wallet) {
		Log.d(WalletUtils.TAG, "onMaskedWalletLoaded(" + status + ", " + wallet + ")");

		if (status.isSuccess()) {
			WalletUtils.logWallet(wallet);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// OnFullWalletLoadedListener

	@Override
	public void onFullWalletLoaded(ConnectionResult status, FullWallet wallet) {
		Log.d(WalletUtils.TAG, "onFullWalletLoaded(" + status + ", " + wallet + ")");

		if (status.isSuccess()) {
			WalletUtils.logWallet(wallet);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Reconnect logic

	// Maximum number of times to try to connect to WalletClient if the connection is failing
	private static final int MAX_RETRIES = 3;
	private static final long INITIAL_RETRY_DELAY = 3000;
	private static final int MESSAGE_RETRY_CONNECTION = 1010;

	private int mRetryCounter = 0;
	private RetryHandler mRetryHandler;

	private void reconnect() {
		if (mRetryCounter < MAX_RETRIES) {
			Message m = mRetryHandler.obtainMessage(MESSAGE_RETRY_CONNECTION);
			mProgressDialog.show();
			// Back off exponentially
			long delay = (long) (INITIAL_RETRY_DELAY * Math.pow(2, mRetryCounter));
			mRetryHandler.sendMessageDelayed(m, delay);
			mRetryCounter++;
		}
		else {
			handleError(WalletConstants.ERROR_CODE_SERVICE_UNAVAILABLE);
		}
	}

	private static class RetryHandler extends Handler {

		private WeakReference<WalletFragment> mWeakReference;

		protected RetryHandler(WalletFragment walletFragment) {
			mWeakReference = new WeakReference<WalletFragment>(walletFragment);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_RETRY_CONNECTION:
				WalletFragment walletFragment = mWeakReference.get();
				if (walletFragment != null) {
					walletFragment.mWalletClient.connect();
				}
				break;
			}
		}
	}
}
