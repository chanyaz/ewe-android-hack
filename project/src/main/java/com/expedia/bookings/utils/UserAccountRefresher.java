package com.expedia.bookings.utils;

import android.content.Context;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FacebookLinkResponse;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.data.User;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.server.ExpediaServices;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.Log;

public class UserAccountRefresher {
	public interface IUserAccountRefreshListener {
		void onUserAccountRefreshed();
	}
	private static final String NET_AUTO_LOGIN = "NET_AUTO_LOGIN";
	private IUserAccountRefreshListener userAccountRefreshListener;

	private String keyRefreshUser;
	private String fbUserId;
	//When we last refreshed user data.
	private long mLastRefreshedUserTimeMillis = 0L;

	private Context context;
	public UserAccountRefresher(Context context, LineOfBusiness lob, IUserAccountRefreshListener userAccountRefreshListener) {
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
			Session session = Session.getActiveSession();
			if (session == null) {
				session = Session.openActiveSessionFromCache(context);
			}
			if (session != null && session.isOpened()) {
				fetchFacebookUserInfo(Session.getActiveSession());
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
	 * @param session
	 */
	protected void fetchFacebookUserInfo(Session session) {
		Log.d("FB: fetchFacebookUserInfo");

		// make request to the /me API
		Request.newMeRequest(session, new Request.GraphUserCallback() {

			// callback after Graph API response with user object
			@Override
			public void onCompleted(GraphUser user, Response response) {
				Log.d("FB: user response:" + response.toString());
				if (user != null && response.getError() == null) {
					Log.d("FB: user response success");
					fbUserId = user.getId();
					BackgroundDownloader bd = BackgroundDownloader.getInstance();
					if (!bd.isDownloading(NET_AUTO_LOGIN)) {
						bd.startDownload(NET_AUTO_LOGIN, mFbLinkAutoLoginDownload, mFbLinkAutoLoginHandler);
					}
				}
				else {
					Log.d("FB: user response failed");
					if (User.isLoggedIn(context)) {
						doLogout();
					}
					userAccountRefreshListener.onUserAccountRefreshed();
				}
			}
		}).executeAsync();
	}

	private final BackgroundDownloader.OnDownloadComplete<FacebookLinkResponse> mFbLinkAutoLoginHandler = new BackgroundDownloader.OnDownloadComplete<FacebookLinkResponse>() {
		@Override
		public void onDownload(FacebookLinkResponse results) {
			if (results != null && results.getFacebookLinkResponseCode() != null && results.isSuccess()) {
				Log.d("FB: Autologin success" + results.getFacebookLinkResponseCode().name());
				BackgroundDownloader bd = BackgroundDownloader.getInstance();
				if (!bd.isDownloading(keyRefreshUser)) {
					bd.startDownload(keyRefreshUser, mRefreshUserDownload, mRefreshUserCallback);
				}
			}
			else {
				Log.d("FB: Autologin failed");
				if (User.isLoggedIn(context)) {
					doLogout();
				}
				userAccountRefreshListener.onUserAccountRefreshed();
			}
		}
	};

	/**
	 * This attmpts to hand our facebook info to expedia and tries to auto login based on that info.
	 * This will only succeed if the user has at some point granted Expedia access to fbconnect.
	 */
	private final BackgroundDownloader.Download<FacebookLinkResponse> mFbLinkAutoLoginDownload = new BackgroundDownloader.Download<FacebookLinkResponse>() {
		@Override
		public FacebookLinkResponse doDownload() {
			Log.d("doDownload: mFbLinkAutoLoginDownload");
			Session fbSession = Session.getActiveSession();
			if (fbSession == null || fbSession.isClosed()) {
				Log.e("fbState invalid");
			}

			ExpediaServices services = new ExpediaServices(context);
			return services.facebookAutoLogin(fbUserId, fbSession.getAccessToken());
		}
	};
}
