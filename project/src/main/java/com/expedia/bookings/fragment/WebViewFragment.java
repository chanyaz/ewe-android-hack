package com.expedia.bookings.fragment;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.R;
import com.expedia.bookings.R2;

import com.expedia.bookings.R2;

import com.expedia.bookings.R2;

import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.services.PersistentCookieManager;
import com.expedia.bookings.services.PersistentCookiesCookieJar;
import com.expedia.bookings.tracking.CarWebViewTracking;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.tracking.PackageWebViewTracking;
import com.expedia.bookings.tracking.RailWebViewTracking;
import com.expedia.bookings.utils.Constants;
import com.expedia.bookings.utils.ServicesUtil;
import com.expedia.bookings.utils.WebViewUtils;
import com.expedia.bookings.webview.BaseWebViewClient;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.Ui;

import okhttp3.Cookie;

@SuppressLint("SetJavaScriptEnabled")
public class WebViewFragment extends DialogFragment {

	public enum TrackingName {
		BaggageFeeOneWay,
		BaggageFeeOutbound,
		BaggageFeeInbound,
		CarWebView,
		Default,
		RailWebView,
		PackageWebView,
	}

	public static final String TAG = WebViewFragment.class.toString();

	private static final String ARG_URL = "ARG_URL";
	private static final String ARG_HTML_DATA = "ARG_HTML_DATA";
	private static final String ARG_ENABLE_LOGIN = "ARG_ENABLE_LOGIN";
	private static final String ARG_LOAD_EXPEDIA_COOKIES = "ARG_LOAD_EXPEDIA_COOKIES";
	private static final String ARG_ALLOW_MOBILE_REDIRECTS = "ARG_ALLOW_MOBILE_REDIRECTS";

	private static final String ARG_DIALOG_MODE = "ARG_DIALOG_MODE";
	private static final String ARG_DIALOG_TITLE = "ARG_DIALOG_TITLE";
	private static final String ARG_DIALOG_BUTTON_TEXT = "ARG_DIALOG_BUTTON_TEXT";

	private static final String ARG_TRACKING_NAME = "ARG_TRACKING_NAME";
	private static final String ARG_HANDLE_BACK = "ARG_HANDLE_BACK";
	private static final String ARG_HANDLE_RETRY_ON_ERROR = "ARG_HANDLE_RETRY_ON_ERROR";
	private static final String ARG_ENABLE_DOM_STORAGE = "ARG_ENABLE_DOM_STORAGE";

	private static final String INSTANCE_LOADED = "com.expedia.bookings.fragment.WebViewFragment.INSTANCE_LOADED";

	private WebViewFragmentListener mListener;

	private FrameLayout mFrame;

	private WebView mWebView;
	private boolean mWebViewLoaded = false;

	private String mUrl;
	protected String mHtmlData;
	private String mBaseUrl;
	private boolean enableSignIn;
	private boolean mLoadCookies;
	private boolean mAllowUseableNetRedirects;
	private TrackingName mTrackingName;
	private boolean handleBack;
	private boolean retryOnError;
	private boolean isMesoDestinationPage = false;

	public static WebViewFragment newInstance(String url, boolean enableSignIn, boolean loadCookies,
		boolean allowUseableNetRedirects, String name, boolean handleBack, boolean retryOnError, boolean enableDomStorage) {
		WebViewFragment frag = new WebViewFragment();

		Bundle args = new Bundle();
		args.putString(ARG_URL, url);
		args.putBoolean(ARG_ENABLE_LOGIN, enableSignIn);
		args.putBoolean(ARG_LOAD_EXPEDIA_COOKIES, loadCookies);
		args.putBoolean(ARG_ALLOW_MOBILE_REDIRECTS, allowUseableNetRedirects);
		args.putString(ARG_TRACKING_NAME, name);
		args.putBoolean(ARG_HANDLE_BACK, handleBack);
		args.putBoolean(ARG_HANDLE_RETRY_ON_ERROR, retryOnError);
		args.putBoolean(ARG_ENABLE_DOM_STORAGE, enableDomStorage);
		frag.setArguments(args);
		frag.setRetainInstance(true);

		return frag;
	}

	public static WebViewFragment newInstance(String htmlData, String baseUrl) {
		WebViewFragment frag = new WebViewFragment();

		Bundle args = new Bundle();
		args.putString(ARG_HTML_DATA, htmlData);
		if (baseUrl != null) {
			args.putString(Constants.ARG_BASE_URL, baseUrl);
		}
		frag.setArguments(args);
		frag.setRetainInstance(true);

		return frag;
	}

