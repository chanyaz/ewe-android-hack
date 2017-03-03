package com.expedia.bookings.activity;

import android.app.ActionBar;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;

import com.expedia.bookings.R;
import com.expedia.bookings.fragment.ItinGuestAddFragment;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.ItineraryLoaderLoginExtender;

public class ItineraryGuestAddActivity extends FragmentActivity {

	public static final String ERROR_FETCHING_GUEST_ITINERARY = "ERROR_FETCHING_GUEST_ITINERARY";
	public static final String ERROR_FETCHING_REGISTERED_USER_ITINERARY = "ERROR_FETCHING_REGISTERED_USER_ITINERARY";

	private static final String TAG_GUEST_ADD_FRAGMENT = "TAG_GUEST_ADD_FRAGMENT";
	private ItinGuestAddFragment mAddGuestItinFragment;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!ExpediaBookingApp.useTabletInterface()) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		if (shouldBail()) {
			return;
		}

		setContentView(R.layout.activity_itinerary_guest_add);

		// Actionbar
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayUseLogoEnabled(true);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);

		setTitle(getString(R.string.find_guest_itinerary));

		// Create/grab the login fragment
		mAddGuestItinFragment = Ui.findSupportFragment(this, TAG_GUEST_ADD_FRAGMENT);
		if (mAddGuestItinFragment == null) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			if (ERROR_FETCHING_GUEST_ITINERARY.equals(this.getIntent().getAction())) {
				mAddGuestItinFragment = ItinGuestAddFragment
					.fetchingGuestItinFailedInstance(new ItineraryLoaderLoginExtender());
			}
			else if (ERROR_FETCHING_REGISTERED_USER_ITINERARY.equals(this.getIntent().getAction())) {
				mAddGuestItinFragment = ItinGuestAddFragment
					.fetchingRegisteredUserItinFailedInstance(new ItineraryLoaderLoginExtender());
			}
			else {
				mAddGuestItinFragment = ItinGuestAddFragment.newInstance(new ItineraryLoaderLoginExtender());
			}
			ft.add(R.id.fragment_container, mAddGuestItinFragment, TAG_GUEST_ADD_FRAGMENT);
			ft.commit();
		}
	}

	private boolean shouldBail() {
		return !ExpediaBookingApp.useTabletInterface() && !getResources().getBoolean(R.bool.portrait);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: {
			finish();
			return true;
		}
		}
		return super.onOptionsItemSelected(item);
	}
}
