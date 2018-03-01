package com.expedia.bookings.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.expedia.bookings.ADMS_Measurement;
import com.expedia.bookings.R;
import com.expedia.bookings.fragment.WebViewFragment;
import com.expedia.bookings.utils.Constants;
import com.mobiata.android.Log;
import com.squareup.phrase.Phrase;

public class WebViewActivity extends AppCompatActivity implements WebViewFragment.WebViewFragmentListener {

	private static final String ARG_URL = "ARG_URL";
	private static final String ARG_TITLE = "ARG_TITLE";
	private static final String ARG_ENABLE_LOGIN = "ARG_ENABLE_LOG_IN";
	private static final String ARG_INJECT_EXPEDIA_COOKIES = "ARG_INJECT_EXPEDIA_COOKIES";
	protected static final String ARG_TRACKING_NAME = "ARG_TRACKING_NAME";
	private static final String ARG_HTML_DATA = "ARG_HTML_DATA";
	private static final String ARG_ITIN_CHECKIN = "ARG_ITIN_CHECKIN";
	private static final String ARG_ALLOW_MOBILE_REDIRECTS = "ARG_ALLOW_MOBILE_REDIRECTS";
	private static final String ARG_RETURN_FROM_HOTEL_ITIN_WEBVIEW = "ARG_RETURN_FROM_HOTEL_ITIN_WEBVIEW";
	private static final String ARG_HANDLE_BACK = "ARG_HANDLE_BACK";
	private static final String ARG_HANDLE_RETRY_ON_ERROR = "ARG_HANDLE_RETRY_ON_ERROR";
	protected static final String ARG_USE_WEB_VIEW_TITLE = "ARG_USE_WEB_VIEW_TITLE";
	private static final String ARG_ALLOW_DOM_STORAGE = "ARG_ALLOW_DOM_STORAGE";
	private static final String ARG_MESO_DESTINATION_PAGE = "ARG_MESO_DESTINATION_PAGE";

	private boolean handleBack;

	private WebViewFragment webViewFragment;
	private ProgressBar mProgressBar;