	private static Bundle addDialogArgs(Bundle inputArgs, String title, String dismissButtonText) {
		inputArgs.putBoolean(ARG_DIALOG_MODE, true);
		inputArgs.putString(ARG_DIALOG_TITLE, title);
		inputArgs.putString(ARG_DIALOG_BUTTON_TEXT, dismissButtonText);
		return inputArgs;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		mListener = Ui.findFragmentListener(this, WebViewFragmentListener.class, false);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();
		if (args.containsKey(ARG_HTML_DATA)) {
			mHtmlData = args.getString(ARG_HTML_DATA);
		}
		else {
			mUrl = args.getString(ARG_URL);
		}

		mBaseUrl = args.getString(Constants.ARG_BASE_URL, null);

		enableSignIn = args.getBoolean(ARG_ENABLE_LOGIN, false);

		String name = args.getString(ARG_TRACKING_NAME);
		if (!TextUtils.isEmpty(name)) {
			mTrackingName = TrackingName.valueOf(name);
		}

		mLoadCookies = args.getBoolean(ARG_LOAD_EXPEDIA_COOKIES, false);
		// TODO when removing feature toggle please remove usage of ARG_LOAD_EXPEDIA_COOKIES and loadCookies method.

		PersistentCookiesCookieJar mCookieManager = new ExpediaServices(
			getContext()).mCookieManager;
		if (mLoadCookies && mCookieManager instanceof PersistentCookieManager) {
			loadCookies((PersistentCookieManager) mCookieManager);
		}

		mAllowUseableNetRedirects = args.getBoolean(ARG_ALLOW_MOBILE_REDIRECTS, true);
		handleBack = args.getBoolean(ARG_HANDLE_BACK, false);
		retryOnError = args.getBoolean(ARG_HANDLE_RETRY_ON_ERROR, false);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (!getArguments().getBoolean(ARG_DIALOG_MODE)) {
			return generateView(savedInstanceState);
		}
		else {
			//If we are in dialog mode, we need to pretend like we aren't overrideing onCreateView
			return super.onCreateView(inflater, container, savedInstanceState);
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		if (getArguments().getBoolean(ARG_DIALOG_MODE)) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			if (getArguments().containsKey(ARG_DIALOG_TITLE)
				&& !TextUtils.isEmpty(getArguments().getString(ARG_DIALOG_TITLE))) {
				builder.setTitle(getArguments().getString(ARG_DIALOG_TITLE));
			}
			if (getArguments().containsKey(ARG_DIALOG_BUTTON_TEXT)
				&& !TextUtils.isEmpty(getArguments().getString(ARG_DIALOG_BUTTON_TEXT))) {
				builder.setNeutralButton(getArguments().getString(ARG_DIALOG_BUTTON_TEXT), new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int arg1) {
						dialog.dismiss();
					}
				});
			}
			builder.setView(generateView(savedInstanceState));
			return builder.create();
		}
		else {
			//If we are NOT in dialog mode, we need to pretend like we aren't overrideing onCreateDialog
			return super.onCreateDialog(savedInstanceState);
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		if (mTrackingName != null) {
			switch (mTrackingName) {
			case BaggageFeeOneWay:
				OmnitureTracking.trackPageLoadFlightBaggageFeeOneWay();
				break;
			case BaggageFeeOutbound:
				OmnitureTracking.trackPageLoadFlightBaggageFeeOutbound();
				break;
			case BaggageFeeInbound:
				OmnitureTracking.trackPageLoadFlightBaggageFeeInbound();
				break;
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		attachWebView();
	}

	@Override
	public void onPause() {
		super.onPause();
		detachWebView();
	}

	@Override
	public void onSaveInstanceState(Bundle out) {
		super.onSaveInstanceState(out);
		if (mWebView != null) {
			out.putBoolean(INSTANCE_LOADED, this.mWebViewLoaded);
		}
	}

	@Override
	public void onDestroyView() {
		//This is a workaround for a rotation bug: http://code.google.com/p/android/issues/detail?id=17423
		if (getDialog() != null && getRetainInstance()) {
			getDialog().setDismissMessage(null);
		}
		super.onDestroyView();
	}

	public void bind(String url) {
		mUrl = url;
		mWebViewLoaded = false;
		constructWebView();
		actOnState(null);
		attachWebView();
	}

	@SuppressLint("NewApi")
	private View generateView(Bundle savedInstanceState) {
		mFrame = new FrameLayout(getActivity());

		if (mWebView == null) {
			constructWebView();
		}

		actOnState(savedInstanceState);

		return mFrame;
	}

	/**
	 * Based on the state, should we load the URL, load HTML, etc..
	 *
	 * @param savedInstanceState
	 */
	private void actOnState(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mWebViewLoaded = savedInstanceState.getBoolean(INSTANCE_LOADED, false);
		}

		if (TextUtils.isEmpty(mHtmlData)) {
			if (mListener != null) {
				mListener.setLoading(true);
			}
			mWebView.loadUrl(mUrl);
		}
		else {
			if (mListener != null) {
				mListener.setLoading(!mWebViewLoaded);
			}
			// Using .loadData() sometimes fails with unescaped html.
			// .loadDataWithBaseURL() does not seem to save the data across configuration changes, so must always reload
			mWebView.loadDataWithBaseURL(mBaseUrl, mHtmlData, "text/html", "UTF-8", null);
		}
	}

	@SuppressLint("NewApi")
	private void constructWebView() {
		mWebView = new WebView(getActivity());

		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setLoadWithOverviewMode(true);
		mWebView.getSettings().setUseWideViewPort(!getArguments().getBoolean(ARG_DIALOG_MODE));
		mWebView.getSettings().setBuiltInZoomControls(true);
		mWebView.getSettings().setDomStorageEnabled(getArguments().getBoolean(ARG_ENABLE_DOM_STORAGE));

		// To allow Usablenet redirects to view mobile version of site, we leave the user agent string as be. The
		// default user-agent string contains "Android" which tips off the redirect to mobile.
		if (!mAllowUseableNetRedirects) {
			String userAgentString = ServicesUtil.generateUserAgentString();
			mWebView.getSettings().setUserAgentString(userAgentString);
		}

		boolean isTabletDevice = AndroidUtils.isTablet(getContext());
		mWebView.getSettings().setUserAgentString(
			WebViewUtils.generateUserAgentStringWithDeviceType(mWebView.getSettings().getUserAgentString(), isTabletDevice));

		mWebView.getSettings().setDisplayZoomControls(false);

		mWebView.setWebViewClient(new BaseWebViewClient(getActivity(), mLoadCookies, mTrackingName) {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (isLoginUrl(url)) {
					trackLoginLinkClick();
					handleLogin();
					return true;
				}
				else {
					return super.shouldOverrideUrlLoading(view, url);
				}
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				if (mListener != null) {
					mListener.setLoading(true);
				}
			}

			@Override
			public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
				// Ignore
				if (BuildConfig.DEBUG) {
					Log.d("WebViewFragment: Got an SSL certificate error (primary: " + error.getPrimaryError()
						+ "), but we're going to proceed anyways because this is a debug build.  URL=" + mUrl);

					handler.proceed();
				}
				else {
					Log.w("WebViewFragment SSL Error: primaryError=" + error.getPrimaryError() + ", url=" + mUrl);

					super.onReceivedSslError(view, handler, error);
				}
			}

			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				Log.w("WebViewFragment error: code=" + errorCode + ", desc=" + description + ", url=" + failingUrl);

				if (isAdded()) {
					String errorFormatStr = getResources().getString(R.string.web_view_loading_error_TEMPLATE);
					String errorMessage = String.format(errorFormatStr, description);
					if (retryOnError) {
						showErrorRetryDialog(this.getActivity(), view, errorMessage, failingUrl);
					}
					else {
						Ui.showToast(getActivity(), errorMessage);
					}
				}
			}

			@Override
			public void onPageFinished(WebView webview, String url) {
				super.onPageFinished(webview, url);
				//Stop progress spinner
				mWebViewLoaded = true;
				mListener.newUrlLoaded(url);
				if (mListener != null) {
					mListener.setLoading(false);
				}

				if (!enableSignIn) {
					// Insert javascript to remove the signin button
					webview.loadUrl("javascript:(function() { " +
						"document.getElementsByClassName('sign_link')[0].style.visibility='hidden'; " +
						"})()");
				}

				// Insert javascript to remove the native app download banner
				webview.loadUrl("javascript:(function() { " +
					"document.getElementById('SmartBanner').style.display='none'; " +
					"})()");

				// Insert javascript to remove navigation header if we are on cars storefront page
				if (webview.getUrl() != null && webview.getUrl().contains(PointOfSale.getPointOfSale().getCarsTabWebViewURL())) {
					mWebView.loadUrl("javascript:(function() { " +
						"if (header = document.getElementsByClassName('site-header-primary')[0]) {" +
						"header.parentElement.removeChild(header);" +
						"}" +
						"})()");
				}

				if (isMesoDestinationPage) {
					// Insert javascript to remove the social media icons and top banners
					webview.loadUrl("javascript: (function() {"
						+ "var social = document.querySelector('.ultimate-social-icons'); social.outerHTML = ''; delete social;"
						+ "})()");
					webview.loadUrl("javascript: (function() {"
						+ "var banner = document.getElementById('masthead'); banner.outerHTML = ''; delete banner;"
						+ "})()");
				}

				if (mListener != null) {
					mListener.setScrapedTitle(webview.getTitle());
				}

				//If we are showing the fragment as a dialog, we need to request layout after the page renders, otherwise the dialog
				//doesnt measure correctly.
				if (getArguments().getBoolean(ARG_DIALOG_MODE)) {
					webview.postDelayed(new Runnable() {
						private long mPostLoopStartTime = 0;

						@Override
						public void run() {
							if (mPostLoopStartTime == 0) {
								mPostLoopStartTime = System.currentTimeMillis();
							}
							else if (System.currentTimeMillis() - mPostLoopStartTime > 5000) {
								//We dont want this loop to go on forever.
								return;
							}

							if (isVisible() && mWebView != null && mWebView.getHeight() <= 0) {
								mWebView.requestLayout();
								mWebView.postDelayed(this, 50);
							}
						}
					}, 50);
				}
			}

		});
	}

	private void attachWebView() {
		if (mFrame != null && mWebView != null) {
			mFrame.removeAllViews();
			mFrame.addView(mWebView);
		}
	}

	private void detachWebView() {
		//Important
		if (mFrame != null) {
			mFrame.removeAllViews();
		}
	}

	private void loadCookies(PersistentCookieManager mCookieManager) {
		CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(getActivity());
		CookieManager cookieManager = CookieManager.getInstance();

		// Set the Expedia cookies for loading the URL properly
		HashMap<String, HashMap<String, Cookie>> cookiesStore = mCookieManager.getCookieStore();
		cookieManager.setAcceptCookie(true);
		cookieManager.removeSessionCookie();

		if (cookiesStore != null) {
			for (HashMap<String, Cookie> cookies : cookiesStore.values()) {
				for (Cookie cookie : cookies.values()) {
					cookieManager.setCookie(cookie.domain(), cookie.toString());
				}
			}
		}

		cookieSyncManager.sync();
	}

	private boolean isItinPageUrl(String url) {
		return url != null && url.contains("/trips/");
	}

	public boolean canGoBack() {
		if (isItinPageUrl(mWebView.getUrl()) || !handleBack) {
			return false;
		}
		return mWebView.canGoBack();
	}

	public void goBack() {
		mWebView.goBack();
	}

	public interface WebViewFragmentListener {
		void setLoading(boolean loading);
		void setScrapedTitle(String title);
		void newUrlLoaded(String url);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Constants.RESULT_NO_CHANGES) {
			mWebView.reload();
		}
	}

	private boolean isLoginUrl(String url) {
		return url.contains("user/signin") || url.contains("user/createaccount");
	}

	private void handleLogin() {
		getActivity().onBackPressed();
	}

	private void showErrorRetryDialog(Context context, final WebView webView, String errorMessage,
		final String retryUrl) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.WebViewAlertDialog);
		builder.setCancelable(false)
			.setMessage(errorMessage)
			.setPositiveButton(context.getResources().getString(R.string.retry), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					trackRetryClick();
					dialog.dismiss();
					webView.loadUrl(retryUrl);
				}
			})
			.show();
	}

	private void trackLoginLinkClick() {
		if (mTrackingName == TrackingName.CarWebView) {
			new CarWebViewTracking().trackAppCarWebViewSignIn();
		}
		if (mTrackingName == TrackingName.RailWebView) {
			RailWebViewTracking.trackAppRailWebViewSignIn();
		}
		if (mTrackingName == TrackingName.PackageWebView) {
			PackageWebViewTracking.trackAppPackageWebViewSignIn();
		}
	}

	private void trackRetryClick() {
		if (mTrackingName == TrackingName.CarWebView) {
			new CarWebViewTracking().trackAppCarWebViewRetry();
		}
		if (mTrackingName == TrackingName.RailWebView) {
			RailWebViewTracking.trackAppRailWebViewRetry();
		}
		if (mTrackingName == TrackingName.PackageWebView) {
			PackageWebViewTracking.trackAppPackageWebViewRetry();
		}
	}

	public void setMesoDestinationPage(boolean isMesoDestinationPage) {
		this.isMesoDestinationPage = isMesoDestinationPage;
	}

}
