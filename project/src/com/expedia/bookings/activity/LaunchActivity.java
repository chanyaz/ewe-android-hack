package com.expedia.bookings.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.mobiata.android.util.AndroidUtils;

public class LaunchActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (AndroidUtils.getSdkVersion() < 11) {
			startActivity(new Intent(this, SearchActivity.class));
		}
		else {
			startActivity(new Intent(this, HoneycombSearchActivity.class));
		}
		finish();
	}
}
