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
	public static final String LOAD_IMAGE_FOR_PROPERTY_ACTION = "com.expedia.bookings.LOAD_IMAGE_FOR_PROPERTY";
	private static final int MAX_RESULTS = 5;

	private Context mContext;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("ExpediaBookings widget just received an update regarding the change in parameters.");
		mContext = context;
		
		try {
			if(intent.getAction().equals(LOAD_PROPERTY_ACTION)) {
				String error = intent.getStringExtra(Codes.SEARCH_ERROR);
				if(error != null) {
					updateWidgetWithText(error);
					return;
				}
				
				Property property = new Property();
				property.fromJson(new JSONObject(intent.getStringExtra(Codes.PROPERTY)));
				updateWidgets(property, intent);
			} else if(intent.getAction().equals(LOAD_IMAGE_FOR_PROPERTY_ACTION)) {
				Property property = new Property();
				property.fromJson(new JSONObject(intent.getStringExtra(Codes.PROPERTY)));
				updateLoadingImage(property);
			} else if(intent.getAction().equals(ExpediaBookingsService.START_CLEAN_SEARCH_ACTION)) {
				updateWidgetWithText(mContext.getString(R.string.loading_hotels));
			}
		} catch (JSONException e) {
			// TODO
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////////

	private void updateWidgets(final Property property, Intent intent) {
		final RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget);
		
		rv.setViewVisibility(R.id.widget_contents_container, View.VISIBLE);
		rv.setTextViewText(R.id.hotel_name_text_view, property.getName());
		
		rv.setTextViewText(R.id.location_text_view, intent.getStringExtra(Codes.PROPERTY_LOCATION));

		rv.setTextViewText(R.id.price_text_view, 
				property.getLowestRate().getDisplayRate().getFormattedMoney(Money.F_NO_DECIMAL + Money.F_ROUND_DOWN));
		
		if(property.getLowestRate().getSavingsPercent() == 0) {
			rv.setViewVisibility(R.id.sale_text_view, View.GONE);
			rv.setInt(R.id.price_per_night_container, "setBackgroundResource", R.drawable.widget_price_bg_no_sale);
		} else {
			rv.setTextViewText(R.id.sale_text_view, mContext.getString(R.string.widget_savings_template, property.getLowestRate().getSavingsPercent() * 100));
			rv.setInt(R.id.price_per_night_container, "setBackgroundResource", R.drawable.widget_price_bg);
			rv.setViewVisibility(R.id.sale_text_view, View.VISIBLE);
		}
		
		Bitmap bitmap = ImageCache.getImage(property.getThumbnail().getUrl());
		if(bitmap == null) {
			rv.setImageViewResource(R.id.hotel_image_view, R.drawable.widget_thumbnail_background);
		} else {
			rv.setImageViewBitmap(R.id.hotel_image_view, bitmap);
		}

		Intent prevIntent = new Intent(PREV_PROPERTY_ACTION);
		prevIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Intent nextIntent = new Intent(NEXT_PROPERTY_ACTION);
		nextIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		rv.setOnClickPendingIntent(R.id.prev_hotel_btn, PendingIntent.getService(mContext, 0, prevIntent, 0));
		rv.setOnClickPendingIntent(R.id.next_hotel_btn, PendingIntent.getService(mContext, 1, nextIntent, 0));

		
		Intent onClickIntent = new Intent(mContext, HotelActivity.class);
		onClickIntent.fillIn(intent, Intent.FLAG_ACTIVITY_CLEAR_TOP);
		rv.setOnClickPendingIntent(R.id.root, PendingIntent.getActivity(mContext, 3, onClickIntent, PendingIntent.FLAG_CANCEL_CURRENT));
		
		rv.setViewVisibility(R.id.loading_text_view, View.GONE);
		
		updateWidget(rv);
	}

	private void updateWidgetWithText(String error) {
		RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget);
		
		rv.setTextViewText(R.id.loading_text_view, error);
		rv.setViewVisibility(R.id.loading_text_view, View.VISIBLE);
		rv.setViewVisibility(R.id.widget_contents_container, View.GONE);
		updateWidget(rv);
	}
	
	private void updateLoadingImage(Property property) {
		RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget);
		rv.setImageViewBitmap(R.id.hotel_image_view, ImageCache.getImage(property.getThumbnail().getUrl()));
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
