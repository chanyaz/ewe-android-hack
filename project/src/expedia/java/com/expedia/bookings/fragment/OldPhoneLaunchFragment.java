package com.expedia.bookings.fragment;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONObject;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.activity.FlightUnsupportedPOSActivity;
import com.expedia.bookings.activity.HotelDetailsFragmentActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Destination;
import com.expedia.bookings.data.ExpediaFlightDestinations;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.HotelDestination;
import com.expedia.bookings.data.HotelFilter;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.HotelSearchResponse;
import com.expedia.bookings.data.LaunchFlightData;
import com.expedia.bookings.data.LaunchHotelData;
import com.expedia.bookings.data.LaunchHotelFallbackData;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.SuggestResponse;
import com.expedia.bookings.data.Suggestion;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.data.trips.Trip;
import com.expedia.bookings.fragment.FusedLocationProviderFragment.FusedLocationProviderListener;
import com.expedia.bookings.interfaces.IPhoneLaunchActivityLaunchFragment;
import com.expedia.bookings.interfaces.IPhoneLaunchFragmentListener;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.Akeakamai;
import com.expedia.bookings.utils.AnimUtils;
import com.expedia.bookings.utils.ExpediaDebugUtil;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.FontCache.Font;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.AirportDropDownAdapter;
import com.expedia.bookings.widget.LaunchFlightAdapter;
import com.expedia.bookings.widget.LaunchHotelAdapter;
import com.expedia.bookings.widget.LaunchStreamListView;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.IoUtils;
import com.mobiata.android.util.NetUtils;
import com.mobiata.android.util.SettingUtils;

