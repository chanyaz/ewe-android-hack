package com.expedia.bookings.activity;

import java.util.Collection;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.data.trips.ItineraryManager.ItinerarySyncListener;
import com.expedia.bookings.data.trips.Trip;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.fragment.ItinCardFragment;
import com.expedia.bookings.fragment.ItinItemListFragment;
import com.expedia.bookings.fragment.ItinItemListFragment.ItinItemListFragmentListener;
import com.expedia.bookings.fragment.ItinMapFragment;
import com.expedia.bookings.fragment.ItinMapFragment.ItineraryMapFragmentListener;
import com.expedia.bookings.fragment.LoginConfirmLogoutDialogFragment.DoLogoutListener;
import com.expedia.bookings.fragment.SupportMapFragment.SupportMapFragmentListener;
import com.expedia.bookings.notification.Notification;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.DebugMenu;
import com.expedia.bookings.utils.DebugMenuFactory;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.itin.ItinListView.OnListModeChangedListener;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.model.CameraPosition;

/**
 * Full-screen Itinerary activity.  Used in tablets.
 */
public class ItineraryActivity extends FragmentActivity implements ItinItemListFragmentListener,
		OnCameraChangeListener, SupportMapFragmentListener, DoLogoutListener, ItineraryMapFragmentListener,
		ItinerarySyncListener, OnListModeChangedListener {

	private static final String ARG_JUMP_TO_NOTIFICATION = "ARG_JUMP_TO_NOTIFICATION";

	private static final String STATE_JUMP_TO_ITIN_ID = "STATE_JUMP_TO_ITIN_ID";

	private boolean mTwoPaneMode;

	private String mSelectedItinCardId;
	private String mJumpToItinId;

	private boolean mAnimatingToItem;
	private boolean mItemHasDetails;

	private DebugMenu debugMenu;

	private View mFallbackPatternView;

	private ItinItemListFragment mItinListFragment;
	private ItinMapFragment mMapFragment;
	private ItinCardFragment mItinCardFragment;

	// #854: There is a very subtle possible timing issue where we can try to modify
	// the fragment stack after onSaveInstanceState().  This helps prevent that.
	private boolean mFragmentSafe;

	//////////////////////////////////////////////////////////////////////////////////////////
	// Static Methods
	//////////////////////////////////////////////////////////////////////////////////////////

	public static Intent createIntent(Context context) {
		Intent intent = new Intent(context, ItineraryActivity.class);
		return intent;
	}

	/**
	 * Create intent to open this activity and jump straight to a particular itin item.
	 */
	public static Intent createIntent(Context context, Notification notification) {
		Intent intent = new Intent(context, ItineraryActivity.class);
		intent.putExtra(ARG_JUMP_TO_NOTIFICATION, notification.toJson().toString());

		// Even though we don't use the url directly anywhere, Android OS needs a way
		// to differentiate multiple intents to this same activity.
		// http://developer.android.com/reference/android/content/Intent.html#filterEquals(android.content.Intent)
		intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

		return intent;
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// Lifecycle Methods
	//////////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_itinerary);

		debugMenu = DebugMenuFactory.newInstance(this, null);

		getWindow().setBackgroundDrawable(null);

		mFallbackPatternView = Ui.findView(this, R.id.fallback_pattern);

		mItinListFragment = Ui.findSupportFragment(this, getString(R.string.tag_itinerary_list));
		mMapFragment = Ui.findSupportFragment(this, getString(R.string.tag_itinerary_map));
		mItinCardFragment = Ui.findSupportFragment(this, getString(R.string.tag_itinerary_card));

		mTwoPaneMode = mMapFragment != null && mMapFragment.isAdded();

		if (mTwoPaneMode) {
			mItinListFragment.setBackgroundColor(getResources().getColor(R.color.itin_list_bg_transparent));
			mItinListFragment.setSimpleMode(true);

			float listWidth = getResources().getDimensionPixelSize(R.dimen.itin_simple_list_width);
			mMapFragment.setListWidth((int) listWidth);

			int bottomPadding = getResources().getDimensionPixelSize(R.dimen.itin_map_marker_bottom_padding);
			mMapFragment.setMarkerSpacing(bottomPadding);

			// Start with itin card hidden
			// TODO: If a card is already expanded from before, do not start hidden
			getSupportFragmentManager().beginTransaction().hide(mItinCardFragment).commit();
		}

		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(STATE_JUMP_TO_ITIN_ID)) {
				mJumpToItinId = savedInstanceState.getString(STATE_JUMP_TO_ITIN_ID);
			}
		}
		else if (getIntent().hasExtra(ARG_JUMP_TO_NOTIFICATION)) {
			handleArgJumpToNotification(getIntent());
		}

		if (!TextUtils.isEmpty(mJumpToItinId)) {
			mItinListFragment.showItinCard(mJumpToItinId, true);
		}

		mItinListFragment.enableLoadItins();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		if (intent.hasExtra(ARG_JUMP_TO_NOTIFICATION)) {
			handleArgJumpToNotification(intent);
			if (!TextUtils.isEmpty(mJumpToItinId)) {
				mItinListFragment.showItinCard(mJumpToItinId, true);
				showPopupWindow(mJumpToItinId, true);
			}
		}
	}

	/**
	 * Parses ARG_JUMP_TO_NOTIFICATION out of the intent into a Notification object,
	 * sets mJumpToItinId.
	 * This function expects to be called only when this activity is started via
	 * the given intent (onCreate or onNewIntent) and has side effects that
	 * rely on that assumption:
	 * 1. Tracks this incoming intent in Omniture.
	 * 2. Updates the Notifications table that this notification is dismissed.
	 *
	 * *** This is duplicated in LaunchActivity ***
	 *
	 * @param intent
	 */
	private void handleArgJumpToNotification(Intent intent) {
		String jsonNotification = intent.getStringExtra(ARG_JUMP_TO_NOTIFICATION);
		Notification notification = Notification.getInstanceFromJsonString(jsonNotification);

		if (!Notification.hasExisting(notification)) {
			return;
		}

		mJumpToItinId = notification.getItinId();
		OmnitureTracking.trackNotificationClick(notification);

		// There's no need to dismiss with the notification manager, since it was set to
		// auto dismiss when clicked.
		Notification.dismissExisting(notification);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Make sure to syncWithManager in the event the ItineraryManager has received updated data while our
		// syncListener was not attached.
		syncWithManager();
		ItineraryManager.getInstance().addSyncListener(this);
	}

	@Override
	protected void onPostResume() {
		super.onPostResume();

		mFragmentSafe = true;
	}

	@Override
	public void onPause() {
		super.onPause();
		ItineraryManager.getInstance().removeSyncListener(this);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mJumpToItinId != null) {
			outState.putString(STATE_JUMP_TO_ITIN_ID, mJumpToItinId);
		}
		else if (mSelectedItinCardId != null) {
			outState.putString(STATE_JUMP_TO_ITIN_ID, mSelectedItinCardId);
		}

		mFragmentSafe = false;
	}

	@Override
	public void onBackPressed() {
		if (mItinListFragment.isInDetailMode()) {
			mItinListFragment.hideDetails();
		}
		else if (mTwoPaneMode && mItinCardFragment.isVisible()) {
			mItinListFragment.hideDetails();
			hidePopupWindow();
			mFallbackPatternView.setVisibility(View.GONE);
		}
		else if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
			// Just in case we started this activity directly (like from clicking a Notification),
			// we always want the back button to take us to the launch screen
			NavUtils.navigateUpTo(this, new Intent(this, TabletLaunchActivity.class));
		}
		else {
			super.onBackPressed();
		}
	}

	private void showPopupWindow(String itinId, boolean animate) {
		ItinCardData data = ItineraryManager.getInstance().getItinCardDataFromItinId(itinId);
		showPopupWindow(data, animate);
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
		getActionBar().setDisplayHomeAsUpEnabled(true);

		getMenuInflater().inflate(R.menu.menu_itinerary, menu);

		mLogInMenuItem = menu.findItem(R.id.menu_log_in);
		mLogOutMenuItem = menu.findItem(R.id.menu_log_out);

		debugMenu.onCreateOptionsMenu(menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		debugMenu.onPrepareOptionsMenu(menu);

		boolean loggedIn = User.isLoggedIn(this);
		mLogInMenuItem.setVisible(!loggedIn && ProductFlavorFeatureConfiguration.getInstance().isSigninEnabled());
		mLogOutMenuItem.setVisible(loggedIn && ProductFlavorFeatureConfiguration.getInstance().isSigninEnabled());

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: {
			// Let onBackPressed handle the task creation logic for us
			onBackPressed();
			return true;
		}
		case R.id.menu_add_guest_itinerary: {
			mItinListFragment.startAddGuestItinActivity(false);
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
		}

		if (debugMenu.onOptionsItemSelected(item)) {
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
			ItinCardData data;
			if (mJumpToItinId != null) {
				data = ItineraryManager.getInstance().getItinCardDataFromItinId(mJumpToItinId);
				// Consume mJumpToItinId now.
				mJumpToItinId = null;
			}
			else {
				data = mItinListFragment.getSelectedItinCardData();
			}

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
	public void onTripFailedFetchingGuestItinerary() {
		// Do nothing
	}

	@Override
	public void onTripFailedFetchingRegisteredUserItinerary() {
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
	public void onCompletedTripAdded(Trip trip) {
		// Do nothing
	}

	@Override
	public void onCancelledTripAdded(Trip trip) {
		// Do nothing
	}

	@Override
	public void onSyncFinished(Collection<Trip> trips) {
		syncWithManager();
	}

	/**
	 * Call this to either redraw or hide the popup after the ItineraryManager could have new data.
	 */
	private void syncWithManager() {
		if (mItinCardFragment != null && mItinCardFragment.isVisible()) {
			ItinCardData selectedCardData = mItinCardFragment.getItinCardData();
			String selectedCardId = selectedCardData.getTripComponent().getUniqueId();

			boolean tripExists = false;
			ItinCardData displayCard = null;
			for (ItinCardData updatedCard : ItineraryManager.getInstance().getItinCardData()) {
				if (selectedCardId.equals(updatedCard.getTripComponent().getUniqueId())) {
					displayCard = updatedCard;
					tripExists = true;
					break;
				}
			}

			if (tripExists) {
				// Redraw the popup
				mItinCardFragment.showItinDetails(displayCard, false);
			}
			else {
				hidePopupWindow();
			}
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// OnListModeChanged

	@Override
	public void onListModeChanged(boolean isInDetailMode, boolean animate) {
		if (mTwoPaneMode) {
			return;
		}

		if (isInDetailMode) {
			getActionBar().hide();
		}
		else {
			getActionBar().show();
		}
	}
}
