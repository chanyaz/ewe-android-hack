package com.expedia.bookings.fragment;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;

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

@SuppressLint("SetJavaScriptEnabled")
public class BaggageFeeFragment extends Fragment {

	public static final String TAG_ORIGIN = "TAG_ORIGIN";
	public static final String TAG_DESTINATION = "TAG_DESTINATION";

	BaggageFeeListener mListener;

	public static BaggageFeeFragment newInstance(String origin, String destination) {
		BaggageFeeFragment fragment = new BaggageFeeFragment();
		Bundle args = new Bundle();
		args.putString(TAG_ORIGIN, origin);
		args.putString(TAG_DESTINATION, destination);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		WebView webview = new WebView(getActivity());

		webview.getSettings().setJavaScriptEnabled(true);

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

		webview.setWebViewClient(new WebViewClient() {

			private boolean mLoaded = false;

			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				String errorFormatStr = getResources().getString(R.string.baggage_fee_loading_error_TEMPLATE);
				String errorMessage = String.format(errorFormatStr, description);
				Ui.showToast(getActivity(), errorMessage);
			}

			@Override
			public void onPageFinished(WebView webview, String url)
			{
				//Stop progress spinner
				mListener.setLoading(false);

				//We insert javascript to remove the signin button
				webview.loadUrl("javascript:(function() { " +
						"document.getElementsByClassName('sign_link')[0].style.visibility='hidden'; " +
						"})()");

				//Set mLoaded to true, allowing us to use ACTION_VIEW for any future url clicked
				mLoaded = true;
			}

			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				//If the first page is loaded and a user clicks a link, we want to open it in an external application
				if (url != null && mLoaded) {
					view.getContext().startActivity(
							new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
					return true;
				}
				else {
					return false;
				}
			}
		});

		//TODO:We need to set the correct url based on Point of Sale
		String urlFormat = "http://www.expedia.com/Flights-BagFees?originapt=%s&destinationapt=%s";
		String url = String.format(urlFormat, origin, destination);
		Log.i("Loading url: " + url);
		mListener.setLoading(true);

		webview.loadUrl(url);

		return webview;
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
}
