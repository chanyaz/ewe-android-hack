package com.expedia.bookings.activity;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

	private static final String ARG_UNIQUE_ID = "ARG_UNIQUE_ID";

	private String mUniqueId;
	private ItinCardData mCurrentData;

	public static Intent createIntent(Context context, String uniqueId) {
		Intent intent = new Intent(context, StandaloneShareActivity.class);
		intent.putExtra(StandaloneShareActivity.ARG_UNIQUE_ID, uniqueId);
		return intent;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getIntent().hasExtra(ARG_UNIQUE_ID)) {
			mUniqueId = getIntent().getStringExtra(ARG_UNIQUE_ID);
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

		mCurrentData = lookupItinCardData(mUniqueId);
		if (mCurrentData == null) {
			Log.w("Itin card not found for this id: " + mUniqueId);
			finish();
			return;
		}
		
		ItinContentGenerator<?> generator = ItinContentGenerator.createGenerator(this, mCurrentData);

		FragmentTransaction ft = fm.beginTransaction();
		ft.addToBackStack(null);
		SocialMessageChooserDialogFragment.newInstance(generator).show(ft, "shareDialog");
	}

	private ItinCardData lookupItinCardData(String uniqueId) {
		ItineraryManager manager = ItineraryManager.getInstance();
		List<ItinCardData> datas = manager.getItinCardData();

		if (datas != null) {
			for (ItinCardData data : datas) {
				if (mUniqueId.equals(data.getId())) {
					return data;
				}
			}
		}

		return null;
	}
}
