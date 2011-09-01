package com.expedia.bookings.appwidget;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.model.WidgetConfigurationState;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.ImageCache;
import com.mobiata.android.ImageCache.OnImageLoaded;
import com.mobiata.android.LocationServices;
import com.mobiata.android.Log;
import com.mobiata.android.util.NetUtils;
import com.mobiata.hotellib.data.Codes;
import com.mobiata.hotellib.data.Money;
import com.mobiata.hotellib.data.Property;
import com.mobiata.hotellib.data.SearchParams;
import com.mobiata.hotellib.data.SearchParams.SearchType;
import com.mobiata.hotellib.data.SearchResponse;
import com.mobiata.hotellib.data.ServerError;
import com.mobiata.hotellib.data.Session;
import com.mobiata.hotellib.server.ExpediaServices;
import com.mobiata.hotellib.utils.StrUtils;

public class ExpediaBookingsService extends Service implements LocationListener {

	private final static long UPDATE_INTERVAL = 1000 * 60 * 60; // Every 60 minutes
	public final static long ROTATE_INTERVAL = 1000 * 5; // Every 5 seconds
	private static final String WIDGET_KEY_SEARCH_PREFIX = "WIDGET_KEY_SEARCH.";
	private static final int MAX_RESULTS = 5;

	// Intent actions
	public static final String START_SEARCH_ACTION = "com.expedia.bookings.START_SEARCH";
	public static final String START_CLEAN_SEARCH_ACTION = "com.expedia.bookings.START_CLEAN_SEARCH";
	public static final String CANCEL_UPDATE_ACTION = "com.expedia.bookings.CANCEL_UPDATE";
	private static final String SEARCH_PARAMS_CHANGED_ACTION = "com.expedia.bookings.SEARCH_PARAMS_CHANGED";
	private static final String WIDGET_THUMBNAIL_KEY_PREFIX = "WIDGET_THUMBNAIL_KEY.";

	private Map<Integer, WidgetState> mWidgets;
	private Queue<WidgetState> mWaitingOnLocationQueue = new LinkedList<ExpediaBookingsService.WidgetState>();

	/*
	 * This object holds all state persisting to the widget 
	 * so that we can support multiple widgets each holding its own
	 * state
	 */

	private class WidgetState {
		private static final String SEARCH_IN_PAST = "Specified arrival date is prior to today's date.";
		Integer appWidgetIdInteger;
		Session mSession;
		SearchResponse mSearchResponse;
		SearchParams mSearchParams;
		List<Property> mProperties;
		String mFreeFormLocation;
		int mCurrentPosition = -1;
		Money savings;
		Property savingsForProperty;
		boolean mUseCurrentLocation;

		Download mSearchDownload = new Download() {
			@Override
			public Object doDownload() {
				ExpediaServices services = new ExpediaServices(getApplicationContext(), mSession);
				mSearchDownloader.addDownloadListener(WIDGET_KEY_SEARCH_PREFIX + appWidgetIdInteger, services);
				return services.search(mSearchParams, 0);
			}
		};

		OnDownloadComplete mSearchCallback = new OnDownloadComplete() {
			@Override
			public void onDownload(Object results) {
				mSearchResponse = (SearchResponse) results;

				if (mSearchResponse != null && !mSearchResponse.hasErrors()) {
					mSession = mSearchResponse.getSession();
					determineRelevantProperties(WidgetState.this);
					mFreeFormLocation = mSearchParams.getFreeformLocation();
					mCurrentPosition = -1;
					loadImageForProperty(WidgetState.this);
				}
				else if (mSearchResponse != null && mSearchResponse.hasErrors()) {
					ServerError error = mSearchResponse.getErrors().get(0);
					/*
					 * NOTE: We have to check for an error based on its description
					 * as there is no unique error code for every error. This is 
					 * obviously prone to error if the message ever changes, but I 
					 * don't see any other way of looking for this particular 
					 * error without server side changes to pass a unique id 
					 * for every error. 
					 */
					if (error.getPresentableMessage(ExpediaBookingsService.this).contains(SEARCH_IN_PAST)) {
						broadcastWidgetError(WidgetState.this, getString(R.string.error_search_in_past), true);
					}
				}

				if (mProperties == null || mProperties.isEmpty()) {
					broadcastWidgetError(WidgetState.this, getString(R.string.progress_search_failed));
				}

				// schedule the next update
				scheduleSearch();
			}
		};
	}

