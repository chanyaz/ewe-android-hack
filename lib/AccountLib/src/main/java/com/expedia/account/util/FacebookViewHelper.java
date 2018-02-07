package com.expedia.account.util;

import java.util.ArrayList;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import com.expedia.account.AccountService;
import com.expedia.account.AccountView;
import com.expedia.account.R;
import com.expedia.account.data.AccountResponse;
import com.expedia.account.data.Db;
import com.expedia.account.data.FacebookLinkResponse;
import com.expedia.account.data.PartialUser;
import com.expedia.account.view.FacebookAPIHostLayout;
import com.expedia.account.view.FacebookLayout;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import com.mobiata.android.Log;

public class FacebookViewHelper {

	private AccountView mAccountView;
	private CallbackManager mFbCallbackManager;

	private FacebookAPIHostLayout vFacebookAPIHostLayout;
	private FacebookLayout vFacebookLayout;

	public FacebookViewHelper(AccountView host) {
		mAccountView = host;

		FacebookSdk.sdkInitialize(getContext().getApplicationContext());
		mFbCallbackManager = CallbackManager.Factory.create();

		FacebookCallback<LoginResult> facebookCallback = new FacebookCallback<LoginResult>() {
			@Override
			public void onCancel() {
				onFacebookLoginCancelled();
			}

			@Override
			public void onError(FacebookException exception) {
				onFacebookLoginError();
			}

			@Override
			public void onSuccess(LoginResult loginResult) {
				onFacebookLoginSuccess(loginResult);
			}
		};

		LoginManager.getInstance().registerCallback(mFbCallbackManager, facebookCallback);

		vFacebookAPIHostLayout = (FacebookAPIHostLayout) mAccountView
			.findViewById(R.id.parent_facebook_api_host_layout);
		vFacebookLayout = (FacebookLayout) mAccountView
			.findViewById(R.id.parent_facebook_layout);
	}

	/**
	 * Login with Facebook.
	 * <p/>
	 * This kicks off the Facebook login using their own Activity.
	 * It uses the Facebook app if it is installed, otherwise it'll use a webview.
	 */
	public void doFacebookLogin() {
		Log.d("FACEBOOK: doFacebookLogin");
		LoginManager loginManager = LoginManager.getInstance();
		ArrayList<String> permissions = new ArrayList<>();
		permissions.add("email");
		// public_profile isn't directly needed, but works around an bug with their API not returning the jsonObject
		permissions.add("public_profile");
		loginManager.logInWithReadPermissions((Activity) getContext(), permissions);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (mFbCallbackManager != null) {
			mFbCallbackManager.onActivityResult(requestCode, resultCode, data);
		}
	}

	public void onLinkClicked() {
		if (vFacebookLayout.everythingChecksOut()) {
			vFacebookLayout.storeDataInNewUser();
			Utils.hideKeyboard(vFacebookLayout);
			mAccountView.show(AccountView.STATE_LOADING_FACEBOOK);
			fbLinkExistingAccount();
		}
	}

	Context getContext() {
		return mAccountView.getContext();
	}

	void onFacebookLoginCancelled() {
		Log.d("FACEBOOK: LoginResult: onCancel!");
		vFacebookAPIHostLayout.setMessage(R.string.acct__fb_sign_in_cancelled);
		mAccountView.getHandler().postDelayed(new Runnable() {
			@Override
			public void run() {
				mAccountView.onFacebookCancel();
			}
		}, 1000);
	}

	void onFacebookLoginError() {
		Log.d("FACEBOOK: LoginResult: onError!");
		mAccountView.onFacebookError();
		showErrorFacebookUnknown();
	}

	void onFacebookLoginSuccess(LoginResult loginResult) {
		// A successful login from Facebook means now we're ready to try
		// connecting the Facebook account to the Expedia account.
		Log.d("FACEBOOK: LoginResult: onSuccess!");
		AccessToken token = loginResult.getAccessToken();

		if (token.getDeclinedPermissions().contains("email")) {
			mAccountView.onFacebookError();
			showErrorFacebookDeclinedEmailAddress();
			return;
		}

		PartialUser user = Db.getNewUser();
		user.isFacebookUser = true;
		user.facebookUserId = token.getUserId();
		user.facebookToken = token.getToken();
		fetchFacebookUserInfo(token);
	}

	/**
	 * Ok so we have a user's facebook session, but we need the users information for
	 * that to be useful. So let's get it.
	 */
	void fetchFacebookUserInfo(AccessToken token) {
		Log.d("FACEBOOK: fetchFacebookUserInfo");

		GraphRequest request = GraphRequest.newMeRequest(token, new GraphRequest.GraphJSONObjectCallback() {
			@Override
			public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {
				if (jsonObject == null) {
					Log.d("FACEBOOK: nullJsonObject");
					mAccountView.onFacebookError();
					showErrorFacebookUnknown();
					return;
				}
				onFacebookUserInfoFetched(jsonObject);
			}
		});
		Bundle parameters = new Bundle();
		parameters.putString("fields", "email,first_name,last_name");
		request.setParameters(parameters);
		request.executeAsync();
	}

