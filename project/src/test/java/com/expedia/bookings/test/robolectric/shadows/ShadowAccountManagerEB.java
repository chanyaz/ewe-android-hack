package com.expedia.bookings.test.robolectric.shadows;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowAccountManager;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

@Implements(AccountManager.class)
public class ShadowAccountManagerEB extends ShadowAccountManager {

	@Implementation
	public AccountManagerFuture<Bundle> getAuthToken (Account account, String authTokenType, Bundle options, Activity activity, AccountManagerCallback<Bundle> callback, Handler handler) {
		return null;
	}

	@Override
	@Implementation
	public String peekAuthToken(Account account, String tokenType) {
		return "authToken";
	}
}
