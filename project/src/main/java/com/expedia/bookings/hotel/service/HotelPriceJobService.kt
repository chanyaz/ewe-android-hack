package com.expedia.bookings.hotel.service

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.appwidget.AppWidgetManager
import android.util.Log
import com.expedia.bookings.hotel.util.HotelAppWidgetUtil
import com.expedia.bookings.hotel.util.HotelFavoriteCache
import com.expedia.bookings.hotel.util.HotelInfoManager
import com.expedia.bookings.utils.Ui
import javax.inject.Inject
import com.expedia.bookings.hotel.util.HotelFavoriteRefreshManager


class HotelPriceJobService : JobService() {
    lateinit var hotelInfoManager: HotelInfoManager
        @Inject set

    private var hotelsRefreshed = 0
    private var appWidgetId = 0

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.v("HotelPriceJobService", ": onStopJob")
        hotelInfoManager.clearSubscriptions()
        return false
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        val app = Ui.getApplication(applicationContext)
        app.defaultHotelComponents()
        app.hotelComponent().inject(this)

        val bundle = params?.extras
        appWidgetId = bundle?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, 0) ?: 0
        refresh(applicationContext, params)
        return true
    }

    private fun refresh(context: Context, params: JobParameters?) {
        hotelsRefreshed = 0
        Log.v("HotelPriceJobService", ": onStartJob")

        val hotelRefreshManager = HotelFavoriteRefreshManager(hotelInfoManager)
        hotelRefreshManager.allHotelsRefreshedSubject.subscribe {
            hotelRefreshFinished(context, params)
        }

        hotelRefreshManager.refreshHotels(context)
    }

    private fun hotelRefreshFinished(context: Context, params: JobParameters?) {
        HotelFavoriteCache.saveLastUpdateTime(context, System.currentTimeMillis())
        HotelAppWidgetUtil.updateRemoteViews(appWidgetId, applicationContext)
        jobFinished(params, false)
    }
}


