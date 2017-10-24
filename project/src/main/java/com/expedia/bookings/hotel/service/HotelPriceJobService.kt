package com.expedia.bookings.hotel.service

import android.app.job.JobParameters
import android.app.job.JobService
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.expedia.bookings.R
import com.expedia.bookings.hotel.provider.HotelPriceAppWidgetProvider
import com.expedia.bookings.hotel.util.HotelFavoriteCache
import com.expedia.bookings.hotel.util.HotelInfoManager
import com.expedia.bookings.utils.Ui
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
                jobFinished(params, false)
            }
            Log.v("HotelPriceJobService", ": ${response.hotelId}")
        }

        val checkInDate = HotelFavoriteCache.getCheckInDate(applicationContext)
        val checkOutDate = HotelFavoriteCache.getCheckOutDate(applicationContext)

        if (checkInDate != null && checkOutDate != null) {
            for (id in favorites) {
                hotelInfoManager.fetchOffers(checkInDate, checkOutDate, id)
            }
        } else {
            for (id in favorites) {
                hotelInfoManager.fetchDatelessInfo(id)
            }
        }
        return true
    }


    private fun sendViews() {
        Log.v("HotelPriceJobService", ": sendViews")
        val remoteViews = RemoteViews(applicationContext.packageName, R.layout.hotel_price_appwidget_layout)
        val intent = Intent(applicationContext, HotelRemoteViewService::class.java)

        remoteViews.setRemoteAdapter(R.id.hotel_price_appwidget_list, intent);
        remoteViews.setEmptyView(R.id.hotel_price_appwidget_list, R.id.hotel_price_appwidget_empty_view)

        val thisWidget = ComponentName(this, HotelPriceAppWidgetProvider::class.java)
        val manager = AppWidgetManager.getInstance(this)
        manager.updateAppWidget(thisWidget, remoteViews)
        manager.notifyAppWidgetViewDataChanged(intent.getIntExtra("PRICE_APP_WIDGET_KEY", 0), R.id.hotel_price_appwidget_list)
    }
}


