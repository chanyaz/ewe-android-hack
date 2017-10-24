package com.expedia.bookings.hotel.service

import android.app.job.JobParameters
import android.app.job.JobService
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.expedia.bookings.R
import com.expedia.bookings.hotel.provider.HotelPriceAppWidgetProvider
import com.expedia.bookings.hotel.util.HotelFavoriteCache
import com.expedia.bookings.hotel.util.HotelInfoManager
import com.expedia.bookings.utils.DateFormatUtils
import com.expedia.bookings.utils.Ui
import org.joda.time.LocalDate
import javax.inject.Inject


class HotelPriceJobService : JobService() {
    lateinit var hotelInfoManager: HotelInfoManager
        @Inject set

    var hotelsRefreshed = 0


    override fun onStopJob(params: JobParameters?): Boolean {
        Log.v("HotelPriceJobService", ": onStopJob")
        hotelInfoManager.clearSubscriptions()
        return false
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        val app = Ui.getApplication(applicationContext)
        app.defaultHotelComponents()
        app.hotelComponent().inject(this)
        hotelsRefreshed = 0
        Log.v("HotelPriceJobService", ": onStartJob")

        val favorites = HotelFavoriteCache.getFavorites(applicationContext)

        hotelInfoManager.offerSuccessSubject.subscribe { response ->
            HotelFavoriteCache.saveHotelData(applicationContext, response)
            hotelsRefreshed++

            if (hotelsRefreshed == favorites.size) {
                sendViews()
                jobFinished(params, false)
            }
            Log.v("HotelPriceJobService", ": ${response.hotelId}")
        }

        hotelInfoManager.infoSuccessSubject.subscribe { response ->
            HotelFavoriteCache.saveHotelData(applicationContext, response)
            hotelsRefreshed++

            if (hotelsRefreshed == favorites.size) {
                sendViews()
                HotelFavoriteCache.saveLastUpdateTime(applicationContext, System.currentTimeMillis())
                jobFinished(params, false)
            }
            Log.v("HotelPriceJobService", ": ${response.hotelId}")
        }

        val checkInDate = HotelFavoriteCache.getCheckInDate(applicationContext)
        val checkOutDate = HotelFavoriteCache.getCheckOutDate(applicationContext)

        if (checkInDate != null && checkOutDate != null) {
            hotelInfoManager.fetchOffers(checkInDate, checkOutDate, favorites)
        } else {
            hotelInfoManager.fetchDatelessInfo(favorites)
        }
        return true
    }


    private fun sendViews() {
        Log.v("HotelPriceJobService", ": sendViews")
        val remoteViews = RemoteViews(applicationContext.packageName, R.layout.hotel_price_appwidget_layout)
        val intent = Intent(applicationContext, HotelRemoteViewService::class.java)

        remoteViews.setRemoteAdapter(R.id.hotel_price_appwidget_list, intent);
        remoteViews.setEmptyView(R.id.hotel_price_appwidget_list, R.id.hotel_price_appwidget_empty_view)
        updateDateView(remoteViews)
        updateLastUpdated(remoteViews)

        val thisWidget = ComponentName(this, HotelPriceAppWidgetProvider::class.java)
        val manager = AppWidgetManager.getInstance(this)
        manager.updateAppWidget(thisWidget, remoteViews)
        manager.notifyAppWidgetViewDataChanged(intent.getIntExtra("PRICE_APP_WIDGET_KEY", 0), R.id.hotel_price_appwidget_list)
    }

    private fun updateDateView(rv: RemoteViews) {
        val checkIn = HotelFavoriteCache.getCheckInDate(applicationContext)
        val checkOut = HotelFavoriteCache.getCheckOutDate(applicationContext)

        if (checkIn != null && checkOut != null) {
            rv.setViewVisibility(R.id.hotel_appwidget_header_dates, View.VISIBLE)
            val dates = DateFormatUtils.formatHotelsV2DateRange(applicationContext, checkIn, checkOut)
            rv.setTextViewText(R.id.hotel_appwidget_header_dates, dates)
        } else {
            rv.setViewVisibility(R.id.hotel_appwidget_header_dates, View.GONE)
        }
    }

    private fun updateLastUpdated(rv: RemoteViews) {
        val lastUpdated = HotelFavoriteCache.getLastUpdated(applicationContext)
        if (lastUpdated != null) {
            rv.setViewVisibility(R.id.hotel_price_appwidget_last_updated, View.VISIBLE)
            val localDate = LocalDate(lastUpdated)
            val date = DateFormatUtils.formatLocalDateToShortDayAndDate(localDate)
            rv.setTextViewText(R.id.hotel_price_appwidget_last_updated, "Last Updated: $date")
        } else {
            rv.setViewVisibility(R.id.hotel_price_appwidget_last_updated, View.GONE)
        }
    }
}