	//----------------------------------
	// THREADS/CALLBACKS
	//----------------------------------

	private BackgroundDownloader mSearchDownloader = BackgroundDownloader.getInstance();

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

		for (WidgetState widget : mWaitingOnLocationQueue) {
			widget.mSearchParams.setSearchLatLon(location.getLatitude(), location.getLongitude());
			startSearchDownloader(widget);
		}
		mWaitingOnLocationQueue.clear();
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

		if (intent.getAction().equals(SEARCH_PARAMS_CHANGED_ACTION)) {
			// when parameters are changed, restart 
			// every search to ensure that all widgets pick up
			// the latest and greatest parameters (number of adults,
			// duration dates, etc)
			for (WidgetState widget : mWidgets.values()) {
				startSearch(widget);
			}
		}
		else if (intent.getAction().equals(START_SEARCH_ACTION)) {
			// updated all widgets around the same time instead of
			// having a different update cycle for each widget 
			// that is added to the home screen
			for (WidgetState state : mWidgets.values()) {
				startSearch(state);
			}
		}
		else if (intent.getAction().equals(START_CLEAN_SEARCH_ACTION)) {
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
			mWidgets.put(appWidgetIdInteger, widget);
			startSearch(widget);
		}
		else if (intent.getAction().equals(CANCEL_UPDATE_ACTION)) {
			Integer appWidgetIdInteger = new Integer(intent.getIntExtra(Codes.APP_WIDGET_ID, -1));
			cancelRotation();

			mWidgets.remove(appWidgetIdInteger);

			if (mWidgets.isEmpty()) {
				cancelScheduledSearch();
			}
		}
		else if (intent.getAction().equals(ExpediaBookingsWidgetReceiver.NEXT_PROPERTY_ACTION)) {
			Integer appWidgetIdInteger = new Integer(intent.getIntExtra(Codes.APP_WIDGET_ID, -1));
			WidgetState widget = mWidgets.get(appWidgetIdInteger);
			loadNextProperty(widget);

		}
		else if (intent.getAction().equals(ExpediaBookingsWidgetReceiver.PREV_PROPERTY_ACTION)) {
			Integer appWidgetIdInteger = new Integer(intent.getIntExtra(Codes.APP_WIDGET_ID, -1));
			WidgetState widget = mWidgets.get(appWidgetIdInteger);
			loadPreviousProperty(widget);

		}
		else if (intent.getAction().equals(ExpediaBookingsWidgetReceiver.ROTATE_PROPERTY_ACTION)) {
			for (WidgetState widget : mWidgets.values()) {
				loadNextProperty(widget);
			}
		}