	void onFacebookUserInfoFetched(JSONObject jsonObject) {
		Log.d("FACEBOOK: meRequest: " + jsonObject.toString());

		PartialUser user = Db.getNewUser();
		user.email = jsonObject.optString("email");
		user.firstName = jsonObject.optString("first_name");
		user.lastName = jsonObject.optString("last_name");

		if (TextUtils.isEmpty(user.email)) {
			// This happens if user created their FB account with phone number only
			mAccountView.onFacebookError();
			showErrorFacebookMissingEmailAddress();
		}
		else {
			fbAutoLogin();
		}
	}

	/**
	 * This attempts to hand our Facebook info to Expedia and tries to auto login based on that info.
	 * This will only succeed if the user has at some point granted Expedia access to fbconnect.
	 */
	private void fbAutoLogin() {
		PartialUser user = Db.getNewUser();
		if (!user.isFacebookUser) {
			throw new RuntimeException("Not a Facebook user");
		}

		vFacebookAPIHostLayout.setMessage(R.string.acct__fb_linking_your_accounts);
		mAccountView.getService().facebookAutoLogin(user.facebookUserId, user.facebookToken)
			.subscribeOn(Schedulers.io())
			.observeOn(AndroidSchedulers.mainThread())
			.subscribe(new Observer<FacebookLinkResponse>() {
				@Override
				public void onComplete() {
					// We're done with the facebookAutoLogin request
				}

				@Override
				public void onError(Throwable e) {
					Log.d("FACEBOOK: unable to facebookAutoLogin: " + e);
					mAccountView.onFacebookError();
					showErrorFacebookUnknown();
				}

				@Override
				public void onSubscribe(Disposable d) {

				}

				@Override
				public void onNext(FacebookLinkResponse facebookLinkResponse) {
					if (facebookLinkResponse == null) {
						Log.d("FACEBOOK: facebookAutoLogin response arrived null");
						mAccountView.onFacebookError();
						showErrorFacebookUnknown();
						return;
					}

					Log.d("FACEBOOK: facebookAutoLogin response: "
						+ facebookLinkResponse.status.name());
					switch (facebookLinkResponse.status) {
					case notLinked:
						fbAskNewOrExistingAccount();
						break;
					case existing:
						mAccountView.show(AccountView.STATE_FACEBOOK);
						vFacebookLayout.setupExisting();
						break;
					case success:
						fbSignInRefreshProfile();
						break;
					case error:
						// Actual possible error message:
						// {
						//   "tlLoginSuccess":"false",
						//   "tlError":"USR:10910:User Service or Global User DB error during auto-login attempt::",
						//   "tlAcctState":"G",
						//   "tlAcctSource":"Facebook",
						//   "tlAcctType":"Individual",
						//   "status":"error",
						//   "rewardsState":"6"
						// }
						mAccountView.onFacebookError();
						showErrorFacebookUnknown();
						break;
					case loginFailed:
						mAccountView.onFacebookError();
						showErrorFacebookUnknown();
						break;
					}
				}
			});
	}

