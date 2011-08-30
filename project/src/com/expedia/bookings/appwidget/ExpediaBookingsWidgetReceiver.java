package com.expedia.bookings.appwidget;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.RemoteViews;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.HotelActivity;
import com.mobiata.android.ImageCache;
import com.mobiata.android.Log;
import com.mobiata.hotellib.data.Codes;
import com.mobiata.hotellib.data.Property;
import com.mobiata.hotellib.utils.StrUtils;

public class ExpediaBookingsWidgetReceiver extends BroadcastReceiver {

	public static final String LOAD_PROPERTY_ACTION = "com.expedia.bookings.LOAD_PROPERTY";
	public static final String NEXT_PROPERTY_ACTION = "com.expedia.bookings.NEXT_PROPERTY";
	public static final String ROTATE_PROPERTY_ACTION = "com.expedia.bookings.ROTATE_PROPERTY";
	public static final String PREV_PROPERTY_ACTION = "com.expedia.bookings.PREV_PROPERTY";
	public static final String LOAD_BRANDING_ACTION = "com.expedia.bookings.LOAD_BRANDING";

	private Context mContext;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("ExpediaBookings widget just received an update regarding the change in parameters.");
		mContext = context;

		try {
			if (intent.getAction().equals(LOAD_BRANDING_ACTION)) {
				updateWidgetBranding(intent);
			}
			else if (intent.getAction().equals(LOAD_PROPERTY_ACTION)) {
				String error = intent.getStringExtra(Codes.SEARCH_ERROR);
				if (error != null) {
					boolean showBranding = intent.getBooleanExtra(Codes.SHOW_BRANDING, false);
					updateWidgetWithText(intent, error, true, showBranding);
					return;
				}

				Property property = new Property();
				property.fromJson(new JSONObject(intent.getStringExtra(Codes.PROPERTY)));
				updateWidgets(property, intent);
			}
			else if (intent.getAction().equals(ExpediaBookingsService.START_CLEAN_SEARCH_ACTION)) {
				updateWidgetWithText(intent, mContext.getString(R.string.loading_hotels), false);
			}
		}
		catch (JSONException e) {
			// TODO
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////////

	private void updateWidgetBranding(Intent intent) {
		final RemoteViews widgetContents = new RemoteViews(mContext.getPackageName(), R.layout.widget_contents);
		RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget);
		Integer appWidgetIntegerId = new Integer(intent.getIntExtra(Codes.APP_WIDGET_ID, -1));

		// add contents to the parent view to give the fade-in animation
		rv.removeAllViews(R.id.hotel_info_contents);
		rv.addView(R.id.hotel_info_contents, widgetContents);

		setWidgetContentsVisibility(widgetContents, View.VISIBLE);

		setWidgetPropertyViewVisibility(widgetContents, View.GONE);

		setBrandingViewVisibility(widgetContents, View.VISIBLE);

		String brandingSavings = intent.getStringExtra(Codes.BRANDING_SAVINGS);
		if (brandingSavings == null) {
			widgetContents.setViewVisibility(R.id.branding_savings_container, View.GONE);
		}
		else {
			widgetContents.setViewVisibility(R.id.branding_savings_container, View.VISIBLE);
			widgetContents.setTextViewText(R.id.branding_savings_container,
					mContext.getString(R.string.save_upto_template, intent.getStringExtra(Codes.BRANDING_SAVINGS)));
		}

		String distanceOfMaxSavingsFromUser = intent.getStringExtra(Codes.DISTANCE_OF_MAX_SAVINGS);
		if (distanceOfMaxSavingsFromUser != null) {
			widgetContents.setTextViewText(R.id.branding_location_text_view, distanceOfMaxSavingsFromUser);
		}
		else {
			widgetContents.setTextViewText(R.id.branding_location_text_view,
					intent.getStringExtra(Codes.PROPERTY_LOCATION_PREFIX + appWidgetIntegerId));
		}

		widgetContents.setTextViewText(R.id.branding_title_text_view, intent.getStringExtra(Codes.BRANDING_TITLE));

		Integer appWidgetIdInteger = new Integer(intent.getIntExtra(Codes.APP_WIDGET_ID, -1));
		Intent prevIntent = new Intent(PREV_PROPERTY_ACTION);
		prevIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		prevIntent.fillIn(intent, 0);

		Intent nextIntent = new Intent(NEXT_PROPERTY_ACTION);
		nextIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		nextIntent.fillIn(intent, 0);

		clearWidgetOnClickIntent(rv);

		widgetContents.setOnClickPendingIntent(R.id.prev_hotel_btn, PendingIntent.getService(mContext,
				appWidgetIdInteger.intValue() + 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT));
		widgetContents.setOnClickPendingIntent(R.id.next_hotel_btn, PendingIntent.getService(mContext,
				appWidgetIdInteger.intValue() + 1, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT));

		setWidgetLoadingTextVisibility(widgetContents, View.GONE);

		updateWidget(intent, rv);
	}

