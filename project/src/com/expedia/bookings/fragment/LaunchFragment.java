package com.expedia.bookings.fragment;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.HotelDetailsFragmentActivity;
import com.expedia.bookings.data.*;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.widget.LaunchStreamAdapter;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.LocationServices;
import com.mobiata.android.Log;
import com.mobiata.android.util.NetUtils;
import com.mobiata.android.util.Ui;

import java.util.Calendar;

public class LaunchFragment extends Fragment implements LocationListener {

	public static final String TAG = LaunchFragment.class.toString();

	public static final String KEY_SEARCH = "LAUNCH_SCREEN_HOTEL_SEARCH";

	public static final long MINIMUM_TIME_AGO = 1000 * 60 * 15; // 15 minutes ago

	private Context mContext;

	private ListView mHotelsStreamListView;
	private LaunchStreamAdapter mHotelsStreamAdapter;
	private ListView mFlightsStreamListView;
	private LaunchStreamAdapter mFlightsStreamAdapter;

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

		((TextView) v.findViewById(R.id.hotels_label_text_view)).setTypeface(FontCache
				.getTypeface(FontCache.Font.ROBOTO_LIGHT));
		((TextView) v.findViewById(R.id.hotels_prompt_text_view)).setTypeface(FontCache
				.getTypeface(FontCache.Font.ROBOTO_LIGHT));

		((TextView) v.findViewById(R.id.flights_label_text_view)).setTypeface(FontCache
				.getTypeface(FontCache.Font.ROBOTO_LIGHT));
		((TextView) v.findViewById(R.id.flights_prompt_text_view)).setTypeface(FontCache
				.getTypeface(FontCache.Font.ROBOTO_LIGHT));

		Ui.findView(v, R.id.hotels_button).setOnClickListener(mHeaderItemOnClickListener);
		Ui.findView(v, R.id.flights_button).setOnClickListener(mHeaderItemOnClickListener);

		mHotelsStreamListView = Ui.findView(v, R.id.hotels_stream_list_view);
		mFlightsStreamListView = Ui.findView(v, R.id.flights_stream_list_view);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		SearchResponse searchResponse = Db.getSearchResponse();

