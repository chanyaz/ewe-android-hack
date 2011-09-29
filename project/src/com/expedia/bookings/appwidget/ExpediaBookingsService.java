package com.expedia.bookings.appwidget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.RemoteViews;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ConfirmationActivity;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.activity.HotelActivity;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.SearchResponse;
import com.expedia.bookings.model.WidgetConfigurationState;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.ImageCache;
import com.mobiata.android.ImageCache.OnImageLoaded;
import com.mobiata.android.Log;
import com.mobiata.android.util.NetUtils;

public class ExpediaBookingsService extends Service {

	//////////////////////////////////////////////////////////////////////////////////////////
	// CONSTANTS
	//////////////////////////////////////////////////////////////////////////////////////////

	/*
	 * Widget config related constants 
	 */
	private final static long UPDATE_INTERVAL = 1000 * 60 * 60; // Every 60 minutes
	public final static long ROTATE_INTERVAL = 1000 * 5; // Every 5 seconds
	public final static long INCREASED_ROTATE_INTERVAL = 1000 * 30; // Every 30 seconds

	// Intent actions
	public static final String START_SEARCH_ACTION = "com.expedia.bookings.START_SEARCH";
	public static final String START_CLEAN_SEARCH_ACTION = "com.expedia.bookings.START_CLEAN_SEARCH";
	public static final String CANCEL_UPDATE_ACTION = "com.expedia.bookings.CANCEL_UPDATE";
	public static final String NEXT_PROPERTY_ACTION = "com.expedia.bookings.NEXT_PROPERTY";
	public static final String ROTATE_PROPERTY_ACTION = "com.expedia.bookings.ROTATE_PROPERTY";
	public static final String PREV_PROPERTY_ACTION = "com.expedia.bookings.PREV_PROPERTY";
	public static final String PAUSE_WIDGETS_ACTION = "com.expedia.bookings.PAUSE_WIDGETS";
	public static final String RESUME_WIDGETS_ACTION = "com.expedia.bookings.RESUME_WIDGETS";
	public static final String LOAD_CONFIRMATION_ACTION = "com.expedia.bookings.LOAD_CONFIRMATION";

	//////////////////////////////////////////////////////////////////////////////////////////
	// BOOKKEEPING DATA STRUCTURES
	//////////////////////////////////////////////////////////////////////////////////////////
	private BackgroundDownloader mSearchDownloader = BackgroundDownloader.getInstance();

	private Map<Integer, WidgetState> mWidgets;

	private ExpediaBookingApp mApp;

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
		if (intent == null && !mWidgets.isEmpty()) {
			intent = new Intent(START_SEARCH_ACTION);
		}