	private void updateWidgets(final Property property, Intent intent) {
		final RemoteViews widgetContents = new RemoteViews(mContext.getPackageName(), R.layout.widget_contents);
		RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget);
		Integer appWidgetIntegerId = new Integer(intent.getIntExtra(Codes.APP_WIDGET_ID, -1));

		// add contents to the parent view to give the fade-in animation
		rv.removeAllViews(R.id.hotel_info_contents);
		rv.addView(R.id.hotel_info_contents, widgetContents);

		setWidgetContentsVisibility(widgetContents, View.VISIBLE);

		setBrandingViewVisibility(widgetContents, View.GONE);

		setWidgetPropertyViewVisibility(widgetContents, View.VISIBLE);

		widgetContents.setTextViewText(R.id.hotel_name_text_view, property.getName());
		widgetContents.setTextViewText(R.id.location_text_view,
				intent.getStringExtra(Codes.PROPERTY_LOCATION_PREFIX + appWidgetIntegerId));
		widgetContents.setTextViewText(R.id.price_text_view,
				StrUtils.formatHotelPrice(property.getLowestRate().getDisplayRate()));

		if (property.getLowestRate().getSavingsPercent() > 0) {
			widgetContents.setTextViewText(R.id.sale_text_view, mContext.getString(R.string.widget_savings_template,
					property.getLowestRate().getSavingsPercent() * 100));
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

		Bitmap bitmap = ImageCache.getImage(property.getThumbnail().getUrl());
		if (bitmap == null) {
			widgetContents.setImageViewResource(R.id.hotel_image_view, R.drawable.widget_thumbnail_background);
		}
		else {
			widgetContents.setImageViewBitmap(R.id.hotel_image_view, bitmap);
		}

		Integer appWidgetIdInteger = new Integer(intent.getIntExtra(Codes.APP_WIDGET_ID, -1));
		Intent prevIntent = new Intent(PREV_PROPERTY_ACTION);
		prevIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		prevIntent.fillIn(intent, 0);

		Intent nextIntent = new Intent(NEXT_PROPERTY_ACTION);
		nextIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		nextIntent.fillIn(intent, 0);

		widgetContents.setOnClickPendingIntent(R.id.prev_hotel_btn, PendingIntent.getService(mContext,
				appWidgetIdInteger.intValue() + 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT));
		widgetContents.setOnClickPendingIntent(R.id.next_hotel_btn, PendingIntent.getService(mContext,
				appWidgetIdInteger.intValue() + 1, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT));

		Intent onClickIntent = new Intent(mContext, HotelActivity.class);
		onClickIntent.fillIn(intent, Intent.FLAG_ACTIVITY_CLEAR_TOP);

		rv.setOnClickPendingIntent(R.id.root, PendingIntent.getActivity(mContext, appWidgetIdInteger.intValue() + 3,
				onClickIntent, PendingIntent.FLAG_UPDATE_CURRENT));

		setWidgetLoadingTextVisibility(widgetContents, View.GONE);

		updateWidget(intent, rv);
	}

	public void updateWidgetWithText(Intent intent, String error, boolean refreshOnClick) {
		updateWidgetWithText(intent, error, refreshOnClick, false);
	}

	private void updateWidgetWithText(Intent intent, String error, boolean refreshOnClick, boolean showBranding) {
		RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget);
		RemoteViews widgetContents = new RemoteViews(mContext.getPackageName(), R.layout.widget_contents);

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
			Integer appWidgetIdInteger = new Integer(intent.getIntExtra(Codes.APP_WIDGET_ID, -1));
			Intent onClickIntent = new Intent(ExpediaBookingsService.START_CLEAN_SEARCH_ACTION);
			onClickIntent.fillIn(intent, 0);
			rv.setOnClickPendingIntent(R.id.root, PendingIntent.getBroadcast(mContext,
					appWidgetIdInteger.intValue() + 4, onClickIntent, PendingIntent.FLAG_UPDATE_CURRENT));
			rv.setViewVisibility(R.id.refresh_text_view, View.VISIBLE);
		}
		else {
			clearWidgetOnClickIntent(rv);
		}

		updateWidget(intent, rv);
	}

	// clear out the on-click intent on the widget by updating the widget
	// with an empty intent that goes into ether.
	private void clearWidgetOnClickIntent(RemoteViews rv) {
		rv.setOnClickPendingIntent(R.id.root,
				PendingIntent.getActivity(mContext, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT));
	}

	private void updateWidget(Intent intent, final RemoteViews rv) {
		AppWidgetManager gm = AppWidgetManager.getInstance(mContext);
		gm.updateAppWidget(intent.getIntExtra(Codes.APP_WIDGET_ID, -1), rv);
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
