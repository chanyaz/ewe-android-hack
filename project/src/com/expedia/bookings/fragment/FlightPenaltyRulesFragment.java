package com.expedia.bookings.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.server.PersistantCookieStore;
import org.apache.http.cookie.Cookie;

public class FlightPenaltyRulesFragment extends Fragment {

	public static final String TAG = FlightPenaltyRulesFragment.class.toString();
	public static final String ARG_URL = "ARG_URL";

	private static final String INSTANCE_LOADED = "com.expedia.bookings.fragment.FlightPenaltyRulesFragment.INSTANCE_LOADED";

	private FlightPenaltyRulesFragmentListener mListener;
	private String mUrl;
	private WebView mWebView;
	private boolean mWebViewLoaded = false;

	public static FlightPenaltyRulesFragment newInstance(String url) {
		FlightPenaltyRulesFragment frag = new FlightPenaltyRulesFragment();

		Bundle args = new Bundle();
		args.putString(ARG_URL, url);
		frag.setArguments(args);

		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mUrl = getArguments().getString(ARG_URL);

		// Set the Expedia cookies for loading the URL properly
		CookieSyncManager.createInstance(getActivity());
		CookieManager cookieManager = CookieManager.getInstance();

		PersistantCookieStore persistantCookieStore = ExpediaServices.getCookieStore(getActivity());
		if (persistantCookieStore != null) {
			for (Cookie cookie : persistantCookieStore.getCookies()) {
				String cookieString = cookie.getName() + "=" + cookie.getValue() + "; domain=" + cookie.getDomain();
				cookieManager.setCookie(".expedia.com", cookieString);
			}

			CookieSyncManager.getInstance().sync();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mWebView = new WebView(getActivity());

		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageFinished(WebView webview, String url) {
				//Stop progress spinner
				mWebViewLoaded = true;
				mListener.setLoading(false);

				//We insert javascript to remove the signin button
				webview.loadUrl("javascript:(function() { " +
						"document.getElementsByClassName('sign_link')[0].style.visibility='hidden'; " +
						"})()");
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return false;
			}
		});

		if (savedInstanceState != null && savedInstanceState.getBoolean(INSTANCE_LOADED, false)) {
			mWebView.restoreState(savedInstanceState);
		}
		else {
			mListener.setLoading(true);
			mWebView.loadUrl(mUrl);
		}

		return mWebView;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof FlightPenaltyRulesFragmentListener)) {
			throw new RuntimeException(
					"BaggageFeeFragment activity must implement BaggageFeeListener!");
		}

		mListener = (FlightPenaltyRulesFragmentListener) activity;
	}

	public interface FlightPenaltyRulesFragmentListener {
		public void setLoading(boolean loading);
	}

	@Override
	public void onSaveInstanceState(Bundle out) {
		super.onSaveInstanceState(out);
		if (mWebView != null) {
			out.putBoolean(INSTANCE_LOADED, this.mWebViewLoaded);
			mWebView.saveState(out);
		}
	}

}
