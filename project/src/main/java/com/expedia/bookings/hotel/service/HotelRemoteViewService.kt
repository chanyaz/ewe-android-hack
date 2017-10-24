package com.expedia.bookings.hotel.service

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.expedia.bookings.R
import com.expedia.bookings.hotel.util.HotelFavoriteCache

class HotelRemoteViewService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return Factory(applicationContext)
    }

    class Factory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {
        private val hotelData = ArrayList<HotelFavoriteCache.HotelCacheItem>()

        override fun onCreate() {
            //nothing
        }

        override fun getLoadingView(): RemoteViews? {
            return null
        }

        override fun getItemId(position: Int): Long {
            return 1 //todo what goes here?
        }

        override fun onDataSetChanged() {
            Log.v("HotelRemoteViewService", ": onDataSetChanged")
            val favoriteIds = HotelFavoriteCache.getFavorites(context)
            for (id in favoriteIds) {
                val item = HotelFavoriteCache.getFavoriteHotelData(context, id)
                item?.let { hotelData.add(item) }
            }
        }

        override fun hasStableIds(): Boolean {
            return true //todo is this true
        }

        override fun getViewAt(position: Int): RemoteViews {
            Log.v("HotelRemoteViewService", ": getViewAt:${position}")

            val hotel = hotelData[position]
            val rv = RemoteViews(context.getPackageName(), R.layout.hotel_price_appwidget_cell);
            rv.setTextViewText(R.id.hotel_appwidget_name, hotel.hotelName)
            rv.setTextViewText(R.id.hotel_appwidget_new_price, hotel.rate.amount.toString()) //todo fix formatting
            rv.setTextViewText(R.id.hotel_appwidget_old_price, (hotel.oldRate?.amount ?: hotel.rate.amount).toString())

            return rv
        }

        override fun getCount(): Int {
            return hotelData.size
        }

        override fun getViewTypeCount(): Int {
            return 1
        }

        override fun onDestroy() {
            //nothing
        }

    }
}