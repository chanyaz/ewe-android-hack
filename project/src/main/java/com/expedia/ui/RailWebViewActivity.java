package com.expedia.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import com.expedia.bookings.R;

import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.user.User;
import com.expedia.bookings.tracking.RailWebViewTracking;
import com.expedia.bookings.utils.UserAccountRefresher;

/**
 * Created by dkumarpanjabi on 6/29/17.
 */

public class RailWebViewActivity extends WebViewActivity implements UserAccountRefresher.IUserAccountRefreshListener {

	private UserAccountRefresher userAccountRefresher;
	private ProgressBar mProgressBar;

	public static class IntentBuilder extends WebViewActivity.IntentBuilder {
		public IntentBuilder(Context context) {
			super(context);
			getIntent().setClass(context, RailWebViewActivity.class);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		userAccountRefresher = new UserAccountRefresher(this, LineOfBusiness.RAILS, this);
		mProgressBar = (ProgressBar) findViewById(R.id.webview_progress_view);
	}

	@Override
	protected void onStop() {
		super.onStop();
		userAccountRefresher.forceAccountRefreshForWebView();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home: {
				RailWebViewTracking.trackAppRailWebViewClose();
				finish();
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		RailWebViewTracking.trackAppRailWebViewBack();
		super.onBackPressed();
	}

	@Override
	public void onUserAccountRefreshed() {
		User.addUserToAccountManager(this, Db.getUser());
	}

	@Override
	public void setLoading(boolean loading) {
		mProgressBar.setVisibility(View.GONE);
	}
}