public class OldPhoneLaunchFragment extends Fragment implements OnGlobalLayoutListener, OnPreDrawListener,
	IPhoneLaunchActivityLaunchFragment {

	public static final String TAG = PhoneLaunchFragment.class.getName();
	public static final String KEY_SEARCH = "LAUNCH_SCREEN_HOTEL_SEARCH";
	public static final String KEY_FLIGHT_DESTINATIONS = "LAUNCH_SCREEN_FLIGHT_DESTINATIONS";
	public static final String KEY_HOTEL_DESTINATIONS = "LAUNCH_SCREEN_HOTEL_DESTINATIONS";

	private static final long MINIMUM_TIME_AGO = 15 * DateUtils.MINUTE_IN_MILLIS; // 15 minutes ago
	private static final int NUM_HOTEL_PROPERTIES = 20;
	private static final int NUM_FLIGHT_DESTINATIONS = 5;

	// Background images
	private static final Integer[] BACKGROUND_RES_IDS = new Integer[] {
		R.drawable.bg_launch_london,
		R.drawable.bg_launch_ny,
		R.drawable.bg_launch_paris,
		R.drawable.bg_launch_sf,
		R.drawable.bg_launch_hongkong,
		R.drawable.bg_launch_vegas,
	};

	private ImageView mBgView;
	private ViewGroup mErrorContainer;
	private ViewGroup mScrollContainer;
	private LaunchStreamListView mHotelsStreamListView;
	private LaunchHotelAdapter mHotelAdapter;
	private LaunchStreamListView mFlightsStreamListView;
	private LaunchFlightAdapter mFlightAdapter;

	private ViewGroup mAirAttachC;
	private View mAirAttachClose;

	// Used to prevent launching of both flight and hotel activities at once
	// (as it is otherwise possible to quickly click on both sides).
	private boolean mLaunchingActivity;

	private boolean mCleanOnStop = false;

	private HotelSearchParams mSearchParams;

	private DateTime mLaunchDataTimestamp;

	// Invisible Fragment that handles FusedLocationProvider
	private FusedLocationProviderFragment mLocationFragment;

	// Background bitmap (for recycling later)
	private int mBgViewIndex;
	private Bitmap mBgBitmap;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (activity instanceof IPhoneLaunchFragmentListener) {
			((IPhoneLaunchFragmentListener) activity).onLaunchFragmentAttached(this);
		}

		mLocationFragment = FusedLocationProviderFragment.getInstance(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_phone_launch, container, false);

		mBgView = Ui.findView(v, R.id.background_view);
		mErrorContainer = Ui.findView(v, R.id.error_container);
		mScrollContainer = Ui.findView(v, R.id.scroll_container);
		mHotelsStreamListView = Ui.findView(v, R.id.hotels_stream_list_view);
		mFlightsStreamListView = Ui.findView(v, R.id.flights_stream_list_view);

		Ui.findView(v, R.id.hotels_button).setOnClickListener(mHeaderItemOnClickListener);
		Ui.findView(v, R.id.flights_button).setOnClickListener(mHeaderItemOnClickListener);

		FontCache.setTypeface(v, R.id.error_message_text_view, Font.ROBOTO_LIGHT);

		mHotelsStreamListView.setScrollMultiplier(2.0);
		mFlightsStreamListView.setScrollMultiplier(1.0);

		// H833 If the prompt is too wide on this POS/in this language, then hide it
		// (and also hide its sibling to maintain a consistent look)
		// Wrap this in a Runnable so as to happen after the TextViews have been measured
		Ui.findView(v, R.id.hotels_prompt_text_view).post(new Runnable() {
			@Override
			public void run() {
				// #843: Check that we are still attached before doing this, as we may not be anymore
				// (in the case of quick rotation).
				if (getActivity() != null) {
					View hotelPrompt = Ui.findView(getActivity(), R.id.hotels_prompt_text_view);
					View hotelIcon = Ui.findView(getActivity(), R.id.big_hotel_icon);
					View flightsPrompt = Ui.findView(getActivity(), R.id.flights_prompt_text_view);
					View flightsIcon = Ui.findView(getActivity(), R.id.big_flights_icon);
					if (hotelPrompt.getLeft() < hotelIcon.getRight()
						|| flightsPrompt.getLeft() < flightsIcon.getRight()) {
						hotelPrompt.setVisibility(View.INVISIBLE);
						flightsPrompt.setVisibility(View.INVISIBLE);
					}
				}
			}
		});

		// Air Attach
		mAirAttachC = Ui.findView(v, R.id.air_attach_banner_container);
		mAirAttachClose = Ui.findView(v, R.id.air_attach_banner_close);
		final ItineraryManager itinMan = ItineraryManager.getInstance();
		if (Db.getTripBucket() != null && Db.getTripBucket().isUserAirAttachQualified()) {
			if (itinMan.isSyncing()) {
				itinMan.addSyncListener(mItinListener);
			}
			else {
				showAirAttachBannerIfNecessary();
			}
		}

		return v;
	}

	@Override
	public void onStart() {
		super.onStart();

		if (mBgView.getParent() == null) {
			mScrollContainer.addView(mBgView, mBgViewIndex);
		}

		ActivityManager am = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
		int memoryClass = am.getMemoryClass();
		float density = getResources().getDisplayMetrics().density;
		if (memoryClass < 48 && memoryClass / density < 28.5f) {
			// We fallback to a low-memory bg color, depending on density and memory class
			Log.d("Launcher using simple bg, memoryClass=" + memoryClass + " density=" + density);
			mBgView.setImageResource(R.color.low_memory_bg_color);
		}
		else {
			// Pick background image at random
			Random rand = new Random();
			mBgBitmap = BitmapFactory.decodeResource(getResources(),
				BACKGROUND_RES_IDS[rand.nextInt(BACKGROUND_RES_IDS.length)]);
			mBgView.setImageBitmap(mBgBitmap);
		}

		initViews();
	}

	@Override
	public void onResume() {
		super.onResume();

		mLaunchingActivity = false;

		if (isExpired()) {
			// Expired so we blow this data away to kick off a new search
			Db.setLaunchHotelData(null);
		}

		onReactToUserActive();
		showAirAttachBannerIfNecessary();

		if (!ExpediaBookingApp.sIsAutomation) {
			startMarquee();
		}

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

		ItineraryManager.getInstance().removeSyncListener(mItinListener);
	}

	@Override
	public void onStop() {
		super.onStop();

		mHotelsStreamListView.savePosition();
		mFlightsStreamListView.savePosition();

		if (mLaunchDataTimestamp == null) {
			mLaunchDataTimestamp = DateTime.now();
		}

		cleanUpOnStop();

		if (getActivity().isFinishing()) {
			// Unload the current hotel/flight data, so we don't reload it
			Db.setLaunchHotelData(null);
			Db.setLaunchFlightData(null);
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
				Location location = ExpediaDebugUtil.getFakeLocation(getActivity());
				Context context = getActivity();

				// force location fetch by setting location null. use fake location if it exists, though.
				if (!AndroidUtils.isRelease(context)) {
					if (SettingUtils.get(context, getString(R.string.preference_force_new_location), false)) {
						String fakeLatLng = SettingUtils.get(context,
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
		return JodaUtils.isExpired(mLaunchDataTimestamp, MINIMUM_TIME_AGO);
	}

	// Location finder

	private void findLocation() {

		if (!NetUtils.isOnline(getActivity())) {
			useHotelFallback();
			return;
		}

		mLocationFragment.find(new FusedLocationProviderListener() {

			@Override
			public void onFound(Location currentLocation) {
				startHotelSearch(currentLocation);
			}

			@Override
			public void onError() {
				useHotelFallback();
			}
		});
	}

	private void stopLocation() {
		if (mLocationFragment != null) {
			mLocationFragment.stop();
		}
	}

	// Hotel search

	private void startHotelSearch(Location loc) {
		Log.i("Start hotel search");

		mSearchParams = new HotelSearchParams();
		mSearchParams.setSearchLatLon(loc.getLatitude(), loc.getLongitude());

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.cancelDownload(KEY_SEARCH);
		bd.startDownload(KEY_SEARCH, mSearchDownload, mSearchCallback);
	}

	private final BackgroundDownloader.Download<HotelSearchResponse> mSearchDownload = new BackgroundDownloader.Download<HotelSearchResponse>() {
		@Override
		public HotelSearchResponse doDownload() {
			ExpediaServices services = new ExpediaServices(getActivity());
			BackgroundDownloader.getInstance().addDownloadListener(KEY_SEARCH, services);
			return services.search(mSearchParams, 0);
		}
	};

	private final BackgroundDownloader.OnDownloadComplete<HotelSearchResponse> mSearchCallback = new BackgroundDownloader.OnDownloadComplete<HotelSearchResponse>() {
		@Override
		public void onDownload(HotelSearchResponse searchResponse) {
			if (searchResponse != null) {
				Log.d("Search complete: " + searchResponse.getPropertiesCount());
			}

			// Response was good, we are going to use this stuff
			if (searchResponse != null && searchResponse.getPropertiesCount() > 1 && !searchResponse.hasErrors()) {

				// We only want to set the the search from Launch if there exists no HotelSearchResponse data already (to avoid
				// sending the user through another network request when jumping to Hotels). If there already exists a
				// Search response in the Db, do not flush it out.
				if (isExpired() || Db.getHotelSearch().getSearchResponse() == null) {
					Db.getHotelSearch().setSearchParams(mSearchParams);
					Db.getHotelSearch().setSearchResponse(searchResponse);
				}

				if (Db.getHotelSearch().getSearchParams() != null) {
					// Extract relevant data here
					LaunchHotelData launchHotelData = new LaunchHotelData();
					List<Property> properties = searchResponse.getFilteredAndSortedProperties(HotelFilter.Sort.DEALS,
						NUM_HOTEL_PROPERTIES, Db.getHotelSearch().getSearchParams());
					launchHotelData.setProperties(properties);
					launchHotelData.setDistanceUnit(searchResponse.getFilter().getDistanceUnit());
					Db.setLaunchHotelData(launchHotelData);
					Db.setLaunchHotelFallbackData(null);

					mLaunchDataTimestamp = DateTime.now();
					onHotelDataRetrieved();
					return;
				}
			}

			// Hotel search failed; use the fallback destination image plan
			startHotelFallbackDownload();
		}
	};

	private void onHotelDataRetrieved() {
		mHotelAdapter.setProperties(Db.getLaunchHotelData());

		mHotelsStreamListView.setSelector(Ui.obtainThemeDrawable(getActivity(), android.R.attr.selectableItemBackground));
		mHotelsStreamListView.setOnItemClickListener(mHotelsStreamOnItemClickListener);
	}

	// Flight destination search

	private Download<List<Destination>> mFlightsDownload = new Download<List<Destination>>() {
		@Override
		public List<Destination> doDownload() {
			ExpediaServices services = new ExpediaServices(getActivity());
			BackgroundDownloader.getInstance().addDownloadListener(KEY_FLIGHT_DESTINATIONS, services);
			List<Destination> destinations = new ArrayList<Destination>();

			Point size = AndroidUtils.getScreenSize(getActivity());
			int width = Math.round(size.x / 2.0f * 0.8f);
			int height = Math.round(getResources().getDimension(R.dimen.launch_tile_height_flight) * 0.8f);

			//Get flight destination list for the current POS
			PointOfSale pos = PointOfSale.getPointOfSale();
			ExpediaFlightDestinations expediaFlightDestinations = new ExpediaFlightDestinations(getActivity());
			List<String> destinationIds;
			if (expediaFlightDestinations.usesDefaultDestinationList(pos)) {
				destinationIds = Arrays.asList(expediaFlightDestinations.getDestinations(pos));
			}
			else {
				//For POSs with their own destination lists, we just get the first NUM_FLIGHT_DESTINATIONS, as they are ordered by popularity
				destinationIds = Arrays.asList(expediaFlightDestinations.getDestinations(pos, NUM_FLIGHT_DESTINATIONS));
			}
			// Randomly shuffle destinations
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
					Log.w("Got an error response from server autocompleting for: " + destinationId + ", "
						+ suggestResponse.getErrors().get(0).getPresentableMessage(getActivity()));
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

				// #1535: If the suggest data is bad for some reason, don't show this destination
				if (displayName == null) {
					continue;
				}

				// Now try to get metadata

				// Before using services, check if this download has been cancelled
				if (Thread.interrupted()) {
					return null;
				}

				final String url = new Akeakamai(Images.getFlightDestination(destId)) //
					.resizeExactly(width, height) //
					.build();
				Destination destination = new Destination(destId, displayName.first, displayName.second, url);
				destinations.add(destination);
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
		mFlightsStreamListView.setSelector(Ui.obtainThemeDrawable(getActivity(), android.R.attr.selectableItemBackground));
		mFlightsStreamListView.setOnItemClickListener(mFlightsStreamOnItemClickListener);
	}

	// Hotels fallback

	private List<HotelDestination> loadFallbackDestinations() {
		List<HotelDestination> destinations = new ArrayList<HotelDestination>();
		try {
			InputStream is = getActivity().getAssets().open("ExpediaSharedData/ExpediaHotelFallbackLocations.json");
			String data = IoUtils.convertStreamToString(is);
			JSONArray locationArr = new JSONArray(data);
			int len = locationArr.length();
			for (int a = 0; a < len; a++) {
				JSONObject locationObj = locationArr.getJSONObject(a);
				HotelDestination destination = new HotelDestination();
				destination.setDestination(locationObj.getString("destination"));
				destination.setImgUrl(locationObj.getString("imageURL"));
				destinations.add(destination);
			}
		}
		catch (Exception e) {
			// If this data fails to load, then we should fail horribly
			throw new RuntimeException(e);
		}
		return destinations;
	}

	private Download<List<HotelDestination>> mHotelsFallbackDownload = new Download<List<HotelDestination>>() {
		@Override
		public List<HotelDestination> doDownload() {
			ExpediaServices services = new ExpediaServices(getActivity());
			BackgroundDownloader.getInstance().addDownloadListener(KEY_HOTEL_DESTINATIONS, services);
			List<HotelDestination> destinations = loadFallbackDestinations();

			for (HotelDestination hotel : destinations) {
				// Before using services, check if this download has been cancelled
				if (Thread.interrupted()) {
					return null;
				}

				SuggestResponse suggestResponse = services.suggest(hotel.getDestination(), ExpediaServices.F_HOTELS);

				if (suggestResponse == null) {
					Log.w("Got a null response from server autocompleting for: " + hotel.getDestination());
					continue;
				}
				else if (suggestResponse.hasErrors()) {
					Log.w("Got an error response from server autocompleting for: " + hotel.getDestination() + ", "
						+ suggestResponse.getErrors().get(0).getPresentableMessage(getActivity()));
					continue;
				}

				List<Suggestion> suggestions = suggestResponse.getSuggestions();
				if (suggestions.size() == 0) {
					Log.w("Got 0 suggestions while autocompleting for: " + hotel.getDestination());
					continue;
				}

				Log.v("Got hotel fallback data for : " + hotel.getDestination());
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

	private void useHotelFallback() {
		if (Db.getLaunchHotelFallbackData() == null) {
			startHotelFallbackDownload();
		}
		else {
			onHotelFallbackDataRetrieved();
		}
	}

	private void startHotelFallbackDownload() {
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.cancelDownload(KEY_HOTEL_DESTINATIONS);
		bd.startDownload(KEY_HOTEL_DESTINATIONS, mHotelsFallbackDownload, mHotelsFallbackCallback);
	}

	private void onHotelFallbackDataRetrieved() {
		mHotelAdapter.setHotelDestinations(Db.getLaunchHotelFallbackData());
		mHotelsStreamListView.setSelector(Ui.obtainThemeDrawable(getActivity(), android.R.attr.selectableItemBackground));
		mHotelsStreamListView.setOnItemClickListener(mHotelsStreamOnItemClickListener);
	}

	// View init + listeners

	private void initViews() {
		if (mHotelAdapter != null && mFlightAdapter != null) {
			return;
		}

		Log.d("LaunchFragment.initViews() - initializing views...");

		if (mHotelAdapter == null) {
			mHotelAdapter = new LaunchHotelAdapter(getActivity());
			mHotelsStreamListView.setAdapter(mHotelAdapter);
			if (!mHotelsStreamListView.restorePosition()) {
				mHotelsStreamListView.selectMiddle();
			}
		}

		mFlightAdapter = new LaunchFlightAdapter(getActivity());
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

	private final AdapterView.OnItemClickListener mHotelsStreamOnItemClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if (mLaunchingActivity) {
				return;
			}

			cleanUp();

			Object item = mHotelAdapter.getItem(position);
			if (item == null) {
				return;
			}

			mLaunchingActivity = true;

			ImageView bgImageView = Ui.findView(view, R.id.background_view);
			Bundle animOptions = AnimUtils.createActivityScaleBundle(bgImageView);

			if (item instanceof Property) {
				Property property = (Property) item;
				String propertyId = property.getPropertyId();

				if (Db.getHotelSearch().getProperty(propertyId) == null) {
					// H1041: Clear out the current search results
					Db.getHotelSearch().resetSearchData();

					// Now that HotelSearch is cleared we need to tell it about the property
					HotelSearchResponse searchResponse = new HotelSearchResponse();
					searchResponse.addProperty(property);
					Db.getHotelSearch().setSearchResponse(searchResponse);
				}

				if (mSearchParams == null) {
					mSearchParams = new HotelSearchParams();
				}

				Db.getHotelSearch().setSearchParams(mSearchParams);
				Db.getHotelSearch().setSelectedProperty(property);

				Intent intent = new Intent(getActivity(), HotelDetailsFragmentActivity.class);
				intent.putExtra(HotelDetailsMiniGalleryFragment.ARG_FROM_LAUNCH, true);

				NavUtils.startActivity(getActivity(), intent, animOptions);
			}
			else if (item instanceof HotelDestination) {
				HotelDestination destination = (HotelDestination) item;

				HotelSearchParams searchParams = Db.getHotelSearch().getSearchParams();

				// where
				searchParams.setQuery(destination.getPhoneSearchDisplayText());
				searchParams.setSearchType(HotelSearchParams.SearchType.CITY);
				searchParams.setRegionId(destination.getRegionId());
				searchParams.setSearchLatLon(destination.getLatitude(), destination.getLongitude());

				// when
				LocalDate now = LocalDate.now();
				searchParams.setCheckInDate(now.plusDays(1));
				searchParams.setCheckOutDate(now.plusDays(2));

				//who
				searchParams.setNumAdults(1);
				searchParams.setChildren(null);

				// Launch hotel search
				NavUtils.goToHotels(getActivity(), searchParams, animOptions, 0);
			}
		}
	};

	private final AdapterView.OnItemClickListener mFlightsStreamOnItemClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if (mLaunchingActivity) {
				return;
			}

			cleanUp();

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

			// F1330: Tapping on tiles should take you to unsupported POS page
			// if you are on an unsupported POS!

			ImageView bgImageView = Ui.findView(view, R.id.background_view);
			Bundle animOptions = AnimUtils.createActivityScaleBundle(bgImageView);

			if (!PointOfSale.getPointOfSale().supportsFlights()) {
				Intent intent = new Intent(getActivity(), FlightUnsupportedPOSActivity.class);
				NavUtils.startActivity(getActivity(), intent, animOptions);
			}
			else {
				NavUtils.goToFlights(getActivity(), true, animOptions);
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Clean up
	//
	// Clean up happens in two phases.  This is because some things you want
	// to clean immediately, but others if you clean them immediately it
	// makes it ugly

	@Override
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

		// Clear the BG View explicitly because otherwise it takes up tons of memory
		if (mBgBitmap != null) {
			mBgView.setImageBitmap(null);
			mBgViewIndex = mScrollContainer.indexOfChild(mBgView);
			mScrollContainer.removeView(mBgView);
			mBgBitmap.recycle();
			mBgBitmap = null;
		}

		mHotelsStreamListView.setAdapter(null);

		if (mHotelAdapter != null) {
			mHotelAdapter.clear();
			mHotelAdapter = null;
		}

		mFlightsStreamListView.setAdapter(null);
		mFlightAdapter.setDestinations(null);
		mFlightAdapter = null;
	}

	/**
	 * Completely resets the results.  Should only be used before initViews()
	 */
	@Override
	public void reset() {
		Db.setLaunchFlightData(null);
		Db.setLaunchHotelData(null);
		Db.setLaunchHotelFallbackData(null);
	}

	//////////////////////////////////////////////////////////////////////////
	// Air Attach

	private final ItineraryManager.ItinerarySyncAdapter mItinListener = new ItineraryManager.ItinerarySyncAdapter() {
		@Override
		public void onSyncFinished(Collection<Trip> trips) {
			showAirAttachBannerIfNecessary();
		}
	};

	private void showAirAttachBannerIfNecessary() {
		if (!Db.getTripBucket().isUserAirAttachQualified()) {
			mAirAttachC.setVisibility(View.GONE);
			return;
		}
		final ItineraryManager itinMan = ItineraryManager.getInstance();
		final HotelSearchParams hotelSearchParams = itinMan.getHotelSearchParamsForAirAttach();
		if (hotelSearchParams != null) {
			if (mAirAttachC.getVisibility() == View.GONE) {
				mAirAttachC.setVisibility(View.VISIBLE);
				mAirAttachC.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
					@Override
					public boolean onPreDraw() {
						if (mAirAttachC.getHeight() == 0 || mAirAttachC.getVisibility() == View.GONE) {
							return true;
						}
						mAirAttachC.getViewTreeObserver().removeOnPreDrawListener(this);
						animateAirAttachBanner(hotelSearchParams, true);
						return false;
					}
				});
			}
			else {
				animateAirAttachBanner(hotelSearchParams, false);
			}

			mAirAttachClose.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mAirAttachC.animate()
						.translationY(mAirAttachC.getHeight())
						.setDuration(300)
						.setListener(new AnimatorListenerAdapter() {
							@Override
							public void onAnimationEnd(Animator animation) {
								mAirAttachC.setVisibility(View.GONE);
							}
						});
				}
			});
		}
	}

	private void animateAirAttachBanner(final HotelSearchParams hotelSearchParams, boolean animate) {
		mAirAttachC.setTranslationY(mAirAttachC.getHeight());
		mAirAttachC.animate()
			.translationY(0f)
			.setDuration(animate ? 300 : 0);
		mAirAttachC.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				NavUtils.goToHotels(getActivity(), hotelSearchParams);
				OmnitureTracking.trackPhoneAirAttachBannerClick(getActivity());
			}
		});
		OmnitureTracking.trackPhoneAirAttachBanner(getActivity());
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

	@Override
	public void startMarquee() {
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

	// Listeners

	private final View.OnClickListener mHeaderItemOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Bundle animOptions = AnimUtils.createActivityScaleBundle(v);

			switch (v.getId()) {
			case R.id.hotels_button:
				if (!mLaunchingActivity) {
					mLaunchingActivity = true;
					NavUtils.goToHotels(getActivity(), animOptions);
					OmnitureTracking.trackLinkLaunchScreenToHotels(getActivity());
				}
				break;
			case R.id.flights_button:
				if (!mLaunchingActivity) {
					mLaunchingActivity = true;
					NavUtils.goToFlights(getActivity(), animOptions);
					OmnitureTracking.trackLinkLaunchScreenToFlights(getActivity());
				}
				break;
			}

			cleanUp();
		}
	};
}
