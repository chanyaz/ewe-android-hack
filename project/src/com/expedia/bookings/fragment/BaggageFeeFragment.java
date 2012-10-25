package com.expedia.bookings.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.SupportUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;

@SuppressLint("SetJavaScriptEnabled")
public class BaggageFeeFragment extends Fragment {

	public static final String TAG_ORIGIN = "TAG_ORIGIN";
	public static final String TAG_DESTINATION = "TAG_DESTINATION";
	public static final String ARG_LEG_POSITION = "ARG_LEG_POSITION";

	public static final String INSTANCE_LOADED = "INSTANCE_LOADED";

	private BaggageFeeListener mListener;
	private WebView mWebView;
	private boolean mWebViewLoaded = false;
	private FrameLayout mFrame;

	public static BaggageFeeFragment newInstance(String origin, String destination, int legPosition) {
		BaggageFeeFragment fragment = new BaggageFeeFragment();
		Bundle args = new Bundle();
		args.putString(TAG_ORIGIN, origin);
		args.putString(TAG_DESTINATION, destination);
		args.putInt(ARG_LEG_POSITION, legPosition);
		fragment.setArguments(args);
		fragment.setRetainInstance(true);
		return fragment;
	}

	@Override
	public void onStart() {
		super.onStart();
		OmnitureTracking.trackPageLoadFlightBaggageFee(getActivity(), getArguments().getInt(ARG_LEG_POSITION, 0));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		String origin = "";
		String destination = "";

		//Pull in origin and destination from the savedInstanceState
		if (getArguments().containsKey(TAG_ORIGIN) && getArguments().containsKey(TAG_DESTINATION)) {
			origin = getArguments().getString(TAG_ORIGIN);
			destination = getArguments().getString(TAG_DESTINATION);
		}
		else {
			Log.e("BaggageFeeActivity requires that the intent contains origin and destination values");
			mListener.exit();
		}

		mFrame = new FrameLayout(getActivity());

		if (mWebView == null) {
			mWebView = new WebView(getActivity());
			mWebView.getSettings().setJavaScriptEnabled(true);

			mWebView.setWebViewClient(new WebViewClient() {

				private boolean mLoaded = false;
				private String mUrl = "";

				public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
					String errorFormatStr = getResources().getString(R.string.baggage_fee_loading_error_TEMPLATE);
					String errorMessage = String.format(errorFormatStr, description);
					Ui.showToast(getActivity(), errorMessage);
				}

				@Override
				public void onPageFinished(WebView webview, String url)
				{
					Log.i("ON PAGE FINISH");

					mUrl = url;

					//Stop progress spinner
					mListener.setLoading(false);

					//We insert javascript to remove the signin button
					webview.loadUrl("javascript:(function() { " +
							"document.getElementsByClassName('sign_link')[0].style.visibility='hidden'; " +
							"})()");

					//Set mLoaded to true, allowing us to use ACTION_VIEW for any future url clicked
					mLoaded = true;
					mWebViewLoaded = true;
				}

				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					//If the first page is loaded and a user clicks a link, we want to open it in an external application
					if (url != null && mLoaded && !url.equalsIgnoreCase(mUrl)) {
						view.getContext().startActivity(
								new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
						return true;
					}
					else {
						return false;
					}
				}
			});

		}
		if (savedInstanceState != null) {
			mWebViewLoaded = savedInstanceState.getBoolean(INSTANCE_LOADED, false);
			mWebView.restoreState(savedInstanceState);
		}
		if (!mWebViewLoaded) {
			//TODO:We need to set the correct url based on Point of Sale
			String url = SupportUtils.getBaggageFeeUrl(origin, destination);
			Log.i("Loading url: " + url);
			mListener.setLoading(true);
			mWebView.loadUrl(url);
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
	public void onResume() {
		super.onResume();

		if (mFrame != null && mWebView != null) {
			mFrame.addView(mWebView);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof BaggageFeeListener)) {
			throw new RuntimeException(
					"BaggageFeeFragment activity must implement BaggageFeeListener!");
		}

		mListener = (BaggageFeeListener) activity;
	}

	public interface BaggageFeeListener {
		public void exit();

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
