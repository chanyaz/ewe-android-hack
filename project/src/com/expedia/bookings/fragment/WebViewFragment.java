package com.expedia.bookings.fragment;

import org.apache.http.cookie.Cookie;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.server.PersistantCookieStore;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.Ui;

@SuppressLint("SetJavaScriptEnabled")
public class WebViewFragment extends Fragment {

	public enum TrackingName {
		BaggageFeeOneWay,
		BaggageFeeOutbound,
		BaggageFeeInbound,
	}

	public static final String TAG = WebViewFragment.class.toString();

	private static final String ARG_URL = "ARG_URL";
	private static final String ARG_DISABLE_SIGN_IN = "ARG_DISABLE_SIGN_IN";
	private static final String ARG_LOAD_EXPEDIA_COOKIES = "ARG_LOAD_EXPEDIA_COOKIES";

	private static final String ARG_TRACKING_NAME = "ARG_TRACKING_NAME";

	private static final String INSTANCE_LOADED = "com.expedia.bookings.fragment.WebViewFragment.INSTANCE_LOADED";

	private WebViewFragmentListener mListener;

	private FrameLayout mFrame;

	private WebView mWebView;
	private boolean mWebViewLoaded = false;

	private String mUrl;
	private boolean mDisableSignIn;
	private boolean mLoadCookies;
	private TrackingName mTrackingName;

	private Context mContext;

	public static WebViewFragment newInstance(String url, boolean disableSignIn, boolean loadCookies, String name) {
		WebViewFragment frag = new WebViewFragment();

		Bundle args = new Bundle();
		args.putString(ARG_URL, url);
		args.putBoolean(ARG_DISABLE_SIGN_IN, disableSignIn);
		args.putBoolean(ARG_LOAD_EXPEDIA_COOKIES, loadCookies);
		args.putString(ARG_TRACKING_NAME, name);
		frag.setArguments(args);
		frag.setRetainInstance(true);

		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity();

		Bundle args = getArguments();
		mUrl = args.getString(ARG_URL);
		mDisableSignIn = args.getBoolean(ARG_DISABLE_SIGN_IN, false);

		String name = args.getString(ARG_TRACKING_NAME, null);
		if (name != null) {
			mTrackingName = TrackingName.valueOf(name);
		}

		mLoadCookies = args.getBoolean(ARG_LOAD_EXPEDIA_COOKIES, false);
		if (mLoadCookies) {
			// Set the Expedia cookies for loading the URL properly
			CookieSyncManager.createInstance(getActivity());
			CookieManager cookieManager = CookieManager.getInstance();

			PersistantCookieStore persistantCookieStore = ExpediaServices.getCookieStore(getActivity());
			cookieManager.setAcceptCookie(true);
			cookieManager.removeSessionCookie();
			if (persistantCookieStore != null) {
				for (Cookie cookie : persistantCookieStore.getCookies()) {
					String cookieString = cookie.getName() + "=" + cookie.getValue() + "; domain=" + cookie.getDomain();

					// Note: this is getting set to two different URLs for Android compatibility reasons. ".expedia.com"
					//       works with ICS, using the url works with 2.1
					cookieManager.setCookie(mUrl, cookieString);
					cookieManager.setCookie(".expedia.com", cookieString);
				}

				CookieSyncManager.getInstance().sync();
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mFrame = new FrameLayout(getActivity());
		if (mWebView == null) {
			mWebView = new WebView(getActivity());

			mWebView.getSettings().setJavaScriptEnabled(true);
			mWebView.getSettings().setLoadWithOverviewMode(true);
			mWebView.getSettings().setUseWideViewPort(true);
			mWebView.setWebViewClient(new WebViewClient() {

				@Override
				public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
					String errorFormatStr = getResources().getString(R.string.web_view_loading_error_TEMPLATE);
					String errorMessage = String.format(errorFormatStr, description);
					Ui.showToast(getActivity(), errorMessage);
				}

				@Override
				public void onPageFinished(WebView webview, String url) {
					//Stop progress spinner
					mWebViewLoaded = true;
					mListener.setLoading(false);

					if (mDisableSignIn) {
						// Insert javascript to remove the signin button
						webview.loadUrl("javascript:(function() { " +
								"document.getElementsByClassName('sign_link')[0].style.visibility='hidden'; " +
								"})()");
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

		if (savedInstanceState != null) {
			mWebViewLoaded = savedInstanceState.getBoolean(INSTANCE_LOADED, false);
			mWebView.restoreState(savedInstanceState);
		}

		if (!mWebViewLoaded) {
			mListener.setLoading(true);
			mWebView.loadUrl(mUrl);
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
				OmnitureTracking.trackPageLoadFlightBaggageFeeOneWay(mContext);
				break;
			}
			case BaggageFeeOutbound: {
				OmnitureTracking.trackPageLoadFlightBaggageFeeOutbound(mContext);
				break;
			}
			case BaggageFeeInbound: {
				OmnitureTracking.trackPageLoadFlightBaggageFeeInbound(mContext);
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

	public interface WebViewFragmentListener {
		public void setLoading(boolean loading);
	}

}
