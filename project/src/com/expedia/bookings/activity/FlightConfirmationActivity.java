package com.expedia.bookings.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.fragment.BlurredBackgroundFragment;
import com.expedia.bookings.fragment.FlightConfirmationFragment;
import com.expedia.bookings.utils.ActionBarNavUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.NavigationButton;
import com.expedia.bookings.widget.NavigationDropdownAdapter;
import com.expedia.bookings.widget.NavigationDropdownAdapter.NoOpButton;

public class FlightConfirmationActivity extends SherlockFragmentActivity {

	private static final boolean QUICKLAUNCH = false;

	private BlurredBackgroundFragment mBgFragment;

	private NavigationButton mNavButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// This is temporary testing code that makes it easy to save/load testing data
		// so that we can quickly test this activity.  DELETE when finished dev!
		if (QUICKLAUNCH) {
			if (savedInstanceState == null) {
				if (Intent.ACTION_MAIN.equals(getIntent().getAction())) {
					Db.loadTestData(this);
				}
				else {
					Db.saveDbForTesting(this);
				}
			}
		}

		setContentView(R.layout.activity_flight_confirmation);

		if (savedInstanceState == null) {
			mBgFragment = new BlurredBackgroundFragment();

			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.add(R.id.background_container, mBgFragment, BlurredBackgroundFragment.TAG);
			ft.add(R.id.content_container, new FlightConfirmationFragment(), FlightConfirmationFragment.TAG);
			ft.commit();
		}
		else {
			mBgFragment = Ui.findSupportFragment(this, BlurredBackgroundFragment.TAG);
		}

		// Action bar setup
		mNavButton = NavigationButton.createNewInstanceAndAttach(this, R.drawable.ic_action_bar_plane,
				R.drawable.ic_action_bar_triangle, getSupportActionBar());
		mNavButton.setDropdownAdapter(new NavigationDropdownAdapter(this, NoOpButton.FLIGHTS));
		mNavButton.setTitle(R.string.booking_complete);
	}

	@Override
	protected void onResume() {
		super.onResume();

		mBgFragment.setFadeEnabled(true);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// dismiss the custom dropdown on touch outside of its PopupWindow
		if (ActionBarNavUtils.removePopupDropdownIfNecessaryOnTouch(ev, mNavButton)) {
			return true;
		}

		return super.dispatchTouchEvent(ev);
	}

	@Override
	public void onBackPressed() {
		if (!ActionBarNavUtils.removePopupDropdownIfNecessaryOnBackPressed(mNavButton)) {
			super.onBackPressed();
		}
	}
}
