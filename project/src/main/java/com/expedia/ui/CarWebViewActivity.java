package com.expedia.ui;

import android.content.Context;
import android.os.Bundle;

import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.User;
import com.expedia.bookings.utils.UserAccountRefresher;

public class CarWebViewActivity extends WebViewActivity implements UserAccountRefresher.IUserAccountRefreshListener {

	private UserAccountRefresher userAccountRefresher;

	public static class IntentBuilder extends WebViewActivity.IntentBuilder {
		public IntentBuilder(Context context) {
			super(context);
			getIntent().setClass(context, CarWebViewActivity.class);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		userAccountRefresher = new UserAccountRefresher(this, LineOfBusiness.CARS, this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		userAccountRefresher.forceAccountRefreshForWebView();
	}

	@Override
	public void onUserAccountRefreshed() {
		User.addUserToAccountManager(this, Db.getUser());
	}
}

