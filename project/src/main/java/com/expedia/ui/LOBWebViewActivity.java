package com.expedia.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;

import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.data.user.User;
import com.expedia.bookings.data.user.UserStateManager;
import com.expedia.bookings.fragment.WebViewFragment;
import com.expedia.bookings.interfaces.LOBWebViewConfigurator;
import com.expedia.bookings.interfaces.helpers.CarWebViewConfiguration;
import com.expedia.bookings.interfaces.helpers.PackageWebViewConfiguration;
import com.expedia.bookings.interfaces.helpers.RailWebViewConfiguration;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.utils.UserAccountRefresher;

/**
 * Created by dkumarpanjabi on 6/29/17.
 */

public class LOBWebViewActivity extends WebViewActivity implements UserAccountRefresher.IUserAccountRefreshListener {

	private UserAccountRefresher userAccountRefresher;
	private UserStateManager userStateManager;
	private LOBWebViewConfigurator mLOBWebViewConfigurator;

	public static class IntentBuilder extends WebViewActivity.IntentBuilder {
		public IntentBuilder(Context context) {
			super(context);
			getIntent().setClass(context, LOBWebViewActivity.class);
			this.setDomStorage(true);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mLOBWebViewConfigurator = getLOBWebViewConfigurator(
			(String) getIntent().getExtras().get(WebViewActivity.ARG_TRACKING_NAME));
		userAccountRefresher = new UserAccountRefresher(this, mLOBWebViewConfigurator.getLineOfBusiness(), this);
		userStateManager = Ui.getApplication(this).appComponent().userStateManager();
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
				mLOBWebViewConfigurator.trackAppWebViewClose();
				finish();
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		mLOBWebViewConfigurator.trackAppWebViewBack();
		super.onBackPressed();
	}

	@Override
	public void onUserAccountRefreshed() {
		User user = userStateManager.getUserSource().getUser();
		userStateManager.addUserToAccountManager(user);
	}

	private LOBWebViewConfigurator getLOBWebViewConfigurator(String trackingName) {
		if (trackingName.equals(WebViewFragment.TrackingName.RailWebView.toString())) {
			return new RailWebViewConfiguration();
		}
		if (trackingName.equals(WebViewFragment.TrackingName.PackageWebView.toString())) {
			return new PackageWebViewConfiguration();
		}
		return new CarWebViewConfiguration();
	}
}
