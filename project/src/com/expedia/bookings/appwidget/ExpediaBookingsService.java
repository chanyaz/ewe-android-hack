package com.expedia.bookings.appwidget;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.ImageCache;
import com.mobiata.android.ImageCache.OnImageLoaded;
import com.mobiata.android.LocationServices;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.util.IoUtils;
import com.mobiata.android.util.NetUtils;
import com.mobiata.hotellib.data.Codes;
import com.mobiata.hotellib.data.Property;
import com.mobiata.hotellib.data.SearchParams;
import com.mobiata.hotellib.data.SearchParams.SearchType;
import com.mobiata.hotellib.data.SearchResponse;
import com.mobiata.hotellib.data.Session;
import com.mobiata.hotellib.server.ExpediaServices;

public class ExpediaBookingsService extends Service implements LocationListener {

	private final static long UPDATE_INTERVAL = 1000 * 60 * 60; // Every 60 minutes
	public final static long ROTATE_INTERVAL = 1000 * 10; // Every 10 seconds
	private static final String WIDGET_KEY_SEARCH = "WIDGET_KEY_SEARCH";
	private static final String WIDGET_SEARCH_RESULTS_FILE = "widgetsavedsearch.dat";
	private static final int MAX_RESULTS = 5;

	// Intent actions
	public static final String START_SEARCH_ACTION = "com.expedia.bookings.START_SEARCH";
	public static final String START_CLEAN_SEARCH_ACTION = "com.expedia.bookings.START_CLEAN_SEARCH";
	public static final String CANCEL_UPDATE_ACTION = "com.expedia.bookings.CANCEL_UPDATE";
	private static final String SEARCH_PARAMS_CHANGED_ACTION = "com.expedia.bookings.SEARCH_PARAMS_CHANGED";

	private Session mSession;
	private SearchResponse mSearchResponse;
	private SearchParams mSearchParams;
	private List<Property> mProperties;
	private String mFreeFormLocation;
	private int mCurrentPosition = 0;
	private boolean mUseCurrentLocation;

	//----------------------------------
	// THREADS/CALLBACKS
	//----------------------------------

	private BackgroundDownloader mSearchDownloader = BackgroundDownloader.getInstance();

	private Download mSearchDownload = new Download() {
		@Override
		public Object doDownload() {
			ExpediaServices services = new ExpediaServices(getApplicationContext(), mSession);
			mSearchDownloader.addDownloadListener(WIDGET_KEY_SEARCH, services);
			return services.search(mSearchParams, 0);
		}
	};

	private OnDownloadComplete mSearchCallback = new OnDownloadComplete() {
		@Override
		public void onDownload(Object results) {
			mSearchResponse = (SearchResponse) results;

			if (mSearchResponse != null && !mSearchResponse.hasErrors()) {
				mSession = mSearchResponse.getSession();
				determineRelevantProperties();
				mFreeFormLocation = mSearchParams.getFreeformLocation();
				mCurrentPosition = 0;
				loadImageForProperty(mProperties.get(mCurrentPosition));
			}
			else if (mProperties == null || mProperties.isEmpty()) {
				broadcastWidgetError(getString(R.string.progress_search_failed));
			}

			// schedule the next update
			scheduleSearch();
		}
	};

	//----------------------------------
	// LOCATION METHODS
	//----------------------------------

