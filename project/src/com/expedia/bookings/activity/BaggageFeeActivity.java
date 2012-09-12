package com.expedia.bookings.activity;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Window;
import com.expedia.bookings.R;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class BaggageFeeActivity extends SherlockActivity {

	public static final String TAG_ORIGIN = "TAG_ORIGIN";
	public static final String TAG_DESTINATION = "TAG_DESTINATION";

	/** Called when the activity is first created. */
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		WebView webview = new WebView(this);
		setContentView(webview);

		webview.getSettings().setJavaScriptEnabled(true);

		String origin = "";
		String destination = "";

		//Pull in origin and destination from the intent
		Intent intent = getIntent();
		if (intent.hasExtra(TAG_ORIGIN) && intent.hasExtra(TAG_DESTINATION)) {
			origin = intent.getStringExtra(TAG_ORIGIN);
			destination = intent.getStringExtra(TAG_DESTINATION);
		}
		else {
			Log.e("BaggageFeeActivity requires that the intent contains origin and destination values");
			finish();
			return;
		}

		webview.setWebViewClient(new WebViewClient() {

			private boolean mLoaded = false;

			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				String errorFormatStr = getResources().getString(R.string.baggage_fee_loading_error_TEMPLATE);
				String errorMessage = String.format(errorFormatStr, description);
				Ui.showToast(BaggageFeeActivity.this, errorMessage);
			}

			@Override
			public void onPageFinished(WebView webview, String url)
			{
				//Stop progress spinner
				getSherlock().setProgressBarIndeterminateVisibility(false);
				
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

		String urlFormat = getString(R.string.baggage_fee_url_TEMPLATE);
		String url = String.format(urlFormat, origin, destination);
		Log.i("Loading url: " + url);
		getSherlock().setProgressBarIndeterminateVisibility(true);
		webview.loadUrl(url);
	}

}
