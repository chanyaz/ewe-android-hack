package com.expedia.bookings.activity;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.FontCache.Font;
import com.mobiata.android.util.Ui;
import com.squareup.phrase.Phrase;

public class RestrictedProfileActivity extends FragmentActivity {

	public static Intent createIntent(Context context) {
		Intent restrictedProfileIntent = new Intent(context, RestrictedProfileActivity.class);
		return restrictedProfileIntent;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_restricted_profile);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);

		TextView messageTv = Ui.findView(this, R.id.restricted_profile_message);
		messageTv.setText(
			Phrase.from(this, R.string.restricted_profile_sign_in_message_TEMPLATE)
				.put("brand", BuildConfig.brand)
				.format());

		FontCache.setTypeface(messageTv, Font.ROBOTO_LIGHT);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
