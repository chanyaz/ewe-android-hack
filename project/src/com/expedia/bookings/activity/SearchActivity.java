package com.expedia.bookings.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import com.expedia.bookings.utils.ConfirmationUtils;
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

		// #7090: First, check to see if the user last confirmed a booking.  If that is the case,
		//        then we should forward the user to the ConfirmationActivity
		boolean hasSavedConfirmationData = ConfirmationUtils.hasSavedConfirmationData(this);

		// Determine where to route the app
		Class<? extends Activity> routingTarget;
		if (AndroidUtils.getSdkVersion() >= 11 && (getResources().getConfiguration().screenLayout &
				Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE) {
			routingTarget = (hasSavedConfirmationData) ? ConfirmationFragmentActivity.class
					: SearchFragmentActivity.class;
		}
		else {
			routingTarget = (hasSavedConfirmationData) ? ConfirmationActivity.class : PhoneSearchActivity.class;
		}

		// Start the routing intent
		startActivity(new Intent(this, routingTarget));

		// Finish this Activity after routing
		finish();
	}

}