	private void startLocationListener() {

		if (!NetUtils.isOnline(getApplicationContext())) {
			// TODO Inform the user in the widget of the lack of an internet connection
			return;
		}

		// Prefer network location (because it's faster).  Otherwise use GPS
		LocationManager lm = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
		String provider = null;
		if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			provider = LocationManager.NETWORK_PROVIDER;
		}
		else if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			provider = LocationManager.GPS_PROVIDER;
		}

		if (provider == null) {
			Log.w("Could not find a location provider, informing user of error...");
			// TODO Figure out what to do if a location provider cannot be found
		}
		else {
			Log.i("Starting location listener, provider=" + provider);
			lm.requestLocationUpdates(provider, 0, 0, this);
		}
	}

	private void stopLocationListener() {
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		lm.removeUpdates(this);
	}

	//----------------------------------
	// LOCATION LISTENER IMPLEMENTATION
	//----------------------------------

	@Override
	public void onLocationChanged(Location location) {
		Log.d("onLocationChanged(): " + location.toString());

		setSearchParams(location.getLatitude(), location.getLongitude());
		startSearchDownloader();

		stopLocationListener();
	}

	@Override
	public void onProviderDisabled(String provider) {
		Log.w("onProviderDisabled(): " + provider);

		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
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
			// TODO Give feedback to the user that the location has been disabled
			// and so the user will no longer receive updates
		}
	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.i("onProviderDisabled(): " + provider);

		// Switch to network if it's now available (because it's much faster)
		if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
			LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			lm.removeUpdates(this);
			lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.w("onStatusChanged(): provider=" + provider + " status=" + status);

		if (status == LocationProvider.OUT_OF_SERVICE) {
			stopLocationListener();
			Log.w("Location listener failed: out of service");
			// TODO
		}
		else if (status == LocationProvider.TEMPORARILY_UNAVAILABLE) {
			stopLocationListener();
			Log.w("Location listener failed: temporarily unavailable");
			// TODO
		}
	}

	//----------------------------------
	// LIFECYLE EVENTS
	//----------------------------------

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		if (intent.getAction().equals(SEARCH_PARAMS_CHANGED_ACTION)) {
			try {
				mSearchParams = new SearchParams(new JSONObject(intent.getStringExtra(Codes.SEARCH_PARAMS)));
			}
			catch (JSONException e) {
				Log.i("Unable to load search params", e);
			}
			startSearch();
		}
		else if (intent.getAction().equals(START_SEARCH_ACTION)) {
			if (mProperties != null) {
				loadImageForProperty(mProperties.get(mCurrentPosition));
			}
			startSearch();
		}
		else if (intent.getAction().equals(START_CLEAN_SEARCH_ACTION)) {
			cleanSavedResults();
			startSearch();
		}
		else if (intent.getAction().equals(CANCEL_UPDATE_ACTION)) {
			cancelScheduledSearch();
			cancelRotation();
		}
		else if (intent.getAction().equals(ExpediaBookingsWidgetReceiver.NEXT_PROPERTY_ACTION) && mProperties != null) {
			mCurrentPosition = ((mCurrentPosition + 1) >= mProperties.size()) ? 0 : mCurrentPosition + 1;
			loadImageForProperty(mProperties.get(mCurrentPosition));

		}
		else if (intent.getAction().equals(ExpediaBookingsWidgetReceiver.PREV_PROPERTY_ACTION) && mProperties != null) {
			mCurrentPosition = ((mCurrentPosition - 1) < 0) ? (mProperties.size() - 1) : (mCurrentPosition - 1);
			loadImageForProperty(mProperties.get(mCurrentPosition));
		}

		return super.onStartCommand(intent, flags, startId);
	}

	private BroadcastReceiver mRefreshReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(START_CLEAN_SEARCH_ACTION)) {
				cleanSavedResults();
				startSearch();
			}
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();
		registerReceiver(mRefreshReceiver, new IntentFilter(ExpediaBookingsService.START_CLEAN_SEARCH_ACTION));
	}

	@Override
	public void onDestroy() {
		persistResultsToFile();
		unregisterReceiver(mRefreshReceiver);
		super.onDestroy();
	}

	//----------------------------------
	// PRIVATE METHODS
	//----------------------------------

	private void scheduleSearch() {
		AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		PendingIntent operation = getUpdatePendingIntent();

		// Cancel any old updates
		am.cancel(operation);

		// Schedule update
		Log.d("Scheduling next hotels search to occur in " + (UPDATE_INTERVAL / 1000) + " seconds.");
		am.set(AlarmManager.RTC, System.currentTimeMillis() + UPDATE_INTERVAL, operation);
	}

	private void scheduleRotation() {
		AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		PendingIntent operation = getRotatePropertyPendingIntent();

		// Cancel any old updates
		am.cancel(operation);

		// Schedule update
		Log.d("Scheduling next hotels search to occur in " + (ROTATE_INTERVAL / 1000) + " seconds.");
		am.set(AlarmManager.RTC, System.currentTimeMillis() + ROTATE_INTERVAL, operation);
	}

	private void cancelRotation() {
		AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		PendingIntent operation = getRotatePropertyPendingIntent();

		am.cancel(operation);
	}

	private void cancelScheduledSearch() {
		AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		PendingIntent operation = getUpdatePendingIntent();

		am.cancel(operation);
	}

	private PendingIntent getUpdatePendingIntent() {
		Intent i = new Intent(START_SEARCH_ACTION);
		return PendingIntent.getService(getApplicationContext(), 0, i, 0);
	}

	private PendingIntent getRotatePropertyPendingIntent() {
		Intent i = new Intent(ExpediaBookingsWidgetReceiver.NEXT_PROPERTY_ACTION);
		PendingIntent operation = PendingIntent.getService(getApplicationContext(), 0, i, 0);
		return operation;
	}

	private void startSearch() {

		Log.i("Starting search");
		mSearchDownloader.cancelDownload(WIDGET_KEY_SEARCH);

		setupSearchParams();

		if (mUseCurrentLocation) {
			// See if we have a good enough location stored
			long minTime = Calendar.getInstance().getTimeInMillis() - SearchActivity.MINIMUM_TIME_AGO;
			Location location = LocationServices.getLastBestLocation(getApplicationContext(), minTime);
			if (location != null) {
				setSearchParams(location.getLatitude(), location.getLongitude());
				startSearchDownloader();
			}
			else {
				startLocationListener();
			}
		}
		else {
			startSearchDownloader();
		}
	}

	private void setSearchParams(double latitude, double longitude) {
		mSearchParams.setSearchLatLon(latitude, longitude);
	}

	private void setupSearchParams() {

		// check whether user would like to search for hotels near current location
		// or based on last search
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		boolean searchHotelsNearYou = prefs.getBoolean(Codes.WIDGET_SHOW_HOTELS_NEAR_YOU, false);
		boolean searchHotelsBasedOnLastSearch = prefs.getBoolean(Codes.WIDGET_HOTELS_FROM_LAST_SEARCH, false);
		String specificLocation = prefs.getString(Codes.WIDGET_SPECIFIC_LOCATION, "");

		mUseCurrentLocation = searchHotelsNearYou;

		if (searchHotelsBasedOnLastSearch) {
			getSearchParamsFromDisk(prefs);
		}
		else if (specificLocation != null && !specificLocation.equals("")) {
			getSearchParamsFromDisk(prefs);
			float locationLat = prefs.getFloat(Codes.WIDGET_LOCATION_LAT, -1);
			float locationLong = prefs.getFloat(Codes.WIDGET_LOCATION_LON, -1);
			setSearchParams(locationLat, locationLong);
			mSearchParams.setFreeformLocation(specificLocation);
		}

		// default to searching for hotels around current location
		if (mSearchParams == null) {
			mUseCurrentLocation = true;
			mSearchParams = new SearchParams();
		}

		// set default stay of 1 night starting today if the current check in date
		// is past the current time
		if (mSearchParams.getCheckInDate().getTimeInMillis() < System.currentTimeMillis()) {
			mSearchParams.setDefaultStay();
		}
	}

	private void getSearchParamsFromDisk(SharedPreferences prefs) {
		String searchParamsString = prefs.getString("searchParams", null);
		try {
			if (searchParamsString != null) {
				mSearchParams = new SearchParams(new JSONObject(searchParamsString));
			}
		}
		catch (JSONException e) {
		}
	}

	private void startSearchDownloader() {

		if (!NetUtils.isOnline(getApplicationContext()) && (mProperties == null || mProperties.isEmpty())) {
			broadcastWidgetError(getString(R.string.widget_error_no_internet));
			return;
		}

		mSearchDownloader.cancelDownload(WIDGET_KEY_SEARCH);
		mSearchDownloader.startDownload(WIDGET_KEY_SEARCH, mSearchDownload, mSearchCallback);
	}

	private void persistResultsToFile() {
		try {
			JSONObject obj = new JSONObject();
			JSONUtils.putJSONableList(obj, "properties", mProperties);
			IoUtils.writeStringToFile(WIDGET_SEARCH_RESULTS_FILE, obj.toString(), getApplicationContext());
		}
		catch (IOException e) {
			Log.w("Couldn't save search results.", e);
		}
		catch (JSONException e) {
			Log.w("Couldn't save search results.", e);
		}
	}

	private void broadcastLoadingWidget(Property property) {
		Intent i = new Intent(ExpediaBookingsWidgetReceiver.LOAD_PROPERTY_ACTION);
		i.putExtra(Codes.PROPERTY, property.toJson().toString());
		i.putExtra(Codes.SEARCH_PARAMS, mSearchParams.toJson().toString());
		String location = (!mUseCurrentLocation && (mSearchParams.getSearchType() != SearchType.MY_LOCATION)) ? mFreeFormLocation
				: property.getDistanceFromUser().formatDistance(this);
		i.putExtra(Codes.PROPERTY_LOCATION, location);
		i.putExtra(Codes.SESSION, mSession.toJson().toString());
		sendBroadcast(i);
		scheduleRotation();
	}

	private void broadcastWidgetError(CharSequence error) {
		Intent i = new Intent(ExpediaBookingsWidgetReceiver.LOAD_PROPERTY_ACTION);
		i.putExtra(Codes.SEARCH_ERROR, error);
		sendBroadcast(i);
	}

	private void loadImageForProperty(final Property property) {
		ImageCache.loadImage(property.getThumbnail().getUrl(), new OnImageLoaded() {

			@Override
			public void onImageLoaded(String url, Bitmap bitmap) {
				if (mProperties.get(mCurrentPosition).getThumbnail().getUrl().equals(url)) {
					broadcastLoadingWidget(mProperties.get(mCurrentPosition));
				}
			}
		});
	}

	private void determineRelevantProperties() {
		List<Property> properties = mSearchResponse.getProperties();
		List<Property> relevantProperties = new ArrayList<Property>();

		// first populate the list with hotels that have rooms on sale
		for (Property property : properties) {
			if (relevantProperties.size() == MAX_RESULTS) {
				break;
			}

			if (property.getLowestRate().getSavingsPercent() > 0) {
				relevantProperties.add(property);
			}
		}

		// then populate with highly rated rooms if there aren't enough
		// hotels with rooms on sale
		for (Property property : properties) {
			if (relevantProperties.size() == MAX_RESULTS) {
				break;
			}

			if (property.isHighlyRated() && (property.getLowestRate().getSavingsPercent() == 0)) {
				relevantProperties.add(property);
			}
		}

		// lastly get enough to fill up the remaining slots
		for (Property property : properties) {
			if (relevantProperties.size() == MAX_RESULTS) {
				break;
			}

			if (property.getLowestRate().getSavingsPercent() == 0 && !property.isHighlyRated()) {
				relevantProperties.add(property);
			}
		}

		mProperties = relevantProperties;
	}

	private void cleanSavedResults() {
		mProperties = null;
		mFreeFormLocation = null;
		mSession = null;
		mSearchResponse = null;
	}

}
