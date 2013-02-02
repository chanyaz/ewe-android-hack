package com.expedia.bookings.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.expedia.bookings.R;
import com.expedia.bookings.fragment.WebViewFragment;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;

public class WebViewActivity extends SherlockFragmentActivity implements WebViewFragment.WebViewFragmentListener {

	private static final String ARG_URL = "ARG_URL";
	private static final String ARG_STYLE_RES_ID = "ARG_STYLE_RES_ID";
	private static final String ARG_TITLE_RES_ID = "ARG_TITLE_RES_ID";
	private static final String ARG_DISABLE_SIGN_IN = "ARG_DISABLE_SIGN_IN";
	private static final String ARG_INJECT_EXPEDIA_COOKIES = "ARG_INJECT_EXPEDIA_COOKIES";
	private static final String ARG_TRACKING_NAME = "ARG_TRACKING_NAME";

	private WebViewFragment mFragment;

	public static Intent getIntent(Context context, String url, int styleResId, int titleResId) {
		return getIntent(context, url, styleResId, titleResId, false, false);
	}

	public static Intent getIntent(Context context, String url, int styleResId, int titleResId, boolean disableSignIn) {
		return getIntent(context, url, styleResId, titleResId, disableSignIn, false);
	}

	public static Intent getIntent(Context context, String url, int styleResId, int titleResId, boolean disableSignIn,
			String trackingName) {
		return getIntent(context, url, styleResId, titleResId, disableSignIn, false, trackingName);
	}

	public static Intent getIntent(Context context, String url, int styleResId, int titleResId, boolean disableSignIn,
			boolean injectExpediaCookies) {
		return getIntent(context, url, styleResId, titleResId, disableSignIn, injectExpediaCookies, null);
	}

	public static Intent getIntent(Context context, String url, int styleResId, int titleResId, boolean disableSignIn,
			boolean injectExpediaCookies, String trackingName) {
		Intent intent = new Intent(context, WebViewActivity.class);
		intent.putExtra(ARG_URL, url);
		if (styleResId != 0) {
			intent.putExtra(ARG_STYLE_RES_ID, styleResId);
		}
		if (titleResId != 0) {
			intent.putExtra(ARG_TITLE_RES_ID, titleResId);
		}
		intent.putExtra(ARG_DISABLE_SIGN_IN, disableSignIn);
		intent.putExtra(ARG_INJECT_EXPEDIA_COOKIES, injectExpediaCookies);
		if (trackingName != null) {
			intent.putExtra(ARG_TRACKING_NAME, trackingName);
		}
		return intent;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		Bundle extras = getIntent().getExtras();
		setTheme(extras.getInt(ARG_STYLE_RES_ID, R.style.FlightTheme));
		setTitle(extras.getInt(ARG_TITLE_RES_ID, R.string.legal_information));

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		if (savedInstanceState == null) {
			String url = extras.getString(ARG_URL);
			Log.v("WebView url: " + url);
			boolean disableSignIn = extras.getBoolean(ARG_DISABLE_SIGN_IN, false);
			boolean injectExpediaCookies = extras.getBoolean(ARG_INJECT_EXPEDIA_COOKIES, false);
			String name = extras.getString(ARG_TRACKING_NAME);
			mFragment = WebViewFragment.newInstance(url, disableSignIn, injectExpediaCookies, name);

			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.replace(android.R.id.content, mFragment, WebViewFragment.TAG);
			ft.commit();
		}
		else {
			mFragment = Ui.findSupportFragment(this, WebViewFragment.TAG);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: {
			finish();
			return true;
		}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void setLoading(boolean loading) {
		getSherlock().setProgressBarIndeterminateVisibility(loading);
	}

}
