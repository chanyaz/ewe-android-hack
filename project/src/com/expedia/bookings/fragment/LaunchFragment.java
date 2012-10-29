package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.HotelDetailsFragmentActivity;
import com.expedia.bookings.data.BackgroundImageResponse;
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
import com.mobiata.android.util.Ui;

public class LaunchFragment extends Fragment {

	private static final boolean DEBUG_ALWAYS_GRAB_NEW_LOCATION = false;

	public static final String TAG = LaunchFragment.class.getName();
	public static final String KEY_SEARCH = "LAUNCH_SCREEN_HOTEL_SEARCH";
	public static final String KEY_FLIGHT_DESTINATIONS = "LAUNCH_SCREEN_FLIGHT_DESTINATIONS";

	private static final long MINIMUM_TIME_AGO = 1000 * 60 * 15; // 15 minutes ago
	private static final int NUM_HOTEL_PROPERTIES = 20;

	private Context mContext;

	private LaunchStreamListView mHotelsStreamListView;
	private LaunchHotelAdapter mHotelAdapter;
	private LaunchStreamListView mFlightsStreamListView;
	private LaunchFlightAdapter mFlightAdapter;

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

		Ui.findView(v, R.id.hotels_button).setOnClickListener(mHeaderItemOnClickListener);
		Ui.findView(v, R.id.flights_button).setOnClickListener(mHeaderItemOnClickListener);

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
	}

	@Override
	public void onPause() {
		super.onPause();

		stopLocation();
		mHotelsStreamListView.stopMarquee();

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.unregisterDownloadCallback(KEY_SEARCH);
		bd.unregisterDownloadCallback(KEY_FLIGHT_DESTINATIONS);
	}

	@Override
	public void onStop() {
		super.onStop();

		mHotelsStreamListView.savePosition();
		mFlightsStreamListView.savePosition();

		// Null out the adapter to prevent potentially recycled images from attempting to redraw and crash
		// Also null out the adapter to release its expensive bitmaps... does this need to be done? not sure but doing
		// this now to unblock people maybe?
		mHotelsStreamListView.setAdapter(null);
		mHotelAdapter = null;
		mFlightsStreamListView.setAdapter(null);
		mFlightAdapter = null;
	}

	private void onReactToUserActive() {
		// This is useful if you want to test a device's ability to find a new location
		if (DEBUG_ALWAYS_GRAB_NEW_LOCATION) {
			findLocation();
			return;
		}

		BackgroundDownloader bd = BackgroundDownloader.getInstance();

		LaunchHotelData launchHotelData = Db.getLaunchHotelData();

		// No cached hotel data exists, perform the least amount of effort in order to get results on screen by following
		// the logic below
		if (launchHotelData == null) {
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

				onHotelDataRetrieved(true);
			}

			// Hotel search failed; user will not see reverse waterfall.
			else {
				// TODO what to do here, I wonder
			}
		}
	};

	private void onHotelDataRetrieved(boolean justLoaded) {
		mHotelAdapter.setProperties(Db.getLaunchHotelData());
		if (justLoaded) {
			mHotelsStreamListView.selectMiddle();
		}
		else {
			mHotelsStreamListView.restorePosition();
		}
		mHotelsStreamListView.setOnItemClickListener(mHotelsStreamOnItemClickListener);
	}

	// Flight destination search

	private List<Destination> getHardcodedDestinations() {
		List<Destination> destinations = new ArrayList<Destination>();

		destinations.add(new Destination("LHR", "London", "London Heathrow"));
		destinations.add(new Destination("MIA", "Miami", "Miami, yo"));
		destinations.add(new Destination("JFK", "New York", "JFK - John F. Kennedy"));

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
			LaunchFlightData data = new LaunchFlightData();
			data.setDestinations(results);
			Db.setLaunchFlightData(data);

			onFlightDataRetrieved(true);
		}
	};

	private void onFlightDataRetrieved(boolean justLoaded) {
		mFlightAdapter.setDestinations(Db.getLaunchFlightData());
		if (justLoaded) {
			mFlightsStreamListView.selectMiddle();
		}
		else {
			mFlightsStreamListView.restorePosition();
		}
		mFlightsStreamListView.setOnItemClickListener(mFlightsStreamOnItemClickListener);
	}

	// View init + listeners

	private void initViews() {
		mHotelAdapter = new LaunchHotelAdapter(mContext);
		mHotelsStreamListView.setAdapter(mHotelAdapter);

		mFlightAdapter = new LaunchFlightAdapter(mContext);
		mFlightsStreamListView.setAdapter(mFlightAdapter);

		mHotelsStreamListView.setSlaveView(mFlightsStreamListView);

		mFlightsStreamListView.setSlaveView(mHotelsStreamListView);

		LaunchHotelData launchHotelData = Db.getLaunchHotelData();
		if (launchHotelData != null) {
			onHotelDataRetrieved(false);
		}

		LaunchFlightData launchFlightData = Db.getLaunchFlightData();
		if (launchFlightData != null) {
			onFlightDataRetrieved(false);
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
			String destinationId = destination.getDestinationId();
			String city = destination.getCity();
			String description = destination.getDescription();
			com.expedia.bookings.data.Location location = new com.expedia.bookings.data.Location(destinationId, city,
					description);

			FlightSearchParams flightSearchParams = Db.getFlightSearch().getSearchParams();
			flightSearchParams.setArrivalLocation(location);

			NavUtils.goToFlights(getActivity(), true);

			cleanUp();
		}
	};

	/**
	 * Call this whenever you want to clean up the Activity, since you're moving
	 * somewhere else.
	 */
	private void cleanUp() {
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.cancelDownload(KEY_SEARCH);
		bd.cancelDownload(KEY_FLIGHT_DESTINATIONS);

	}
}