		return super.onStartCommand(intent, flags, startId);
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
		Intent i = new Intent(ExpediaBookingsWidgetReceiver.ROTATE_PROPERTY_ACTION);
		PendingIntent operation = PendingIntent.getService(getApplicationContext(), 0, i,
				PendingIntent.FLAG_UPDATE_CURRENT);
		return operation;
	}

	private void startSearch(WidgetState widget) {
		Log.i("Starting search");
		mSearchDownloader.cancelDownload(WIDGET_KEY_SEARCH_PREFIX + widget.appWidgetIdInteger);

		setupSearchParams(widget);

		if (widget.mUseCurrentLocation) {
			// See if we have a good enough location stored
			long minTime = Calendar.getInstance().getTimeInMillis() - SearchActivity.MINIMUM_TIME_AGO;
			Location location = LocationServices.getLastBestLocation(getApplicationContext(), minTime);
			if (location != null) {
				widget.mSearchParams.setSearchLatLon(location.getLatitude(), location.getLongitude());
				startSearchDownloader(widget);
			}
			else {
				mWaitingOnLocationQueue.add(widget);
				startLocationListener();
			}
		}
		else {
			startSearchDownloader(widget);
		}
	}

	private void setupSearchParams(WidgetState widget) {
		WidgetConfigurationState cs = WidgetConfigurationState.getWidgetConfiguration(this,
				widget.appWidgetIdInteger.intValue());

		// check whether user would like to search for hotels near current location
		// or based on last search
		boolean searchHotelsNearYou = cs.showHotelsNearCurrentLocation();
		boolean searchHotelsBasedOnLastSearch = cs.showHotelsBasedOnLastSearch();
		String specificLocation = cs.getExactSearchLocation();

		widget.mUseCurrentLocation = searchHotelsNearYou;

		if (searchHotelsBasedOnLastSearch) {
			getSearchParamsFromDisk(widget);
		}
		else if (specificLocation != null && !specificLocation.equals("")) {
			getSearchParamsFromDisk(widget);
			double locationLat = cs.getExactSearchLocationLat();
			double locationLong = cs.getExactSearchLocationLon();
			widget.mSearchParams.setSearchLatLon(locationLat, locationLong);
			widget.mSearchParams.setFreeformLocation(specificLocation);
			widget.mSearchParams.setSearchType(SearchType.KEYWORD);
		}

		// default to searching for hotels around current location
		if (widget.mSearchParams == null) {
			widget.mUseCurrentLocation = true;
			widget.mSearchParams = new SearchParams();
		}

		// set default stay of 1 night starting today if the current check in date
		// is past the current time
		if (widget.mSearchParams.getCheckInDate().getTimeInMillis() < System.currentTimeMillis()) {
			widget.mSearchParams.setDefaultStay();
		}
	}

	private void getSearchParamsFromDisk(WidgetState widget) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String searchParamsString = prefs.getString("searchParams", null);
		try {
			if (searchParamsString != null) {
				widget.mSearchParams = new SearchParams(new JSONObject(searchParamsString));
			}
			else {
				widget.mSearchParams = new SearchParams();
			}
		}
		catch (JSONException e) {
		}
	}

	private void startSearchDownloader(WidgetState widget) {

		if (!NetUtils.isOnline(getApplicationContext()) && (widget.mProperties == null || widget.mProperties.isEmpty())) {
			broadcastWidgetError(widget, getString(R.string.widget_error_no_internet));
			return;
		}

		String key = WIDGET_KEY_SEARCH_PREFIX + widget.appWidgetIdInteger;
		mSearchDownloader.cancelDownload(key + widget.appWidgetIdInteger);
		mSearchDownloader.startDownload(key, widget.mSearchDownload, widget.mSearchCallback);
	}

	private void broadcastLoadingWidget(WidgetState widget) {
		Property property = widget.mProperties.get(widget.mCurrentPosition);
		Intent i = new Intent(ExpediaBookingsWidgetReceiver.LOAD_PROPERTY_ACTION);
		i.putExtra(Codes.PROPERTY, property.toJson().toString());
		i.putExtra(Codes.SEARCH_PARAMS, widget.mSearchParams.toJson().toString());
		String location = (!widget.mUseCurrentLocation && (widget.mSearchParams.getSearchType() != SearchType.MY_LOCATION)) ? widget.mFreeFormLocation
				: property.getDistanceFromUser().formatDistance(this);
		i.putExtra(Codes.PROPERTY_LOCATION_PREFIX + widget.appWidgetIdInteger, location);
		i.putExtra(Codes.SESSION, widget.mSession.toJson().toString());
		i.putExtra(Codes.APP_WIDGET_ID, widget.appWidgetIdInteger.intValue());
		sendBroadcast(i);
		scheduleRotation();
	}

	private void broadcastWidgetError(WidgetState widget, CharSequence error) {
		broadcastWidgetError(widget, error, false);
	}

	private void broadcastWidgetError(WidgetState widget, CharSequence error, boolean showBranding) {
		Intent i = new Intent(ExpediaBookingsWidgetReceiver.LOAD_PROPERTY_ACTION);
		i.putExtra(Codes.APP_WIDGET_ID, widget.appWidgetIdInteger.intValue());
		i.putExtra(Codes.SEARCH_ERROR, error);
		i.putExtra(Codes.SHOW_BRANDING, showBranding);
		sendBroadcast(i);
	}

	private void loadImageForProperty(final WidgetState widget) {
		if (widget.mCurrentPosition == -1) {
			broadcastWidgetBranding(widget);
			return;
		}

		final Property property = widget.mProperties.get(widget.mCurrentPosition);
		Bitmap bitmap = ImageCache.getImage(property.getThumbnail().getUrl());
		// if the bitmap doesn't exist in the cache, asynchronously load the image while
		// updating the widget remote view with the rest of the information
		if (bitmap == null) {
			ImageCache.loadImage(WIDGET_THUMBNAIL_KEY_PREFIX + widget.appWidgetIdInteger, property.getThumbnail().getUrl(), new OnImageLoaded() {

				@Override
				public void onImageLoaded(String url, Bitmap bitmap) {
					// once the image loads, update the remote view of the widget with the image, 
					// making sure that the image actually belongs to the current property loaded 
					// in the remote view
					if (widget.mCurrentPosition != -1
							&& widget.mProperties.get(widget.mCurrentPosition).getThumbnail().getUrl().equals(url)) {
						broadcastLoadedImage(widget);
					}
				}
			});
		}
		broadcastLoadingWidget(widget);
	}

	private void broadcastWidgetBranding(final WidgetState widget) {
		Intent i = new Intent(ExpediaBookingsWidgetReceiver.LOAD_BRANDING_ACTION);
		if (widget.savings != null) {
			i.putExtra(Codes.BRANDING_SAVINGS, StrUtils.formatHotelPrice(widget.savings));
		}

		if (widget.mUseCurrentLocation && widget.savingsForProperty != null) {
			i.putExtra(Codes.DISTANCE_OF_MAX_SAVINGS,
					widget.savingsForProperty.getDistanceFromUser().formatDistance(this));
		}

		if (DateUtils.isToday(widget.mSearchParams.getCheckInDate().getTimeInMillis())) {
			i.putExtra(Codes.BRANDING_TITLE, getString(R.string.tonight_top_deals));
		}
		else {
			i.putExtra(Codes.BRANDING_TITLE, getString(R.string.top_deals));
		}

		String location = (!widget.mUseCurrentLocation && (widget.mSearchParams.getSearchType() != SearchType.MY_LOCATION)) ? widget.mFreeFormLocation
				: "Current Location";
		i.putExtra(Codes.PROPERTY_LOCATION_PREFIX + widget.appWidgetIdInteger, location);
		i.putExtra(Codes.APP_WIDGET_ID, widget.appWidgetIdInteger.intValue());
		sendBroadcast(i);
		scheduleRotation();
		return;
	}

	private void broadcastLoadedImage(WidgetState widget) {
		Property property = widget.mProperties.get(widget.mCurrentPosition);
		Intent i = new Intent(ExpediaBookingsWidgetReceiver.LOAD_IMAGE_FOR_PROPERTY_ACTION);
		i.putExtra(Codes.PROPERTY, property.toJson().toString());
		i.putExtra(Codes.APP_WIDGET_ID, widget.appWidgetIdInteger.intValue());
		sendBroadcast(i);
		scheduleRotation();
	}

	private void determineRelevantProperties(WidgetState widget) {
		List<Property> properties = widget.mSearchResponse.getProperties();
		List<Property> relevantProperties = new ArrayList<Property>();

		// first populate the list with hotels that have rooms on sale
		for (Property property : properties) {
			if (relevantProperties.size() == MAX_RESULTS) {
				break;
			}

			if (property.getLowestRate().getSavingsPercent() > 0) {
				trackMaximumSavingsForWidget(widget, property);
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

		widget.mProperties = relevantProperties;
	}

	private void trackMaximumSavingsForWidget(WidgetState widget, Property property) {
		double savings = property.getLowestRate().getDisplayBaseRate().getAmount()
				- property.getLowestRate().getDisplayRate().getAmount();
		Money propertySavings = property.getLowRate().copy();
		propertySavings.setAmount(savings);
		if (widget.savings == null || widget.savings.getAmount() < propertySavings.getAmount()) {
			widget.savings = propertySavings;
			widget.savingsForProperty = property;
		}
	}

	private void loadNextProperty(WidgetState widget) {
		if (widget.mProperties != null) {
			widget.mCurrentPosition = ((widget.mCurrentPosition + 1) >= widget.mProperties.size()) ? -1
					: widget.mCurrentPosition + 1;
			loadImageForProperty(widget);
		}
	}

	private void loadPreviousProperty(WidgetState widget) {
		if (widget.mProperties != null) {

			widget.mCurrentPosition = ((widget.mCurrentPosition - 1) < -1) ? (widget.mProperties.size() - 1)
					: (widget.mCurrentPosition - 1);
			loadImageForProperty(widget);
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
			setupSearchParams(widget);
		}
	}
}
