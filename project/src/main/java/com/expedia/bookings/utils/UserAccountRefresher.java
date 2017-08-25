package com.expedia.bookings.utils;

import javax.inject.Inject;

import android.content.Context;
import android.support.annotation.Nullable;

import com.expedia.account.data.FacebookLinkResponse;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.data.user.User;
import com.expedia.bookings.data.user.UserStateManager;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.model.UserLoginStateChangedModel;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.Log;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class UserAccountRefresher {
	public interface IUserAccountRefreshListener {
		void onUserAccountRefreshed();
	}

	private IUserAccountRefreshListener userAccountRefreshListener;

	private String keyRefreshUser;
	//When we last refreshed user data.
	private long mLastRefreshedUserTimeMillis = 0L;
	private Context context;

	@Inject
	UserLoginStateChangedModel userLoginStateChangedModel;

	@Inject
	UserStateManager userStateManager;

	public UserAccountRefresher(Context context, LineOfBusiness lob,
		@Nullable IUserAccountRefreshListener userAccountRefreshListener) {
		this.context = context.getApplicationContext();
		this.userAccountRefreshListener = userAccountRefreshListener;
		this.keyRefreshUser = lob + "_" + "KEY_REFRESH_USER";
		Ui.getApplication(context).appComponent().inject(this);
	}

	public void setUserAccountRefreshListener(IUserAccountRefreshListener listener) {
		userAccountRefreshListener = listener;
	}

	private final BackgroundDownloader.Download<SignInResponse> mRefreshUserDownload = new BackgroundDownloader.Download<SignInResponse>() {
		@Override
		public SignInResponse doDownload() {
			ExpediaServices services = new ExpediaServices(context);
			BackgroundDownloader.getInstance().addDownloadListener(keyRefreshUser, services);
			return services.signIn(ExpediaServices.F_FLIGHTS | ExpediaServices.F_HOTELS);
		}
	};

	private final BackgroundDownloader.OnDownloadComplete<SignInResponse> mRefreshUserCallback = new BackgroundDownloader.OnDownloadComplete<SignInResponse>() {
		@Override
		public void onDownload(SignInResponse results) {
			boolean resultsNoError = false;
			if (results != null) {
				if (results.hasErrors()) {
					//The refresh failed, so we just log them out. They can always try to login again.
					if (userStateManager.isUserAuthenticated()) {
						if (isUserFacebookSessionActive()) {
							forceAccountRefresh();
						}
						else if (isAuthenticationError(results)) {
							logOut(true);
						}
					}
				}
				else {
					// Update our existing saved data
					onSuccessfulUserAuthentication(results);
					resultsNoError = true;
				}
			}

			if (userAccountRefreshListener != null) {
				userAccountRefreshListener.onUserAccountRefreshed();
			}
			if (resultsNoError) {
				userLoginStateChangedModel.getUserLoginStateChanged().onNext(true);
			}
		}
	};

	private boolean isAuthenticationError(SignInResponse results) {
		return results.getErrors().get(0).getErrorCode() == ServerError.ErrorCode.NOT_AUTHENTICATED;
	}

	public void ensureAccountIsRefreshed() {
		int userRefreshIntervalThreshold = context.getResources().getInteger(R.integer.account_sync_interval_ms);
		if (userStateManager.isUserAuthenticated()
			&& mLastRefreshedUserTimeMillis + userRefreshIntervalThreshold < System.currentTimeMillis()) {
			//Force Refresh if Threshold has expired!
			forceAccountRefresh();
		}
		else {
			if (userAccountRefreshListener != null) {
				userAccountRefreshListener.onUserAccountRefreshed();
			}
			userLoginStateChangedModel.getUserLoginStateChanged().onNext(userStateManager.isUserAuthenticated());
		}
	}

	public void forceAccountRefresh() {
		Log.d("Refreshing user profile...");
		mLastRefreshedUserTimeMillis = System.currentTimeMillis();

		if (isUserFacebookSessionActive()) {
			fetchFacebookUserInfo();
		}
		else {
			BackgroundDownloader bd = BackgroundDownloader.getInstance();
			if (!bd.isDownloading(keyRefreshUser)) {
				bd.startDownload(keyRefreshUser, mRefreshUserDownload, mRefreshUserCallback);
			}

		}
	}

	public void forceAccountRefreshForWebView() {
		Log.d("Refreshing user profile from webview...");
		mLastRefreshedUserTimeMillis = System.currentTimeMillis();
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (!bd.isDownloading(keyRefreshUser)) {
			bd.startDownload(keyRefreshUser, mRefreshUserDownload, mRefreshUserCallbackForWebView);
		}
	}

	private final BackgroundDownloader.OnDownloadComplete<SignInResponse> mRefreshUserCallbackForWebView = new BackgroundDownloader.OnDownloadComplete<SignInResponse>() {
		@Override
		public void onDownload(SignInResponse results) {
			boolean resultsNoError = false;
			if (results != null) {
				if (results.hasErrors()) {
					//The refresh failed, so we just log them out. They can always try to login again.
					if (userStateManager.isUserAuthenticated()) {
						if (isAuthenticationError(results)) {
							logOut(false);
						}
					}
				}
				else {
					onSuccessfulUserAuthentication(results);
					resultsNoError = true;
				}
			}
			if (userAccountRefreshListener != null) {
				userAccountRefreshListener.onUserAccountRefreshed();
			}
			if (resultsNoError) {
				userLoginStateChangedModel.getUserLoginStateChanged().onNext(true);
			}
		}
	};

	private void onSuccessfulUserAuthentication(SignInResponse results) {
		// Update our existing saved data
		User user = results.getUser();
		user.save(context);
		Db.setUser(user);
	}

	private void logOut(boolean clearCookies) {
		if (clearCookies) {
			userStateManager.signOut();
		}
		else {
			userStateManager.signOutPreservingCookies();
		}

		BackgroundDownloader.getInstance().cancelDownload(keyRefreshUser);
		mLastRefreshedUserTimeMillis = 0L;
		Events.post(new Events.SignOut());
		userLoginStateChangedModel.getUserLoginStateChanged().onNext(false);
	}

	/**
	 * Ok so we have a users facebook session, but we need the users information for that to be useful so lets get that
	 */
	private void fetchFacebookUserInfo() {
		Log.d("FB: fetchFacebookUserInfo");

		ServicesUtil.generateAccountService(context).facebookReauth(context)
			.subscribeOn(Schedulers.io())
			.observeOn(AndroidSchedulers.mainThread())
			.subscribe(new Subscriber<FacebookLinkResponse>() {
				@Override
				public void onCompleted() {
					// unused
				}

				@Override
				public void onError(Throwable e) {
					failure();
				}

				@Override
				public void onNext(FacebookLinkResponse facebookLinkResponse) {
					if (facebookLinkResponse.isSuccess()) {
						success();
					}
					else {
						if (userStateManager.isUserAuthenticated()) {
							logOut(true);
						}
						failure();
					}
				}

				private void success() {
					Log.d("FB: Autologin success");
					BackgroundDownloader bd = BackgroundDownloader.getInstance();
					if (!bd.isDownloading(keyRefreshUser)) {
						bd.startDownload(keyRefreshUser, mRefreshUserDownload, mRefreshUserCallback);
					}
				}

				private void failure() {
					Log.d("FB: Autologin failed");
					if (userAccountRefreshListener != null) {
						userAccountRefreshListener.onUserAccountRefreshed();
					}
				}
			});
	}

	private boolean isUserFacebookSessionActive() {
		FacebookSdk.sdkInitialize(context);
		return AccessToken.getCurrentAccessToken() != null;
	}
}
