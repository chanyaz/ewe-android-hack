package com.expedia.bookings.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.widget.Toast;
import android.view.MenuItem;
import android.view.Window;

import com.expedia.bookings.R;
import com.expedia.bookings.fragment.WebViewFragment;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;

public class WebViewActivity extends FragmentActivity implements WebViewFragment.WebViewFragmentListener {

	private static final String ARG_URL = "ARG_URL";
	private static final String ARG_STYLE_RES_ID = "ARG_STYLE_RES_ID";
	private static final String ARG_TITLE = "ARG_TITLE";
	private static final String ARG_ENABLE_LOGIN = "ARG_ENABLE_LOG_IN";
	private static final String ARG_INJECT_EXPEDIA_COOKIES = "ARG_INJECT_EXPEDIA_COOKIES";
	private static final String ARG_TRACKING_NAME = "ARG_TRACKING_NAME";
	private static final String ARG_HTML_DATA = "ARG_HTML_DATA";
	private static final String ARG_ALLOW_MOBILE_REDIRECTS = "ARG_ALLOW_MOBILE_REDIRECTS";

	private WebViewFragment mFragment;

	public static class IntentBuilder {

		private Context mContext;
		private Intent mIntent;

		public IntentBuilder(Context context) {
			mContext = context;
			mIntent = new Intent(context, WebViewActivity.class);
		}

		public Intent getIntent() {
			return mIntent;
		}

		public IntentBuilder setUrl(String url) {
			mIntent.putExtra(ARG_URL, url);
			return this;
		}

		public IntentBuilder setTheme(int themeResId) {
			mIntent.putExtra(ARG_STYLE_RES_ID, themeResId);
			return this;
		}

		public IntentBuilder setTitle(String title) {
			mIntent.putExtra(ARG_TITLE, title);
			return this;
		}

		public IntentBuilder setTitle(int titleResId) {
			mIntent.putExtra(ARG_TITLE, mContext.getString(titleResId));
			return this;
		}

		public IntentBuilder setLoginEnabled(boolean enableLogin) {
			mIntent.putExtra(ARG_ENABLE_LOGIN, enableLogin);
			return this;
		}

		public IntentBuilder setInjectExpediaCookies(boolean injectExpediaCookies) {
			mIntent.putExtra(ARG_INJECT_EXPEDIA_COOKIES, injectExpediaCookies);
			return this;
		}

		public IntentBuilder setTrackingName(String trackingName) {
			mIntent.putExtra(ARG_TRACKING_NAME, trackingName);
			return this;
		}

		public IntentBuilder setHtmlData(String data) {
			mIntent.putExtra(ARG_HTML_DATA, data);
			return this;
		}

		public IntentBuilder setAllowMobileRedirects(boolean allowMobileRedirects) {
			mIntent.putExtra(ARG_ALLOW_MOBILE_REDIRECTS, allowMobileRedirects);
			return this;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();

		setTheme(extras.getInt(ARG_STYLE_RES_ID, R.style.Theme_Phone_WebView));

		// Title
		String title = null;
		if (intent.hasExtra(ARG_TITLE)) {
			title = extras.getString(ARG_TITLE);
		}

		if (!TextUtils.isEmpty(title)) {
			setTitle(title);
		}
		else {
			if (getActionBar() != null) {
				getActionBar().setDisplayShowTitleEnabled(false);
			}
		}

		if (getActionBar() != null) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}

		if (savedInstanceState == null) {
			boolean enableLogin = extras.getBoolean(ARG_ENABLE_LOGIN, false);
			boolean injectExpediaCookies = extras.getBoolean(ARG_INJECT_EXPEDIA_COOKIES, false);

			// We default to true as we want to be redirected to the mobile site in most cases, except where we
			// do want to view desktop version (itineraries, for example).
			boolean allowMobileRedirects = extras.getBoolean(ARG_ALLOW_MOBILE_REDIRECTS, true);
			String name = extras.getString(ARG_TRACKING_NAME);

			if (extras.containsKey(ARG_HTML_DATA)) {
				String htmlData = extras.getString(ARG_HTML_DATA);
				Log.v("WebView html data: " + htmlData);
				mFragment = WebViewFragment.newInstance(htmlData);
			}
			else {
				String url = extras.getString(ARG_URL);
				Log.v("WebView url: " + url);

				// Some error checking: if not given a URL, display a toast and finish
				if (TextUtils.isEmpty(url)) {
					String t = getString(R.string.web_view_loading_error_TEMPLATE, getString(R.string.web_view_no_url));
					Toast.makeText(this, t, Toast.LENGTH_SHORT).show();
					finish();
					return;
				}

				mFragment = WebViewFragment.newInstance(url, enableLogin, injectExpediaCookies, allowMobileRedirects,
						name);
			}

			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.replace(android.R.id.content, mFragment, WebViewFragment.TAG);
			ft.commit();
		}
		else {
			mFragment = Ui.findSupportFragment(this, WebViewFragment.TAG);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		OmnitureTracking.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		OmnitureTracking.onPause();
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
		setProgressBarIndeterminateVisibility(loading);
	}
}
