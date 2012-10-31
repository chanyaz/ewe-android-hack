package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.FlightSearchActivity;
import com.expedia.bookings.activity.HotelDetailsFragmentActivity;
import com.expedia.bookings.data.BackgroundImageResponse;
import com.expedia.bookings.data.ConfirmationState;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Destination;
import com.expedia.bookings.data.Filter;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.LaunchFlightData;
import com.expedia.bookings.data.LaunchHotelData;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.SearchResponse;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.FontCache.Font;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.widget.LaunchFlightAdapter;
import com.expedia.bookings.widget.LaunchHotelAdapter;
import com.expedia.bookings.widget.LaunchStreamListView;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.LocationServices;
import com.mobiata.android.Log;
import com.mobiata.android.location.LocationFinder;
import com.mobiata.android.util.NetUtils;
import com.mobiata.android.util.Ui;

public class LaunchFragment extends Fragment {

	private static final boolean DEBUG_ALWAYS_GRAB_NEW_LOCATION = false;

	public static final String TAG = LaunchFragment.class.getName();
	public static final String KEY_SEARCH = "LAUNCH_SCREEN_HOTEL_SEARCH";
	public static final String KEY_FLIGHT_DESTINATIONS = "LAUNCH_SCREEN_FLIGHT_DESTINATIONS";

	private static final long MINIMUM_TIME_AGO = 1000 * 60 * 15; // 15 minutes ago
	private static final int NUM_HOTEL_PROPERTIES = 20;

	private Context mContext;

	private ViewGroup mErrorContainer;
	private ViewGroup mScrollContainer;
	private LaunchStreamListView mHotelsStreamListView;
	private LaunchHotelAdapter mHotelAdapter;
	private LaunchStreamListView mFlightsStreamListView;
	private LaunchFlightAdapter mFlightAdapter;

	private boolean mCleanOnStop = false;

	public static LaunchFragment newInstance() {
		return new LaunchFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_launch, container, false);

		FontCache.setTypeface(v, R.id.hotels_label_text_view, FontCache.Font.ROBOTO_LIGHT);
		FontCache.setTypeface(v, R.id.hotels_prompt_text_view, FontCache.Font.ROBOTO_LIGHT);
		FontCache.setTypeface(v, R.id.flights_label_text_view, FontCache.Font.ROBOTO_LIGHT);
		FontCache.setTypeface(v, R.id.flights_prompt_text_view, FontCache.Font.ROBOTO_LIGHT);
		FontCache.setTypeface(v, R.id.launch_welcome_text_view, FontCache.Font.ROBOTO_LIGHT);
		FontCache.setTypeface(v, R.id.error_message_text_view, Font.ROBOTO_LIGHT);

		Ui.findView(v, R.id.hotels_button).setOnClickListener(mHeaderItemOnClickListener);
		Ui.findView(v, R.id.flights_button).setOnClickListener(mHeaderItemOnClickListener);

		mErrorContainer = Ui.findView(v, R.id.error_container);
		mScrollContainer = Ui.findView(v, R.id.scroll_container);
		mHotelsStreamListView = Ui.findView(v, R.id.hotels_stream_list_view);
		mFlightsStreamListView = Ui.findView(v, R.id.flights_stream_list_view);

