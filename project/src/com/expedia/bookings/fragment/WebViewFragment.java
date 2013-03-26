package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.http.cookie.Cookie;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.expedia.bookings.server.PersistantCookieStore;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;

@SuppressLint("SetJavaScriptEnabled")
public class WebViewFragment extends Fragment {

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
	private TrackingName mTrackingName;

	public static WebViewFragment newInstance(String url, boolean enableSignIn, boolean loadCookies, String name) {
		WebViewFragment frag = new WebViewFragment();

		Bundle args = new Bundle();
		args.putString(ARG_URL, url);
		args.putBoolean(ARG_ENABLE_LOGIN, enableSignIn);
		args.putBoolean(ARG_LOAD_EXPEDIA_COOKIES, loadCookies);
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
	}

	@SuppressLint("NewApi")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mFrame = new FrameLayout(getActivity());
		if (mWebView == null) {
			mWebView = new WebView(getActivity());

			mWebView.getSettings().setJavaScriptEnabled(true);
			mWebView.getSettings().setLoadWithOverviewMode(true);
			mWebView.getSettings().setUseWideViewPort(true);
			mWebView.getSettings().setBuiltInZoomControls(true);
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
					mListener.setLoading(false);

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

		if (savedInstanceState != null) {
			mWebViewLoaded = savedInstanceState.getBoolean(INSTANCE_LOADED, false);
			mWebView.restoreState(savedInstanceState);
		}

		if (!mWebViewLoaded) {
			mListener.setLoading(true);
			if (!TextUtils.isEmpty(mHtmlData)) {
				mWebView.loadData(mHtmlData, "text/html", "UTF-8");
			}
			else {
				mWebView.loadUrl(mUrl);
			}
		}
		else {
			mListener.setLoading(false);
		}

		return mFrame;
	}

	@Override
	public void onPause() {
		super.onPause();

		//Important
		if (mFrame != null) {
			mFrame.removeAllViews();
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

		if (mFrame != null && mWebView != null) {
			mFrame.addView(mWebView);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof WebViewFragmentListener)) {
			throw new RuntimeException("WebView Activity must implement WebViewFragmentListener!");
		}

		mListener = (WebViewFragmentListener) activity;
	}

	@Override
	public void onSaveInstanceState(Bundle out) {
		super.onSaveInstanceState(out);
		if (mWebView != null) {
			out.putBoolean(INSTANCE_LOADED, this.mWebViewLoaded);
			mWebView.saveState(out);
		}
	}

	private void loadCookies() {
		CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(getActivity());
		CookieManager cookieManager = CookieManager.getInstance();

		// Set the Expedia cookies for loading the URL properly
		PersistantCookieStore persistantCookieStore = ExpediaServices.getCookieStore(getActivity());
		cookieManager.setAcceptCookie(true);
		cookieManager.removeSessionCookie();
		if (persistantCookieStore != null) {
			//Sort cookies by name and expiration, so newest cookies are last (and will squash old cookies when added to the manager)
			ArrayList<Cookie> cookies = new ArrayList<Cookie>();
			cookies.addAll(persistantCookieStore.getCookies());
			Collections.sort(cookies, new Comparator<Cookie>() {
				@Override
				public int compare(Cookie lhs, Cookie rhs) {
					int nameCompare = lhs.getName().compareTo(rhs.getName());
					if (nameCompare == 0) {
						if (lhs.getExpiryDate() != null && rhs.getExpiryDate() != null) {
							return lhs.getExpiryDate().compareTo(rhs.getExpiryDate());
						}
						else if (lhs.getExpiryDate() != null) {
							//The first expiration is null so it has no expiration and thus comes after the other
							return 1;
						}
						else if (rhs.getExpiryDate() != null) {
							return -1;
						}
						else {
							return 0;
						}
					}
					else {
						return nameCompare;
					}
				}
			});

			//for (Cookie cookie : persistantCookieStore.getCookies()) {
			for (Cookie cookie : cookies) {
				String cookieString = PersistantCookieStore.generateSetCookieString(cookie);

				// Note: this is getting set to two different URLs for Android compatibility reasons. ".expedia.com"
				//       works with ICS, using the url works with 2.1

				cookieManager.setCookie(mUrl, cookieString);
				cookieManager.setCookie(cookie.getDomain(), cookieString);
			}
			cookieSyncManager.sync();
		}
	}

	public interface WebViewFragmentListener {
		public void setLoading(boolean loading);
	}

}
