package com.expedia.bookings.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class FlightWebViewFragment extends Fragment {

	public static final String TAG = FlightWebViewFragment.class.toString();
	public static final String ARG_URL = "ARG_URL";

	private static final String INSTANCE_LOADED = "com.expedia.bookings.fragment.FlightWebViewFragment.INSTANCE_LOADED";

	private String mUrl;
	private WebView mWebView;
	private boolean mWebViewLoaded = false;

	private FlightWebViewFragmentListener mListener;

	public static FlightWebViewFragment newInstance(String url) {
		FlightWebViewFragment frag = new FlightWebViewFragment();

		Bundle args = new Bundle();
		args.putString(ARG_URL, url);
		frag.setArguments(args);

		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mUrl = getArguments().getString(ARG_URL);

		mWebView = new WebView(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mWebView = new WebView(getActivity());

		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setLoadWithOverviewMode(true);
		mWebView.getSettings().setUseWideViewPort(true);
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

		if (!(activity instanceof FlightWebViewFragmentListener)) {
			throw new RuntimeException(
					"FlightWebView activity must implement FlightWebViewFragmentListener!");
		}

		mListener = (FlightWebViewFragmentListener) activity;
	}

	public interface FlightWebViewFragmentListener {
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
