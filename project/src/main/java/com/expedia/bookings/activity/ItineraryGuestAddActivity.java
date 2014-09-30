package com.expedia.bookings.activity;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;

import com.expedia.bookings.R;
import com.expedia.bookings.fragment.ItinGuestAddFragment;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.ItineraryLoaderLoginExtender;

public class ItineraryGuestAddActivity extends FragmentActivity {

	private static final String TAG_GUEST_ADD_FRAGMENT = "TAG_GUEST_ADD_FRAGMENT";

	private ItinGuestAddFragment mAddGuestItinFragment;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_itinerary_guest_add);

		// Actionbar
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayUseLogoEnabled(true);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);

		setTitle(getString(R.string.find_itinerary));

		// Create/grab the login fragment
		mAddGuestItinFragment = Ui.findSupportFragment(this, TAG_GUEST_ADD_FRAGMENT);
		if (mAddGuestItinFragment == null) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			mAddGuestItinFragment = ItinGuestAddFragment.newInstance(new ItineraryLoaderLoginExtender());
			ft.add(R.id.fragment_container, mAddGuestItinFragment, TAG_GUEST_ADD_FRAGMENT);
			ft.commit();
		}
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
