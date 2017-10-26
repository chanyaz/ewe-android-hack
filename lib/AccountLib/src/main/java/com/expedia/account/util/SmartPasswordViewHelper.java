package com.expedia.account.util;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.expedia.account.AnalyticsListener;
import com.expedia.account.R;
import com.expedia.account.view.SignInLayout;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialPickerConfig;
import com.google.android.gms.auth.api.credentials.CredentialRequest;
import com.google.android.gms.auth.api.credentials.CredentialRequestResult;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.auth.api.credentials.IdentityProviders;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

public class SmartPasswordViewHelper implements
	GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
	private GoogleApiClient credentialsApiClient;

	private static final String TAG = "SignInActivity";
	private static final int RC_SAVE = 1;
	private static final int RC_HINT = 2;
	private static final int RC_READ = 3;
	private static final int RESULT_OK = -1;
	private FragmentActivity currentActivity;
	private SignInLayout vSignInLayout;
	private AnalyticsListener analyticsListener;
	ProgressDialog retrieveCredentialsDialog;
	private boolean isUserAutoLoggedIn = false;

	public SmartPasswordViewHelper(AnalyticsListener analyticsListener, FragmentActivity currentActivity) {
		this.currentActivity = currentActivity;
		this.analyticsListener = analyticsListener;
		vSignInLayout = (SignInLayout) currentActivity.findViewById(R.id.parent_sign_in_layout);
		credentialsApiClient = new GoogleApiClient.Builder(currentActivity)
			.addConnectionCallbacks(this)
			.enableAutoManage(currentActivity, this)
			.addApi(Auth.CREDENTIALS_API)
			.build();
		retrieveCredentialsDialog = new ProgressDialog(currentActivity,R.style.acct__Theme_Progress_Dialog);
		retrieveCredentialsDialog.setCancelable(false);
		retrieveCredentialsDialog.show();
		retrieveCredentialsDialog.setContentView(R.layout.acct__progress__dialog);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == RC_READ) {
			Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
			if (resultCode == RESULT_OK) {
				onCredentialRetrieved(credential);
			}
		}
		if (requestCode == RC_HINT) {
			if (resultCode == RESULT_OK) {
				Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
				vSignInLayout.vEmailAddressPresenter.setText(credential.getId());

			}
		}
	}

	public void requestCredentials() {

		final CredentialRequest mCredentialRequest = new CredentialRequest.Builder()
			.setPasswordLoginSupported(true)
			.build();

		Auth.CredentialsApi.request(credentialsApiClient, mCredentialRequest).setResultCallback(
			new ResultCallback<CredentialRequestResult>() {
				@Override
				public void onResult(@NonNull CredentialRequestResult credentialRequestResult) {
					retrieveCredentialsDialog.dismiss();
					if (credentialRequestResult.getStatus().isSuccess()) {
						onCredentialRetrieved(credentialRequestResult.getCredential());
						if (analyticsListener != null) {
							analyticsListener.userAutoLoggedInBySmartPassword();
							isUserAutoLoggedIn = true;
						}
					}
					else {
						resolveResult(credentialRequestResult.getStatus());
					}
				}
			});
	}

	public void onCredentialRetrieved(Credential credential) {
		String accountType = credential.getAccountType();
		if (accountType == null) {
			String userID = credential.getId();
			String password = credential.getPassword();
			if (userID != null && password != null) {
				if (analyticsListener != null && !isUserAutoLoggedIn) {
					analyticsListener.userSignedInUsingSmartPassword();
				}
				Events.post(new Events.SignInButtonClicked(userID, password));
			}
			else {
				Log.e(TAG, "The credential requests need delete");
			}
		}
	}

	public void resolveResult(Status status) {
		if (status.getStatusCode() == CommonStatusCodes.RESOLUTION_REQUIRED) {
			try {
				status.startResolutionForResult(currentActivity, RC_READ);
			}
			catch (IntentSender.SendIntentException e) {
				Log.e(TAG, "STATUS: Failed to send resolution.", e);
			}
		}
		else if (status.getStatusCode() == CommonStatusCodes.SIGN_IN_REQUIRED) {
			showHint();
		}
		else {
			Log.e(TAG, "STATUS: Unsuccessful credential request.");
		}
	}

	public void showHint() {
		HintRequest hintRequest = new HintRequest.Builder()
			.setHintPickerConfig(new CredentialPickerConfig.Builder()
				.setShowCancelButton(true)
				.build())
			.setEmailAddressIdentifierSupported(true)
			.setAccountTypes(IdentityProviders.GOOGLE)
			.build();

		PendingIntent intent = Auth.CredentialsApi.getHintPickerIntent(credentialsApiClient, hintRequest);
		try {
			currentActivity.startIntentSenderForResult(intent.getIntentSender(), RC_HINT, null, 0, 0, 0);
		}
		catch (IntentSender.SendIntentException e) {
			Log.e(TAG, "Could not start hint picker Intent", e);
		}
	}

	public void saveCredentials(String email, String password) {

		Credential credential = new Credential.Builder(email)
			.setPassword(password)
			.build();

		Auth.CredentialsApi.save(credentialsApiClient, credential).setResultCallback(new ResultCallback() {
			@Override
			public void onResult(Result result) {
				Status status = result.getStatus();
				if (status.isSuccess()) {
					Log.d(TAG, "The credentials are saved with google");
				}
				else {
					if (status.hasResolution()) {
						try {
							status.startResolutionForResult(currentActivity, RC_SAVE);
						}
						catch (IntentSender.SendIntentException e) {
							Log.e(TAG, "Could not resolve credentials");
						}
					}
				}
			}
		});
	}

	@Override
	public void onConnected(@Nullable Bundle bundle) {
		requestCredentials();
	}

	@Override
	public void onConnectionSuspended(int i) {
		Log.e(TAG, "Connection Suspended.");
		retrieveCredentialsDialog.dismiss();
		// this means that our connection to Google API has been lost and will be retried later
		// we don't need to try very hard to recover since this is a nice-to-have feature and not critical
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
		Log.e(TAG, "Connection Failed.");
		retrieveCredentialsDialog.dismiss();
		// this means that our connection to Google API has failed and will not show google dialogues.
		// Google Play Services. If it does happen, we just don't get the Smart Lock functionality.
	}

}