		return v;
	}

	@Override
	public void onStart() {
		super.onStart();

		// Note: We call this here to avoid reusing recycled Bitmaps. Not ideal, but a simple fix for now
		initViews();
	}

	@Override
	public void onResume() {
		super.onResume();

		onReactToUserActive();
		mHotelsStreamListView.startMarquee();

		IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		getActivity().registerReceiver(mConnReceiver, filter);
	}

	@Override
	public void onPause() {
		super.onPause();

		stopLocation();
		mHotelsStreamListView.stopMarquee();

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.unregisterDownloadCallback(KEY_SEARCH);
		bd.unregisterDownloadCallback(KEY_FLIGHT_DESTINATIONS);

		getActivity().unregisterReceiver(mConnReceiver);
	}

	@Override
	public void onStop() {
		super.onStop();

		mHotelsStreamListView.savePosition();
		mFlightsStreamListView.savePosition();

		cleanUpOnStop();
	}

	private void onReactToUserActive() {
		// Check that we're online first
		if (!checkConnection()) {
			return;
		}

		BackgroundDownloader bd = BackgroundDownloader.getInstance();

		LaunchHotelData launchHotelData = Db.getLaunchHotelData();

		if (DEBUG_ALWAYS_GRAB_NEW_LOCATION) {
			// This is useful if you want to test a device's ability to find a new location
			findLocation();
		}
		if (launchHotelData == null) {
			// No cached hotel data exists, perform the least amount of effort in order to get results on screen by following
			// the logic below

			// A hotel search is underway, register dl callback in case it was removed
			if (bd.isDownloading(KEY_SEARCH)) {
				bd.registerDownloadCallback(KEY_SEARCH, mSearchCallback);
			}

			// No cached hotel data and no hotel search downloading...that must mean we need to find a Location!
			else {

				// Attempt to find last best Location from OS cache
				long minTime = Calendar.getInstance().getTimeInMillis() - MINIMUM_TIME_AGO;
				Location location = LocationServices.getLastBestLocation(mContext, minTime);

				// No cached location found, find a new location update as quickly and low-power as possible
				if (location == null) {
					findLocation();
				}

				// Location found from cache, kick off hotel search
				else {
					startHotelSearch(location);
				}
			}
		}

		LaunchFlightData launchFlightData = Db.getLaunchFlightData();
		if (launchFlightData == null) {
			if (bd.isDownloading(KEY_FLIGHT_DESTINATIONS)) {
				bd.registerDownloadCallback(KEY_FLIGHT_DESTINATIONS, mFlightsCallback);
			}
			else {
				bd.startDownload(KEY_FLIGHT_DESTINATIONS, mFlightsDownload, mFlightsCallback);
			}
		}
	}

	// Location finder

	private LocationFinder mLocationFinder;

	private void findLocation() {
		if (mLocationFinder == null) {
			mLocationFinder = LocationFinder.getInstance(mContext);
			mLocationFinder.setListener(new LocationFinder.LocationFinderListener() {
				@Override
				public void onReceiveNewLocation(Location location) {
					startHotelSearch(location);
				}
			});
		}
		mLocationFinder.find();
	}

	private void stopLocation() {
		if (mLocationFinder != null) {
			mLocationFinder.stop();
		}
	}

	// Hotel search

	private void startHotelSearch(Location loc) {
		Log.i("Start hotel search");

		SearchParams searchParams = new SearchParams();
		searchParams.setSearchLatLon(loc.getLatitude(), loc.getLongitude());
		Db.setSearchParams(searchParams);

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.cancelDownload(KEY_SEARCH);
		bd.startDownload(KEY_SEARCH, mSearchDownload, mSearchCallback);
	}

	private final BackgroundDownloader.Download<SearchResponse> mSearchDownload = new BackgroundDownloader.Download<SearchResponse>() {
		@Override
		public SearchResponse doDownload() {
			ExpediaServices services = new ExpediaServices(getActivity());
			BackgroundDownloader.getInstance().addDownloadListener(KEY_SEARCH, services);
			return services.search(Db.getSearchParams(), 0);
		}
	};

	private final BackgroundDownloader.OnDownloadComplete<SearchResponse> mSearchCallback = new BackgroundDownloader.OnDownloadComplete<SearchResponse>() {
		@Override
		public void onDownload(SearchResponse searchResponse) {
			if (searchResponse != null) {
				Log.d("Search complete: " + searchResponse.getPropertiesCount());
			}

			// Response was good, we are going to use this stuff
			if (searchResponse != null && searchResponse.getPropertiesCount() > 0 && !searchResponse.hasErrors()) {

				// We only want to set the the search from Launch if there exists no SearchResponse data already (to avoid
				// sending the user through another network request when jumping to Hotels). If there already exists a 
				// Search response in the Db, do not flush it out.
				if (Db.getSearchResponse() == null) {
					Db.setSearchResponse(searchResponse);
				}

				// Extract relevant data here
				LaunchHotelData launchHotelData = new LaunchHotelData();
				List<Property> properties = searchResponse.getFilteredAndSortedProperties(Filter.Sort.DEALS,
						NUM_HOTEL_PROPERTIES);
				launchHotelData.setProperties(properties);
				launchHotelData.setDistanceUnit(searchResponse.getFilter().getDistanceUnit());
				Db.setLaunchHotelData(launchHotelData);

				onHotelDataRetrieved();
			}

			// Hotel search failed; user will not see reverse waterfall.
			else {
				// TODO what to do here, I wonder
			}
		}
	};

	private void onHotelDataRetrieved() {
		mHotelAdapter.setProperties(Db.getLaunchHotelData());
		mHotelsStreamListView.setOnItemClickListener(mHotelsStreamOnItemClickListener);
	}

	// Flight destination search

	private List<Destination> getHardcodedDestinations() {
		List<Destination> destinations = new ArrayList<Destination>();

		destinations.add(new Destination("LHR", "London", "London Heathrow"));
		destinations.add(new Destination("MIA", "Miami", "Miami, yo"));
		destinations.add(new Destination("JFK", "New York", "JFK - John F. Kennedy"));
		destinations.add(new Destination("ABQ", "Albuquerque", "Albuquerque International Sunport "));
		destinations.add(new Destination("CDG", "Paris", "Charles de Gaulle"));
		destinations.add(new Destination("DTW", "Detroit", "Detroit Metro Airport"));
		destinations.add(new Destination("PRG", "Prague", "Vaclav Havel Airport Prague"));
		destinations.add(new Destination("RNO", "Reno", "Reno-Tahoe International Airport"));
		destinations.add(new Destination("SEA", "Seattle", "Seattle-Tacoma International Airport"));
		destinations.add(new Destination("SFO", "San Francisco", "San Francisco International Airport"));
		destinations.add(new Destination("YUL", "Montreal", "Pierre Elliott Trudeau International Airport"));
		destinations.add(new Destination("YYZ", "Toronto", "Toronto Pearson Airport"));

		return destinations;
	}

	private Download<List<Destination>> mFlightsDownload = new Download<List<Destination>>() {
		@Override
		public List<Destination> doDownload() {
			ExpediaServices services = new ExpediaServices(mContext);
			BackgroundDownloader.getInstance().addDownloadListener(KEY_FLIGHT_DESTINATIONS, services);

			Display display = getActivity().getWindowManager().getDefaultDisplay();
			int width = Math.round(display.getWidth() / 2);
			int height = Math.round(getResources().getDimension(R.dimen.launch_tile_height_flight));

			List<Destination> destinations = new ArrayList<Destination>();
			for (Destination destination : getHardcodedDestinations()) {
				String destId = destination.getDestinationId();

				BackgroundImageResponse response = services.getFlightsBackgroundImage(destId, width, height);

				if (response == null) {
					Log.w("Got a null response from server looking for destination bg for: " + destId);
				}
				else if (response.hasErrors()) {
					Log.w("Got an error response from server looking for destination bg for: "
							+ destId + ", " + response.getErrors().get(0).getPresentableMessage(mContext));
				}
				else {
					Log.v("Got destination data for: " + destId);
					destination.setImageMeta(response.getCacheKey(), response.getImageUrl());
					destinations.add(destination);
				}
			}

			return destinations;
		}
	};

	private OnDownloadComplete<List<Destination>> mFlightsCallback = new OnDownloadComplete<List<Destination>>() {
		@Override
		public void onDownload(List<Destination> results) {
			if (results != null && results.size() > 0) {
				LaunchFlightData data = new LaunchFlightData();
				data.setDestinations(results);
				Db.setLaunchFlightData(data);

				onFlightDataRetrieved();
			}
		}
	};

	private void onFlightDataRetrieved() {
		mFlightAdapter.setDestinations(Db.getLaunchFlightData());
		mFlightsStreamListView.setOnItemClickListener(mFlightsStreamOnItemClickListener);
	}

	// View init + listeners

	private void initViews() {
		if (mHotelAdapter != null && mFlightAdapter != null) {
			return;
		}

		Log.d("LaunchFragment.initViews() - initializing views...");

		mHotelAdapter = new LaunchHotelAdapter(mContext);
		mHotelsStreamListView.setAdapter(mHotelAdapter);
		if (!mHotelsStreamListView.restorePosition()) {
			mHotelsStreamListView.selectMiddle();
		}

		mFlightAdapter = new LaunchFlightAdapter(mContext);
		mFlightsStreamListView.setAdapter(mFlightAdapter);
		if (!mFlightsStreamListView.restorePosition()) {
			mFlightsStreamListView.selectMiddle();
		}

		mHotelsStreamListView.setSlaveView(mFlightsStreamListView);

		mFlightsStreamListView.setSlaveView(mHotelsStreamListView);

		LaunchHotelData launchHotelData = Db.getLaunchHotelData();
		if (launchHotelData != null) {
			onHotelDataRetrieved();
		}

		LaunchFlightData launchFlightData = Db.getLaunchFlightData();
		if (launchFlightData != null) {
			onFlightDataRetrieved();
		}
	}

	private final View.OnClickListener mHeaderItemOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.hotels_button:
				NavUtils.goToHotels(getActivity());

				OmnitureTracking.trackLinkLaunchScreenToHotels(getActivity());
				break;
			case R.id.flights_button:
				NavUtils.goToFlights(getActivity());

				OmnitureTracking.trackLinkLaunchScreenToFlights(getActivity());
				break;
			}

			cleanUp();
		}
	};

	private final AdapterView.OnItemClickListener mHotelsStreamOnItemClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Property property = mHotelAdapter.getItem(position);
			if (property != null) {
				Db.setSelectedProperty(property);

				Intent intent = new Intent(mContext, HotelDetailsFragmentActivity.class);
				intent.putExtra(HotelDetailsMiniGalleryFragment.ARG_FROM_LAUNCH, true);
				mContext.startActivity(intent);

				cleanUp();
			}
		}
	};

	private final AdapterView.OnItemClickListener mFlightsStreamOnItemClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Destination destination = mFlightAdapter.getItem(position);

			com.expedia.bookings.data.Location location = new com.expedia.bookings.data.Location();
			location.setDestinationId(destination.getDestinationId());
			location.setCity(destination.getCity());
			location.setDescription(destination.getDescription());

			FlightSearchParams flightSearchParams = Db.getFlightSearch().getSearchParams();
			flightSearchParams.setArrivalLocation(location);

			// Make sure to delete ConfirmationState if it exists
			ConfirmationState confirmationState = new ConfirmationState(mContext, ConfirmationState.Type.FLIGHT);
			if (confirmationState.hasSavedData()) {
				confirmationState.delete();
			}

			Intent intent = new Intent(mContext, FlightSearchActivity.class);
			intent.putExtra(FlightSearchActivity.ARG_FROM_LAUNCH_WITH_SEARCH_PARAMS, true);
			mContext.startActivity(intent);

			cleanUp();
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Clean up
	//
	// Clean up happens in two phases.  This is because some things you want
	// to clean immediately, but others if you clean them immediately it
	// makes it ugly

	private void cleanUp() {
		Log.d("LaunchFragment.cleanUp()");

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.cancelDownload(KEY_SEARCH);
		bd.cancelDownload(KEY_FLIGHT_DESTINATIONS);

		mCleanOnStop = true;
	}

	/**
	 * Call this whenever you want to clean up the Activity, since you're moving
	 * somewhere else.
	 */
	private void cleanUpOnStop() {
		if (!mCleanOnStop) {
			return;
		}

		mCleanOnStop = false;

		Log.d("LaunchFragment.cleanUpOnStop()");

		mHotelsStreamListView.setAdapter(null);
		mHotelAdapter.setProperties(null);
		mHotelAdapter = null;
		mFlightsStreamListView.setAdapter(null);
		mFlightAdapter.setDestinations(null);
		mFlightAdapter = null;

		// TODO: Clean up more as necessary (e.g., cleaning out the ImageCache).
	}

	//////////////////////////////////////////////////////////////////////////
	// Connectivity

	private BroadcastReceiver mConnReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i("Detected connectivity change, checking connection...");

			// If we changed state, react 
			boolean wasOffline = mErrorContainer.getVisibility() == View.VISIBLE;
			boolean isOffline = !checkConnection();
			if (isOffline != wasOffline) {
				Log.i("Connectivity changed from " + wasOffline + " to " + isOffline);

				if (isOffline) {
					cleanUp();
				}
				else {
					// Clear out previous results, then start over again
					Db.setLaunchFlightData(null);
					Db.setLaunchHotelData(null);

					onReactToUserActive();
				}
			}
		}
	};

	private boolean checkConnection() {
		if (!NetUtils.isOnline(getActivity())) {
			Log.d("Launch page is offline.");

			mErrorContainer.setVisibility(View.VISIBLE);
			mScrollContainer.setVisibility(View.GONE);

			return false;
		}
		else {
			mErrorContainer.setVisibility(View.GONE);
			mScrollContainer.setVisibility(View.VISIBLE);
			mCleanOnStop = false;
			return true;
		}
	}
}
