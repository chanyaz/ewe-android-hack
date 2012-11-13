package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Pair;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.FlightSearchActivity;
import com.expedia.bookings.activity.FlightUnsupportedPOSActivity;
import com.expedia.bookings.activity.HotelDetailsFragmentActivity;
import com.expedia.bookings.activity.PhoneSearchActivity;
import com.expedia.bookings.data.BackgroundImageResponse;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.ConfirmationState;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Destination;
import com.expedia.bookings.data.Filter;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.HotelDestination;
import com.expedia.bookings.data.LaunchFlightData;
import com.expedia.bookings.data.LaunchHotelData;
import com.expedia.bookings.data.LaunchHotelFallbackData;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.SearchResponse;
import com.expedia.bookings.data.SuggestResponse;
import com.expedia.bookings.data.Suggestion;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.FontCache.Font;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.widget.AirportDropDownAdapter;
import com.expedia.bookings.widget.LaunchFlightAdapter;
import com.expedia.bookings.widget.LaunchHotelAdapter;
import com.expedia.bookings.widget.LaunchStreamListView;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.ImageCache;
import com.mobiata.android.LocationServices;
import com.mobiata.android.Log;
import com.mobiata.android.location.LocationFinder;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.NetUtils;
import com.mobiata.android.util.SettingUtils;
import com.mobiata.android.util.Ui;

public class LaunchFragment extends Fragment implements OnGlobalLayoutListener, OnPreDrawListener {

	public static final String TAG = LaunchFragment.class.getName();
	public static final String KEY_SEARCH = "LAUNCH_SCREEN_HOTEL_SEARCH";
	public static final String KEY_FLIGHT_DESTINATIONS = "LAUNCH_SCREEN_FLIGHT_DESTINATIONS";
	public static final String KEY_HOTEL_DESTINATIONS = "LAUNCH_SCREEN_HOTEL_DESTINATIONS";

	private static final long MINIMUM_TIME_AGO = 1000 * 60 * 15; // 15 minutes ago
	private static final int NUM_HOTEL_PROPERTIES = 20;
	private static final int NUM_FLIGHT_DESTINATIONS = 5;

	// Background images
	private static final Integer[] BACKGROUND_RES_IDS = new Integer[] {
			R.drawable.bg_launch_london,
			R.drawable.bg_launch_ny,
			R.drawable.bg_launch_paris,
			R.drawable.bg_launch_sea,
			R.drawable.bg_launch_sf,
			R.drawable.bg_launch_toronto,
			R.drawable.bg_launch_hongkong,
			R.drawable.bg_launch_petronas,
			R.drawable.bg_launch_vegas
	};

	private Context mContext;

	private ViewGroup mErrorContainer;
	private ViewGroup mScrollContainer;
	private LaunchStreamListView mHotelsStreamListView;
	private LaunchHotelAdapter mHotelAdapter;
	private LaunchStreamListView mFlightsStreamListView;
	private LaunchFlightAdapter mFlightAdapter;

	// Used to prevent launching of both flight and hotel activities at once
	// (as it is otherwise possible to quickly click on both sides).
	private boolean mLaunchingActivity;

	private boolean mCleanOnStop = false;

	private SearchParams mSearchParams;

	private long mLaunchDataTimestamp = -1;

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
		final View v = inflater.inflate(R.layout.fragment_launch, container, false);

		mErrorContainer = Ui.findView(v, R.id.error_container);
		mScrollContainer = Ui.findView(v, R.id.scroll_container);
		mHotelsStreamListView = Ui.findView(v, R.id.hotels_stream_list_view);
		mFlightsStreamListView = Ui.findView(v, R.id.flights_stream_list_view);

		// Pick background image at random
		ImageView bgView = Ui.findView(v, R.id.background_view);
		Random rand = new Random();
		bgView.setImageResource(BACKGROUND_RES_IDS[rand.nextInt(BACKGROUND_RES_IDS.length)]);

