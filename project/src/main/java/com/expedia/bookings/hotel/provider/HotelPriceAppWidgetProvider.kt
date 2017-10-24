package com.expedia.bookings.hotel.provider

import android.app.job.JobInfo
import android.app.job.JobInfo.NETWORK_TYPE_ANY
import android.app.job.JobScheduler
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import com.expedia.bookings.hotel.service.HotelPriceJobService
import android.content.ComponentName
import android.os.PersistableBundle
import android.util.Log


class HotelPriceAppWidgetProvider : AppWidgetProvider() {
    val MY_FANCY_ID = 26548

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        Log.v("HotelPriceAppWidgetProvider", ": onUpdate")
        runPriceJob(context, appWidgetIds[0])
    }

    private fun runPriceJob(context: Context, id: Int) {
        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val jobInfoBuilder = JobInfo.Builder(MY_FANCY_ID, ComponentName(context, HotelPriceJobService::class.java))
        jobInfoBuilder.setRequiredNetworkType(NETWORK_TYPE_ANY)
        jobInfoBuilder.setOverrideDeadline(0)


        val bundle = PersistableBundle()
        bundle.putInt("PRICE_APP_WIDGET_KEY", id)
        jobInfoBuilder.setExtras(bundle)

        jobScheduler.schedule(jobInfoBuilder.build())
    }
}