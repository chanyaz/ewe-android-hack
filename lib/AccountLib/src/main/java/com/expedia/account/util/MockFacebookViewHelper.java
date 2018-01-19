package com.expedia.account.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.expedia.account.AccountView;
import com.facebook.AccessToken;
import com.facebook.login.LoginResult;

import com.mobiata.android.Log;

/**
 * Created by doug on 8/31/15.
 */
public class MockFacebookViewHelper extends FacebookViewHelper {

	static final CharSequence CANCEL = "cancel";
	static final CharSequence DENIED_EMAIL = "Denied email permission";
	static final CharSequence MISSING_EMAIL = "Missing email";
	static final CharSequence NOT_LINKED = "notLinked";
	static final CharSequence EXISTING = "existing";
	static final CharSequence SUCCESS = "success";
	static final CharSequence ERROR = "generic Facebook error";

	public static final String DENIED_EMAIL_ADDRESS = "deniedemail@fb.com";
	public static final String NOT_LINKED_ADDRESS = "notlinked@fb.com";
	public static final String EXISTING_ADDRESS = "existing@fb.com";
	public static final String SUCCESS_ADDRESS = "success@fb.com";

	public static final String MISSING_EMAIL_USERID = "1234";

	public MockFacebookViewHelper(AccountView host) {
		super(host);
	}

	///////////////////////////////////////////////////////////////////////////
	// MOCK doFacebookLogin
	///////////////////////////////////////////////////////////////////////////

	@Override
	public void doFacebookLogin() {
		final CharSequence[] items = new CharSequence[] {
			CANCEL, DENIED_EMAIL, MISSING_EMAIL, NOT_LINKED, EXISTING, SUCCESS, ERROR
		};

		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				CharSequence s = items[which];
				if (CANCEL.equals(s)) {
					onFacebookLoginCancelled();
				}
				else if (DENIED_EMAIL.equals(s)) {
					LoginResult result = MockedLoginResult.getDeniedEmailInstance();
					onFacebookLoginSuccess(result);
				}
				else if (MISSING_EMAIL.equals(s)) {
					LoginResult result = MockedLoginResult.getMissingEmailInstance();
					onFacebookLoginSuccess(result);
				}
				else if (NOT_LINKED.equals(s)) {
					LoginResult result = MockedLoginResult.getNotLinkedInstance();
					onFacebookLoginSuccess(result);
				}
				else if (EXISTING.equals(s)) {
					LoginResult result = MockedLoginResult.getExistingInstance();
					onFacebookLoginSuccess(result);
				}
				else if (SUCCESS.equals(s)) {
					LoginResult result = MockedLoginResult.getSuccessInstance();
					onFacebookLoginSuccess(result);
				}
				else if (ERROR.equals(s)) {
					onFacebookLoginError();
				}
			}
		};

		new AlertDialog.Builder(getContext())
			.setTitle("Choose scenario")
			.setItems(items, listener)
			.setCancelable(false)
			.create()
			.show();
	}

	private static class MockedLoginResult extends LoginResult {
		static final String APP_ID = "AccountCreation";

		private static AccessToken getAccessToken(CharSequence scenario, String emailAddress) {
			Collection<String> permissions = new HashSet<>();
			return new AccessToken(scenario.toString(), APP_ID, emailAddress,
				null, permissions, null, null, null);
		}

		public static MockedLoginResult getDeniedEmailInstance() {
			Set<String> denied = new HashSet<>();
			denied.add("email");
			AccessToken token = new AccessToken(DENIED_EMAIL.toString(), APP_ID, DENIED_EMAIL_ADDRESS,
				null, denied, null, null, null);
			return new MockedLoginResult(token, denied);
		}

		public static MockedLoginResult getMissingEmailInstance() {
			AccessToken token = getAccessToken(MISSING_EMAIL, MISSING_EMAIL_USERID);
			return new MockedLoginResult(token);
		}

		public static MockedLoginResult getNotLinkedInstance() {
			AccessToken token = getAccessToken(NOT_LINKED, NOT_LINKED_ADDRESS);
			return new MockedLoginResult(token);
		}

		public static MockedLoginResult getExistingInstance() {
			AccessToken token = getAccessToken(EXISTING, EXISTING_ADDRESS);
			return new MockedLoginResult(token);
		}

		public static MockedLoginResult getSuccessInstance() {
			AccessToken token = getAccessToken(SUCCESS, SUCCESS_ADDRESS);
			return new MockedLoginResult(token);
		}

		private MockedLoginResult(AccessToken accessToken, Set<String> denied) {
			super(accessToken, new HashSet<String>(), denied);
		}

		private MockedLoginResult(AccessToken accessToken) {
			super(accessToken, new HashSet<String>(), new HashSet<String>());
		}
	}

	///////////////////////////////////////////////////////////////////////////
	// MOCK fetchFacebookUserInfo
	///////////////////////////////////////////////////////////////////////////

	@Override
	public void fetchFacebookUserInfo(AccessToken token) {
		try {
			JSONObject object = new JSONObject();
			if (!token.getDeclinedPermissions().contains("email") && !token.getUserId().equals(MISSING_EMAIL_USERID)) {
				object.put("email", token.getUserId());
			}
			object.put("first_name", "Test");
			object.put("last_name", "Testerson");
			onFacebookUserInfoFetched(object);
		}
		catch (JSONException e) {
			Log.e(e.toString());
		}
	}


}
