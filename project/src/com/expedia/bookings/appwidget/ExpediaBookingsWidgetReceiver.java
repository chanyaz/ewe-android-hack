package com.expedia.bookings.appwidget;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
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
import com.mobiata.hotellib.data.Money;
import com.mobiata.hotellib.data.Property;

public class ExpediaBookingsWidgetReceiver extends BroadcastReceiver {

	public static final String LOAD_PROPERTY_ACTION = "com.expedia.bookings.LOAD_PROPERTY";
	public static final String NEXT_PROPERTY_ACTION = "com.expedia.bookings.NEXT_PROPERTY";
	public static final String PREV_PROPERTY_ACTION = "com.expedia.bookings.PREV_PROPERTY";

	private Context mContext;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("ExpediaBookings widget just received an update regarding the change in parameters.");
		mContext = context;
		
		try {
			if(intent.getAction().equals(LOAD_PROPERTY_ACTION)) {
				String error = intent.getStringExtra(Codes.SEARCH_ERROR);
				if(error != null) {
					updateWidgetWithText(error, true);
					return;
				}
				
				Property property = new Property();
				property.fromJson(new JSONObject(intent.getStringExtra(Codes.PROPERTY)));
				updateWidgets(property, intent);
			} else if(intent.getAction().equals(ExpediaBookingsService.START_CLEAN_SEARCH_ACTION)) {
				updateWidgetWithText(mContext.getString(R.string.loading_hotels), false);
			}
		} catch (JSONException e) {
			// TODO
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////////

	private void updateWidgets(final Property property, Intent intent) {
		final RemoteViews widgetContents = new RemoteViews(mContext.getPackageName(), R.layout.widget_contents);
		RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget);
		
		// add contents to the parent view to give the fade-in animation
		rv.removeAllViews(R.id.hotel_info_contents);
		rv.addView(R.id.hotel_info_contents, widgetContents);

		widgetContents.setViewVisibility(R.id.widget_contents_container, View.VISIBLE);
		widgetContents.setViewVisibility(R.id.navigation_container, View.VISIBLE);

		widgetContents.setTextViewText(R.id.hotel_name_text_view, property.getName());
		widgetContents.setTextViewText(R.id.location_text_view, intent.getStringExtra(Codes.PROPERTY_LOCATION));
		widgetContents.setTextViewText(R.id.price_text_view, 
				property.getLowestRate().getDisplayRate().getFormattedMoney(Money.F_NO_DECIMAL + Money.F_ROUND_DOWN));
		
		if(property.getLowestRate().getSavingsPercent() == 0) {
			widgetContents.setViewVisibility(R.id.sale_text_view, View.GONE);
			widgetContents.setInt(R.id.price_per_night_container, "setBackgroundResource", R.drawable.widget_price_bg_no_sale);
		} else {
			widgetContents.setTextViewText(R.id.sale_text_view, mContext.getString(R.string.widget_savings_template, property.getLowestRate().getSavingsPercent() * 100));
			widgetContents.setInt(R.id.price_per_night_container, "setBackgroundResource", R.drawable.widget_price_bg);
			widgetContents.setViewVisibility(R.id.sale_text_view, View.VISIBLE);
		}
		
		Bitmap bitmap = ImageCache.getImage(property.getThumbnail().getUrl());
		if(bitmap == null) {
			widgetContents.setImageViewResource(R.id.hotel_image_view, R.drawable.widget_thumbnail_background);
		} else {
			widgetContents.setImageViewBitmap(R.id.hotel_image_view, bitmap);
		}

		Intent prevIntent = new Intent(PREV_PROPERTY_ACTION);
		prevIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Intent nextIntent = new Intent(NEXT_PROPERTY_ACTION);
		nextIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		widgetContents.setOnClickPendingIntent(R.id.prev_hotel_btn, PendingIntent.getService(mContext, 0, prevIntent, 0));
		widgetContents.setOnClickPendingIntent(R.id.next_hotel_btn, PendingIntent.getService(mContext, 1, nextIntent, 0));

		Intent onClickIntent = new Intent(mContext, HotelActivity.class);
		onClickIntent.fillIn(intent, Intent.FLAG_ACTIVITY_CLEAR_TOP);
		rv.setOnClickPendingIntent(R.id.root, PendingIntent.getActivity(mContext, 3, onClickIntent, PendingIntent.FLAG_CANCEL_CURRENT));
		
		widgetContents.setViewVisibility(R.id.loading_text_view, View.GONE);
		widgetContents.setViewVisibility(R.id.loading_text_container, View.GONE);
		widgetContents.setViewVisibility(R.id.refresh_text_view, View.GONE);
		
		updateWidget(rv);
	}

	private void updateWidgetWithText(String error, boolean refreshOnClick) {
		RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget);
		RemoteViews widgetContents = new RemoteViews(mContext.getPackageName(), R.layout.widget_contents);
		
		rv.removeAllViews(R.id.hotel_info_contents);
		rv.addView(R.id.hotel_info_contents, widgetContents);
		
		widgetContents.setTextViewText(R.id.loading_text_view, error);
		widgetContents.setViewVisibility(R.id.loading_text_view, View.VISIBLE);
		widgetContents.setViewVisibility(R.id.loading_text_container, View.VISIBLE);
		widgetContents.setViewVisibility(R.id.widget_contents_container, View.GONE);
		widgetContents.setViewVisibility(R.id.refresh_text_view, View.GONE);
		rv.setViewVisibility(R.id.navigation_container, View.GONE);
		
		if(refreshOnClick) {
			Intent onClickIntent = new Intent(ExpediaBookingsService.START_CLEAN_SEARCH_ACTION);
			rv.setOnClickPendingIntent(R.id.root, PendingIntent.getBroadcast(mContext, 0, onClickIntent, PendingIntent.FLAG_CANCEL_CURRENT));
			rv.setViewVisibility(R.id.refresh_text_view, View.VISIBLE);
		}
		updateWidget(rv);
	}
	
	private void updateWidget(final RemoteViews rv) {
		AppWidgetManager gm = AppWidgetManager.getInstance(mContext);
		int[] appWidgetIds = gm.getAppWidgetIds(new ComponentName(mContext, ExpediaBookingsWidgetProvider.class));
		for (int appWidgetId : appWidgetIds) {
			gm.updateAppWidget(appWidgetId, rv);
		}
	}
}
