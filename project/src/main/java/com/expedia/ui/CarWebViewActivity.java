package com.expedia.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.user.UserStateManager;
import com.expedia.bookings.tracking.CarWebViewTracking;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.utils.UserAccountRefresher;

public class CarWebViewActivity extends WebViewActivity implements UserAccountRefresher.IUserAccountRefreshListener {

	private UserAccountRefresher userAccountRefresher;
	private UserStateManager userStateManager;

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
			new CarWebViewTracking().trackAppCarWebViewClose();
			finish();
			return true;
		}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		new CarWebViewTracking().trackAppCarWebViewBack();
		super.onBackPressed();
	}

	@Override
	public void onUserAccountRefreshed() {
		userStateManager.addUserToAccountManager(Db.getUser());
	}

}
