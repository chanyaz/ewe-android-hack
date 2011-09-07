package com.expedia.bookings.appwidget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
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
import android.view.View;
import android.widget.RemoteViews;

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
import com.mobiata.hotellib.data.Property;
import com.mobiata.hotellib.data.SearchParams;
import com.mobiata.hotellib.data.SearchParams.SearchType;
import com.mobiata.hotellib.data.SearchResponse;
import com.mobiata.hotellib.data.ServerError;
import com.mobiata.hotellib.data.Session;
import com.mobiata.hotellib.server.ExpediaServices;
import com.mobiata.hotellib.utils.StrUtils;

public class ExpediaBookingsService extends Service implements LocationListener {

	//////////////////////////////////////////////////////////////////////////////////////////
	// CONSTANTS
	//////////////////////////////////////////////////////////////////////////////////////////

	/*
	 * Widget config related constants 
	 */
	private final static long UPDATE_INTERVAL = 1000 * 60 * 60; // Every 60 minutes
	public final static long ROTATE_INTERVAL = 1000 * 5; // Every 5 seconds
	private static final int MAX_RESULTS = 5;
	// maintain a bounded cache for the thumbnails to prevent OOM errors
	private static final int MAX_IMAGE_CACHE_SIZE = 30;

	// download key prefix to have 1 download key per widget installed
	private static final String WIDGET_KEY_SEARCH_PREFIX = "WIDGET_KEY_SEARCH.";

	// Intent actions
	public static final String START_SEARCH_ACTION = "com.expedia.bookings.START_SEARCH";
	public static final String START_CLEAN_SEARCH_ACTION = "com.expedia.bookings.START_CLEAN_SEARCH";
	public static final String CANCEL_UPDATE_ACTION = "com.expedia.bookings.CANCEL_UPDATE";
	private static final String SEARCH_PARAMS_CHANGED_ACTION = "com.expedia.bookings.SEARCH_PARAMS_CHANGED";
	public static final String NEXT_PROPERTY_ACTION = "com.expedia.bookings.NEXT_PROPERTY";
	public static final String ROTATE_PROPERTY_ACTION = "com.expedia.bookings.ROTATE_PROPERTY";
	public static final String PREV_PROPERTY_ACTION = "com.expedia.bookings.PREV_PROPERTY";

	private static final String WIDGET_THUMBNAIL_KEY_PREFIX = "WIDGET_THUMBNAIL_KEY.";

	//////////////////////////////////////////////////////////////////////////////////////////
	// BOOKKEEPING DATA STRUCTURES
	//////////////////////////////////////////////////////////////////////////////////////////
	private BackgroundDownloader mSearchDownloader = BackgroundDownloader.getInstance();

	private Map<Integer, WidgetState> mWidgets;
	private Queue<WidgetState> mWaitingOnLocationQueue = new LinkedList<ExpediaBookingsService.WidgetState>();

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
		private static final String SEARCH_IN_PAST = "Specified arrival date is prior to today's date.";
		Integer appWidgetIdInteger;
		Session mSession;
		SearchParams mSearchParams;
		List<Property> mProperties;
		int mCurrentPosition = -1;
		double maxPercentSavings;
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
				SearchResponse searchResponse = (SearchResponse) results;

				if (searchResponse != null && !searchResponse.hasErrors()) {
					mSession = searchResponse.getSession();
					Property[] properties = searchResponse.getProperties().toArray(new Property[0]).clone();
					Arrays.sort(properties, Property.PRICE_COMPARATOR);
					determineRelevantProperties(WidgetState.this, properties);
					mCurrentPosition = -1;
					loadPropertyIntoWidget(WidgetState.this);
				}
				else if (searchResponse != null && searchResponse.hasErrors()) {
					ServerError error = searchResponse.getErrors().get(0);
					/*
					 * NOTE: We have to check for an error based on its description
					 * as there is no unique error code for every error. This is 
					 * obviously prone to error if the message ever changes, but I 
					 * don't see any other way of looking for this particular 
					 * error without server side changes to pass a unique id 
					 * for every error. 
					 */
					if (error.getPresentableMessage(ExpediaBookingsService.this).contains(SEARCH_IN_PAST)) {
						updateWidgetWithText(WidgetState.this, getString(R.string.error_search_in_past), true);
					}
				}

				if (mProperties == null || mProperties.isEmpty()) {
					updateWidgetWithText(WidgetState.this, getString(R.string.progress_search_failed), true);
				}

