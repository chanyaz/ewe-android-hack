package com.expedia.bookings.activity;

import android.os.Bundle;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.expedia.bookings.fragment.LaunchFragment;
import com.expedia.bookings.utils.DebugMenu;
import com.expedia.bookings.utils.Ui;

public class LaunchActivity extends SherlockFragmentActivity {

	private LaunchFragment mLaunchFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			mLaunchFragment = LaunchFragment.newInstance();
			getSupportFragmentManager().beginTransaction()
					.add(android.R.id.content, mLaunchFragment, LaunchFragment.TAG).commit();
		}
		else {
			mLaunchFragment = Ui.findSupportFragment(this, LaunchFragment.TAG);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		DebugMenu.onCreateOptionsMenu(this, menu);
		return super.onCreateOptionsMenu(menu);
	}

}