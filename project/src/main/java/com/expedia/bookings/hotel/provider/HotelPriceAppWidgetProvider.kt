package com.expedia.bookings.hotel.provider

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.util.Log
import com.expedia.bookings.hotel.util.HotelAppWidgetUtil


class HotelPriceAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        Log.v("HotelPriceAppWidgetProvider", ": onUpdate")

        for (id in appWidgetIds) {
            HotelAppWidgetUtil.scheduleUpdate(context, id)
        }
    }
}