package com.expedia.bookings.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.DebugInfoUtils;
import com.mobiata.android.SocialUtils;

public class AboutWebViewActivity extends WebViewActivity {
	private static final String ARG_SHOW_EMAIL_BUTTON = "ARG_SHOW_EMAIL_BUTTON";

	private MenuItem mEmailMenuItem;

	private boolean mLoading = false;
	private boolean mShowEmailButton = false;

	public static class IntentBuilder extends WebViewActivity.IntentBuilder {
		public IntentBuilder(Context context) {
			super(context);
			getIntent().setClass(context, AboutWebViewActivity.class);
			this.setDomStorage(true);
		}

		public IntentBuilder setShowEmailButton(boolean showEmailButton) {
			getIntent().putExtra(ARG_SHOW_EMAIL_BUTTON, showEmailButton);
			return this;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (shouldBail()) {
			return;
		}
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();

		if (intent.hasExtra(ARG_SHOW_EMAIL_BUTTON)) {
			mShowEmailButton = extras.getBoolean(ARG_SHOW_EMAIL_BUTTON);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_webview, menu);

		mEmailMenuItem = menu.findItem(R.id.menu_email);
		if (mEmailMenuItem != null) {
			mEmailMenuItem.setVisible(mShowEmailButton && !mLoading);
		}
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_email: {
			sendSupportEmail();
			return true;
		}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void setLoading(boolean loading) {
		super.setLoading(loading);

		mLoading = loading;

		if (mEmailMenuItem != null) {
			mEmailMenuItem.setVisible(mShowEmailButton && !mLoading);
			supportInvalidateOptionsMenu();
		}
	}

	private void sendSupportEmail() {
		SocialUtils.email(this, getString(R.string.email_app_support), "", DebugInfoUtils.generateEmailBody(this));
	}

	private boolean shouldBail() {
		return !getResources().getBoolean(R.bool.portrait);
	}

}
