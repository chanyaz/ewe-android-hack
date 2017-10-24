package com.expedia.bookings.hotel.service

import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.hotel.util.HotelFavoriteCache
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.widget.priceFormatter
import com.mobiata.android.text.StrikethroughTagHandler
import java.math.BigDecimal

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

            updatePrices(rv, hotel.rate, hotel.oldRate ?: hotel.rate)
            setPriceDeltaIcon(rv, hotel.oldRate?.amount, hotel.rate.amount)
            setUrgency(rv, hotel.roomsLeft.toInt())

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

        private fun updatePrices(rv: RemoteViews, newRate: HotelFavoriteCache.HotelRate,
                                 oldRate:  HotelFavoriteCache.HotelRate?) {
            val newMoney = Money(java.lang.Float.toString(newRate.amount), newRate.currency)
            rv.setTextViewText(R.id.hotel_appwidget_new_price, newMoney.formattedMoney)

            if (oldRate != null) {
                val oldMoney = Money(BigDecimal(oldRate.amount.toDouble()), oldRate.currency)
                val oldPrice = oldMoney?.formattedPrice
                if (oldPrice != null)  {
                    val oldText = HtmlCompat.fromHtml(context.getString(R.string.strike_template,
                            oldPrice), null, StrikethroughTagHandler())
                    rv.setTextViewText(R.id.hotel_appwidget_old_price, oldText)
                }
                else {
                    rv.setTextViewText(R.id.hotel_appwidget_old_price, newMoney.formattedMoney)

                }
            }
        }

        private fun setPriceDeltaIcon(rv: RemoteViews, oldPrice: Float?, newPrice: Float) {
            rv.setImageViewResource(R.id.hotel_appwidget_price_icon,
                    R.drawable.neutral_price_change)
            rv.setTextColor(R.id.hotel_appwidget_new_price,
                    ContextCompat.getColor(context, R.color.light_text_color))
            oldPrice?.let {
                if (oldPrice < newPrice) {
                    rv.setTextColor(R.id.hotel_appwidget_new_price,
                            ContextCompat.getColor(context, R.color.error_red))
                    rv.setImageViewResource(R.id.hotel_appwidget_price_icon, R.drawable.red_arrow)
                } else if (oldPrice > newPrice) {
                    rv.setTextColor(R.id.hotel_appwidget_new_price,
                            ContextCompat.getColor(context,R.color.success_green))
                    rv.setImageViewResource(R.id.hotel_appwidget_price_icon, R.drawable.green_arrow)
                }
            }
        }

        private fun setUrgency(rv: RemoteViews, roomsLeft: Int) {
            if (roomsLeft > 0 && roomsLeft <= 5) {
                rv.setViewVisibility(R.id.hotel_appwidget_urgency_container, View.VISIBLE)
                rv.setTextViewText(R.id.hotel_appwidget_urgency, "$roomsLeft Rooms Left!")
            } else {
                rv.setViewVisibility(R.id.hotel_appwidget_urgency_container, View.INVISIBLE)
            }
        }
    }
}