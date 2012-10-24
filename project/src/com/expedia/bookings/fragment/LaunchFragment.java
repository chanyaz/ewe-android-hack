package com.expedia.bookings.fragment;

import java.util.Calendar;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.HotelDetailsFragmentActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchParams;
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
import com.mobiata.android.LocationServices;
import com.mobiata.android.Log;
import com.mobiata.android.location.LocationFinder;
import com.mobiata.android.util.Ui;

public class LaunchFragment extends Fragment {

	private static final boolean DEBUG_ALWAYS_GRAB_NEW_LOCATION = false;

	public static final String TAG = LaunchFragment.class.toString();
	public static final String KEY_SEARCH = "LAUNCH_SCREEN_HOTEL_SEARCH";

	public static final long MINIMUM_TIME_AGO = 1000 * 60 * 15; // 15 minutes ago

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
	}

	@Override
	public void onStop() {
		super.onStop();

		mHotelsStreamListView.savePosition();

		// Null out the adapter to prevent potentially recycled images from attempting to redraw and crash
		mHotelsStreamListView.setAdapter(null);
	}

	private void onReactToUserActive() {
		// This is useful if you want to test a device's ability to find a new location
		if (DEBUG_ALWAYS_GRAB_NEW_LOCATION) {
			findLocation();
			return;
		}

		SearchResponse searchResponse = Db.getSearchResponse();

		// No cached hotel data exists, perform the least amount of effort in order to get results on screen by following
		// the logic below
		if (searchResponse == null) {
			BackgroundDownloader bd = BackgroundDownloader.getInstance();

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
	}

	// Location Stuff

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

			Db.setSearchResponse(searchResponse);

			// Response was good, we are going to use this stuff
			if (searchResponse != null && searchResponse.getPropertiesCount() > 0 && !searchResponse.hasErrors()) {
				mHotelAdapter.setProperties(searchResponse);
				mHotelsStreamListView.selectMiddle();
			}

			// Hotel search failed; user will not see reverse waterfall.
			else {
				// TODO what to do here, I wonder
			}

		}
	};

	// View Stuff

	private void initViews() {
		mHotelAdapter = new LaunchHotelAdapter(mContext);
		mHotelsStreamListView.setAdapter(mHotelAdapter);

		mHotelsStreamListView.setOnItemClickListener(mHotelsStreamOnItemClickListener);
		mHotelsStreamListView.setSlaveView(mFlightsStreamListView);

		mFlightAdapter = new LaunchFlightAdapter(mContext);
		mFlightsStreamListView.setAdapter(mFlightAdapter);

		mFlightsStreamListView.setOnItemClickListener(mFlightsStreamOnItemClickListener);
		mFlightsStreamListView.setSlaveView(mHotelsStreamListView);

		SearchResponse searchResponse = Db.getSearchResponse();
		if (searchResponse != null) {
			mHotelAdapter.setProperties(searchResponse);
			mHotelsStreamListView.restorePosition();
		}

		mFlightAdapter.setLocations(LaunchFlightAdapter.getHardcodedDestinations());
	}

	private final View.OnClickListener mHeaderItemOnClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.hotels_button:
				NavUtils.goToHotels(getActivity());

				BackgroundDownloader.getInstance().cancelDownload(KEY_SEARCH);

				OmnitureTracking.trackLinkLaunchScreenToHotels(getActivity());
				break;
			case R.id.flights_button:
				NavUtils.goToFlights(getActivity());

				BackgroundDownloader.getInstance().cancelDownload(KEY_SEARCH);

				OmnitureTracking.trackLinkLaunchScreenToFlights(getActivity());
				break;
			}
		}

	};

	private final AdapterView.OnItemClickListener mHotelsStreamOnItemClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Property property = mHotelAdapter.getItem(position);
			Db.setSelectedProperty(property);

			BackgroundDownloader.getInstance().cancelDownload(KEY_SEARCH);

			Intent intent = new Intent(mContext, HotelDetailsFragmentActivity.class);
			mContext.startActivity(intent);
		}
	};

	private final AdapterView.OnItemClickListener mFlightsStreamOnItemClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			com.expedia.bookings.data.Location location = mFlightAdapter.getItem(position);

			FlightSearchParams flightSearchParams = Db.getFlightSearch().getSearchParams();
			flightSearchParams.setArrivalLocation(location);

			NavUtils.goToFlights(getActivity(), true);

			BackgroundDownloader.getInstance().cancelDownload(KEY_SEARCH);
		}
	};
}
