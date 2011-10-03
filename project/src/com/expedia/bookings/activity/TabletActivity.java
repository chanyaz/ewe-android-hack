package com.expedia.bookings.activity;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.ActionBar;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Resources;
import android.location.Address;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

import com.expedia.bookings.R;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.SearchParams.SearchType;
import com.expedia.bookings.data.SearchResponse;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.Session;
import com.expedia.bookings.fragment.CalendarDialogFragment;
import com.expedia.bookings.fragment.GuestsDialogFragment;
import com.expedia.bookings.fragment.HotelListFragment;
import com.expedia.bookings.server.ExpediaServices;
import com.google.android.maps.MapActivity;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.LocationServices;
import com.mobiata.android.Log;
import com.mobiata.android.util.NetUtils;

public class TabletActivity extends MapActivity {

	private Context mContext;
	private Resources mResources;

	//////////////////////////////////////////////////////////////////////////
	// Constants

	public static final int EVENT_SEARCH_STARTED = 1;
	public static final int EVENT_SEARCH_COMPLETE = 2;
	public static final int EVENT_SEARCH_ERROR = 3;

	private static final String KEY_SEARCH = "KEY_SEARCH";
	private static final String KEY_GEOCODE = "KEY_GEOCODE";

	//////////////////////////////////////////////////////////////////////////
	// Search state variables

	private SearchParams mSearchParams;

	private Session mSession;

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

		mContext = this;
		mResources = getResources();

		mEventHandlers = new HashSet<EventHandler>();

		mSearchParams = new SearchParams();

		setContentView(R.layout.activity_tablet);

		// Setup search interface.  This is probably not ultimately where this will go.
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction ft = fragmentManager.beginTransaction();
		ft.add(R.id.fragment_left, HotelListFragment.newInstance());
		ft.commit();
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
				startSearch();
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

	public void setLatLng(double latitude, double longitude) {
		Log.d("Setting lat/lng: lat=" + latitude + ", lng=" + longitude);

		mSearchParams.setSearchLatLon(latitude, longitude);
	}

	//////////////////////////////////////////////////////////////////////////
	// Search

	public void startSearch() {
		Log.i("startSearch(): " + mSearchParams.toString());

		notifyEventHandlers(EVENT_SEARCH_STARTED, null);

		if (!NetUtils.isOnline(this)) {
			Log.w("startSearch() - no internet connection.");
			notifyEventHandlers(EVENT_SEARCH_ERROR, getString(R.string.error_no_internet));
			return;
		}

		BackgroundDownloader bd = BackgroundDownloader.getInstance();

		// Cancel existing downloads
		bd.cancelDownload(KEY_SEARCH);
		bd.cancelDownload(KEY_GEOCODE);

		// Determine search type, conduct search
		switch (mSearchParams.getSearchType()) {
		case FREEFORM:
			if (mSearchParams.hasSearchLatLon()) {
				startSearchDownloader();
			}
			else {
				startGeocode();
			}
			break;
		case PROXIMITY:
			// TODO: Implement PROXIMITY search (once a MapView is available)
			Log.w("PROXIMITY searches not yet supported!");
			break;
		case MY_LOCATION:
			// TODO: Implement MY_LOCATION search
			Log.w("MY_LOCATION searches not yet supported!");
			break;
		}
	}

	public void startGeocode() {
		Log.i("startGeocode(): " + mSearchParams.getFreeformLocation());
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.startDownload(KEY_GEOCODE, mGeocodeDownload, mGeocodeCallback);
	}

	private Download mGeocodeDownload = new Download() {
		public Object doDownload() {
			return LocationServices.geocode(mContext, mSearchParams.getFreeformLocation());
		}
	};

	private OnDownloadComplete mGeocodeCallback = new OnDownloadComplete() {
		@SuppressWarnings("unchecked")
		public void onDownload(Object results) {
			List<Address> addresses = (List<Address>) results;

			if (addresses != null) {
				int size = addresses.size();
				if (size == 0) {
					Log.w("Geocode callback - got zero results.");
					notifyEventHandlers(EVENT_SEARCH_ERROR, getString(R.string.geolocation_failed));
				}
				else if (size == 1) {
					Address address = addresses.get(0);

					mSearchParams.setFreeformLocation(address);
					updateActionBarViews();

					setLatLng(address.getLatitude(), address.getLongitude());

					startSearchDownloader();
				}
				else {
					Log.i("Geocode callback - got multiple results.");
					// TODO: Show geocode disambiguation dialog
				}
			}
			else {
				Log.w("Geocode callback - got null results.");
				notifyEventHandlers(EVENT_SEARCH_ERROR, getString(R.string.geolocation_failed));
			}
		}
	};

	public void startSearchDownloader() {
		Log.i("startSearchDownloader()");

		BackgroundDownloader.getInstance().startDownload(KEY_SEARCH, mSearchDownload, mSearchCallback);
	}

	private Download mSearchDownload = new Download() {
		public Object doDownload() {
			ExpediaServices services = new ExpediaServices(mContext, mSession);
			BackgroundDownloader.getInstance().addDownloadListener(KEY_SEARCH, services);
			return services.search(mSearchParams, 0);
		}
	};

	private OnDownloadComplete mSearchCallback = new OnDownloadComplete() {
		public void onDownload(Object results) {
			SearchResponse response = (SearchResponse) results;

			if (response == null) {
				notifyEventHandlers(EVENT_SEARCH_ERROR, getString(R.string.progress_search_failed));
			}
			else if (response.hasErrors()) {
				notifyEventHandlers(EVENT_SEARCH_ERROR, response.getErrors().get(0).getPresentableMessage(mContext));
			}
			else {
				notifyEventHandlers(EVENT_SEARCH_COMPLETE, response);
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// MapActivity

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}