		if (searchResponse == null) {
			Location loc = getLocationAndFindLocationIfNull();

			// TODO: sends off a request for hardcoded location to make debugging easier, find way to properly manage
			if (loc == null) {
				SearchParams searchParams = new SearchParams();
				searchParams.setSearchLatLon(37.774541, -122.419453);
				Db.setSearchParams(searchParams);

				startHotelSearch();
			}

			else {
				startHotelSearch(loc);
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		startLocationListener();

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(KEY_SEARCH)) {
			bd.registerDownloadCallback(KEY_SEARCH, mSearchCallback);
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		// Note: We call this here to avoid reusing recycled Bitmaps. Not ideal, but a simple fix for now
		initViews();
	}

	@Override
	public void onStop() {
		super.onStop();

		// Null out the adapter to prevent potentially recycled images from attempting to redraw and crash
		mHotelsStreamListView.setAdapter(null);
	}

	@Override
	public void onPause() {
		super.onPause();

		stopLocationListener();

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.unregisterDownloadCallback(KEY_SEARCH);
	}

	private void initViews() {
		mHotelsStreamAdapter = new LaunchStreamAdapter(mContext);
		mHotelsStreamListView.setAdapter(mHotelsStreamAdapter);

		mHotelsStreamListView.setOnItemClickListener(mHotelsStreamOnItemClickListener);
		mHotelsStreamListView.setOnTouchListener(mHotelsListViewOnTouchListener);

		mFlightsStreamAdapter = new LaunchStreamAdapter(mContext);
		mFlightsStreamListView.setAdapter(mFlightsStreamAdapter);

		mFlightsStreamListView.setOnTouchListener(mFlightsListViewOnTouchListener);

		SearchResponse searchResponse = Db.getSearchResponse();
		if (Db.getSearchResponse() != null) {
			mHotelsStreamAdapter.setProperties(searchResponse);
		}
	}

	private void startHotelSearch(Location loc) {
		SearchParams searchParams = new SearchParams();
		searchParams.setSearchLatLon(loc.getLatitude(), loc.getLongitude());
		Db.setSearchParams(searchParams);

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.cancelDownload(KEY_SEARCH);
		bd.startDownload(KEY_SEARCH, mSearchDownload, mSearchCallback);
	}

	private void startHotelSearch() {
		Log.i("Start hotel search");

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.cancelDownload(KEY_SEARCH);
		bd.startDownload(KEY_SEARCH, mSearchDownload, mSearchCallback);
	}

	private Location getLocationAndFindLocationIfNull() {
		long minTime = Calendar.getInstance().getTimeInMillis() - MINIMUM_TIME_AGO;
		Location loc = LocationServices.getLastBestLocation(mContext, minTime);

		if (loc == null) {
			Log.i("Loc not found, starting the location listener");
			startLocationListener();
		}

		return loc;
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
				mHotelsStreamAdapter.setProperties(searchResponse);
			}

			// TODO: no properties returned, so figure out something to do here... try again with different params?
			else {

			}

		}
	};

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
			Property property = (Property) mHotelsStreamAdapter.getItem(position);
			Db.setSelectedProperty(property);

			BackgroundDownloader.getInstance().cancelDownload(KEY_SEARCH);

			Intent intent = new Intent(mContext, HotelDetailsFragmentActivity.class);
			mContext.startActivity(intent);
		}
	};

	// Note: there is a lot of code duplication between these two onTouchListeners TODO: refactor to one class

	private final View.OnTouchListener mHotelsListViewOnTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// Set the other ListView onTouchListener to null to ensure there does not exist resonance in touch dispatch
			mFlightsStreamListView.setOnTouchListener(null);

			// dispatch the touch event to the other ListView such that they will scroll in unison
			mFlightsStreamListView.dispatchTouchEvent(event);

			// post the resetting of the onTouchListener on ACTION_UP as that is when the ListView is no longer touched
			if (event.getAction() == MotionEvent.ACTION_UP) {
				mFlightsStreamListView.post(new Runnable() {
					@Override
					public void run() {
						mFlightsStreamListView.setOnTouchListener(mFlightsListViewOnTouchListener);
					}
				});
			}

			return false;
		}
	};

	private final View.OnTouchListener mFlightsListViewOnTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			mHotelsStreamListView.setOnTouchListener(null);

			mHotelsStreamListView.dispatchTouchEvent(event);

			if (event.getAction() == MotionEvent.ACTION_UP) {
				mHotelsStreamListView.post(new Runnable() {
					@Override
					public void run() {
						mHotelsStreamListView.setOnTouchListener(mHotelsListViewOnTouchListener);
					}
				});
			}

			return false;
		}
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Location Stuff

	private void startLocationListener() {

		if (!NetUtils.isOnline(mContext)) {
			// We are not going to be able to find location. uhoh
			return;
		}

		// Prefer network location (because it's faster).  Otherwise use GPS
		LocationManager lm = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
		String provider = null;
		if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			provider = LocationManager.NETWORK_PROVIDER;
		}
		else if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			provider = LocationManager.GPS_PROVIDER;
		}

		if (provider == null) {
			Log.w("Could not find a location provider");
		}
		else {
			Log.i("Starting location listener, provider=" + provider);
			lm.requestLocationUpdates(provider, 0, 0, this);
		}
	}

	private void stopLocationListener() {
		LocationManager lm = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
		lm.removeUpdates(this);
	}

	@Override
	public void onLocationChanged(Location location) {
		Log.i("Location found!");

		// TODO: uncomment once we actually want this
		//		startHotelSearch(location);
		//
		//		stopLocationListener();
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.w("onStatusChanged(): provider=" + provider + " status=" + status);

		if (status == LocationProvider.OUT_OF_SERVICE) {
			stopLocationListener();
			Log.w("Location listener failed: out of service");
			//      			simulateErrorResponse(R.string.ProviderOutOfService);
		}
		else if (status == LocationProvider.TEMPORARILY_UNAVAILABLE) {
			stopLocationListener();
			Log.w("Location listener failed: temporarily unavailable");
			//      			simulateErrorResponse(R.string.ProviderTemporarilyUnavailable);
		}

	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.i("onProviderDisabled(): " + provider);

		// Switch to network if it's now available (because it's much faster)
		if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
			LocationManager lm = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
			lm.removeUpdates(this);
			lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
		}

	}

	@Override
	public void onProviderDisabled(String provider) {
		Log.w("onProviderDisabled(): " + provider);

		LocationManager lm = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
		boolean stillWorking = true;

		// If the NETWORK provider is disabled, switch to GPS (if available)
		if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
			if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				lm.removeUpdates(this);
				lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
			}
			else {
				stillWorking = false;
			}
		}
		// If the GPS provider is disabled and we were using it, send error
		else if (provider.equals(LocationManager.GPS_PROVIDER)
				&& !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			stillWorking = false;
		}

		if (!stillWorking) {
			lm.removeUpdates(this);
		}
	}
}
