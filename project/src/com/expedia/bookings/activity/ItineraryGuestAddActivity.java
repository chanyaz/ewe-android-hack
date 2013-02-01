package com.expedia.bookings.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.expedia.bookings.R;
import com.expedia.bookings.fragment.ItineraryGuestAddFragment;
import com.expedia.bookings.fragment.LoginFragment.TitleSettable;
import com.expedia.bookings.utils.Ui;

public class ItineraryGuestAddActivity extends SherlockFragmentActivity implements TitleSettable {

	private static final String TAG_GUEST_ADD_FRAGMENT = "TAG_GUEST_ADD_FRAGMENT";
	private static final String STATE_TITLE = "STATE_TITLE";

	private ItineraryGuestAddFragment mAddGuestItinFragment;
	private String mTitle;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_itinerary_guest_add);

		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(STATE_TITLE)) {
				setTitle(savedInstanceState.getString(STATE_TITLE));
			}
		}

		// Actionbar
		ActionBar actionBar = this.getSupportActionBar();
		actionBar.setIcon(R.drawable.ic_logo_flights);

		actionBar.setDisplayUseLogoEnabled(false);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		if (mTitle != null) {
			setTitle(mTitle);
		}
		else {
			setTitle(getString(R.string.find_itinerary));
		}

		// Create/grab the login fragment
		mAddGuestItinFragment = Ui.findSupportFragment(this, TAG_GUEST_ADD_FRAGMENT);
		if (mAddGuestItinFragment == null) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			mAddGuestItinFragment = ItineraryGuestAddFragment.newInstance();
			ft.add(R.id.fragment_container, mAddGuestItinFragment, TAG_GUEST_ADD_FRAGMENT);
			ft.commit();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mTitle != null) {
			outState.putString(STATE_TITLE, mTitle);
		}
	}

	@Override
	public void setActionBarTitle(String title) {
		mTitle = title;
		ActionBar actionBar = this.getSupportActionBar();
		actionBar.setTitle(mTitle);
	}
}