				// schedule the next update
				scheduleSearch();
			}
		};
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// LOCATION METHODS
	//////////////////////////////////////////////////////////////////////////////////////////

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

	//////////////////////////////////////////////////////////////////////////////////////////
	// LOCATION LISTENER IMPLEMENTATION
	//////////////////////////////////////////////////////////////////////////////////////////

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
		
		/*
		 * Start search for all the widgets
		 * if there are widgets installed and the
		 * service was invoked without any intent
		 * (which means that, in all probability,
		 * it was restarted after a force close)
		 */
		if(intent == null && !mWidgets.isEmpty()) {
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
			mWidgets.put(appWidgetIdInteger, widget);
			startSearch(widget);
			updateWidgetWithText(widget, getString(R.string.loading_hotels), false);
		} else {
			
			/*
			 * kill the widget if there are no widgets installed
			 */
			if(mWidgets.isEmpty()) {
				stopSelf();
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
			} else if (intent.getAction().equals(CANCEL_UPDATE_ACTION)) {
				Integer appWidgetIdInteger = new Integer(intent.getIntExtra(Codes.APP_WIDGET_ID, -1));
				cancelRotation();
	
				mWidgets.remove(appWidgetIdInteger);
	
				if (mWidgets.isEmpty()) {
					cancelScheduledSearch();
					// if all widgets have been deleted, kill
					// the widget
					stopSelf();
				}
			}
			else if (intent.getAction().equals(NEXT_PROPERTY_ACTION)) {
				Integer appWidgetIdInteger = new Integer(intent.getIntExtra(Codes.APP_WIDGET_ID, -1));
				WidgetState widget = mWidgets.get(appWidgetIdInteger);
				loadNextProperty(widget);
	
			}
			else if (intent.getAction().equals(PREV_PROPERTY_ACTION)) {
				Integer appWidgetIdInteger = new Integer(intent.getIntExtra(Codes.APP_WIDGET_ID, -1));
				WidgetState widget = mWidgets.get(appWidgetIdInteger);
				loadPreviousProperty(widget);
	
			}
			else if (intent.getAction().equals(ROTATE_PROPERTY_ACTION)) {
				for (WidgetState widget : mWidgets.values()) {
					loadNextProperty(widget);
				}
			}
		}
		
		return Service.START_STICKY;
	}
	
	

	//////////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////////

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
		Intent i = new Intent(ROTATE_PROPERTY_ACTION);
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

		boolean searchForHotelsTonight = false;

		if (searchHotelsNearYou) {
			searchForHotelsTonight = true;
		}
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
			searchForHotelsTonight = true;
		}

		// default to searching for hotels around current location
		if (widget.mSearchParams == null) {
			widget.mUseCurrentLocation = true;
			widget.mSearchParams = new SearchParams();
		}

		// set default stay of 1 night starting today if the current check in date
		// is past the current time OR if the widget is configured to be searched
		// based on current location or exact search location
		if (searchForHotelsTonight
				|| (widget.mSearchParams.getCheckInDate().getTimeInMillis() < System.currentTimeMillis())) {
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
			updateWidgetWithText(widget, getString(R.string.widget_error_no_internet), true);
			return;
		}

		String key = WIDGET_KEY_SEARCH_PREFIX + widget.appWidgetIdInteger;
		mSearchDownloader.cancelDownload(key + widget.appWidgetIdInteger);
		mSearchDownloader.startDownload(key, widget.mSearchDownload, widget.mSearchCallback);
	}

	private void loadPropertyIntoWidget(final WidgetState widget) {
		if (widget.mCurrentPosition == -1) {
			updateWidgetBranding(widget);
			scheduleRotation();
			return;
		}

		final Property property = widget.mProperties.get(widget.mCurrentPosition);
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
							&& widget.mProperties.get(widget.mCurrentPosition).getThumbnail().getUrl().equals(url)) {

						if (thumbnailCache.size() >= MAX_IMAGE_CACHE_SIZE) {
							// clear out the cache if it ever reaches its max size
							thumbnailCache.clear();
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
			});
		}
		updateWidgetWithProperty(property, widget);
		scheduleRotation();
	}

	private void determineRelevantProperties(WidgetState widget, Property[] properties) {
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
		double savingsPercent = property.getLowestRate().getSavingsPercent();
		if (widget.maxPercentSavings == 0 || widget.maxPercentSavings < savingsPercent) {
			widget.maxPercentSavings = savingsPercent;
			widget.savingsForProperty = property;
		}
	}

	private void loadNextProperty(WidgetState widget) {
		if (widget.mProperties != null) {
			widget.mCurrentPosition = ((widget.mCurrentPosition + 1) >= widget.mProperties.size()) ? -1
					: widget.mCurrentPosition + 1;
			loadPropertyIntoWidget(widget);
		}
	}

	private void loadPreviousProperty(WidgetState widget) {
		if (widget.mProperties != null) {

			widget.mCurrentPosition = ((widget.mCurrentPosition - 1) < -1) ? (widget.mProperties.size() - 1)
					: (widget.mCurrentPosition - 1);
			loadPropertyIntoWidget(widget);
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

		setBrandingViewVisibility(widgetContents, View.GONE);

		setWidgetPropertyViewVisibility(widgetContents, View.VISIBLE);

		widgetContents.setTextViewText(R.id.hotel_name_text_view, property.getName());
		String location = (!widget.mUseCurrentLocation && (widget.mSearchParams.getSearchType() != SearchType.MY_LOCATION)) ? widget.mSearchParams
				.getFreeformLocation() : property.getDistanceFromUser().formatDistance(this);
		widgetContents.setTextViewText(R.id.location_text_view, location);
		widgetContents.setTextViewText(R.id.price_text_view,
				StrUtils.formatHotelPrice(property.getLowestRate().getDisplayRate()));

		if (property.getLowestRate().getSavingsPercent() > 0) {
			widgetContents.setTextViewText(R.id.sale_text_view,
					getString(R.string.widget_savings_template, property.getLowestRate().getSavingsPercent() * 100));
			widgetContents.setInt(R.id.price_per_night_container, "setBackgroundResource", R.drawable.widget_price_bg);
			widgetContents.setViewVisibility(R.id.sale_text_view, View.VISIBLE);
			widgetContents.setViewVisibility(R.id.highly_rated_text_view, View.GONE);
		}
		else if (property.getLowestRate().getSavingsPercent() == 0 && property.isHighlyRated()) {
			widgetContents.setViewVisibility(R.id.sale_text_view, View.GONE);
			widgetContents.setViewVisibility(R.id.highly_rated_text_view, View.VISIBLE);
		}
		else {
			widgetContents.setViewVisibility(R.id.sale_text_view, View.GONE);
			widgetContents.setViewVisibility(R.id.highly_rated_text_view, View.GONE);
			widgetContents.setInt(R.id.price_per_night_container, "setBackgroundResource",
					R.drawable.widget_price_bg_no_sale);
		}

		Bitmap bitmap = ExpediaBookingsService.thumbnailCache.get(property.getThumbnail().getUrl());
		if (bitmap == null) {
			widgetContents.setImageViewResource(R.id.hotel_image_view, R.drawable.widget_thumbnail_background);
		}
		else {
			widgetContents.setImageViewBitmap(R.id.hotel_image_view, bitmap);
		}

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

		Intent onClickIntent = new Intent(this, SearchActivity.class);
		onClickIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		onClickIntent.putExtra(Codes.PROPERTY, property.toJson().toString());
		onClickIntent.putExtra(Codes.SESSION, widget.mSession.toJson().toString());
		onClickIntent.putExtra(Codes.APP_WIDGET_ID, widget.appWidgetIdInteger);
		onClickIntent.putExtra(Codes.SEARCH_PARAMS, widget.mSearchParams.toJson().toString());
		onClickIntent.putExtra(Codes.OPENED_FROM_WIDGET, true);
		
		rv.setOnClickPendingIntent(R.id.root, PendingIntent.getActivity(this, widget.appWidgetIdInteger.intValue() + 3,
				onClickIntent, PendingIntent.FLAG_UPDATE_CURRENT));

		setWidgetLoadingTextVisibility(widgetContents, View.GONE);

		updateWidget(widget, rv);
	}

	public void updateWidgetWithText(WidgetState widget, String error, boolean refreshOnClick) {
		updateWidgetWithText(widget, error, refreshOnClick, false);
	}

	private void updateWidgetWithText(WidgetState widget, String error, boolean refreshOnClick, boolean showBranding) {
		RemoteViews rv = new RemoteViews(getPackageName(), R.layout.widget);
		RemoteViews widgetContents = new RemoteViews(getPackageName(), R.layout.widget_contents);

		rv.removeAllViews(R.id.hotel_info_contents);
		rv.addView(R.id.hotel_info_contents, widgetContents);

		setWidgetPropertyViewVisibility(widgetContents, View.GONE);

		if (showBranding) {
			widgetContents.setViewVisibility(R.id.widget_contents_container, View.VISIBLE);
			widgetContents.setViewVisibility(R.id.navigation_container, View.GONE);

			widgetContents.setViewVisibility(R.id.branding_text_container, View.VISIBLE);
			widgetContents.setViewVisibility(R.id.expedia_logo_image_view, View.VISIBLE);
			widgetContents.setViewVisibility(R.id.branding_title_text_view, View.GONE);
			widgetContents.setViewVisibility(R.id.branding_location_text_view, View.GONE);
			widgetContents.setViewVisibility(R.id.branding_savings_container, View.GONE);
			widgetContents.setViewVisibility(R.id.branding_error_message_text_view, View.VISIBLE);

			widgetContents.setViewVisibility(R.id.loading_text_view, View.GONE);
			widgetContents.setViewVisibility(R.id.loading_text_container, View.GONE);

			widgetContents.setTextViewText(R.id.branding_error_message_text_view, error);
		}
		else {
			setWidgetContentsVisibility(widgetContents, View.GONE);
			widgetContents.setTextViewText(R.id.loading_text_view, error);
			widgetContents.setViewVisibility(R.id.loading_text_view, View.VISIBLE);
			widgetContents.setViewVisibility(R.id.loading_text_container, View.VISIBLE);
		}

		widgetContents.setViewVisibility(R.id.refresh_text_view, View.GONE);

		if (refreshOnClick && !showBranding) {
			Intent onClickIntent = new Intent(ExpediaBookingsService.START_CLEAN_SEARCH_ACTION);
			onClickIntent.putExtra(Codes.APP_WIDGET_ID, widget.appWidgetIdInteger);

			rv.setOnClickPendingIntent(R.id.root, PendingIntent.getService(this,
					widget.appWidgetIdInteger.intValue() + 4, onClickIntent, PendingIntent.FLAG_UPDATE_CURRENT));
			rv.setViewVisibility(R.id.refresh_text_view, View.VISIBLE);
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

		setBrandingViewVisibility(widgetContents, View.VISIBLE);

		String brandingSavings = widget.maxPercentSavings > 0 ? Integer
				.toString((int) (widget.maxPercentSavings * 100)) : null;
		if (brandingSavings == null) {
			widgetContents.setViewVisibility(R.id.branding_savings_container, View.GONE);
		}
		else {
			widgetContents.setViewVisibility(R.id.branding_savings_container, View.VISIBLE);
			widgetContents.setTextViewText(R.id.branding_savings_container,
					getString(R.string.save_upto_template, brandingSavings));
		}

		String distanceOfMaxSavingsFromUser = (widget.mUseCurrentLocation && widget.savingsForProperty != null) ? widget.savingsForProperty
				.getDistanceFromUser().formatDistance(this) : null;
		if (distanceOfMaxSavingsFromUser != null) {
			widgetContents.setTextViewText(R.id.branding_location_text_view, distanceOfMaxSavingsFromUser);
		}
		else {
			String location = (!widget.mUseCurrentLocation && (widget.mSearchParams.getSearchType() != SearchType.MY_LOCATION)) ? widget.mSearchParams
					.getFreeformLocation() : "Current Location";

			widgetContents.setTextViewText(R.id.branding_location_text_view, location);
		}

		String brandingTitle = null;
		if (DateUtils.isToday(widget.mSearchParams.getCheckInDate().getTimeInMillis())) {
			brandingTitle = getString(R.string.tonight_top_deals);
		}
		else {
			brandingTitle = getString(R.string.top_deals);
		}

		widgetContents.setTextViewText(R.id.branding_title_text_view, brandingTitle);

		Intent prevIntent = new Intent(PREV_PROPERTY_ACTION);
		prevIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		prevIntent.putExtra(Codes.APP_WIDGET_ID, widget.appWidgetIdInteger);

		Intent nextIntent = new Intent(NEXT_PROPERTY_ACTION);
		nextIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		nextIntent.putExtra(Codes.APP_WIDGET_ID, widget.appWidgetIdInteger);

		clearWidgetOnClickIntent(rv);

		widgetContents.setOnClickPendingIntent(R.id.prev_hotel_btn, PendingIntent.getService(this,
				widget.appWidgetIdInteger.intValue() + 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT));
		widgetContents.setOnClickPendingIntent(R.id.next_hotel_btn, PendingIntent.getService(this,
				widget.appWidgetIdInteger.intValue() + 1, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT));

		setWidgetLoadingTextVisibility(widgetContents, View.GONE);

		updateWidget(widget, rv);
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
		widgetContents.setViewVisibility(R.id.highly_rated_text_view, visibility);
	}

	private void setBrandingViewVisibility(final RemoteViews widgetContents, int visibility) {
		widgetContents.setViewVisibility(R.id.branding_text_container, visibility);
		widgetContents.setViewVisibility(R.id.expedia_logo_image_view, visibility);
	}

	private void setWidgetContentsVisibility(final RemoteViews widgetContents, int visibility) {
		widgetContents.setViewVisibility(R.id.widget_contents_container, visibility);
		widgetContents.setViewVisibility(R.id.navigation_container, visibility);
	}

	private void setWidgetLoadingTextVisibility(final RemoteViews widgetContents, int visibility) {
		widgetContents.setViewVisibility(R.id.loading_text_view, visibility);
		widgetContents.setViewVisibility(R.id.loading_text_container, visibility);
		widgetContents.setViewVisibility(R.id.refresh_text_view, visibility);
	}

}
