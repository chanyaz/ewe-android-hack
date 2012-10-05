package com.expedia.bookings.fragment;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.activity.FlightSearchActivity;
import com.expedia.bookings.activity.PhoneSearchActivity;
import com.expedia.bookings.activity.SearchFragmentActivity;
import com.expedia.bookings.data.*;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.Log;
import com.mobiata.android.location.ILastLocationFinder;
import com.mobiata.android.location.LastLocationFinderFactory;
import com.mobiata.android.util.Ui;

public class LaunchFragment extends Fragment {

	public static final String TAG = LaunchFragment.class.toString();

	public static final String KEY_SEARCH = "LAUNCH_SCREEN_HOTEL_SEARCH";

	private static final int LOCATION_DISTANCE_THRESHOLD_IN_MILES = 7;
	private static final int LOCATION_TIME_THRESHOLD_IN_SECONDS = 10;

	private ILastLocationFinder mLastLocationFinder;

	private SearchParams mSearchParams;

	public static LaunchFragment newInstance() {
		return new LaunchFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mLastLocationFinder = LastLocationFinderFactory.getLastLocationFinder(getActivity());
		Location loc = mLastLocationFinder.getLastBestLocation(LOCATION_DISTANCE_THRESHOLD_IN_MILES,
				LOCATION_TIME_THRESHOLD_IN_SECONDS);

		if (loc != null) {
			// create search params with current location
			mSearchParams = new SearchParams();
			mSearchParams.setSearchLatLon(loc.getLatitude(), loc.getLongitude());

			// kick off hotels search
			BackgroundDownloader bd = BackgroundDownloader.getInstance();
			bd.cancelDownload(KEY_SEARCH);
			bd.startDownload(KEY_SEARCH, mSearchDownload, mSearchCallback);
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_launch, container, false);

		Ui.findView(v, R.id.hotels_button).setOnClickListener(mHeaderItemOnClickListener);
		Ui.findView(v, R.id.flights_button).setOnClickListener(mHeaderItemOnClickListener);

		return v;
	}

	private final BackgroundDownloader.Download<SearchResponse> mSearchDownload = new BackgroundDownloader.Download<SearchResponse>() {
		@Override
		public SearchResponse doDownload() {
			ExpediaServices services = new ExpediaServices(getActivity());
			BackgroundDownloader.getInstance().addDownloadListener(KEY_SEARCH, services);
			return services.search(mSearchParams, 0);
		}
	};

	private final BackgroundDownloader.OnDownloadComplete<SearchResponse> mSearchCallback = new BackgroundDownloader.OnDownloadComplete<SearchResponse>() {
		@Override
		public void onDownload(SearchResponse searchResponse) {
			Log.d("Search complete: " + searchResponse.getPropertiesCount());

			Property[] props = searchResponse.getFilteredAndSortedProperties(Filter.Sort.DEALS);
		}
	};

	private final View.OnClickListener mHeaderItemOnClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.hotels_button:
				// #11076 - for Android 3.0, we still use the phone version of the app due to crippling bugs.
				Class<? extends Activity> routingTarget = ExpediaBookingApp.useTabletInterface(getActivity()) ? SearchFragmentActivity.class
						: PhoneSearchActivity.class;

				startActivity(new Intent(getActivity(), routingTarget));

				OmnitureTracking.trackLinkLaunchScreenToHotels(getActivity());
				break;
			case R.id.flights_button:
				startActivity(new Intent(getActivity(), FlightSearchActivity.class));

				OmnitureTracking.trackLinkLaunchScreenToFlights(getActivity());
				break;
			}
		}

	};

}
