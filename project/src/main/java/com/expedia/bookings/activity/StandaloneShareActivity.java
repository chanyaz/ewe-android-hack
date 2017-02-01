package com.expedia.bookings.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;

import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.dialog.SocialMessageChooserDialogFragment;
import com.expedia.bookings.widget.itin.ItinContentGenerator;
import com.mobiata.android.Log;

// This is an activity whose sole purpose is to display the "Share" dialog fragment for
// itin cards. It's being called from "share" actions on expanded notifications.
public class StandaloneShareActivity extends FragmentActivity {

	public static Intent createIntent(Context context, String uniqueId) {
		Intent intent = new Intent(context, StandaloneShareActivity.class);

		// Android OS needs a way to differentiate multiple intents to this same activity.
		// http://developer.android.com/reference/android/content/Intent.html#filterEquals(android.content.Intent)
		String uriString = "expedia://notification/share/" + uniqueId;
		intent.setData(Uri.parse(uriString));

		return intent;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String uniqueId = getIntent().getData().getLastPathSegment();
		ItineraryManager manager = ItineraryManager.getInstance();
		ItinCardData data = manager.getItinCardDataFromItinId(uniqueId);

		if (data == null) {
			Log.w("Itin card not found for this id: " + uniqueId);
			finish();
			return;
		}

		final FragmentManager fm = getSupportFragmentManager();
		fm.addOnBackStackChangedListener(new OnBackStackChangedListener() {
			@Override
			public void onBackStackChanged() {
				if (fm.getBackStackEntryCount() == 0) {
					finish();
				}
			}
		});

		FragmentTransaction ft = fm.beginTransaction();
		ft.addToBackStack(null);

		ItinContentGenerator<?> generator = ItinContentGenerator.createGenerator(this, data);
		SocialMessageChooserDialogFragment.newInstance(generator).show(ft, "shareDialog");
	}
}
