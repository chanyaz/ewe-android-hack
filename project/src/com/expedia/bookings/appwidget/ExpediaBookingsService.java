package com.expedia.bookings.appwidget;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.RemoteViews;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.HotelActivity;
import com.expedia.bookings.activity.PhoneSearchActivity;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.SearchResponse;
import com.expedia.bookings.model.WidgetConfigurationState;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.ImageCache;
import com.mobiata.android.ImageCache.OnImageLoaded;
import com.mobiata.android.LocationServices;
import com.mobiata.android.Log;
import com.mobiata.android.util.NetUtils;

public class ExpediaBookingsService extends Service implements LocationListener {

	//////////////////////////////////////////////////////////////////////////////////////////
	// CONSTANTS
	//////////////////////////////////////////////////////////////////////////////////////////

	// Widget config related constants 
	private final static long UPDATE_INTERVAL = 1000 * 60 * 60; // 1 hour
	public final static long ROTATE_INTERVAL = 1000 * 5; // Every 5 seconds
	public final static long INCREASED_ROTATE_INTERVAL = 1000 * 30; // Every 30 seconds

	// maintain a bounded cache for the thumbnails to prevent OOM errors
	private static final int MAX_IMAGE_CACHE_SIZE = 30;
	private static final int MIN_DISTANCE_BEFORE_UPDATE = 5 * 1000; // 5 km
	private static final int MIN_TIME_BETWEEN_CHECKS_IN_MILLIS = 1000 * 60 * 15; // 15 minutes
	private static final int TIME_THRESHOLD_FOR_DISTANCE_TRAVELLED = 1000 * 60 * 10; // 10 minutes

	private static final int POST_NO_CONNECTIVITY_MSG_PAUSE = 1000;

	// download key for downloading results around current location
	private static final String WIDGET_KEY_SEARCH = "WIDGET_KEY_SEARCH";

	// Intent actions
	public static final String START_SEARCH_ACTION = "com.expedia.bookings.START_SEARCH";
	public static final String START_CLEAN_SEARCH_ACTION = "com.expedia.bookings.START_CLEAN_SEARCH";
	public static final String CANCEL_UPDATE_ACTION = "com.expedia.bookings.CANCEL_UPDATE";
	public static final String NEXT_PROPERTY_ACTION = "com.expedia.bookings.NEXT_PROPERTY";
	public static final String ROTATE_PROPERTY_ACTION = "com.expedia.bookings.ROTATE_PROPERTY";
	public static final String PREV_PROPERTY_ACTION = "com.expedia.bookings.PREV_PROPERTY";

	private static final String WIDGET_THUMBNAIL_KEY_PREFIX = "WIDGET_THUMBNAIL_KEY.";

	//////////////////////////////////////////////////////////////////////////////////////////
	// BOOKKEEPING DATA STRUCTURES
	//////////////////////////////////////////////////////////////////////////////////////////

	private BackgroundDownloader mSearchDownloader = BackgroundDownloader.getInstance();
	private Map<Integer, WidgetState> mWidgets;
	private WidgetDeals mWidgetDeals = WidgetDeals.getInstance(this);

	/*
	 * Maintain local image cache to avoid bitmaps from being garbage collected from underneath,
	 * by the SearchActivity or another other component using it
	 */
	public static ConcurrentHashMap<String, Bitmap> thumbnailCache = new ConcurrentHashMap<String, Bitmap>();

