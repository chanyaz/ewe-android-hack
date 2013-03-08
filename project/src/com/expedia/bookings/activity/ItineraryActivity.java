package com.expedia.bookings.activity;

import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.fragment.ItinCardFragment;
import com.expedia.bookings.fragment.ItinItemListFragment;
import com.expedia.bookings.fragment.ItinItemListFragment.ItinItemListFragmentListener;
import com.expedia.bookings.fragment.ItineraryMapFragment;
import com.expedia.bookings.maps.SupportMapFragment.SupportMapFragmentListener;
import com.expedia.bookings.utils.DebugMenu;
import com.expedia.bookings.utils.Ui;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.model.CameraPosition;

/**
 * Full-screen Itinerary activity.  Used in tablets.
 */
public class ItineraryActivity extends SherlockFragmentActivity implements ItinItemListFragmentListener,
		OnCameraChangeListener, SupportMapFragmentListener {

	private boolean mTwoPaneMode;

	private String mSelectedItinCardId;

	private boolean mAnimatingToItem;

	private ItinItemListFragment mItinListFragment;
	private ItineraryMapFragment mMapFragment;
	private ItinCardFragment mItinCardFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_itinerary);

		mItinListFragment = Ui.findSupportFragment(this, getString(R.string.tag_itinerary_list));
		mMapFragment = Ui.findSupportFragment(this, getString(R.string.tag_itinerary_map));
		mItinCardFragment = Ui.findSupportFragment(this, getString(R.string.tag_itinerary_card));

		mTwoPaneMode = mMapFragment != null && mMapFragment.isAdded();

		if (mTwoPaneMode) {
			mItinListFragment.setBackgroundColor(getResources().getColor(R.color.itin_list_bg_transparent));
			mItinListFragment.setSimpleMode(true);

			// Setup the correct offset for the map
			float offsetCenterX = getResources().getDimensionPixelSize(R.dimen.itin_simple_list_width) / 2.0f;

			int height = getWindowManager().getDefaultDisplay().getHeight();
			int bottomPadding = getResources().getDimensionPixelSize(R.dimen.itin_map_marker_bottom_padding);
			int markerHeight = getResources().getDrawable(R.drawable.map_pin_normal).getIntrinsicHeight();
			float offsetCenterY = (height / 2.0f) - markerHeight - bottomPadding;

			mMapFragment.setCenterOffset(-offsetCenterX, -offsetCenterY);

			// Start with itin card hidden
			// TODO: If a card is already expanded from before, do not start hidden
			getSupportFragmentManager().beginTransaction().hide(mItinCardFragment).commit();
		}

		mItinListFragment.enableLoadItins();
	}

	@Override
	public void onBackPressed() {
		if (!mItinListFragment.inListMode()) {
			mItinListFragment.setListMode();
		}
		else if (mTwoPaneMode && mItinCardFragment.isVisible()) {
			mItinListFragment.setListMode();
			getSupportFragmentManager().beginTransaction().hide(mItinCardFragment).commit();
		}
		else {
			super.onBackPressed();
		}
	}

	private void showPopupWindow(ItinCardData data) {
		// Don't react if it's the same card as before being clicked
		String id = data.getId();
		if (id.equals(mSelectedItinCardId)) {
			return;
		}

		mSelectedItinCardId = id;

		if (mTwoPaneMode) {
			if (!mItinCardFragment.isHidden()) {
				getSupportFragmentManager().beginTransaction().hide(mItinCardFragment).commit();
			}

			mAnimatingToItem = true;

			mMapFragment.showItinItem(data);
			mItinCardFragment.showItinDetails(data);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Action bar

	private MenuItem mLogOutMenuItem;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		getSupportMenuInflater().inflate(R.menu.menu_itinerary, menu);

		mLogOutMenuItem = menu.findItem(R.id.menu_log_out);

		DebugMenu.onCreateOptionsMenu(this, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		DebugMenu.onPrepareOptionsMenu(this, menu);

		mLogOutMenuItem.setVisible(User.isLoggedIn(this));

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: {
			finish();
			return true;
		}
		case R.id.menu_log_out: {
			mItinListFragment.accountLogoutClicked();
			return true;
		}
		case R.id.menu_settings: {
			// Possible TODO: Reset the activity when settings are changed?
			Intent intent = new Intent(this, TabletPreferenceActivity.class);
			startActivity(intent);
			return true;
		}
		}

		if (DebugMenu.onOptionsItemSelected(this, item)) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	//////////////////////////////////////////////////////////////////////////
	// ItinItemListFragmentListener

	@Override
	public void onItinItemListFragmentAttached(ItinItemListFragment frag) {
		// Do nothing
	}

	@Override
	public void onItinCardClicked(ItinCardData data) {
		showPopupWindow(data);
	}

	//////////////////////////////////////////////////////////////////////////
	// OnCameraChangeListener

	@Override
	public void onCameraChange(CameraPosition position) {
		if (mAnimatingToItem) {
			getSupportFragmentManager().beginTransaction().show(mItinCardFragment).commit();
			mAnimatingToItem = false;
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// SupportMapFragmentListener

	@Override
	public void onMapLayout() {
		if (mTwoPaneMode) {
			ItinCardData data = mItinListFragment.getSelectedItinCardData();
			if (data != null) {
				showPopupWindow(data);
			}
		}
	}
}