		FontCache.setTypeface(v, R.id.hotels_label_text_view, FontCache.Font.ROBOTO_LIGHT);
		FontCache.setTypeface(v, R.id.hotels_prompt_text_view, FontCache.Font.ROBOTO_LIGHT);
		FontCache.setTypeface(v, R.id.flights_label_text_view, FontCache.Font.ROBOTO_LIGHT);
		FontCache.setTypeface(v, R.id.flights_prompt_text_view, FontCache.Font.ROBOTO_LIGHT);
		FontCache.setTypeface(v, R.id.error_message_text_view, Font.ROBOTO_LIGHT);

		Ui.findView(v, R.id.hotels_button).setOnClickListener(mHeaderItemOnClickListener);
		Ui.findView(v, R.id.flights_button).setOnClickListener(mHeaderItemOnClickListener);

		// H833 If the prompt is too wide on this POS/in this language, then hide it
		// (and also hide its sibling to maintain a consistent look)
		// Wrap this in a Runnable so as to happen after the TextViews have been measured
		Ui.findView(v, R.id.hotels_prompt_text_view).post(new Runnable() {
			@Override
			public void run() {
				View hotelPrompt = Ui.findView(v, R.id.hotels_prompt_text_view);
				View hotelIcon = Ui.findView(v, R.id.big_hotel_icon);
				View flightsPrompt = Ui.findView(v, R.id.flights_prompt_text_view);
				View flightsIcon = Ui.findView(v, R.id.big_flights_icon);
				if (hotelPrompt.getLeft() < hotelIcon.getRight() || flightsPrompt.getLeft() < flightsIcon.getRight()) {
					hotelPrompt.setVisibility(View.INVISIBLE);
					flightsPrompt.setVisibility(View.INVISIBLE);
				}
			}
		});

		mHotelsStreamListView.setScrollMultiplier(2.0);
		mFlightsStreamListView.setScrollMultiplier(1.0);

