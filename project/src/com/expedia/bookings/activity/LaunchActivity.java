package com.expedia.bookings.activity;

import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.fragment.LaunchFragment;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.DebugMenu;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;
import com.mobiata.android.hockey.HockeyPuck;
import com.mobiata.android.util.AndroidUtils;

public class LaunchActivity extends SherlockFragmentActivity {

	private static final int REQUEST_SETTINGS = 1;

	private LaunchFragment mLaunchFragment;

	private HockeyPuck mHockeyPuck;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().setFormat(android.graphics.PixelFormat.RGBA_8888);

		if (savedInstanceState == null) {
			mLaunchFragment = LaunchFragment.newInstance();
			getSupportFragmentManager().beginTransaction()
					.add(android.R.id.content, mLaunchFragment, LaunchFragment.TAG).commit();
		}
		else {
			mLaunchFragment = Ui.findSupportFragment(this, LaunchFragment.TAG);
		}

		// HockeyApp init
		mHockeyPuck = new HockeyPuck(this, Codes.HOCKEY_APP_ID, !AndroidUtils.isRelease(this));
		mHockeyPuck.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();

		//HockeyApp crash
		mHockeyPuck.onResume();
	}

	@Override
	public void onStart() {
		super.onStart();
		OmnitureTracking.trackPageLoadLaunchScreen(this);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		mHockeyPuck.onSaveInstanceState(outState);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_SETTINGS && resultCode != RESULT_CANCELED) {
			mLaunchFragment = LaunchFragment.newInstance();
			getSupportFragmentManager().beginTransaction()
					.replace(android.R.id.content, mLaunchFragment, LaunchFragment.TAG).commitAllowingStateLoss();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_launch, menu);

		DebugMenu.onCreateOptionsMenu(this, menu);
		mHockeyPuck.onCreateOptionsMenu(menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		DebugMenu.onPrepareOptionsMenu(this, menu);

		mHockeyPuck.onPrepareOptionsMenu(menu);

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.settings:
			Intent intent = new Intent(this, ExpediaBookingPreferenceActivity.class);
			startActivityForResult(intent, REQUEST_SETTINGS);
			return true;
		case R.id.about:
			Intent aboutIntent = new Intent(this, AboutActivity.class);
			startActivity(aboutIntent);
			return true;
		}

		if (DebugMenu.onOptionsItemSelected(this, item) || mHockeyPuck.onOptionsItemSelected(item)) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