	/*
	 * This object holds all state persisting to the widget 
	 * so that we can support multiple widgets each holding its own
	 * state
	 */
	private class WidgetState {
		Integer appWidgetIdInteger;
		int mCurrentPosition = -1;
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// WIDGET DATA DOWNLOADING BOOKKEEPING
	//////////////////////////////////////////////////////////////////////////////////////////
	private long mLastUpdatedTimeInMillis;
	private Download mSearchDownload = new Download() {
		@Override
		public Object doDownload() {
			ExpediaServices services = new ExpediaServices(getApplicationContext(), mWidgetDeals.getSession());
			mSearchDownloader.addDownloadListener(WIDGET_KEY_SEARCH, services);
			return services.search(mWidgetDeals.getSearchParams(), 0);
		}
	};
	private OnDownloadComplete mSearchCallback = new OnDownloadComplete() {
		@Override
		public void onDownload(Object results) {
			SearchResponse searchResponse = (SearchResponse) results;
			mLastUpdatedTimeInMillis = System.currentTimeMillis();
			// schedule the next update
			scheduleSearch();

			// determine the widget deals regardless of whether the search has results
			// to nullify existing deals if no results were returned on the download
			mWidgetDeals.determineRelevantProperties(searchResponse);

			// start a background thread to save the deals to disk
			new Thread(new Runnable() {

				@Override
				public void run() {
					mWidgetDeals.deleteFromDisk();
					mWidgetDeals.persistToDisk();
				}
			}).start();

			if (searchResponse != null && !searchResponse.hasErrors()) {

				for (WidgetState widget : mWidgets.values()) {
					widget.mCurrentPosition = -1;
					loadPropertyIntoWidget(widget, ROTATE_INTERVAL);
				}
			}
			else {
				// if there are errors in the search results or if no results are returned
				// update the widget to accurately reflect the hotels around the current location
				updateAllWidgetsWithText(getString(R.string.progress_search_failed),
						getString(R.string.refresh_widget), getRefreshIntent());
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////////////////////
	// LOCATION METHODS
	//////////////////////////////////////////////////////////////////////////////////////////

	private void startLocationListener() {

		if (!NetUtils.isOnline(getApplicationContext())
				&& (mWidgetDeals.getDeals() == null || mWidgetDeals.getDeals().isEmpty())) {
			mHandler.sendMessageDelayed(Message.obtain(mHandler, NO_INTERNET_CONNECTIVITY),
					POST_NO_CONNECTIVITY_MSG_PAUSE);
			return;
		}
		else if ((mWidgetDeals.getDeals() == null || mWidgetDeals.getDeals().isEmpty())) {
			updateAllWidgetsWithText(getString(R.string.loading_hotels), null, null);
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
			updateAllWidgetsWithText(getString(R.string.progress_finding_location), getString(R.string.refresh_widget),
					getRefreshIntent());
			// TODO Should we inform the user that the reason we're unable to 
			// determine location is because of the lack of an available provider?
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

	private void startLocationListenerForCurrentLocationWidgets() {
		Log.i("Starting location listener for current location widgets.");
		if (!NetUtils.isOnline(getApplicationContext())) {
			updateAllWidgetsWithText(getString(R.string.progress_finding_location), getString(R.string.refresh_widget),
					getRefreshIntent());
			return;
		}

		LocationManager lm = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

		String provider = null;
		if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			provider = LocationManager.NETWORK_PROVIDER;
		}
		else if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			provider = LocationManager.GPS_PROVIDER;
		}

		/*
		 * Setup the Network/GPS provider to update with a location only if
		 * the location has changed beyond the specified threshold. Also, check for location 
		 * updates only after the minimum time interval specified. This will help with 
		 * conserving battery life.
		 */
		if (provider != null) {
			lm.requestLocationUpdates(provider, MIN_TIME_BETWEEN_CHECKS_IN_MILLIS, MIN_DISTANCE_BEFORE_UPDATE,
					mListenerForCurrentLocationWidgets);
		}

		/*
		 * Use the passive provider as a way of getting updates without actually
		 * requesting a GPS fix. Ignore if the provider is not available on the current 
		 * device (either due to being disabled or unavailable completely),.
		 */
		requestLocationUpdatesFromPassiveProvider(lm);

	}

	private void requestLocationUpdatesFromPassiveProvider(LocationManager lm) {
		/*
		 * Setup the passive provider to update with a location whenever another application
		 * gets a location fix. This is okay sine the location update is user/app-intended.
		 * Use this location to determine whether or not to re-download data.	
		 */
		try {
			if (lm.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
				lm.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, mListenerForCurrentLocationWidgets);
			}
		}
		catch (SecurityException e) {
			Log.i("This exception is expected for api version < 8 since PASSIVE_PROVIDER only exists for API Levels >= 8");
		}
	}

	private void stopLocationListenerforCurrentLocationWidgets() {
		Log.i("Stopping location listener for current location widgets.");
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		lm.removeUpdates(mListenerForCurrentLocationWidgets);
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// LOCATION LISTENER IMPLEMENTATION
	//////////////////////////////////////////////////////////////////////////////////////////

	private LocationListener mListenerForCurrentLocationWidgets = new LocationListener() {

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		@Override
		public void onProviderEnabled(String provider) {
			LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

			// Use the network provider as it provides faster location updates
			if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
				lm.removeUpdates(mListenerForCurrentLocationWidgets);
				lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BETWEEN_CHECKS_IN_MILLIS,
						MIN_DISTANCE_BEFORE_UPDATE, mListenerForCurrentLocationWidgets);
				requestLocationUpdatesFromPassiveProvider(lm);
			}

		}

		@Override
		public void onProviderDisabled(String provider) {
			LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			boolean stillWorking = true;

			if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
				if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
					lm.removeUpdates(mListenerForCurrentLocationWidgets);
					lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BETWEEN_CHECKS_IN_MILLIS,
							MIN_DISTANCE_BEFORE_UPDATE, mListenerForCurrentLocationWidgets);
					requestLocationUpdatesFromPassiveProvider(lm);
				}
				else {
					stillWorking = false;
				}
			}
			else if (provider.equals(LocationManager.GPS_PROVIDER)
					&& !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
				stillWorking = false;
			}