		return v;
	}

	@Override
	public void onResume() {
		super.onResume();

		// Note: We call this here to avoid reusing recycled Bitmaps. Not ideal, but a simple fix for now
		initViews();

		mLaunchingActivity = false;

		if (isExpired()) {
			// Expired so we blow this data away to kick off a new search
			Db.setLaunchHotelData(null);
		}

		onReactToUserActive();

		startMarquee();

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
		bd.unregisterDownloadCallback(KEY_HOTEL_DESTINATIONS);
		getActivity().unregisterReceiver(mConnReceiver);
	}

	@Override
	public void onStop() {
		super.onStop();

		mHotelsStreamListView.savePosition();
		mFlightsStreamListView.savePosition();

		if (mLaunchDataTimestamp == -1) {
			mLaunchDataTimestamp = Calendar.getInstance().getTimeInMillis();
		}

		cleanUpOnStop();

		if (getActivity().isFinishing()) {
			// Unload the current hotel/flight data, so we don't reload it
			Db.setLaunchHotelData(null);
			Db.setLaunchFlightData(null);
			ImageCache.recycleCache(true);
		}
	}

	private void onReactToUserActive() {
		// Check that we're online first
		if (!checkConnection()) {
			return;
		}

		BackgroundDownloader bd = BackgroundDownloader.getInstance();

		LaunchHotelData launchHotelData = Db.getLaunchHotelData();

		if (launchHotelData == null) {
			// No cached hotel data exists, perform the least amount of effort in order to get results on screen by following
			// the logic below

			// A hotel search is underway, register dl callback in case it was removed
			if (bd.isDownloading(KEY_SEARCH)) {
				bd.registerDownloadCallback(KEY_SEARCH, mSearchCallback);
			}

			// No cached hotel data and no hotel search downloading...that must mean we need to find a Location!
			else {
				Location location = null;
				if (!AndroidUtils.isRelease(mContext)) {
					String fakeLatLng = SettingUtils.get(mContext,
							getString(R.string.preference_fake_current_location), "");
					if (!TextUtils.isEmpty(fakeLatLng)) {
						String[] split = fakeLatLng.split(",");
						if (split.length == 2) {
							Log.i("Using fake location for hotel search!");
							location = new Location("fakeProvider");
							location.setLatitude(Double.parseDouble(split[0]));
							location.setLongitude(Double.parseDouble(split[1]));
						}
					}
				}

				// Attempt to find last best Location from OS cache
				if (location == null) {
					long minTime = Calendar.getInstance().getTimeInMillis() - MINIMUM_TIME_AGO;
					location = LocationServices.getLastBestLocation(mContext, minTime);
				}

				// force location fetch by setting location null. use fake location if it exists, though.
				if (!AndroidUtils.isRelease(mContext)) {
					if (SettingUtils.get(mContext, getString(R.string.preference_force_new_location), false)) {
						String fakeLatLng = SettingUtils.get(mContext,
								getString(R.string.preference_fake_current_location), "");
						if (TextUtils.isEmpty(fakeLatLng)) {
							location = null;
						}
					}
				}

				// No cached location found, find a new location update as quickly and low-power as possible
				if (location == null) {
					findLocation();
				}

				// Location found from cache, kick off hotel search
				else {
					Log.i("Location found from OS cache");
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

	private boolean isExpired() {
		if (mLaunchDataTimestamp != -1 && mLaunchDataTimestamp + MINIMUM_TIME_AGO < Calendar.getInstance().getTimeInMillis()) {
			return true;
		}
		else {
			return false;
		}
	}

	// Location finder

	private LocationFinder mLocationFinder;

	private void findLocation() {
		if (mLocationFinder == null) {
			mLocationFinder = LocationFinder.getInstance(mContext);
			mLocationFinder.setListener(new LocationFinder.LocationFinderListener() {
				@Override
				public void onLocationFound(Location location) {
					startHotelSearch(location);
				}

				@Override
				public void onLocationServicesDisabled() {
					if (Db.getLaunchHotelFallbackData() == null) {
						startHotelFallbackDownload();
					}
					else {
						onHotelFallbackDataRetrieved();
					}
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

		mSearchParams = new SearchParams();
		mSearchParams.setSearchLatLon(loc.getLatitude(), loc.getLongitude());

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.cancelDownload(KEY_SEARCH);
		bd.startDownload(KEY_SEARCH, mSearchDownload, mSearchCallback);
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
			if (searchResponse != null) {
				Log.d("Search complete: " + searchResponse.getPropertiesCount());
			}

			// Response was good, we are going to use this stuff
			if (searchResponse != null && searchResponse.getPropertiesCount() > 0 && !searchResponse.hasErrors()) {

				// We only want to set the the search from Launch if there exists no SearchResponse data already (to avoid
				// sending the user through another network request when jumping to Hotels). If there already exists a 
				// Search response in the Db, do not flush it out.
				if (isExpired() || Db.getSearchResponse() == null) {
					Db.setSearchParams(mSearchParams);
					Db.setSearchResponse(searchResponse);
				}

				// Extract relevant data here
				LaunchHotelData launchHotelData = new LaunchHotelData();
				List<Property> properties = searchResponse.getFilteredAndSortedProperties(Filter.Sort.DEALS,
						NUM_HOTEL_PROPERTIES);
				launchHotelData.setProperties(properties);
				launchHotelData.setDistanceUnit(searchResponse.getFilter().getDistanceUnit());
				Db.setLaunchHotelData(launchHotelData);
				Db.setLaunchHotelFallbackData(null);

				mLaunchDataTimestamp = Calendar.getInstance().getTimeInMillis();
				onHotelDataRetrieved();
			}

			// Hotel search failed; use the fallback destination image plan
			else {
				startHotelFallbackDownload();
			}
		}
	};

	private void onHotelDataRetrieved() {
		mHotelAdapter.setProperties(Db.getLaunchHotelData());
		mHotelsStreamListView.setSelector(R.drawable.abs__item_background_holo_dark);
		mHotelsStreamListView.setOnItemClickListener(mHotelsStreamOnItemClickListener);
	}

	// Flight destination search

	private final static String[] DESTINATION_IDS = new String[] {
			"SEA",
			"SFO",
			"LON",
			"PAR",
			"LAS",
			"NYC",
			"YYZ",
			"HKG",
			"MIA",
			"BKK",
	};

	private Download<List<Destination>> mFlightsDownload = new Download<List<Destination>>() {
		@Override
		public List<Destination> doDownload() {
			ExpediaServices services = new ExpediaServices(mContext);
			BackgroundDownloader.getInstance().addDownloadListener(KEY_FLIGHT_DESTINATIONS, services);
			List<Destination> destinations = new ArrayList<Destination>();

			Display display = getActivity().getWindowManager().getDefaultDisplay();
			int width = Math.round(display.getWidth() / 2);
			int height = Math.round(getResources().getDimension(R.dimen.launch_tile_height_flight));

			// Randomly shuffle destinations
			List<String> destinationIds = Arrays.asList(DESTINATION_IDS);
			Collections.shuffle(destinationIds);

			// Collection up to NUM_FLIGHT_DESTINATIONS destinations
			for (String destinationId : destinationIds) {
				if (destinations.size() == NUM_FLIGHT_DESTINATIONS) {
					break;
				}

				// Before using services, check if this download has been cancelled
				if (Thread.interrupted()) {
					return null;
				}

				// Autocomplete each location to get the proper info on it
				SuggestResponse suggestResponse = services.suggest(destinationId, ExpediaServices.F_FLIGHTS);

				if (suggestResponse == null) {
					Log.w("Got a null response from server autocompleting for: " + destinationId);
					continue;
				}
				else if (suggestResponse.hasErrors()) {
					Log.w("Got an error response from server autocompleting for: "
							+ destinationId + ", " + suggestResponse.getErrors().get(0).getPresentableMessage(mContext));
					continue;
				}

				List<Suggestion> suggestions = suggestResponse.getSuggestions();
				if (suggestions.size() == 0) {
					Log.w("Got 0 suggestions while autocompleting for: " + destinationId);
					continue;
				}

				Suggestion firstSuggestion = suggestions.get(0);
				Pair<String, String> displayName = firstSuggestion.splitDisplayNameForFlights();
				String destId = firstSuggestion.getAirportLocationCode();
				Destination destination = new Destination(destId, displayName.first, displayName.second);

				// Now try to get metadata

				// Before using services, check if this download has been cancelled
				if (Thread.interrupted()) {
					return null;
				}

				BackgroundImageResponse imageResponse = services.getFlightsBackgroundImage(destId, width, height);
				if (imageResponse == null) {
					Log.w("Got a null response from server looking for destination bg for: " + destId);
				}
				else if (imageResponse.hasErrors()) {
					Log.w("Got an error response from server looking for destination bg for: "
							+ destId + ", " + imageResponse.getErrors().get(0).getPresentableMessage(mContext));
				}
				else {
					Log.v("Got destination data for: " + destId);
					destination.setImageMeta(imageResponse.getCacheKey(), imageResponse.getImageUrl());
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
		mFlightsStreamListView.setSelector(R.drawable.abs__item_background_holo_dark);
		mFlightsStreamListView.setOnItemClickListener(mFlightsStreamOnItemClickListener);
	}

	// Hotels fallback

	private final static List<HotelDestination> HOTEL_DESTINATION_FALLBACK_LIST = new ArrayList<HotelDestination>() {
		{
			add(new HotelDestination().setLaunchTileText("Dubai").setImgUrl(
					"http://media.expedia.com/hotels/1000000/530000/527500/527497/527497_66_y.jpg"));
			add(new HotelDestination().setLaunchTileText("Miami").setImgUrl(
					"http://media.expedia.com/hotels/2000000/1200000/1190600/1190549/1190549_45_y.jpg"));
			add(new HotelDestination().setLaunchTileText("Las Vegas").setImgUrl(
					"http://media.expedia.com/hotels/1000000/20000/16000/15930/15930_147_y.jpg"));
			add(new HotelDestination().setLaunchTileText("San Francisco").setImgUrl(
					"http://media.expedia.com/hotels/1000000/30000/22200/22148/22148_34_y.jpg"));
			add(new HotelDestination().setLaunchTileText("Seattle").setImgUrl(
					"http://media.expedia.com/hotels/1000000/550000/546500/546475/546475_84_y.jpg"));
			add(new HotelDestination().setLaunchTileText("Honolulu").setImgUrl(
					"http://media.expedia.com/hotels/1000000/40000/34500/34498/34498_111_y.jpg"));
			add(new HotelDestination().setLaunchTileText("New York").setImgUrl(
					"http://media.expedia.com/hotels/4000000/3490000/3481700/3481640/3481640_39_y.jpg"));
			add(new HotelDestination().setLaunchTileText("Maldives").setImgUrl(
					"http://media.expedia.com/hotels/2000000/1780000/1775600/1775548/1775548_63_y.jpg"));
			add(new HotelDestination().setLaunchTileText("Paris").setImgUrl(
					"http://media.expedia.com/hotels/1000000/30000/23100/23034/23034_175_y.jpg"));
			add(new HotelDestination().setLaunchTileText("Boston").setImgUrl(
					"http://media.expedia.com/hotels/1000000/10000/400/395/395_36_y.jpg"));
		}
	};

	private Download<List<HotelDestination>> mHotelsFallbackDownload = new Download<List<HotelDestination>>() {
		@Override
		public List<HotelDestination> doDownload() {
			ExpediaServices services = new ExpediaServices(mContext);
			BackgroundDownloader.getInstance().addDownloadListener(KEY_HOTEL_DESTINATIONS, services);
			List<HotelDestination> destinations = HOTEL_DESTINATION_FALLBACK_LIST;

			for (HotelDestination hotel : destinations) {
				// Before using services, check if this download has been cancelled
				if (Thread.interrupted()) {
					return null;
				}

				SuggestResponse suggestResponse = services.suggest(hotel.getLaunchTileText(), ExpediaServices.F_HOTELS);

				if (suggestResponse == null) {
					Log.w("Got a null response from server autocompleting for: " + hotel.getLaunchTileText());
					continue;
				}
				else if (suggestResponse.hasErrors()) {
					Log.w("Got an error response from server autocompleting for: " + hotel.getLaunchTileText() + ", "
							+ suggestResponse.getErrors().get(0).getPresentableMessage(mContext));
					continue;
				}

				List<Suggestion> suggestions = suggestResponse.getSuggestions();
				if (suggestions.size() == 0) {
					Log.w("Got 0 suggestions while autocompleting for: " + hotel.getLaunchTileText());
					continue;
				}

				Suggestion suggestion = suggestions.get(0);
				hotel.setLatitudeLongitude(suggestion.getLatitude(), suggestion.getLongitude());
				hotel.setRegionId(suggestion.getId());
				hotel.setPhoneSearchDisplayText(suggestion.getDisplayName());
			}

			return destinations;
		}
	};

	private OnDownloadComplete<List<HotelDestination>> mHotelsFallbackCallback = new OnDownloadComplete<List<HotelDestination>>() {
		@Override
		public void onDownload(List<HotelDestination> results) {
			if (results != null && results.size() > 0) {
				LaunchHotelFallbackData launchHotelFallbackData = new LaunchHotelFallbackData();
				launchHotelFallbackData.setDestinations(results);
				Db.setLaunchHotelFallbackData(launchHotelFallbackData);
				Db.setLaunchHotelData(null);

				onHotelFallbackDataRetrieved();
			}
		}
	};

	private void startHotelFallbackDownload() {
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.cancelDownload(KEY_HOTEL_DESTINATIONS);
		bd.startDownload(KEY_HOTEL_DESTINATIONS, mHotelsFallbackDownload, mHotelsFallbackCallback);
	}

	private void onHotelFallbackDataRetrieved() {
		mHotelAdapter.setHotelDestinations(Db.getLaunchHotelFallbackData());
		mHotelsStreamListView.setSelector(R.drawable.abs__item_background_holo_dark);
		mHotelsStreamListView.setOnItemClickListener(mHotelsStreamOnItemClickListener);
	}

	// View init + listeners

	private void initViews() {
		if (mHotelAdapter != null && mFlightAdapter != null) {
			return;
		}

		Log.d("LaunchFragment.initViews() - initializing views...");

		if (mHotelAdapter == null) {
			mHotelAdapter = new LaunchHotelAdapter(mContext);
			mHotelsStreamListView.setAdapter(mHotelAdapter);
			if (!mHotelsStreamListView.restorePosition()) {
				mHotelsStreamListView.selectMiddle();
			}
		}

		mFlightAdapter = new LaunchFlightAdapter(mContext);
		mFlightsStreamListView.setAdapter(mFlightAdapter);
		if (!mFlightsStreamListView.restorePosition()) {
			mFlightsStreamListView.selectMiddle();
		}

		mHotelsStreamListView.setSlaveView(mFlightsStreamListView);

		mFlightsStreamListView.setSlaveView(mHotelsStreamListView);

		if (Db.getLaunchHotelFallbackData() != null) {
			onHotelFallbackDataRetrieved();
		}
		else if (Db.getLaunchHotelData() != null) {
			onHotelDataRetrieved();
		}

		if (Db.getLaunchFlightData() != null) {
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
			if (mLaunchingActivity) {
				return;
			}

			Object item = mHotelAdapter.getItem(position);
			if (item == null) {
				return;
			}

			mLaunchingActivity = true;

			// Delete Hotel ConfirmationState if it exists
			ConfirmationState.delete(mContext, ConfirmationState.Type.HOTEL);

			if (item instanceof Property) {
				Property property = (Property) item;

				// H1041: Clear out the current search results
				Db.clearAvailabilityResponses();
				Db.clearReviewsResponses();
				Db.clearReviewsStatisticsResponses();
				Db.setSearchResponse(null);
				if (mSearchParams == null) {
					mSearchParams = new SearchParams();
				}

				Db.setSearchParams(mSearchParams);
				Db.setSelectedProperty(property);

				Intent intent = new Intent(mContext, HotelDetailsFragmentActivity.class);
				intent.putExtra(HotelDetailsMiniGalleryFragment.ARG_FROM_LAUNCH, true);
				mContext.startActivity(intent);
			}
			else if (item instanceof HotelDestination) {
				HotelDestination destination = (HotelDestination) item;

				SearchParams searchParams = Db.getSearchParams();

				// where
				searchParams.setQuery(destination.getPhoneSearchDisplayText());
				searchParams.setSearchType(SearchParams.SearchType.CITY);
				searchParams.setRegionId(destination.getRegionId());
				searchParams.setSearchLatLon(destination.getLatitude(), destination.getLongitude());

				// when
				Calendar cal = Calendar.getInstance();
				int year = cal.get(Calendar.YEAR);
				int month = cal.get(Calendar.MONTH);
				int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
				searchParams.setCheckInDate(new GregorianCalendar(year, month, dayOfMonth + 1));
				searchParams.setCheckOutDate(new GregorianCalendar(year, month, dayOfMonth + 2));

				//who
				searchParams.setNumAdults(1);
				searchParams.setChildren(null);

				// Launch hotel search
				Intent intent = new Intent(mContext, PhoneSearchActivity.class);
				intent.putExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS, true);
				mContext.startActivity(intent);
			}

			cleanUp();
		}
	};

	private final AdapterView.OnItemClickListener mFlightsStreamOnItemClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if (mLaunchingActivity) {
				return;
			}

			mLaunchingActivity = true;

			Destination destination = mFlightAdapter.getItem(position);

			com.expedia.bookings.data.Location location = new com.expedia.bookings.data.Location();
			location.setDestinationId(destination.getDestinationId());
			location.setCity(destination.getCity());
			location.setDescription(destination.getDescription());

			FlightSearchParams flightSearchParams = Db.getFlightSearch().getSearchParams();
			flightSearchParams.reset(); // F1303: clear all params on tile click
			flightSearchParams.setArrivalLocation(location);

			// F1304: Add this tile to recently selected airports
			AirportDropDownAdapter.addAirportToRecents(getActivity(), location);

			// Make sure to delete Flight confirmation state if it exists
			ConfirmationState.delete(mContext, ConfirmationState.Type.FLIGHT);

			// F1330: Tapping on tiles should take you to unsupported POS page
			// if you are on an unsupported POS!
			if (!FlightUnsupportedPOSActivity.isSupportedPOS(mContext)) {
				mContext.startActivity(new Intent(mContext, FlightUnsupportedPOSActivity.class));
			}
			else {
				Intent intent = new Intent(mContext, FlightSearchActivity.class);
				intent.putExtra(FlightSearchActivity.ARG_FROM_LAUNCH_WITH_SEARCH_PARAMS, true);
				mContext.startActivity(intent);
			}

			cleanUp();
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Clean up
	//
	// Clean up happens in two phases.  This is because some things you want
	// to clean immediately, but others if you clean them immediately it
	// makes it ugly

	public void cleanUp() {
		Log.d("LaunchFragment.cleanUp()");

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.cancelDownload(KEY_SEARCH);
		bd.cancelDownload(KEY_FLIGHT_DESTINATIONS);
		bd.cancelDownload(KEY_HOTEL_DESTINATIONS);

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

		if (mHotelAdapter != null) {
			mHotelAdapter.clear();
			mHotelAdapter = null;
		}

		mFlightsStreamListView.setAdapter(null);
		mFlightAdapter.setDestinations(null);
		mFlightAdapter = null;

		// TODO: Clean up more as necessary (e.g., cleaning out the ImageCache).
	}

	/**
	 * Completely resets the results.  Should only be used before initViews()
	 */
	public void reset() {
		Db.setLaunchFlightData(null);
		Db.setLaunchHotelData(null);
		Db.setLaunchHotelFallbackData(null);
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

					// Only react to user being active if we are active
					if (isAdded()) {
						onReactToUserActive();
					}
				}
			}
		}
	};

	private boolean checkConnection() {
		Context context = getActivity();
		if (context != null && !NetUtils.isOnline(context)) {
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

	//////////////////////////////////////////////////////////////////////////
	// OnGlobalLayoutListener, OnPreDrawListener
	//
	// F1128: Starting the marquee before a LaunchStreamListView has actually
	// rendered seems to cause much sadness.  So we delay the start of the
	// marquee until it's actually been rendered once.
	//
	// We also need to use both preDraw and onGlobalLayout, as sometimes one
	// or the other is only called.

	private void startMarquee() {
		ViewTreeObserver vto = mHotelsStreamListView.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(this);
		vto.addOnPreDrawListener(this);
	}

	private void startMarqueeImpl() {
		mHotelsStreamListView.startMarquee();

		ViewTreeObserver vto = mHotelsStreamListView.getViewTreeObserver();
		vto.removeGlobalOnLayoutListener(this);
		vto.removeOnPreDrawListener(this);
	}

	@Override
	public void onGlobalLayout() {
		startMarqueeImpl();
	}

	@Override
	public boolean onPreDraw() {
		startMarqueeImpl();
		return true;
	}
}
