package com.expedia.bookings.activity;

import java.net.HttpCookie;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.notification.GCMRegistrationKeeper;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.DebugUtils;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.util.AndroidUtils;

public class AboutWebViewActivity extends WebViewActivity {
	private static final String ARG_SHOW_EMAIL_BUTTON = "ARG_SHOW_EMAIL_BUTTON";

	private MenuItem mEmailMenuItem;

	private boolean mLoading = false;
	private boolean mShowEmailButton = false;

	public static class IntentBuilder extends WebViewActivity.IntentBuilder {
		public IntentBuilder(Context context) {
			super(context);
			getIntent().setClass(context, AboutWebViewActivity.class);
		}

		public IntentBuilder setShowEmailButton(boolean showEmailButton) {
			getIntent().putExtra(ARG_SHOW_EMAIL_BUTTON, showEmailButton);
			return this;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();

		if (intent.hasExtra(ARG_SHOW_EMAIL_BUTTON)) {
			mShowEmailButton = extras.getBoolean(ARG_SHOW_EMAIL_BUTTON);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_webview, menu);

		mEmailMenuItem = menu.findItem(R.id.menu_email);
		mEmailMenuItem.setVisible(mShowEmailButton && !mLoading);

		return super.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_email: {
			sendSupportEmail();
			return true;
		}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void setLoading(boolean loading) {
		super.setLoading(loading);

		mLoading = loading;

		if (mEmailMenuItem != null) {
			mEmailMenuItem.setVisible(mShowEmailButton && !mLoading);
			supportInvalidateOptionsMenu();
		}
	}

	private void sendSupportEmail() {
		StringBuilder body = new StringBuilder();

		body.append("\n\n\n");
		body.append("------");
		body.append("\n\n");
		body.append(getString(R.string.app_support_message_body));
		body.append("\n\n");

		body.append("PACKAGE: ");
		body.append(getPackageName());
		body.append("\n");
		body.append("VERSION: ");
		body.append(AndroidUtils.getAppVersion(this));
		body.append("\n");
		body.append("CODE: ");
		body.append(AndroidUtils.getAppCode(this));
		body.append("\n");
		body.append("POS: ");
		body.append(PointOfSale.getPointOfSale().getPointOfSaleId().toString());
		body.append("\n");
		body.append("LOCALE: ");
		body.append(Locale.getDefault().toString());

		body.append("\n\n");

		body.append("MC1 COOKIE: ");
		body.append(getMC1CookieStr());

		body.append("\n\n");

		if (User.isLoggedIn(this) && Db.getUser() != null) {
			body.append(getString(Ui.obtainThemeResID(this, R.attr.emailUserNameString)) + Db.getUser().getPrimaryTraveler().getEmail());

			body.append("\n\n");
		}

		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (lm != null) {
			boolean gpsEnabled = false;
			boolean networkEnabled = false;

			try{
				gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
			}
			catch (Exception ex) {
				//ignore
			}

			try {
				networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			}
			catch (Exception ex) {
				//ignore
			}

			body.append("Location Services: GPS=" + gpsEnabled + ", Network=" + networkEnabled);

			body.append("\n\n");
		}

		if (GCMRegistrationKeeper.getInstance(this) != null && !TextUtils.isEmpty(GCMRegistrationKeeper.getInstance(this).getRegistrationId(this))) {
			body.append("GCM Push Token: " + GCMRegistrationKeeper.getInstance(this).getRegistrationId(this));

			body.append("\n\n");
		}

		body.append(DebugUtils.getBuildInfo());

		SocialUtils.email(this, getString(R.string.email_app_support), "", body);
	}

	private String getMC1CookieStr() {
		List<HttpCookie> cookies = ExpediaServices.getCookies(this);
		if (cookies != null) {
			for (HttpCookie cookie : cookies) {
				if (cookie.getName() != null && cookie.getName().equals("MC1")) {
					return cookie.getValue();
				}
			}
		}
		return "";
	}
}
