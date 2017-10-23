package com.expedia.bookings.hotel.service

import android.app.job.JobParameters
import android.app.job.JobService
import com.expedia.bookings.hotel.util.HotelFavoriteCache
import com.expedia.bookings.hotel.util.HotelInfoManager
import com.expedia.bookings.utils.Ui
import javax.inject.Inject
import android.widget.RemoteViews
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelOffersResponse
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import com.expedia.bookings.hotel.provider.HotelPriceAppWidgetProvider
import android.content.Intent
import android.util.Log


class HotelPriceJobService : JobService() {
    lateinit var hotelInfoManager: HotelInfoManager
        @Inject set

    var hotelsRefreshed = 0


    override fun onStopJob(params: JobParameters?): Boolean {
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

        if (favorites != null) {
            hotelInfoManager.infoSuccessSubject.subscribe { response ->
                sendViews(response)
                hotelsRefreshed++
                if (hotelsRefreshed == favorites.size) {
                    jobFinished(params, false)
                }
                Log.v("HotelPriceJobService", ": ${response.hotelId}")
            }

            for (id in favorites) {
                hotelInfoManager.fetchDatelessInfo(id)
            }
            return true
        } else {
            return false
        }
    }

    private fun sendViews(response: HotelOffersResponse) {
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