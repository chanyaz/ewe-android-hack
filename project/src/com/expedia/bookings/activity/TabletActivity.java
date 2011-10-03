package com.expedia.bookings.activity;

import java.util.HashSet;
import java.util.Set;

import android.app.ActionBar;
import android.app.DialogFragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.fragment.GuestsDialogFragment;
import com.google.android.maps.MapActivity;

public class TabletActivity extends MapActivity {

	private Resources mResources;

	//////////////////////////////////////////////////////////////////////////
	// Search state variables

	private SearchParams mSearchParams;

	//////////////////////////////////////////////////////////////////////////
	// Event handling

	private Set<EventHandler> mEventHandlers;

	public interface EventHandler {
		public void handleEvent(int eventCode, Object data);
	};

	public boolean registerEventHandler(EventHandler eventHandler) {
		return mEventHandlers.add(eventHandler);
	}

	public boolean unregisterEventHandler(EventHandler eventHandler) {
		return mEventHandlers.remove(eventHandler);
	}

	public void notifyEventHandlers(int eventCode, Object data) {
		for (EventHandler eventHandler : mEventHandlers) {
			eventHandler.handleEvent(eventCode, data);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle events

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mResources = getResources();

		mEventHandlers = new HashSet<EventHandler>();

		mSearchParams = new SearchParams();
	}

	@Override
	protected void onStart() {
		super.onStart();

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	//////////////////////////////////////////////////////////////////////////
	// ActionBar

	private SearchView mSearchView;
	private MenuItem mGuestsMenuItem;
	private MenuItem mDatesMenuItem;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_tablet, menu);

		mSearchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
		mGuestsMenuItem = menu.findItem(R.id.menu_guests);
		mDatesMenuItem = menu.findItem(R.id.menu_dates);

		mSearchView.setIconifiedByDefault(false);

		updateActionBarViews(mSearchParams);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_guests:
			showGuestsDialog();
			return true;
		case R.id.menu_dates:
			// TODO: Display calendar picker dialog
			return true;
		case R.id.menu_filter:
			// TODO: Display filter options
			return true;
		case R.id.menu_about:
			// TODO: Launch About fragment
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void updateActionBarViews(SearchParams searchParams) {
		mSearchView.setQuery(searchParams.getSearchDisplayText(this), false);

		int numGuests = searchParams.getNumAdults() + searchParams.getNumChildren();
		mGuestsMenuItem.setTitle(mResources.getQuantityString(R.plurals.number_of_guests, numGuests, numGuests));

		int numNights = searchParams.getStayDuration();
		mDatesMenuItem.setTitle(mResources.getQuantityString(R.plurals.number_of_nights, numNights, numNights));
	}

	//////////////////////////////////////////////////////////////////////////
	// Dialogs

	void showGuestsDialog() {
		DialogFragment newFragment = GuestsDialogFragment.newInstance(mSearchParams.getNumAdults(),
				mSearchParams.getNumChildren());
		newFragment.show(getFragmentManager(), "GuestsDialog");
	}

	//////////////////////////////////////////////////////////////////////////
	// SearchParams management

	public void setGuests(int numAdults, int numChildren) {
		mSearchParams.setNumAdults(numAdults);
		mSearchParams.setNumChildren(numChildren);

		updateActionBarViews(mSearchParams);
	}

	//////////////////////////////////////////////////////////////////////////
	// MapActivity

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}
