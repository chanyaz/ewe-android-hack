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
import com.expedia.bookings.data.User;
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
			if (results != null) {
				if (results.hasErrors()) {
					//The refresh failed, so we just log them out. They can always try to login again.
					if (User.isLoggedIn(context)) {
						if (isUserFacebookSessionActive()) {
							forceAccountRefresh();
						}
						else if (results.getErrors().get(0).getErrorCode() == ServerError.ErrorCode.NOT_AUTHENTICATED) {
							doLogout();
						}
					}
				}
				else {
					// Update our existing saved data
					User user = results.getUser();
					user.save(context);
					Db.setUser(user);
					userLoginStateChangedModel.getUserLoginStateChanged().onNext(true);
				}
			}

			if (userAccountRefreshListener != null) {
				userAccountRefreshListener.onUserAccountRefreshed();
			}
		}
	};

	public void ensureAccountIsRefreshed() {
		int userRefreshIntervalThreshold = context.getResources().getInteger(R.integer.account_sync_interval_ms);
		if (User.isLoggedIn(context) && mLastRefreshedUserTimeMillis + userRefreshIntervalThreshold < System
			.currentTimeMillis()) {
			//Force Refresh if Threshold has expired!
			forceAccountRefresh();
		}
		else {
			if (userAccountRefreshListener != null) {
				userAccountRefreshListener.onUserAccountRefreshed();
			}
			userLoginStateChangedModel.getUserLoginStateChanged().onNext(User.isLoggedIn(context));
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

	private void doLogout() {
		User.signOut(context);
		BackgroundDownloader.getInstance().cancelDownload(keyRefreshUser);
		mLastRefreshedUserTimeMillis = 0L;
		Events.post(new Events.SignOut());
		userLoginStateChangedModel.getUserLoginStateChanged().onNext(false);
	}

	/**
	 * Ok so we have a users facebook session, but we need the users information for that to be useful so lets get that
	 */
	protected void fetchFacebookUserInfo() {
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
						if (User.isLoggedIn(context)) {
							doLogout();
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
