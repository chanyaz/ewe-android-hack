package com.expedia.bookings.hotel.service

import android.app.job.JobParameters
import android.app.job.JobService
import android.util.Log
import com.expedia.bookings.hotel.util.HotelFavoriteCache
import com.expedia.bookings.hotel.util.HotelInfoManager
import com.expedia.bookings.utils.Ui
import javax.inject.Inject
import android.content.Context
import com.expedia.bookings.hotel.deeplink.HotelExtras
import com.expedia.bookings.hotel.util.HotelAppWidgetUtil


class HotelPriceJobService : JobService() {
    lateinit var hotelInfoManager: HotelInfoManager
        @Inject set

    private var hotelsRefreshed = 0

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.v("HotelPriceJobService", ": onStopJob")
        hotelInfoManager.clearSubscriptions()
        return false
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        val app = Ui.getApplication(applicationContext)
        app.defaultHotelComponents()
        app.hotelComponent().inject(this)

        refresh(applicationContext, params)
        return true
    }

    private fun refresh(context: Context, params: JobParameters?) {
        hotelsRefreshed = 0
        Log.v("HotelPriceJobService", ": onStartJob")

        val favorites = HotelFavoriteCache.getFavorites(context)

        hotelInfoManager.offerSuccessSubject.subscribe { response ->
            HotelFavoriteCache.saveHotelData(context, response)
            hotelsRefreshed++

            if (hotelsRefreshed == favorites.size) {
                hotelRefreshFinished(context, params)
            }
            Log.v("HotelPriceJobService", ": ${response.hotelId}")
        }

        hotelInfoManager.infoSuccessSubject.subscribe { response ->
            HotelFavoriteCache.saveHotelData(context, response)
            hotelsRefreshed++

            if (hotelsRefreshed == favorites.size) {
                hotelRefreshFinished(context, params)
            }
            Log.v("HotelPriceJobService", ": ${response.hotelId}")
        }

        val checkInDate = HotelFavoriteCache.getCheckInDate(context)
        val checkOutDate = HotelFavoriteCache.getCheckOutDate(context)

        if (checkInDate != null && checkOutDate != null) {
            hotelInfoManager.fetchOffers(checkInDate, checkOutDate, favorites)
        } else {
            hotelInfoManager.fetchDatelessInfo(favorites)
        }
    }

    private fun hotelRefreshFinished(context: Context, params: JobParameters?) {
        HotelFavoriteCache.saveLastUpdateTime(context, System.currentTimeMillis())

        val bundle = params?.extras
        val id = bundle?.getInt(HotelExtras.PRICE_APPWIDGET_KEY, 0) ?: 0
        HotelAppWidgetUtil.updateRemoteViews(id, applicationContext)
        jobFinished(params, false)
    }
}