		/* 
		 * The widget is "paused" when the 
		 * app is opened and the user is interacting with
		 * to do a hotel search. 
		 */
		if (intent.getAction().equals(PAUSE_WIDGETS_ACTION)) {
			pauseWidgetActivity();
		}
		else if (intent.getAction().equals(RESUME_WIDGETS_ACTION)) {
			loadPropertyIntoWidgets(ROTATE_INTERVAL);
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

			/*
			 * Load deals either from memory or disk, if 
			 * they are available. If not, it implies
			 * that there are no search parameters either,
			 * and hence nothing to load into the widget.
			 */
			int dealsAvailable = loadWidgetDeals();
			if (dealsAvailable == WidgetDeals.WIDGET_DEALS_RESTORED) {
				loadPropertyIntoWidget(widget);
				scheduleRotation(ROTATE_INTERVAL);
			}
			else {
				if (ConfirmationActivity.hasSavedConfirmationData(this)) {
					updateWidgetsWithConfirmation();
				}
				else if (dealsAvailable == WidgetDeals.NO_DEALS_EXIST) {
					updateWidgetsWithText(getString(R.string.progress_search_failed),
							getString(R.string.tap_to_start_new_search), getStartNewSearchIntent());
				}
				else if (dealsAvailable == WidgetDeals.NO_WIDGET_FILE_EXISTS) {
					updateWidgetsWithText(getString(R.string.tap_to_start_new_search), null, getStartNewSearchIntent());
				}
			}
		}
		else {

			/*
			 * kill the service if there are no widgets installed
			 */
			if (mWidgets.isEmpty()) {
				stopSelf();
			}
			else if (intent.getAction().equals(START_SEARCH_ACTION)) {
				startSearchForWidgets();
			}
			else if (intent.getAction().equals(CANCEL_UPDATE_ACTION)) {
				cancelRotation();

				if (mWidgets.isEmpty()) {
					cancelScheduledSearch();
					mSearchDownloader.cancelDownload(SearchActivity.KEY_SEARCH);
					// if all widgets have been deleted, kill the widget
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
			else if (intent.getAction().equals(LOAD_CONFIRMATION_ACTION)) {
				pauseWidgetActivity();
				updateWidgetsWithConfirmation();
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
		mApp = (ExpediaBookingApp) getApplication();

		startSearchForWidgets();
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////////

	private Download mSearchDownload = new Download() {
		@Override
		public Object doDownload() {
			ExpediaServices services = new ExpediaServices(getApplicationContext(), mApp.widgetDeals.getSession());
			mSearchDownloader.addDownloadListener(SearchActivity.KEY_SEARCH, services);
			return services.search(mApp.widgetDeals.getSearchParams(), 0);
		}
	};

	private OnDownloadComplete mSearchCallback = new OnDownloadComplete() {
		@Override
		public void onDownload(Object results) {
			SearchResponse searchResponse = (SearchResponse) results;
			boolean toPersistDeals = false;
			if (searchResponse != null && !searchResponse.hasErrors()) {
				searchResponse.setFilter(mApp.widgetDeals.getFilter());
				loadPropertyIntoWidgets(ROTATE_INTERVAL);
				toPersistDeals = true;
			}

			mApp.widgetDeals.determineRelevantProperties(searchResponse);

			if (mApp.widgetDeals.getDeals() == null || mApp.widgetDeals.getDeals().isEmpty()) {
				updateWidgetsWithText(getString(R.string.progress_search_failed),
						getString(R.string.tap_to_start_new_search), getStartNewSearchIntent());
			}

			if (toPersistDeals) {
				mApp.widgetDeals.deleteFromDisk();
				mApp.widgetDeals.persistToDisk();
			}
			// schedule the next update
			scheduleSearch();
		}
	};

	private void pauseWidgetActivity() {
		cancelRotation();
		cancelScheduledSearch();
		if (mSearchDownloader.isDownloading(SearchActivity.KEY_SEARCH)) {
			mSearchDownloader.unregisterDownloadCallback(SearchActivity.KEY_SEARCH, mSearchCallback);
		}
	}

	private void scheduleSearch() {
		AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		PendingIntent operation = getUpdatePendingIntent();

		// Cancel any old updates
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
		Log.v("Scheduling next hotel rotation to occur in " + (ROTATE_INTERVAL / 1000) + " seconds.");
		am.set(AlarmManager.RTC, System.currentTimeMillis() + rotateInterval, operation);
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

	private int loadWidgetDeals() {
		if (mApp.widgetDeals.getDeals() == null) {
			return mApp.widgetDeals.restoreFromDisk();
		}
		return WidgetDeals.WIDGET_DEALS_RESTORED;
	}

	private void startSearch() {
		Log.i("Starting search");

		// default to searching for hotels around current location
		if (mApp.widgetDeals.getSearchParams() == null) {
			return;
		}

		startSearchDownloader();
	}

	private void startSearchDownloader() {

		if (!NetUtils.isOnline(getApplicationContext())
				&& (mApp.widgetDeals.getDeals() == null || mApp.widgetDeals.getDeals().isEmpty())) {
			updateWidgetsWithText(getString(R.string.widget_error_no_internet), getString(R.string.refresh_widget),
					getRefreshIntent());
			return;
		}
		else if (mApp.widgetDeals.getDeals() == null || mApp.widgetDeals.getDeals().isEmpty()) {
			updateWidgetsWithText(getString(R.string.loading_hotels), null, null);
		}

		mSearchDownloader.cancelDownload(SearchActivity.KEY_SEARCH);
		mSearchDownloader.startDownload(SearchActivity.KEY_SEARCH, mSearchDownload, mSearchCallback);
	}

	private void loadPropertyIntoWidgets(long rotateInterval) {
		if (mWidgets == null) {
			loadWidgets();
		}

		if (mApp.widgetDeals.getDeals() == null || mApp.widgetDeals.getDeals().isEmpty()) {
			updateWidgetsWithText(getString(R.string.progress_search_failed),
					getString(R.string.tap_to_start_new_search), getStartNewSearchIntent());
			return;
		}

		for (WidgetState widget : mWidgets.values()) {
			// restart all widgets to load from branding since the deals
			// have been updated
			widget.mCurrentPosition = -1;
			loadPropertyIntoWidget(widget);
		}

		scheduleRotation(rotateInterval);
	}

	private void loadPropertyIntoWidget(final WidgetState widget) {
		if (widget.mCurrentPosition == -1) {
			updateWidgetBranding(widget);
			return;
		}

		final Property property = mApp.widgetDeals.getDeals().get(widget.mCurrentPosition);
		Bitmap bitmap = ImageCache.getImage(property.getThumbnail().getUrl());
		// if the bitmap doesn't exist in the cache, asynchronously load the image while
		// updating the widget remote view with the rest of the information
		if (bitmap == null) {
			ImageCache.loadImage(property.getThumbnail().getUrl(), new OnImageLoaded() {

				@Override
				public void onImageLoaded(String url, Bitmap bitmap) {

					// making sure that the image actually belongs to the current property loaded 
					// in the remote view
					if (widget.mCurrentPosition != -1
							&& mApp.widgetDeals.getDeals().get(widget.mCurrentPosition).getThumbnail().getUrl()
									.equals(url)) {

						if (bitmap == null) {
							return;
						}

						updateWidgetWithImage(widget, property);
					}
				}
			});
		}
		updateWidgetWithProperty(property, widget);
	}

	private void loadNextProperty(WidgetState widget, long rotateInterval) {
		if (mApp.widgetDeals.getDeals() != null) {
			widget.mCurrentPosition = ((widget.mCurrentPosition + 1) >= mApp.widgetDeals.getDeals().size()) ? -1
					: widget.mCurrentPosition + 1;
			loadPropertyIntoWidget(widget);
			scheduleRotation(rotateInterval);
		}
	}

	private void loadPreviousProperty(WidgetState widget, long rotateInterval) {
		if (mApp.widgetDeals.getDeals() != null) {

			widget.mCurrentPosition = ((widget.mCurrentPosition - 1) < -1) ? (mApp.widgetDeals.getDeals().size() - 1)
					: (widget.mCurrentPosition - 1);
			loadPropertyIntoWidget(widget);
			scheduleRotation(rotateInterval);
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
	}

	private void startSearchForWidgets() {
		if (mWidgets == null) {
			loadWidgets();
		}

		if (mWidgets.isEmpty()) {
			return;
		}

		int dealsAvailable = loadWidgetDeals();

		if (dealsAvailable == WidgetDeals.WIDGET_DEALS_RESTORED) {
			// updated all widgets around the same time instead of
			// having a different update cycle for each widget 
			// that is added to the home screen
			for (WidgetState state : mWidgets.values()) {
				loadPropertyIntoWidget(state);
			}
			scheduleRotation(ROTATE_INTERVAL);
			startSearch();
		}
		else {
			if (ConfirmationActivity.hasSavedConfirmationData(this)) {
				updateWidgetsWithConfirmation();
			}
			else if (dealsAvailable == WidgetDeals.NO_WIDGET_FILE_EXISTS) {
				updateWidgetsWithText(getString(R.string.tap_to_start_new_search), null, getStartNewSearchIntent());
			}
			else if (dealsAvailable == WidgetDeals.NO_DEALS_EXIST) {
				updateWidgetsWithText(getString(R.string.progress_search_failed),
						getString(R.string.tap_to_start_new_search), getStartNewSearchIntent());
			}
		}
	}

	private PendingIntent getStartNewSearchIntent() {
		Intent newIntent = new Intent(this, SearchActivity.class);
		newIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		return PendingIntent.getActivity(this, 4, newIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	}

	private PendingIntent getRefreshIntent() {
		Intent onClickIntent = new Intent(ExpediaBookingsService.START_SEARCH_ACTION);
		return PendingIntent.getService(this, 4, onClickIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	}

	/*
	 * This method only updates the image in the remote view
	 * when its asynchronously downloaded by the ExpediaBookingsService
	 */
	private void updateWidgetWithImage(WidgetState widget, Property property) {
		RemoteViews rv = new RemoteViews(getPackageName(), R.layout.widget);

		Bitmap bitmap = ImageCache.getImage(property.getThumbnail().getUrl());
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
		String location = mApp.widgetDeals.toSpecifyDistanceFromUser() ? property.getDistanceFromUser().formatDistance(
				this) : mApp.widgetDeals.getSearchParams().getFreeformLocation();
		widgetContents.setTextViewText(R.id.location_text_view, location);

		if (property.getLowestRate().getSavingsPercent() > 0) {
			widgetContents.setTextViewText(R.id.sale_text_view,
					getString(R.string.widget_savings_template, property.getLowestRate().getSavingsPercent() * 100));

			widgetContents.setTextViewText(R.id.price_text_view,
					StrUtils.formatHotelPrice(property.getLowestRate().getDisplayRate()));

			widgetContents.setViewVisibility(R.id.sale_text_view, View.VISIBLE);
			widgetContents.setViewVisibility(R.id.highly_rated_text_view, View.GONE);
			widgetContents.setViewVisibility(R.id.price_per_night_with_no_sale_container, View.GONE);
		}
		else if (property.getLowestRate().getSavingsPercent() == 0 && property.isHighlyRated()) {
			widgetContents.setViewVisibility(R.id.sale_text_view, View.GONE);
			widgetContents.setViewVisibility(R.id.highly_rated_text_view, View.VISIBLE);
			widgetContents.setViewVisibility(R.id.price_per_night_with_no_sale_container, View.GONE);
			widgetContents.setViewVisibility(R.id.price_per_night_container, View.VISIBLE);
			widgetContents.setTextViewText(R.id.price_text_view,
					StrUtils.formatHotelPrice(property.getLowestRate().getDisplayRate()));

		}
		else {
			widgetContents.setViewVisibility(R.id.sale_text_view, View.GONE);
			widgetContents.setViewVisibility(R.id.highly_rated_text_view, View.GONE);
			widgetContents.setViewVisibility(R.id.price_per_night_with_no_sale_container, View.VISIBLE);
			widgetContents.setViewVisibility(R.id.price_per_night_container, View.GONE);
			widgetContents.setTextViewText(R.id.price_text_with_no_sale_view,
					StrUtils.formatHotelPrice(property.getLowestRate().getDisplayRate()));
		}

		Bitmap bitmap = ImageCache.getImage(property.getThumbnail().getUrl());
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

	private void updateWidgetsWithText(String error, String tip, PendingIntent onClickIntent) {
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

		setWidgetContentsVisibility(widgetContents, View.GONE);
		widgetContents.setTextViewText(R.id.widget_error_text_view, error);
		widgetContents.setViewVisibility(R.id.widget_error_text_view, View.VISIBLE);
		widgetContents.setViewVisibility(R.id.loading_text_container, View.VISIBLE);
		widgetContents.setViewVisibility(R.id.loading_expedia_logo_image_view, View.VISIBLE);

		if (tip != null) {
			widgetContents.setTextViewText(R.id.widget_error_tip_text_view, tip);
			widgetContents.setViewVisibility(R.id.widget_error_tip_text_view, View.VISIBLE);
		}

		if (onClickIntent != null) {
			rv.setOnClickPendingIntent(R.id.root, onClickIntent);
		}
		else {
			clearWidgetOnClickIntent(rv);
		}

		updateWidget(widget, rv);
	}

	private void updateWidgetsWithConfirmation() {
		RemoteViews rv = new RemoteViews(getPackageName(), R.layout.widget);
		RemoteViews widgetContents = new RemoteViews(getPackageName(), R.layout.widget_contents);

		rv.removeAllViews(R.id.hotel_info_contents);
		rv.addView(R.id.hotel_info_contents, widgetContents);

		setWidgetPropertyViewVisibility(widgetContents, View.GONE);

		setWidgetContentsVisibility(widgetContents, View.GONE);
		widgetContents.setViewVisibility(R.id.widget_error_text_view, View.GONE);
		widgetContents.setViewVisibility(R.id.loading_text_container, View.VISIBLE);
		widgetContents.setViewVisibility(R.id.enjoy_your_booking_image_view, View.VISIBLE);
		widgetContents.setImageViewBitmap(R.id.enjoy_your_booking_image_view, createEnjoyYourStayImage());
		widgetContents.setTextViewText(R.id.widget_error_tip_text_view, getString(R.string.tap_to_see_booking));
		widgetContents.setViewVisibility(R.id.widget_error_tip_text_view, View.VISIBLE);
		widgetContents.setViewVisibility(R.id.loading_expedia_logo_image_view, View.VISIBLE);

		Intent newIntent = new Intent(this, SearchActivity.class);
		rv.setOnClickPendingIntent(R.id.root,
				PendingIntent.getActivity(this, 4, newIntent, PendingIntent.FLAG_CANCEL_CURRENT));

		for (WidgetState widget : mWidgets.values()) {
			updateWidget(widget, rv);
		}
	}

		private static final String FORMAT_HEADER = "MMM d";

	private void updateWidgetBranding(WidgetState widget) {
		final RemoteViews widgetContents = new RemoteViews(getPackageName(), R.layout.widget_contents);
		RemoteViews rv = new RemoteViews(getPackageName(), R.layout.widget);

		// add contents to the parent view to give the fade-in animation
		rv.removeAllViews(R.id.hotel_info_contents);
		rv.addView(R.id.hotel_info_contents, widgetContents);

		setWidgetContentsVisibility(widgetContents, View.VISIBLE);

		setWidgetPropertyViewVisibility(widgetContents, View.GONE);

		setBrandingViewVisibility(widgetContents, View.VISIBLE);

		if (mApp.widgetDeals.getMaxPercentSavings() == 0.0) {
			widgetContents.setViewVisibility(R.id.branding_savings_container, View.GONE);
		}
		else {
			widgetContents.setViewVisibility(R.id.branding_savings_container, View.VISIBLE);
			widgetContents.setTextViewText(R.id.branding_savings_container,
					getString(R.string.save_upto_template, mApp.widgetDeals.getMaxPercentSavings() * 100));
		}

		widgetContents.setTextViewText(R.id.branding_location_text_view, mApp.widgetDeals.getSearchParams()
				.getFreeformLocation());

		String brandingTitle = null;
		if (DateUtils.isToday(mApp.widgetDeals.getSearchParams().getCheckInDate().getTimeInMillis())) {
			brandingTitle = getString(R.string.tonight_top_deals);
		}
		else {
			brandingTitle = getString(R.string.top_deals_template, android.text.format.DateFormat.format(FORMAT_HEADER,
					mApp.widgetDeals.getSearchParams().getCheckInDate()));
		}

		widgetContents.setTextViewText(R.id.branding_title_text_view, brandingTitle);

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

	private class WidgetState {
		Integer appWidgetIdInteger;
		int mCurrentPosition = -1;
	}

	private void setupOnClickIntentForWidget(WidgetState widget, Property property, RemoteViews rv) {
		Intent onClickIntent = new Intent(this.getApplicationContext(), HotelActivity.class);
		onClickIntent.putExtra(Codes.SESSION, mApp.widgetDeals.getSession().toJson().toString());
		onClickIntent.putExtra(Codes.APP_WIDGET_ID, widget.appWidgetIdInteger);
		onClickIntent.putExtra(Codes.SEARCH_PARAMS, mApp.widgetDeals.getSearchParams().toJson().toString());
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
		widgetContents.setViewVisibility(R.id.price_per_night_with_no_sale_container, visibility);
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
		widgetContents.setViewVisibility(R.id.widget_error_text_view, visibility);
		widgetContents.setViewVisibility(R.id.loading_text_container, visibility);
		widgetContents.setViewVisibility(R.id.widget_error_tip_text_view, visibility);
		widgetContents.setViewVisibility(R.id.enjoy_your_booking_image_view, visibility);
		widgetContents.setViewVisibility(R.id.loading_expedia_logo_image_view, visibility);
	}

	private static final float TEXT_SIZE_IN_DIP = 30.0f;
	private static final float ROUND_UP = 0.5f;
	private static final float PADDING = 5.0f;
	
	private Bitmap createEnjoyYourStayImage() {
		
		// if the user's default language is english, 
		// display the enjoy your stay drawable as thats optimal
		if(Locale.getDefault().getLanguage().equals(Locale.ENGLISH.getLanguage())) {
			return BitmapFactory.decodeResource(getResources(), R.drawable.widget_enjoy_your_stay);
		}

		Paint paint = new Paint();
		Rect textBounds = new Rect();
		float scale = this.getResources().getDisplayMetrics().density;
		float paddingInPx = scale * PADDING;
		String enjoyYourStayString = getString(R.string.enjoy_your_stay);

		Typeface customFont = Typeface.createFromAsset(this.getAssets(), "fonts/HoneyScript-SemiBold.ttf");
		paint.setAntiAlias(true);
		paint.setSubpixelText(true);
		paint.setTypeface(customFont);
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(getResources().getColor(R.color.widget_text_color));
		paint.setShadowLayer(0.1f, 0f, 1f, getResources().getColor(android.R.color.white));
		paint.setTextSize((int) (scale * TEXT_SIZE_IN_DIP + ROUND_UP));
		paint.setTextAlign(Align.LEFT);

		// get the dimensions of the minimum rectangle required to cover the text
		paint.getTextBounds(enjoyYourStayString, 0, enjoyYourStayString.length(), textBounds);

		// include a padding around the text to ensure that we're not chopping off the
		// edges of the text (it still may happen if the text is long enough)
		Bitmap bitmap = Bitmap.createBitmap((int) (textBounds.width() + 2 * paddingInPx),
				(int) (textBounds.height() + 2 * paddingInPx), Bitmap.Config.ARGB_8888);
		Canvas myCanvas = new Canvas(bitmap);

		myCanvas.drawText(enjoyYourStayString, paddingInPx, textBounds.height() - paddingInPx, paint);
		return bitmap;
	}
}
