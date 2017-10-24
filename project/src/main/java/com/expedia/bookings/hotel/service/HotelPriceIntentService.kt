package com.expedia.bookings.hotel.service

import android.app.IntentService
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.util.Log
import com.expedia.bookings.hotel.deeplink.HotelExtras
import com.expedia.bookings.hotel.util.HotelAppWidgetUtil
import com.expedia.bookings.hotel.util.HotelFavoriteRefreshManager
import com.expedia.bookings.hotel.util.HotelInfoManager
import com.expedia.bookings.utils.Ui
import javax.inject.Inject

class HotelPriceIntentService : IntentService("HotelPriceIntentService") {
    lateinit var hotelInfoManager: HotelInfoManager
        @Inject set

    override fun onHandleIntent(intent: Intent?) {
        Log.v("HotelPriceIntentService", ": onHandleIntent")
        val app = Ui.getApplication(applicationContext)
        app.defaultHotelComponents()
        app.hotelComponent().inject(this)

        val appWidgetId = intent?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0) ?: 0

        val hotelRefreshManager = HotelFavoriteRefreshManager(hotelInfoManager)
        hotelRefreshManager.allHotelsRefreshedSubject.subscribe {
            HotelAppWidgetUtil.updateRemoteViews(appWidgetId, applicationContext)
            Log.v("HotelPriceIntentService", ": allHotelsRefreshedSubject id:$appWidgetId")
        }

        HotelAppWidgetUtil.showLoading(appWidgetId, applicationContext)
        hotelRefreshManager.refreshHotels(applicationContext)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v("HotelPriceIntentService", ": onDestroy")
        hotelInfoManager.clearSubscriptions()
    }
}