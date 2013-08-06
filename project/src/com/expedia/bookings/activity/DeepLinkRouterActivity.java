package com.expedia.bookings.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.expedia.bookings.utils.NavUtils;
import com.mobiata.android.util.Ui;

/**
 * This class acts as a router for incoming deep links.  It seems a lot
 * easier to just route through one Activity rather than try to handle it
 * all in the manifest (where you may need to handle the same scheme in multiple
 * possible activities).
 */
public class DeepLinkRouterActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Handle incoming intents
		Intent intent = getIntent();
		Uri data = intent.getData();
		String host = data.getHost();
		if (host.equals("home")) {
			NavUtils.goToLaunchScreen(this, true);
		}
		else {
			Ui.showToast(this, "Cannot yet handle data: " + data);
		}

		// This Activity should never fully launch
		finish();
	}

}
