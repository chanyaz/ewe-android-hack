package com.expedia.bookings.utils;

import android.content.Context;

import com.expedia.account.data.FacebookLinkResponse;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.data.User;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.server.ExpediaServices;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.Log;

import rx.Subscriber;

public class UserAccountRefresher {
	public interface IUserAccountRefreshListener {
		void onUserAccountRefreshed();
	}

	private IUserAccountRefreshListener userAccountRefreshListener;

	private String keyRefreshUser;
	private String fbUserId;
	//When we last refreshed user data.
	private long mLastRefreshedUserTimeMillis = 0L;

	private Context context;

	public UserAccountRefresher(Context context, LineOfBusiness lob,
		IUserAccountRefreshListener userAccountRefreshListener) {
		this.context = context;
		this.userAccountRefreshListener = userAccountRefreshListener;
		this.keyRefreshUser = lob + "_" + "KEY_REFRESH_USER";
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
			if (results == null || results.hasErrors()) {
				//The refresh failed, so we just log them out. They can always try to login again.
				if (User.isLoggedIn(context)) {
					doLogout();
				}
			}
			else {
				// Update our existing saved data
				User user = results.getUser();
				user.save(context);
				Db.setUser(user);
			}

			userAccountRefreshListener.onUserAccountRefreshed();
		}
	};

	public void ensureAccountIsRefreshed() {
		int userRefreshInterval = context.getResources().getInteger(R.integer.account_sync_interval_ms);
		if (mLastRefreshedUserTimeMillis + userRefreshInterval < System.currentTimeMillis()) {
			Log.d("Refreshing user profile...");
			mLastRefreshedUserTimeMillis = System.currentTimeMillis();

			FacebookSdk.sdkInitialize(context);
			AccessToken token = AccessToken.getCurrentAccessToken();
			if (token != null) {
				fetchFacebookUserInfo(token);
			}
			else {
				BackgroundDownloader bd = BackgroundDownloader.getInstance();
				if (!bd.isDownloading(keyRefreshUser)) {
					bd.startDownload(keyRefreshUser, mRefreshUserDownload, mRefreshUserCallback);
				}
			}
		}
		else {
			userAccountRefreshListener.onUserAccountRefreshed();
		}
	}

	private void doLogout() {
		BackgroundDownloader.getInstance().cancelDownload(keyRefreshUser);
		mLastRefreshedUserTimeMillis = 0L;
		Events.post(new Events.SignOut());
	}

	/**
	 * Ok so we have a users facebook session, but we need the users information for that to be useful so lets get that
	 *
	 * @param token
	 */
	protected void fetchFacebookUserInfo(AccessToken token) {
		Log.d("FB: fetchFacebookUserInfo");

		fbUserId = token.getUserId();

		ServicesUtil.generateAccountService(context)
			.facebookReauth(context).subscribe(new Subscriber<FacebookLinkResponse>() {
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
				if (User.isLoggedIn(context)) {
					doLogout();
				}
				userAccountRefreshListener.onUserAccountRefreshed();
			}
		});
	}
}
