package com.expedia.bookings.activity;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import android.app.ActionBar;
import android.app.DialogFragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

import com.expedia.bookings.R;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.SearchParams.SearchType;
import com.expedia.bookings.fragment.CalendarDialogFragment;
import com.expedia.bookings.fragment.GuestsDialogFragment;
import com.google.android.maps.MapActivity;
import com.mobiata.android.Log;

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
		mSearchView.setSubmitButtonEnabled(true);
		mSearchView.setOnQueryTextListener(new OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				setFreeformLocation(query);
				mSearchView.clearFocus();
				return true;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				return false;
			}
		});

		updateActionBarViews();

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_guests:
			showGuestsDialog();
			return true;
		case R.id.menu_dates:
			showCalendarDialog();
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

	private void updateActionBarViews() {
		mSearchView.setQuery(mSearchParams.getSearchDisplayText(this), false);

		int numGuests = mSearchParams.getNumAdults() + mSearchParams.getNumChildren();
		mGuestsMenuItem.setTitle(mResources.getQuantityString(R.plurals.number_of_guests, numGuests, numGuests));

		int numNights = mSearchParams.getStayDuration();
		mDatesMenuItem.setTitle(mResources.getQuantityString(R.plurals.number_of_nights, numNights, numNights));
	}

	//////////////////////////////////////////////////////////////////////////
	// Dialogs

	void showGuestsDialog() {
		DialogFragment newFragment = GuestsDialogFragment.newInstance(mSearchParams.getNumAdults(),
				mSearchParams.getNumChildren());
		newFragment.show(getFragmentManager(), "GuestsDialog");
	}

	private void showCalendarDialog() {
		DialogFragment newFragment = CalendarDialogFragment.newInstance(mSearchParams.getCheckInDate(),
				mSearchParams.getCheckOutDate());
		newFragment.show(getFragmentManager(), "CalendarDialog");
	}

	//////////////////////////////////////////////////////////////////////////
	// SearchParams management

	public void setFreeformLocation(String freeformLocation) {
		Log.d("Setting freeform location: " + freeformLocation);

		mSearchParams.setFreeformLocation(freeformLocation);
		mSearchParams.setSearchType(SearchType.FREEFORM);

		updateActionBarViews();
	}

	public void setGuests(int numAdults, int numChildren) {
		Log.d("Setting guests: " + numAdults + " adult(s), " + numChildren + " child(ren)");

		mSearchParams.setNumAdults(numAdults);
		mSearchParams.setNumChildren(numChildren);

		updateActionBarViews();
	}

	public void setDates(Calendar checkIn, Calendar checkOut) {
		Log.d("Setting dates: " + checkIn.getTimeInMillis() + " to " + checkOut.getTimeInMillis());

		mSearchParams.setCheckInDate(checkIn);
		mSearchParams.setCheckOutDate(checkOut);

		updateActionBarViews();
	}

	//////////////////////////////////////////////////////////////////////////
	// MapActivity

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}
