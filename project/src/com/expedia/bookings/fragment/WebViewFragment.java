package com.expedia.bookings.fragment;

import java.net.HttpCookie;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
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
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.Ui;

@SuppressLint("SetJavaScriptEnabled")
public class WebViewFragment extends DialogFragment {

	public enum TrackingName {
		BaggageFeeOneWay,
		BaggageFeeOutbound,
		BaggageFeeInbound,
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

	private static final String INSTANCE_LOADED = "com.expedia.bookings.fragment.WebViewFragment.INSTANCE_LOADED";

	private WebViewFragmentListener mListener;

	private FrameLayout mFrame;

	private WebView mWebView;
	private boolean mWebViewLoaded = false;

	private String mUrl;
	private String mHtmlData;
	private boolean enableSignIn;
	private boolean mLoadCookies;
	private boolean mAllowUseableNetRedirects;
	private TrackingName mTrackingName;

	public static WebViewFragment newInstance(String url, boolean enableSignIn, boolean loadCookies,
			boolean allowUseableNetRedirects, String name) {
		WebViewFragment frag = new WebViewFragment();

		Bundle args = new Bundle();
		args.putString(ARG_URL, url);
		args.putBoolean(ARG_ENABLE_LOGIN, enableSignIn);
		args.putBoolean(ARG_LOAD_EXPEDIA_COOKIES, loadCookies);
		args.putBoolean(ARG_ALLOW_MOBILE_REDIRECTS, allowUseableNetRedirects);
		args.putString(ARG_TRACKING_NAME, name);
		frag.setArguments(args);
		frag.setRetainInstance(true);

		return frag;
	}

	public static WebViewFragment newInstance(String htmlData) {
		WebViewFragment frag = new WebViewFragment();

		Bundle args = new Bundle();
		args.putString(ARG_HTML_DATA, htmlData);
		frag.setArguments(args);
		frag.setRetainInstance(true);

		return frag;
	}

	public static WebViewFragment newDialogInstance(String url, boolean enableSignIn, boolean loadCookies,
			boolean allowUseableNetRedirects, String name, String title, String dismissButtonText) {
		WebViewFragment frag = newInstance(url, enableSignIn, loadCookies, allowUseableNetRedirects, name);

		frag.setArguments(addDialogArgs(frag.getArguments(), title, dismissButtonText));

		return frag;
	}

	public static WebViewFragment newDialogInstance(String htmlData, String title, String dismissButtonText) {
		WebViewFragment frag = newInstance(htmlData);

		frag.setArguments(addDialogArgs(frag.getArguments(), title, dismissButtonText));

		return frag;
	}

	private static Bundle addDialogArgs(Bundle inputArgs, String title, String dismissButtonText) {
		inputArgs.putBoolean(ARG_DIALOG_MODE, true);
		inputArgs.putString(ARG_DIALOG_TITLE, title);
		inputArgs.putString(ARG_DIALOG_BUTTON_TEXT, dismissButtonText);
		return inputArgs;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
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
		enableSignIn = args.getBoolean(ARG_ENABLE_LOGIN, false);

		String name = args.getString(ARG_TRACKING_NAME);
		if (!TextUtils.isEmpty(name)) {
			mTrackingName = TrackingName.valueOf(name);
		}

		mLoadCookies = args.getBoolean(ARG_LOAD_EXPEDIA_COOKIES, false);
		if (mLoadCookies) {
			loadCookies();
		}

		mAllowUseableNetRedirects = args.getBoolean(ARG_ALLOW_MOBILE_REDIRECTS, true);
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
			case BaggageFeeOneWay: {
				OmnitureTracking.trackPageLoadFlightBaggageFeeOneWay(getActivity());
				break;
			}
			case BaggageFeeOutbound: {
				OmnitureTracking.trackPageLoadFlightBaggageFeeOutbound(getActivity());
				break;
			}
			case BaggageFeeInbound: {
				OmnitureTracking.trackPageLoadFlightBaggageFeeInbound(getActivity());
				break;
			}
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
			mWebView.saveState(out);
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
			mWebView.restoreState(savedInstanceState);
		}

		if (!mWebViewLoaded) {
			if (mListener != null) {
				mListener.setLoading(true);
			}
			if (!TextUtils.isEmpty(mHtmlData)) {
				//Using .loadData() sometimes fails with unescaped html. loadDataWithBaseUrl() doesnt
				mWebView.loadDataWithBaseURL(null, mHtmlData, "text/html", "UTF-8", null);
			}
			else {
				mWebView.loadUrl(mUrl);
			}
		}
		else {
			if (mListener != null) {
				mListener.setLoading(false);
			}
		}
	}

	@SuppressLint("NewApi")
	private void constructWebView() {
		mWebView = new WebView(getActivity());

		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setLoadWithOverviewMode(true);
		mWebView.getSettings().setUseWideViewPort(!getArguments().getBoolean(ARG_DIALOG_MODE));
		mWebView.getSettings().setBuiltInZoomControls(true);

		// To allow Usablenet redirects to view mobile version of site, we leave the user agent string as be. The
		// default user-agent string contains "Android" which tips off the redirect to mobile.
		if (!mAllowUseableNetRedirects) {
			String userAgentString = ExpediaServices.getUserAgentString(getActivity());
			mWebView.getSettings().setUserAgentString(userAgentString);
		}
		if (AndroidUtils.getSdkVersion() >= 11) {
			mWebView.getSettings().setDisplayZoomControls(false);
		}

		mWebView.setWebViewClient(new WebViewClient() {

			@Override
			public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
				// Ignore
				if (!AndroidUtils.isRelease(getActivity())) {
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
					Ui.showToast(getActivity(), errorMessage);
				}
			}

			@Override
			public void onPageFinished(WebView webview, String url) {
				//Stop progress spinner
				mWebViewLoaded = true;
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

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (mLoadCookies) {
					view.loadUrl(url);
					return false;
				}
				else {
					return super.shouldOverrideUrlLoading(view, url);
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

	private void loadCookies() {
		CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(getActivity());
		CookieManager cookieManager = CookieManager.getInstance();

		// Set the Expedia cookies for loading the URL properly
		List<HttpCookie> cookies = ExpediaServices.getCookies(getActivity());
		cookieManager.setAcceptCookie(true);
		cookieManager.removeSessionCookie();
		Collections.sort(cookies, new Comparator<HttpCookie>() {
			@Override
			public int compare(HttpCookie lhs, HttpCookie rhs) {
				int nameCompare = lhs.getName().compareTo(rhs.getName());
				if (nameCompare == 0) {
					long lage = lhs.getMaxAge();
					long rage = rhs.getMaxAge();

					// -1 is a special case and means it never expires
					if (lage == -1 && rage == -1) {
						return 0;
					}
					if (lage == -1) {
						return 1;
					}
					if (rage == -1) {
						return -1;
					}
					if (lage > rage) {
						return 1;
					}
					else {
						return -1;
					}

				}
				else {
					return nameCompare;
				}
			}
		});

		for (HttpCookie cookie : cookies) {
			String cookieString = cookie.toString();

			// Note: this is getting set to two different URLs for Android compatibility reasons. ".expedia.com"
			//       works with ICS, using the url works with 2.1

			cookieManager.setCookie(mUrl, cookieString);
			cookieManager.setCookie(cookie.getDomain(), cookieString);
		}
		cookieSyncManager.sync();
	}

	public interface WebViewFragmentListener {
		public void setLoading(boolean loading);
	}

}
