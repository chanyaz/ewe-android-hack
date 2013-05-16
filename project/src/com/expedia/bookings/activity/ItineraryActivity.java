package com.expedia.bookings.activity;

import java.util.Collection;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.View;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.expedia.bookings.R;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.data.trips.ItineraryManager.ItinerarySyncListener;
import com.expedia.bookings.data.trips.Trip;
import com.expedia.bookings.fragment.ConfirmLogoutDialogFragment.DoLogoutListener;
import com.expedia.bookings.fragment.ItinCardFragment;
import com.expedia.bookings.fragment.ItinItemListFragment;
import com.expedia.bookings.fragment.ItinItemListFragment.ItinItemListFragmentListener;
import com.expedia.bookings.fragment.ItineraryMapFragment;
import com.expedia.bookings.fragment.ItineraryMapFragment.ItineraryMapFragmentListener;
import com.expedia.bookings.maps.SupportMapFragment.SupportMapFragmentListener;
import com.expedia.bookings.utils.DebugMenu;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.ItinListView;
import com.expedia.bookings.widget.ItinListView.OnListModeChangedListener;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.model.CameraPosition;
import com.mobiata.android.util.AndroidUtils;

/**
 * Full-screen Itinerary activity.  Used in tablets.
 */
public class ItineraryActivity extends SherlockFragmentActivity implements ItinItemListFragmentListener,
		OnCameraChangeListener, SupportMapFragmentListener, DoLogoutListener, ItineraryMapFragmentListener,
		ItinerarySyncListener, OnListModeChangedListener {

	public static final int REQUEST_SETTINGS = 1;

	private boolean mTwoPaneMode;

	private String mSelectedItinCardId;

	private boolean mAnimatingToItem;
	private boolean mItemHasDetails;

	private View mFallbackPatternView;

	private ItinItemListFragment mItinListFragment;
	private ItineraryMapFragment mMapFragment;
	private ItinCardFragment mItinCardFragment;

	// #854: There is a very subtle possible timing issue where we can try to modify
	// the fragment stack after onSaveInstanceState().  This helps prevent that.
	private boolean mFragmentSafe;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getResources().getBoolean(R.bool.portrait)) {
			requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		}

		setContentView(R.layout.activity_itinerary);

		getWindow().setBackgroundDrawable(null);

		mFallbackPatternView = Ui.findView(this, R.id.fallback_pattern);

		mItinListFragment = Ui.findSupportFragment(this, getString(R.string.tag_itinerary_list));
		mMapFragment = Ui.findSupportFragment(this, getString(R.string.tag_itinerary_map));
		mItinCardFragment = Ui.findSupportFragment(this, getString(R.string.tag_itinerary_card));

		mTwoPaneMode = mMapFragment != null && mMapFragment.isAdded();

		if (mTwoPaneMode) {
			mItinListFragment.setBackgroundColor(getResources().getColor(R.color.itin_list_bg_transparent));
			mItinListFragment.setSimpleMode(true);

			// Setup the correct offset for the map
			float listWidth = getResources().getDimensionPixelSize(R.dimen.itin_simple_list_width);
			float offsetCenterX = listWidth / 2.0f;

			Point size = AndroidUtils.getScreenSize(this);
			int height = size.y;
			int bottomPadding = getResources().getDimensionPixelSize(R.dimen.itin_map_marker_bottom_padding);
			int markerHeight = getResources().getDrawable(R.drawable.map_pin_normal).getIntrinsicHeight();
			float offsetCenterY = (height / 2.0f) - markerHeight - bottomPadding;

			mMapFragment.setCenterOffset(-offsetCenterX, -offsetCenterY);

			float width = size.x;
			float usableWidth = 1.0f - (listWidth / width);
			float horizCenterPercent = .5f + ((listWidth / width) / 2.0f);
			mMapFragment.setUsableArea(usableWidth, horizCenterPercent);

			// Start with itin card hidden
			// TODO: If a card is already expanded from before, do not start hidden
			getSupportFragmentManager().beginTransaction().hide(mItinCardFragment).commit();
		}

		mItinListFragment.enableLoadItins();
		ItineraryManager.getInstance().addSyncListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		ItineraryManager.getInstance().removeSyncListener(this);
	}

	@Override
	protected void onPostResume() {
		super.onPostResume();

		mFragmentSafe = true;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		mFragmentSafe = false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_SETTINGS && resultCode == ExpediaBookingPreferenceActivity.RESULT_CHANGED_PREFS) {
			// Just to be safe, hide the popup window (as the itin may have been removed)
			mItinListFragment.setListMode();
			hidePopupWindow();
		}
	}

	@Override
	public void onBackPressed() {
		if (!mItinListFragment.inListMode()) {
			mItinListFragment.setListMode();
		}
		else if (mTwoPaneMode && mItinCardFragment.isVisible()) {
			mItinListFragment.setListMode();
			hidePopupWindow();
			mFallbackPatternView.setVisibility(View.GONE);
		}
		else {
			super.onBackPressed();
		}
	}

	private void showPopupWindow(ItinCardData data, boolean animate) {
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

			if (data.getLocation() == null || (data.getLocation().latitude == 0 && data.getLocation().latitude == 0)) {
				if (data.hasDetailData()) {
					// Item has no location but has details to show in a popup, show the default background
					mFallbackPatternView.setVisibility(View.VISIBLE);
				}
				mAnimatingToItem = false;
			}
			else {
				// Item has location, animate to it
				mFallbackPatternView.setVisibility(View.GONE);
				mAnimatingToItem = mMapFragment.showItinItem(data, animate);
			}

			mItemHasDetails = mItinCardFragment.showItinDetails(data);

			if (!mAnimatingToItem && mItemHasDetails) {
				getSupportFragmentManager().beginTransaction().show(mItinCardFragment).commit();
			}
		}
	}

	private void hidePopupWindow() {
		if (mTwoPaneMode) {
			if (!mItinCardFragment.isHidden()) {
				getSupportFragmentManager().beginTransaction().hide(mItinCardFragment).commit();
			}

			mMapFragment.hideItinItem();

			mItinCardFragment.showItinDetails(null);

			mSelectedItinCardId = null;
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Action bar

	private MenuItem mLogInMenuItem;
	private MenuItem mLogOutMenuItem;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		getSupportMenuInflater().inflate(R.menu.menu_itinerary, menu);
		getSupportMenuInflater().inflate(R.menu.menu_fragment_standard, menu);

		mLogInMenuItem = menu.findItem(R.id.menu_log_in);
		mLogOutMenuItem = menu.findItem(R.id.menu_log_out);

		DebugMenu.onCreateOptionsMenu(this, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		DebugMenu.onPrepareOptionsMenu(this, menu);

		boolean loggedIn = User.isLoggedIn(this);
		mLogInMenuItem.setVisible(!loggedIn);
		mLogOutMenuItem.setVisible(loggedIn);

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: {
			finish();
			return true;
		}
		case R.id.menu_add_guest_itinerary: {
			mItinListFragment.startAddGuestItinActivity();
			return true;
		}
		case R.id.menu_log_in: {
			mItinListFragment.startLoginActivity();
			return true;
		}
		case R.id.menu_log_out: {
			mItinListFragment.accountLogoutClicked();
			return true;
		}
		case R.id.menu_settings: {
			Intent intent = new Intent(this, TabletPreferenceActivity.class);
			startActivityForResult(intent, REQUEST_SETTINGS);
			return true;
		}
		case R.id.menu_about: {
			Intent intent = new Intent(this, AboutActivity.class);
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
		showPopupWindow(data, true);
	}

	//////////////////////////////////////////////////////////////////////////
	// OnCameraChangeListener

	@Override
	public void onCameraChange(CameraPosition position) {
		if (mAnimatingToItem && mItemHasDetails && mFragmentSafe) {
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
				showPopupWindow(data, false);
			}
			else {
				mMapFragment.showFallback(false);
			}
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// DoLogoutListener

	@Override
	public void doLogout() {
		mItinListFragment.doLogout();

		hidePopupWindow();
	}

	//////////////////////////////////////////////////////////////////////////
	// ItineraryMapFragmentListener

	@Override
	public void onItinMarkerClicked(ItinCardData data) {
		mItinListFragment.showItinCard(data.getId(), true);
		showPopupWindow(data, true);
	}

	//////////////////////////////////////////////////////////////////////////
	// ItinerarySyncListener

	@Override
	public void onTripAdded(Trip trip) {
		// Do nothing
	}

	@Override
	public void onTripUpdated(Trip trip) {
		// Do nothing
	}

	@Override
	public void onTripUpdateFailed(Trip trip) {
		// Do nothing
	}

	@Override
	public void onTripRemoved(Trip trip) {
		// Do nothing
	}

	@Override
	public void onSyncFailure(ItineraryManager.SyncError error) {
		// Do nothing
	}

	@Override
	public void onSyncFinished(Collection<Trip> trips) {
		if (mItinCardFragment != null && mItinCardFragment.isVisible()) {
			ItinCardData selectedCardData = mItinCardFragment.getItinCardData();
			String selectedCardId = selectedCardData.getTripComponent().getUniqueId();

			boolean tripExists = false;
			for (ItinCardData updatedCard : ItineraryManager.getInstance().getItinCardData()) {
				if (selectedCardId.equals(updatedCard.getTripComponent().getUniqueId())) {
					tripExists = true;
					mItinCardFragment.showItinDetails(updatedCard, false);
					break;
				}
			}

			if (!tripExists) {
				getSupportFragmentManager().beginTransaction().hide(mItinCardFragment).commit();
			}
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// OnListModeChanged

	@Override
	public void onListModeChanged(int mode) {
		if (mTwoPaneMode) {
			return;
		}

		if (mode == ItinListView.MODE_LIST) {
			getSupportActionBar().show();
		}
		else if (mode == ItinListView.MODE_DETAIL) {
			getSupportActionBar().hide();
		}
	}
}
