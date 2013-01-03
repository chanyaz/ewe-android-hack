package com.expedia.bookings.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.expedia.bookings.R;
import com.expedia.bookings.fragment.WebViewFragment;
import com.expedia.bookings.utils.Ui;

public class WebViewActivity extends SherlockFragmentActivity implements WebViewFragment.WebViewFragmentListener {

	public static final String ARG_URL = "ARG_URL";
	public static final String ARG_STYLE_RES_ID = "ARG_STYLE_RES_ID";
	public static final String ARG_TITLE_RES_ID = "ARG_TITLE_RES_ID";
	public static final String ARG_DISABLE_SIGN_IN = "ARG_DISABLE_SIGN_IN";
	public static final String ARG_INJECT_EXPEDIA_COOKIES = "ARG_INJECT_EXPEDIA_COOKIES";
	public static final String ARG_TRACKING_NAME = "ARG_TRACKING_NAME";

	private WebViewFragment mFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		Bundle extras = getIntent().getExtras();
		setTheme(extras.getInt(ARG_STYLE_RES_ID, R.style.FlightTheme));
		setTitle(extras.getInt(ARG_TITLE_RES_ID, R.string.legal_information));

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		if (savedInstanceState == null) {
			String url = extras.getString(ARG_URL);
			boolean disableSignIn = extras.getBoolean(ARG_DISABLE_SIGN_IN, false);
			boolean injectExpediaCookies = extras.getBoolean(ARG_INJECT_EXPEDIA_COOKIES, false);
			String name = extras.getString(ARG_TRACKING_NAME);
			mFragment = WebViewFragment.newInstance(url, disableSignIn, injectExpediaCookies, name);

			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.replace(android.R.id.content, mFragment, WebViewFragment.TAG);
			ft.commit();
		}
		else {
			mFragment = Ui.findSupportFragment(this, WebViewFragment.TAG);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: {
			finish();
		}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void setLoading(boolean loading) {
		getSherlock().setProgressBarIndeterminateVisibility(loading);
	}

}
