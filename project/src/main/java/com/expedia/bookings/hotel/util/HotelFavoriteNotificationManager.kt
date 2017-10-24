package com.expedia.bookings.hotel.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.expedia.bookings.R
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import com.expedia.bookings.data.Codes
import com.expedia.bookings.hotel.deeplink.HotelExtras
import com.expedia.bookings.hotel.deeplink.HotelIntentBuilder
import com.expedia.bookings.utils.HotelsV2DataUtil
import com.expedia.bookings.utils.navigation.NavUtils
import com.expedia.ui.HotelActivity
import com.squareup.phrase.Phrase

class HotelFavoriteNotificationManager {

    companion object {
        private val FAVORITE_NOTIFICATION_ID = 4000

        fun sendPush(context: Context, cacheItem: HotelFavoriteCache.HotelCacheItem) {

            if (shouldSendPush(cacheItem)) {
                val content = getNotificationContent(context, cacheItem)
                val title = getNotificationTitle(context, cacheItem)

                val builder = NotificationCompat.Builder(context)
                builder.setSmallIcon(R.drawable.brand_logo)
                        .setColor(ContextCompat.getColor(context, R.color.hotels_primary_color))
                        .setContentTitle(title)
                        .setTicker(title)
                        .setContentText(content)
                        .setStyle(NotificationCompat.BigTextStyle().bigText(content))
                        .setLocalOnly(true)
                        .setAutoCancel(true)
                        .setContentIntent(getPendingIntent(context, cacheItem))

                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(generateTag(cacheItem), FAVORITE_NOTIFICATION_ID, builder.build())
            }
        }

        private fun getPendingIntent(context: Context, cacheItem: HotelFavoriteCache.HotelCacheItem): PendingIntent {
            var intent = buildHotelDetailIntent(context, cacheItem.hotelId)
            if (intent == null) {
                intent = NavUtils.getLaunchIntent(context)
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }

            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)    //,0
            return pendingIntent
        }

        private fun buildHotelDetailIntent(context: Context, hotelId: String): Intent? {
            val checkIn = HotelFavoriteCache.getCheckInDate(context)
            val checkOut = HotelFavoriteCache.getCheckOutDate(context)

            if (checkIn != null && checkOut != null) {
                val params = HotelIntentBuilder().buildParams(context, checkIn, checkOut, hotelId)
                val extras = Bundle()
                val gson = HotelsV2DataUtil.generateGson()
                extras.putString(HotelExtras.EXTRA_HOTEL_SEARCH_PARAMS, gson.toJson(params))
                extras.putBoolean(Codes.TAG_EXTERNAL_SEARCH_PARAMS, true)
                val intent = Intent()
                intent.setClass(context, HotelActivity::class.java)
                intent.putExtras(extras)
                return intent
            }
            return null
        }

        private fun getNotificationTitle(context: Context, cacheItem: HotelFavoriteCache.HotelCacheItem): String {
            return if (cacheItem.isPriceDown()) {
                context.getString(R.string.hotel_price_drop_title)
            } else {
                context.getString(R.string.hotel_price_increase_title)
            }
        }

        private fun getNotificationContent(context: Context, cacheItem: HotelFavoriteCache.HotelCacheItem): String {
            if (cacheItem.isPriceDown()) {
                return Phrase.from(context, R.string.notification_price_down_TEMPLATE)
                        .put("hotelname", cacheItem.hotelName).put("price", cacheItem.rate.formattedRate())
                        .format().toString()
            }

            return Phrase.from(context, R.string.notification_price_up_TEMPLATE)
                    .put("hotelname", cacheItem.hotelName).put("price", cacheItem.rate.formattedRate())
                    .format().toString()
        }

        private fun shouldSendPush(cacheItem: HotelFavoriteCache.HotelCacheItem): Boolean {

            if (cacheItem.oldRate == null) {
                return false
            }

            return !cacheItem.isSamePrice()
        }

        private fun generateTag(cacheItem: HotelFavoriteCache.HotelCacheItem): String = "FAVORITE_${cacheItem.hotelId}"

    }
}