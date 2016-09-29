package com.expedia.bookings.account;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.expedia.bookings.activity.AccountLibActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.data.User;
import com.expedia.bookings.server.ExpediaServices;

/**
 * ExpediaAccountAuthenticator - for using the AccountManager with expedia accounts.
 * <p/>
 * NOTE: This AccountAuthenticator is in some ways breaking its contract based on the token returned.
 * Typically a AccountAuthenticator would return a valid signin token that can be used to authenticate with a service.
 * We are returning the Tuid of our logged in User, which can in no way be used to log into expedia. Why?
 * I'LL TELL YOU WHY! We do this because the only true  "token" that we have are the cookies themselves, which
 * are often manipulated by expedia's services (also, which cookies do we use as a token? Non-logged-in users have cookies too, etc...).
 * More importantly, we do not want our Authenticator to become a cookie broker. The cookies are such that any sort of manipulation of them
 * is likely to introduce insanity bugs, so the idea of getting cookies (and storing them) anywhere other than from
 * the web requests themselves seems too dangerous. Hence our Tuid approach which can be used to at least verify
 * the token against that of the logged in user.
 * <p/>
 * Account name: expedia account email address
 * Token: Primary Traveler uuid
 */
public class ExpediaAccountAuthenticator extends AbstractAccountAuthenticator {

	private Context mContext;

	public ExpediaAccountAuthenticator(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType,
							 String[] requiredFeatures, Bundle options)
		throws NetworkErrorException {

		final Intent intent = AccountLibActivity.createIntent(mContext, options);
		intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
		final Bundle bundle = new Bundle();
		bundle.putParcelable(AccountManager.KEY_INTENT, intent);
		return bundle;
	}

	@Override
	public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options)
		throws NetworkErrorException {
		// Default method implementation - currently not used.
		return null;
	}

	@Override
	public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
		// Default method implementation - currently not used.
		return null;
	}

	/**
	 * See top of ExpediaAccountAuthenticator.java (this file) for detailed explaination of our token.
	 *
	 * @param options - This is an argument bundle that will be passed to the LoginActivity
	 * @return Token = Expedia Account TUID - Not an actual authentication token.
	 */
	@Override
	public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType,
							   Bundle options)
		throws NetworkErrorException {

		Bundle result = new Bundle();
		String tuidStr = null;
		if (User.isLoggedIn(mContext) && Db.getUser() != null && !TextUtils.isEmpty(Db.getUser().getTuidString())) {
			//We are already logged in and things look ok, lets get us that tuid
			tuidStr = Db.getUser().getTuidString();
		}
		else if (User.isLoggedIn(mContext)) {
			//Try to log in with stored stuff
			ExpediaServices services = new ExpediaServices(mContext);

			SignInResponse signInResponse = services.signIn(ExpediaServices.F_FLIGHTS | ExpediaServices.F_HOTELS);
			if (signInResponse != null && !signInResponse.hasErrors()) {
				User user = signInResponse.getUser();
				Db.setUser(user);
				tuidStr = user.getTuidString();
			}
		}
		else {
			//Send the user to the login activity
			final Intent intent = AccountLibActivity.createIntent(mContext, options);
			intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
			final Bundle bundle = new Bundle();
			bundle.putParcelable(AccountManager.KEY_INTENT, intent);
			return bundle;
		}

		if (TextUtils.isEmpty(tuidStr)) {
			//Error //KEY_ERROR_CODE and KEY_ERROR_MESSAGE to indicate an error
			result.putInt(AccountManager.KEY_ERROR_CODE, 1);
			result.putString(AccountManager.KEY_ERROR_MESSAGE, "Could not get user information.");
		}
		else {
			//success //KEY_ACCOUNT_NAME, KEY_ACCOUNT_TYPE, and KEY_AUTHTOKEN
			result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
			result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
			result.putString(AccountManager.KEY_AUTHTOKEN, tuidStr);
		}

		return result;
	}

	@Override
	public String getAuthTokenLabel(String authTokenType) {
		// Default method implementation - currently not used.
		return null;
	}

	@Override
	public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features)
		throws NetworkErrorException {
		// Default method implementation - currently not used.
		return null;
	}

	@Override
	public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType,
									Bundle options)
		throws NetworkErrorException {
		// Default method implementation - currently not used.
		return null;
	}

	@Override
	public Bundle getAccountRemovalAllowed(AccountAuthenticatorResponse response, Account account) {
		//Always allow removal of expedia accounts
		Bundle bundle = new Bundle();
		bundle.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, true);
		return bundle;
	}

}