	private boolean useOriginalTitle;

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
			if (url != null) {
				mIntent.putExtra(ARG_URL, ADMS_Measurement.getUrlWithVisitorData(url));
			}
			return this;
		}

		public IntentBuilder setUrlWithAnchor(String url, String anchor) {
			if (url != null && anchor != null) {
				mIntent.putExtra(ARG_URL, Phrase.from(mContext, R.string.itin_hotel_details_price_summary_url_TEMPLATE)
					.put("url", ADMS_Measurement.getUrlWithVisitorData(url))
					.put("anchor", anchor)
					.format().toString());
			}
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

		public IntentBuilder setCheckInLink(Boolean checkInLink) {
			mIntent.putExtra(ARG_ITIN_CHECKIN, checkInLink);
			return this;
		}

		public IntentBuilder setOriginalWebViewTitle(Boolean scrapTitle) {
			mIntent.putExtra(ARG_USE_WEB_VIEW_TITLE, scrapTitle);
			return this;
		}

		public IntentBuilder setItinTripIdForRefresh(String tripId) {
			mIntent.putExtra(Constants.ITIN_WEBVIEW_REFRESH_ON_EXIT_TRIP_NUMBER, tripId);
			mIntent.putExtra(ARG_RETURN_FROM_HOTEL_ITIN_WEBVIEW, true);
			return this;
		}

		public IntentBuilder setDomStorage(boolean setDomStorage) {
			mIntent.putExtra(ARG_ALLOW_DOM_STORAGE, setDomStorage);
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

		public IntentBuilder setHandleBack(boolean handleBack) {
			mIntent.putExtra(ARG_HANDLE_BACK, handleBack);
			return this;
		}

		public IntentBuilder setRetryOnFailure(boolean retry) {
			mIntent.putExtra(ARG_HANDLE_RETRY_ON_ERROR, retry);
			return this;
		}

		public IntentBuilder setMesoDestinationPage(boolean isMesoPage) {
			mIntent.putExtra(ARG_MESO_DESTINATION_PAGE, isMesoPage);
			return this;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		setTheme(R.style.Material_WebView_Theme);
		setContentView(R.layout.web_view_toolbar);
		mProgressBar = findViewById(R.id.webview_progress_view);

		if (shouldBail()) {
			return;
		}

		if (extras.getBoolean(ARG_ITIN_CHECKIN)) {
			Intent resultIntent = new Intent();
			String airlineName = extras.getString(Constants.ITIN_CHECK_IN_AIRLINE_NAME, "");
			String airlineCode = extras.getString(Constants.ITIN_CHECK_IN_AIRLINE_CODE, "");
			String confirmationCode = extras.getString(Constants.ITIN_CHECK_IN_CONFIRMATION_CODE, "");
			resultIntent.putExtra(Constants.ITIN_CHECK_IN_AIRLINE_NAME, airlineName);
			resultIntent.putExtra(Constants.ITIN_CHECK_IN_AIRLINE_CODE, airlineCode);
			resultIntent.putExtra(Constants.ITIN_CHECK_IN_CONFIRMATION_CODE, confirmationCode);
			setResult(RESULT_OK, resultIntent);
		}
		else if (extras.getBoolean(ARG_RETURN_FROM_HOTEL_ITIN_WEBVIEW)) {
			Intent resultIntent = new Intent(intent);
			setResult(RESULT_OK, resultIntent);
		}
		// Title
		String title = null;
		if (intent.hasExtra(ARG_TITLE)) {
			title = extras.getString(ARG_TITLE);
		}

		useOriginalTitle = extras.getBoolean(ARG_USE_WEB_VIEW_TITLE, false);

		setSupportActionBar((android.support.v7.widget.Toolbar) findViewById(R.id.toolbar));

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
			handleBack = extras.getBoolean(ARG_HANDLE_BACK, false);
			boolean retryOnError = extras.getBoolean(ARG_HANDLE_RETRY_ON_ERROR, false);
			boolean enableDomStorage = extras.getBoolean(ARG_ALLOW_DOM_STORAGE, false);

			webViewFragment =
				createWebViewFragment(extras, enableLogin, injectExpediaCookies, allowMobileRedirects,
					name, handleBack, retryOnError, enableDomStorage);


			if (extras.getBoolean(ARG_MESO_DESTINATION_PAGE)) {
				webViewFragment.setMesoDestinationPage(true);
			}

			if (webViewFragment == null) {
				finish();
				return;
			}

			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.replace(R.id.root_content, webViewFragment, WebViewFragment.TAG);
			ft.commit();
		}
	}

	protected WebViewFragment createWebViewFragment(Bundle extras, boolean enableLogin, boolean injectExpediaCookies,
		boolean allowMobileRedirects, String name, boolean handleBack, boolean retryOnError, boolean enableDomStorage) {
		WebViewFragment fragment;

		if (extras.containsKey(ARG_HTML_DATA)) {
			String htmlData = extras.getString(ARG_HTML_DATA);
			Log.v("WebView html data: " + htmlData);
			fragment = WebViewFragment.newInstance(htmlData);
		}
		else {
			String url = extras.getString(ARG_URL);
			Log.v("WebView url: " + url);

			// Some error checking: if not given a URL, display a toast and finish
			if (TextUtils.isEmpty(url)) {
				String t = getString(R.string.web_view_loading_error_TEMPLATE, getString(R.string.web_view_no_url));
				Toast.makeText(this, t, Toast.LENGTH_SHORT).show();
				return null;
			}
			fragment = WebViewFragment.newInstance(url, enableLogin, injectExpediaCookies, allowMobileRedirects,
				name, handleBack, retryOnError, enableDomStorage);
		}
		return fragment;
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
	public void onBackPressed() {
		if (webViewFragment != null && webViewFragment.canGoBack() && handleBack) {
			webViewFragment.goBack();
		}
		else {
			super.onBackPressed();
		}
	}

	@Override
	public void setLoading(boolean loading) {
		if (loading) {
			mProgressBar.setVisibility(View.VISIBLE);
		}
		else {
			mProgressBar.setVisibility(View.GONE);
		}
	}

	@Override
	public void setScrapedTitle(String title) {
		if (useOriginalTitle) {
			setTitle(title);
		}
	}

	private boolean shouldBail() {
		return !getResources().getBoolean(R.bool.portrait);
	}
}