			if (!stillWorking) {
				lm.removeUpdates(mListenerForCurrentLocationWidgets);
			}
		}

		@Override
		public void onLocationChanged(Location location) {
			Log.i("location changed for current location widgets listener");

			Location lastSearchedLocation = new Location(location);

			/*
			 * Get the last searched/downloaded time for the current location widgets 
			 * as well as the last searched location
			 */
			SearchParams searchParams = mWidgetDeals.getSearchParams();
			if (searchParams != null) {
				lastSearchedLocation.setLatitude(searchParams.getSearchLatitude());
				lastSearchedLocation.setLongitude(searchParams.getSearchLongitude());
			}
			else {
				searchParams = new SearchParams();
				mWidgetDeals.setSearchParams(searchParams);
			}

			/*
			 * If the time elapsed is at least the minimum time required and the distance moved is beyond
			 * the specified threshold, cause all widgets to redownload their data to get an accurate
			 * view of deals around the current location.
			 */
			long timeBetweenChecks = System.currentTimeMillis() - mLastUpdatedTimeInMillis;
			float distanceFromLastSearchedLocation = location.distanceTo(lastSearchedLocation);

			Log.i("Time between checks = " + timeBetweenChecks);
			Log.i("Distance from location last searched = " + distanceFromLastSearchedLocation);
			Log.i("Provider = " + location.getProvider());
			if (mLastUpdatedTimeInMillis == 0
					|| ((timeBetweenChecks > TIME_THRESHOLD_FOR_DISTANCE_TRAVELLED) && (distanceFromLastSearchedLocation >= MIN_DISTANCE_BEFORE_UPDATE))) {
				Log.i("Starting download for current location widgets since location has changed");
				mWidgetDeals.getSearchParams().setSearchLatLon(location.getLatitude(), location.getLongitude());
				startSearchDownloader();
			}
		}
	};

	@Override
	public void onLocationChanged(Location location) {
		Log.d("onLocationChanged(): " + location.toString());
		mWidgetDeals.setSearchParams(new SearchParams());
		mWidgetDeals.getSearchParams().setSearchLatLon(location.getLatitude(), location.getLongitude());
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

	//////////////////////////////////////////////////////////////////////////////////////////
	// LIFECYCLE EVENTS
	//////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		if (mWidgets == null) {
			loadWidgets();
		}

		if (mWidgetDeals.getDeals() == null) {
			mWidgetDeals.restoreFromDisk();
		}

		/*
		 * Start search for all the widgets
		 * if there are widgets installed and the
		 * service was invoked without any intent
		 * (which means that, in all probability,
		 * it was restarted after a force close)
		 */
		if (intent == null || intent.getAction() == null && !mWidgets.isEmpty()) {
			intent = new Intent(START_SEARCH_ACTION);
		}

		if (intent.getAction().equals(START_CLEAN_SEARCH_ACTION)) {
			/*
			 * Note: This action should ONLY take place when a new widget
			 * is being added to the home screen. This helps ensure that
			 * we are keeping track of active widgets only
			 * and getting rid of those (when onDelete is called)
			 * that aren't active. I've seen widget ids persisted
			 * even after the corresponding widget has been deleted from the
			 * home screen
			 */
			Integer appWidgetIdInteger = new Integer(intent.getIntExtra(Codes.APP_WIDGET_ID, -1));
			WidgetState widget = new WidgetState();
			widget.appWidgetIdInteger = appWidgetIdInteger;
			boolean startListener = mWidgets.isEmpty();
			mWidgets.put(appWidgetIdInteger, widget);

			if (mWidgetDeals.getDeals() != null && !mWidgetDeals.getDeals().isEmpty()) {
				loadPropertyIntoWidget(widget, ROTATE_INTERVAL);
			}
			else {
				startSearch();
			}

			// start the listener for keeping track of
			// current location more aggressively
			if (startListener) {
				startLocationListenerForCurrentLocationWidgets();
			}
		}
		else {

			/*
			 * kill the widget if there are no widgets installed
			 */
			if (mWidgets.isEmpty()) {
				Log.i("Stopping self, Action = " + intent.getAction());
				stopSelf();
			}

			if (intent.getAction().equals(START_SEARCH_ACTION)) {
				startSearchForWidgets();
			}
			else if (intent.getAction().equals(CANCEL_UPDATE_ACTION)) {
				Integer appWidgetIdInteger = new Integer(intent.getIntExtra(Codes.APP_WIDGET_ID, -1));
				mWidgets.remove(appWidgetIdInteger);

				if (mWidgets.isEmpty()) {
					cancelRotation();
					cancelScheduledSearch();
					stopLocationListenerforCurrentLocationWidgets();
					mSearchDownloader.cancelDownload(WIDGET_KEY_SEARCH);

					// if all widgets have been deleted, kill
					// the widget
					Log.i("Stopping self, Action = " + intent.getAction());
					stopSelf();
				}
			}
			else if (intent.getAction().equals(NEXT_PROPERTY_ACTION)) {
				Integer appWidgetIdInteger = new Integer(intent.getIntExtra(Codes.APP_WIDGET_ID, -1));
				WidgetState widget = mWidgets.get(appWidgetIdInteger);
				loadNextProperty(widget, INCREASED_ROTATE_INTERVAL);

			}
			else if (intent.getAction().equals(PREV_PROPERTY_ACTION)) {
				Integer appWidgetIdInteger = new Integer(intent.getIntExtra(Codes.APP_WIDGET_ID, -1));
				WidgetState widget = mWidgets.get(appWidgetIdInteger);
				loadPreviousProperty(widget, INCREASED_ROTATE_INTERVAL);

			}
			else if (intent.getAction().equals(ROTATE_PROPERTY_ACTION)) {
				for (WidgetState widget : mWidgets.values()) {
					loadNextProperty(widget, ROTATE_INTERVAL);
				}
			}
		}

		/*
		 * returning this value helps to ensure to restart 
		 * the service if the application crashes for 
		 * an unexpected reason
		 */
		return Service.START_STICKY;
	}

	/*
	 * The following thread recommended to 
	 * call the necessary methods in the onCreate
	 * method as the onStartCommand method is not called
	 * if there are no pending intents:
	 * (http://groups.google.com/group/android-developers/browse_thread/thread/d87fb390c13d141d/52f7154ab49f229?hl=en&q=dianne+hackborn+onstartcommand+bug&pli=1) 
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		startSearchForWidgets();
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////////

	private static final int NO_INTERNET_CONNECTIVITY = -1;
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case NO_INTERNET_CONNECTIVITY:
				updateAllWidgetsWithText(getString(R.string.widget_error_no_internet),
						getString(R.string.refresh_widget), getRefreshIntent());
				break;
			}

			super.handleMessage(msg);
		}

	};

	private void scheduleSearch() {
		AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		PendingIntent operation = getUpdatePendingIntent();

		// Cancel any old updates
		Log.i("Cancelling search");
		am.cancel(operation);

		// Schedule update
		Log.d("Scheduling next hotels search to occur in " + (UPDATE_INTERVAL / 1000) + " seconds.");
		am.set(AlarmManager.RTC, System.currentTimeMillis() + UPDATE_INTERVAL, operation);
	}

	private void scheduleRotation(long rotateInterval) {
		AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		PendingIntent operation = getRotatePropertyPendingIntent();

		// Cancel any old updates
		am.cancel(operation);

		// Schedule update
		Log.d("Scheduling rotation to occur in " + (ROTATE_INTERVAL / 1000) + " seconds.");
		am.set(AlarmManager.RTC, System.currentTimeMillis() + rotateInterval, operation);
	}

	private void cancelRotation() {
		AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		PendingIntent operation = getRotatePropertyPendingIntent();

		am.cancel(operation);
	}

	private void cancelScheduledSearch() {
		Log.i("Cancelling search");

		AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		PendingIntent operation = getUpdatePendingIntent();

		am.cancel(operation);
	}

	private PendingIntent getUpdatePendingIntent() {
		Intent i = new Intent(START_SEARCH_ACTION);
		return PendingIntent.getService(getApplicationContext(), 2, i, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	private PendingIntent getRotatePropertyPendingIntent() {
		Intent i = new Intent(ROTATE_PROPERTY_ACTION);
		PendingIntent operation = PendingIntent.getService(getApplicationContext(), 0, i,
				PendingIntent.FLAG_UPDATE_CURRENT);
		return operation;
	}

	private void startSearch() {
		Log.i("Starting search");

		// See if we have a good enough location stored
		long minTime = Calendar.getInstance().getTimeInMillis() - PhoneSearchActivity.MINIMUM_TIME_AGO;
		Location location = LocationServices.getLastBestLocation(getApplicationContext(), minTime);
		if (location != null) {
			mWidgetDeals.setSearchParams(new SearchParams());
			mWidgetDeals.getSearchParams().setSearchLatLon(location.getLatitude(), location.getLongitude());
			startSearchDownloader();
		}
		else {
			startLocationListener();
		}
	}

	private void startSearchDownloader() {
		// search for 1 guest by default
		mWidgetDeals.getSearchParams().setNumAdults(1);
		
		if (!NetUtils.isOnline(getApplicationContext())
				&& (mWidgetDeals.getDeals() == null || mWidgetDeals.getDeals().isEmpty())) {
			mHandler.sendMessageDelayed(Message.obtain(mHandler, NO_INTERNET_CONNECTIVITY),
					POST_NO_CONNECTIVITY_MSG_PAUSE);
			return;
		}
		else if (mWidgetDeals.getDeals() == null || mWidgetDeals.getDeals().isEmpty()) {
			updateAllWidgetsWithText(getString(R.string.loading_hotels), null, null);
		}

		mSearchDownloader.cancelDownload(WIDGET_KEY_SEARCH);
		mSearchDownloader.startDownload(WIDGET_KEY_SEARCH, mSearchDownload, mSearchCallback);
	}

	private void loadPropertyIntoAllWidgets(long rotateInterval) {
		for (WidgetState widget : mWidgets.values()) {
			loadPropertyIntoWidget(widget, rotateInterval);
		}
	}

	private void loadPropertyIntoWidget(final WidgetState widget, long rotateInterval) {
		if (widget.mCurrentPosition == -1) {
			updateWidgetBranding(widget);
			scheduleRotation(rotateInterval);
			return;
		}

		final Property property = mWidgetDeals.getDeals().get(widget.mCurrentPosition);
		Bitmap bitmap = thumbnailCache.get(property.getThumbnail().getUrl());
		// if the bitmap doesn't exist in the cache, asynchronously load the image while
		// updating the widget remote view with the rest of the information
		if (bitmap == null) {
			ImageCache.loadImage(WIDGET_THUMBNAIL_KEY_PREFIX + widget.appWidgetIdInteger, property.getThumbnail()
					.getUrl(), new OnImageLoaded() {

				@Override
				public void onImageLoaded(String url, Bitmap bitmap) {

					// making sure that the image actually belongs to the current property loaded 
					// in the remote view
					if (widget.mCurrentPosition != -1
							&& mWidgetDeals.getDeals().get(widget.mCurrentPosition).getThumbnail().getUrl().equals(url)) {

						if (thumbnailCache.size() >= MAX_IMAGE_CACHE_SIZE) {
							// clear out the cache if it ever reaches its max size
							thumbnailCache.clear();
						}

						if (bitmap == null) {
							return;
						}

						thumbnailCache.put(url, bitmap);
						// remove the image from the global image cache without 
						// causing the bitmap to get recycled as we maintain our own copy
						// of the bitmap to prevent interference from the main thread
						// attempting to recycle the bitmap
						ImageCache.removeImage(url, false);
						updateWidgetWithImage(widget, property);
					}
				}

				@Override
				public void onImageLoadFailed(String url) {
					// Do nothing
				}
			});
		}
		updateWidgetWithProperty(property, widget);
		scheduleRotation(rotateInterval);
	}

	private void loadNextProperty(WidgetState widget, long rotateInterval) {
		if (mWidgetDeals.getDeals() != null) {
			widget.mCurrentPosition = ((widget.mCurrentPosition + 1) >= mWidgetDeals.getDeals().size()) ? -1
					: widget.mCurrentPosition + 1;
			loadPropertyIntoWidget(widget, rotateInterval);
		}
	}

	private void loadPreviousProperty(WidgetState widget, long rotateInterval) {
		if (mWidgetDeals.getDeals() != null) {

			widget.mCurrentPosition = ((widget.mCurrentPosition - 1) < -1) ? (mWidgetDeals.getDeals().size() - 1)
					: (widget.mCurrentPosition - 1);
			loadPropertyIntoWidget(widget, rotateInterval);
		}
	}

	private void loadWidgets() {
		mWidgets = new HashMap<Integer, ExpediaBookingsService.WidgetState>();

		ArrayList<Object> widgetConfigs = WidgetConfigurationState.getAll(this);
		for (Object config : widgetConfigs) {
			WidgetConfigurationState cs = (WidgetConfigurationState) config;
			Integer appWidgetIdInteger = new Integer(cs.getAppWidgetId());
			WidgetState widget = new WidgetState();
			widget.appWidgetIdInteger = appWidgetIdInteger;
			mWidgets.put(appWidgetIdInteger, widget);
		}

		if (!mWidgets.isEmpty()) {
			startLocationListenerForCurrentLocationWidgets();
		}
	}

	private void startSearchForWidgets() {
		if (mWidgets == null) {
			loadWidgets();
		}

		boolean toStartSearch = true;
		if (mWidgetDeals.getDeals() == null) {
			int result = mWidgetDeals.restoreFromDisk();
			if (result == WidgetDeals.WIDGET_DEALS_RESTORED) {
				loadPropertyIntoAllWidgets(ROTATE_INTERVAL);
				scheduleSearch();
				toStartSearch = false;
			}
		}

		if (toStartSearch) {
			if (mWidgetDeals.getDeals() == null || mWidgetDeals.getDeals().isEmpty()) {
				updateAllWidgetsWithText(getString(R.string.loading_hotels), null, null);
			}
			startSearch();
		}

	}

	/*
	 * This method only updates the image in the remote view
	 * when its asynchronously downloaded by the ExpediaBookingsService
	 */
	private void updateWidgetWithImage(WidgetState widget, Property property) {
		RemoteViews rv = new RemoteViews(getPackageName(), R.layout.widget);

		Bitmap bitmap = ExpediaBookingsService.thumbnailCache.get(property.getThumbnail().getUrl());
		if (bitmap == null) {
			rv.setImageViewResource(R.id.hotel_image_view, R.drawable.widget_thumbnail_background);
		}
		else {
			rv.setImageViewBitmap(R.id.hotel_image_view, bitmap);
		}
		updateWidget(widget, rv);
	}

	private void updateWidgetWithProperty(final Property property, WidgetState widget) {
		final RemoteViews widgetContents = new RemoteViews(getPackageName(), R.layout.widget_contents);
		RemoteViews rv = new RemoteViews(getPackageName(), R.layout.widget);

		// add contents to the parent view to give the fade-in animation
		rv.removeAllViews(R.id.hotel_info_contents);
		rv.addView(R.id.hotel_info_contents, widgetContents);

		setWidgetContentsVisibility(widgetContents, View.VISIBLE);

		setBrandingViewVisibility(rv, widgetContents, View.GONE);

		setWidgetPropertyViewVisibility(widgetContents, View.VISIBLE);

		widgetContents.setTextViewText(R.id.hotel_name_text_view, property.getName());
		String location = property.getDistanceFromUser().formatDistance(this);
		widgetContents.setTextViewText(R.id.location_text_view, location);

		if (property.getLowestRate().getSavingsPercent() > 0) {
			widgetContents.setTextViewText(R.id.sale_text_view,
					getString(R.string.widget_savings_template, property.getLowestRate().getSavingsPercent() * 100));

			widgetContents.setTextViewText(R.id.price_text_view,
					StrUtils.formatHotelPrice(property.getLowestRate().getDisplayRate()));

			widgetContents.setViewVisibility(R.id.sale_text_view, View.VISIBLE);
			widgetContents.setViewVisibility(R.id.sale_image_view, View.VISIBLE);
		}
		else {
			widgetContents.setViewVisibility(R.id.sale_text_view, View.GONE);
			widgetContents.setViewVisibility(R.id.sale_image_view, View.GONE);
			widgetContents.setViewVisibility(R.id.price_per_night_container, View.VISIBLE);
			widgetContents.setTextViewText(R.id.price_text_view,
					StrUtils.formatHotelPrice(property.getLowestRate().getDisplayRate()));
		}

		Bitmap bitmap = ExpediaBookingsService.thumbnailCache.get(property.getThumbnail().getUrl());
		if (bitmap == null) {
			widgetContents.setImageViewResource(R.id.hotel_image_view, R.drawable.widget_thumbnail_background);
		}
		else {
			widgetContents.setImageViewBitmap(R.id.hotel_image_view, bitmap);
		}

		Intent prevIntent = new Intent(this, ExpediaBookingsService.class);
		prevIntent.setAction(PREV_PROPERTY_ACTION);
		prevIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		prevIntent.putExtra(Codes.APP_WIDGET_ID, widget.appWidgetIdInteger);

		Intent nextIntent = new Intent(this, ExpediaBookingsService.class);
		nextIntent.setAction(NEXT_PROPERTY_ACTION);
		nextIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		nextIntent.putExtra(Codes.APP_WIDGET_ID, widget.appWidgetIdInteger);

		widgetContents.setOnClickPendingIntent(R.id.prev_hotel_btn, PendingIntent.getService(this,
				widget.appWidgetIdInteger.intValue() + 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT));
		widgetContents.setOnClickPendingIntent(R.id.next_hotel_btn, PendingIntent.getService(this,
				widget.appWidgetIdInteger.intValue() + 1, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT));

		setupOnClickIntentForWidget(widget, property, rv);

		setWidgetLoadingTextVisibility(widgetContents, View.GONE);

		updateWidget(widget, rv);
	}

	private PendingIntent getRefreshIntent() {
		Intent newIntent = new Intent(START_SEARCH_ACTION);
		return PendingIntent.getService(this, 4, newIntent, PendingIntent.FLAG_CANCEL_CURRENT);

	}

	private void updateAllWidgetsWithText(String error, String tip, PendingIntent onClickIntent) {
		for (WidgetState widget : mWidgets.values()) {
			updateWidgetWithText(widget, error, tip, onClickIntent);
		}
	}

	private void updateWidgetWithText(WidgetState widget, String error, String tip, PendingIntent onClickIntent) {
		RemoteViews rv = new RemoteViews(getPackageName(), R.layout.widget);
		RemoteViews widgetContents = new RemoteViews(getPackageName(), R.layout.widget_contents);

		rv.removeAllViews(R.id.hotel_info_contents);
		rv.addView(R.id.hotel_info_contents, widgetContents);

		setWidgetPropertyViewVisibility(widgetContents, View.GONE);
		setBrandingViewVisibility(rv, widgetContents, View.GONE);

		setWidgetContentsVisibility(widgetContents, View.GONE);
		widgetContents.setTextViewText(R.id.widget_error_text_view, error);
		widgetContents.setViewVisibility(R.id.widget_error_text_view, View.VISIBLE);
		widgetContents.setViewVisibility(R.id.loading_text_container, View.VISIBLE);
		widgetContents.setViewVisibility(R.id.loading_expedia_logo_image_view, View.VISIBLE);

		if (tip != null) {
			widgetContents.setViewVisibility(R.id.widget_error_tip_text_view, View.VISIBLE);
			widgetContents.setTextViewText(R.id.widget_error_tip_text_view, tip);
		}

		if (onClickIntent != null) {
			rv.setOnClickPendingIntent(R.id.root, onClickIntent);
		}
		else {
			clearWidgetOnClickIntent(rv);
		}

		updateWidget(widget, rv);
	}

	private void updateWidgetBranding(WidgetState widget) {
		final RemoteViews widgetContents = new RemoteViews(getPackageName(), R.layout.widget_contents);
		RemoteViews rv = new RemoteViews(getPackageName(), R.layout.widget);

		// add contents to the parent view to give the fade-in animation
		rv.removeAllViews(R.id.hotel_info_contents);
		rv.addView(R.id.hotel_info_contents, widgetContents);

		setWidgetContentsVisibility(widgetContents, View.VISIBLE);

		setWidgetPropertyViewVisibility(widgetContents, View.GONE);

		setBrandingViewVisibility(rv, widgetContents, View.VISIBLE);

		widgetContents.setTextViewText(R.id.branding_location_text_view, getString(R.string.book_a_room_tonight));

		widgetContents.setTextViewText(R.id.branding_title_text_view, getString(R.string.hotel_radar));

		Intent prevIntent = new Intent(PREV_PROPERTY_ACTION);
		prevIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		prevIntent.putExtra(Codes.APP_WIDGET_ID, widget.appWidgetIdInteger);

		Intent nextIntent = new Intent(NEXT_PROPERTY_ACTION);
		nextIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		nextIntent.putExtra(Codes.APP_WIDGET_ID, widget.appWidgetIdInteger);

		widgetContents.setOnClickPendingIntent(R.id.prev_hotel_btn, PendingIntent.getService(this,
				widget.appWidgetIdInteger.intValue() + 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT));
		widgetContents.setOnClickPendingIntent(R.id.next_hotel_btn, PendingIntent.getService(this,
				widget.appWidgetIdInteger.intValue() + 1, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT));

		clearWidgetOnClickIntent(rv);

		setWidgetLoadingTextVisibility(widgetContents, View.GONE);

		updateWidget(widget, rv);
	}

	private void setupOnClickIntentForWidget(WidgetState widget, Property property, RemoteViews rv) {
		Intent onClickIntent = new Intent(this.getApplicationContext(), HotelActivity.class);
		onClickIntent.putExtra(Codes.SESSION, mWidgetDeals.getSession().toJson().toString());
		onClickIntent.putExtra(Codes.APP_WIDGET_ID, widget.appWidgetIdInteger);
		onClickIntent.putExtra(Codes.SEARCH_PARAMS, mWidgetDeals.getSearchParams().toJson().toString());
		onClickIntent.putExtra(Codes.OPENED_FROM_WIDGET, true);
		if (property != null) {
			onClickIntent.putExtra(Codes.PROPERTY, property.toJson().toString());
		}

		rv.setOnClickPendingIntent(R.id.root, PendingIntent.getActivity(this.getApplicationContext(),
				widget.appWidgetIdInteger.intValue() + 3, onClickIntent, PendingIntent.FLAG_UPDATE_CURRENT));
	}

	private void updateWidget(WidgetState widget, final RemoteViews rv) {
		AppWidgetManager gm = AppWidgetManager.getInstance(this);
		gm.updateAppWidget(widget.appWidgetIdInteger, rv);
	}

	// clear out the on-click intent on the widget by updating the widget
	// with an empty intent that goes into ether.
	private void clearWidgetOnClickIntent(RemoteViews rv) {
		rv.setOnClickPendingIntent(R.id.root,
				PendingIntent.getActivity(this, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT));
	}

	private void setWidgetPropertyViewVisibility(final RemoteViews widgetContents, int visibility) {
		widgetContents.setViewVisibility(R.id.price_per_night_container, visibility);
		widgetContents.setViewVisibility(R.id.hotel_image_view_wrapper, visibility);
		widgetContents.setViewVisibility(R.id.hotel_image_view, visibility);
		widgetContents.setViewVisibility(R.id.location_text_view, visibility);
		widgetContents.setViewVisibility(R.id.hotel_name_text_view, visibility);
		widgetContents.setViewVisibility(R.id.sale_text_view, visibility);
	}

	private void setBrandingViewVisibility(RemoteViews rootView, RemoteViews widgetContents, int visibility) {
		widgetContents.setViewVisibility(R.id.branding_text_container, visibility);
		widgetContents.setViewVisibility(R.id.hotel_radar_logo_image_view, visibility);

		/*
		 * Add the hangtag to the to the contianer so that 
		 * the animation is played with the image view is laid out
		 * as a nested remote view for the first time
		 */
		if (visibility == View.VISIBLE) {
			RemoteViews hangTagRemoteView = new RemoteViews(getPackageName(), R.layout.widget_hangtag);
			rootView.addView(R.id.branding_hang_tag_container, hangTagRemoteView);
		}
		else {
			rootView.removeAllViews(R.id.branding_hang_tag_container);
		}
	}

	private void setWidgetContentsVisibility(final RemoteViews widgetContents, int visibility) {
		widgetContents.setViewVisibility(R.id.widget_contents_container, visibility);
		widgetContents.setViewVisibility(R.id.navigation_container, visibility);
	}

	private void setWidgetLoadingTextVisibility(final RemoteViews widgetContents, int visibility) {
		widgetContents.setViewVisibility(R.id.widget_error_text_view, visibility);
		widgetContents.setViewVisibility(R.id.widget_error_tip_text_view, visibility);
		widgetContents.setViewVisibility(R.id.loading_expedia_logo_image_view, visibility);
	}

}