	private void fbAskNewOrExistingAccount() {
		Context context = getContext();

		CharSequence message = Utils.obtainBrandedPhrase(context,
			R.string.acct__fb_notLinked_description_TEMPLATE, mAccountView.getBrand())
			.put("email_address", Db.getNewUser().email).format();

		new AlertDialog.Builder(context)
			.setTitle(R.string.acct__fb_notLinked_title)
			.setMessage(message)
			.setNegativeButton(R.string.acct__fb_notLinked_new_button, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					fbLinkNewAccount();
				}
			})
			.setPositiveButton(R.string.acct__fb_notLinked_existing_button, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					mAccountView.show(AccountView.STATE_FACEBOOK);
					// We don't recognize your facebook email address,
					// and you choose "sign in with existing account",
					// meaning you want to use a different email address.
					Db.getNewUser().email = "";
					vFacebookLayout.setupNotLinked();
				}
			})
			.setCancelable(true)
			.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					onFacebookLoginCancelled();
				}
			})
			.create()
			.show();
	}

	/**
	 * Create a new Expedia user and associate the newly created user with the provided Facebook account and
	 * primary email address from Facebook.
	 */
	private void fbLinkNewAccount() {
		PartialUser user = Db.getNewUser();
		if (!user.isFacebookUser) {
			throw new RuntimeException("Not a Facebook user");
		}

		vFacebookAPIHostLayout.setMessage(Utils.obtainBrandedPhrase(getContext(),
			R.string.acct__fb_creating_a_new_brand_account, mAccountView.getBrand()).format());

		mAccountView.getService().facebookLinkNewAccount(user.facebookUserId, user.facebookToken, user.email)
			.subscribeOn(Schedulers.io())
			.observeOn(AndroidSchedulers.mainThread())
			.subscribe(new Observer<FacebookLinkResponse>() {
				@Override
				public void onComplete() {
					// Done with facebookLinkNewAccount request
				}

				@Override
				public void onError(Throwable e) {
					Log.d("FACEBOOK: unable to facebookLinkNewAccount: " + e);
					showErrorFacebookUnknown();
					mAccountView.onFacebookError();
				}

				@Override
				public void onSubscribe(Disposable d) {

				}

				@Override
				public void onNext(FacebookLinkResponse response) {
					if (response.isSuccess()) {
						fbSignInRefreshProfile();
					}
					else {
						Log.d("FACEBOOK: facebookLinkNewAccount failure: " + response);
						showErrorFacebookUnknown();
						mAccountView.onFacebookError();
					}
				}
			});
	}

	/**
	 * Create a new Expedia user and associate the newly created user with the provided Facebook account and
	 * primary email address from Facebook.
	 */
	private void fbLinkExistingAccount() {
		PartialUser user = Db.getNewUser();
		if (!user.isFacebookUser) {
			throw new RuntimeException("Not a Facebook user");
		}

		mAccountView.getService().facebookLinkExistingAccount(user.facebookUserId,
			user.facebookToken, user.email, user.password)
			.subscribeOn(Schedulers.io())
			.observeOn(AndroidSchedulers.mainThread())
			.subscribe(new Observer<FacebookLinkResponse>() {
				@Override
				public void onComplete() {
					// Done with facebookLinkNewAccount request
				}

				@Override
				public void onError(Throwable e) {
					mAccountView.onFacebookError();
					showErrorFacebookLinkExisting();
				}

				@Override
				public void onSubscribe(Disposable d) {

				}

				@Override
				public void onNext(FacebookLinkResponse response) {
					if (response.isSuccess()) {
						fbSignInRefreshProfile();
					}
					else {
						mAccountView.onFacebookError();
						showErrorFacebookLinkExisting();
					}
				}
			});
	}

	private void fbSignInRefreshProfile() {
		mAccountView.show(AccountView.STATE_LOADING_FACEBOOK);
		mAccountView.getService().signInProfileOnly()
			.subscribeOn(Schedulers.io())
			.observeOn(AndroidSchedulers.mainThread())
			.subscribe(new Observer<AccountResponse>() {
				@Override
				public void onComplete() {
					// We're done handing the signIn request
				}

				@Override
				public void onError(Throwable e) {
					Log.d("FACEBOOK: unable to fbSignInRefreshProfile: " + e);
					mAccountView.onFacebookError();
					showErrorFacebookUnknown();
				}

				@Override
				public void onSubscribe(Disposable d) {

				}

				@Override
				public void onNext(AccountResponse response) {
					if (response.success) {
						fbSignInSuccessful();
					}
					else {
						Log.d("FACEBOOK: fbSignInRefreshProfile not successful: " + response);
						mAccountView.onFacebookError();
						showErrorFacebookUnknown();
					}
				}
			});
	}

	private void fbSignInSuccessful() {
		Log.d("!!!Sign in succeeded");
		mAccountView.doFacebookSignInSuccessful();
		mAccountView.show(AccountView.STATE_WELCOME);
	}

	private void showErrorFacebookUnknown() {
		facebookLogOut();
		new AlertDialog.Builder(getContext())
			.setTitle(R.string.acct__Sign_in_failed_TITLE)
			.setMessage(R.string.acct__fb_unable_to_sign_into_facebook)
			.setPositiveButton(android.R.string.ok, null)
			.create()
			.show();
	}

	private void showErrorFacebookLinkExisting() {
		facebookLogOut();
		Context context = getContext();
		new AlertDialog.Builder(context)
			.setTitle(R.string.acct__Sign_in_failed_TITLE)
			.setMessage(Utils.obtainBrandedPhrase(context,
				R.string.acct__fb_link_existing_failed,
				mAccountView.getBrand())
				.format())
			.setPositiveButton(android.R.string.ok, null)
			.create()
			.show();
	}

	private void showErrorFacebookDeclinedEmailAddress() {
		facebookLogOut();
		Context context = getContext();
		new AlertDialog.Builder(context)
			.setTitle(R.string.acct__fb_user_denied_email_heading)
			.setMessage(Utils.obtainBrandedPhrase(context,
				R.string.acct__fb_user_denied_email_message,
				mAccountView.getBrand())
				.format())
			.setPositiveButton(android.R.string.ok, null)
			.create()
			.show();
	}

	private void showErrorFacebookMissingEmailAddress() {
		facebookLogOut();
		Context context = getContext();
		new AlertDialog.Builder(context)
			.setTitle(R.string.acct__fb_user_missing_email_heading)
			.setMessage(Utils.obtainBrandedPhrase(context,
				R.string.acct__fb_user_missing_email_message,
				mAccountView.getBrand())
				.format())
			.setPositiveButton(android.R.string.ok, null)
			.create()
			.show();
	}

	/**
	 * Log out from facebook when we failed signing in at our end.
	 */
	private void facebookLogOut() {
		AccountService.facebookLogOut();
	}

}
