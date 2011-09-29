package com.expedia.bookings.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import com.mobiata.android.util.AndroidUtils;

/**
 * This is a routing Activity that points users towards either the phone or
 * tablet version of this app.
 * 
 * It is named SearchActivity for historical reasons; this was the original
 * starting Activity for older versions of EH, and we don't want to break any
 * future installs (which may have setup quick links to EH).
 */
public class SearchActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Determine where to route the app
		Intent routingIntent;
		if (AndroidUtils.getSdkVersion() >= 11 && (getResources().getConfiguration().screenLayout &
				Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE) {
			routingIntent = new Intent(this, TabletActivity.class);
		}
		else {
			routingIntent = new Intent(this, PhoneSearchActivity.class);
		}

		// Start the routing intent
		startActivity(routingIntent);

		// Finish this Activity after routing
		finish();
	}

}
