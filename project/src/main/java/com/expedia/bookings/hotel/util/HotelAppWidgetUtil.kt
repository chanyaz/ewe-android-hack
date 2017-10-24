package com.expedia.bookings.hotel.util

import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.expedia.bookings.R
import com.expedia.bookings.hotel.activity.HotelAppWidgetConfigureActivity
import com.expedia.bookings.hotel.deeplink.HotelExtras
import com.expedia.bookings.hotel.provider.HotelPriceAppWidgetProvider
import com.expedia.bookings.hotel.service.HotelPriceJobService
import com.expedia.bookings.hotel.service.HotelRemoteViewService
import com.expedia.bookings.utils.DateFormatUtils
import com.expedia.ui.HotelActivity
import org.joda.time.LocalDate

class HotelAppWidgetUtil {
    companion object {
        val MY_JOB_ID = 265

        fun scheduleUpdate(context: Context, appWidgetId: Int) {
            Log.v("HotelAppWidgetUtil", ": scheduleUpdate id:$appWidgetId")
            val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            val jobInfoBuilder = JobInfo.Builder(MY_JOB_ID + appWidgetId, ComponentName(context, HotelPriceJobService::class.java))
            jobInfoBuilder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            jobInfoBuilder.setOverrideDeadline(0)


            val bundle = PersistableBundle()
            bundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            jobInfoBuilder.setExtras(bundle)

            jobScheduler.schedule(jobInfoBuilder.build())
        }

        fun showLoading(appWidgetId: Int, context: Context) {
            Log.v("HotelAppWidgetUtil", ": showLoading id:$appWidgetId")
            val remoteViews = RemoteViews(context.packageName, R.layout.hotel_price_appwidget_layout)
            remoteViews.setViewVisibility(R.id.hotel_price_appwidget_empty_view, View.VISIBLE)

            remoteViews.setViewVisibility(R.id.hotel_price_appwidget_list, View.GONE)
            remoteViews.setViewVisibility(R.id.hotel_appwidget_header_dates, View.GONE)
            remoteViews.setViewVisibility(R.id.hotel_price_appwidget_last_updated, View.GONE)
            updateEditView(context, appWidgetId, remoteViews)

            val manager = AppWidgetManager.getInstance(context)
            manager.updateAppWidget(appWidgetId, remoteViews)
        }

        fun updateRemoteViews(appWidgetId: Int, context: Context) {
            Log.v("HotelAppWidgetUtil", ": updateRemoteViews id:$appWidgetId")
            val remoteViews = RemoteViews(context.packageName, R.layout.hotel_price_appwidget_layout)

            remoteViews.setViewVisibility(R.id.hotel_price_appwidget_empty_view, View.GONE)
            updateListView(context, remoteViews)
            updateEditView(context, appWidgetId, remoteViews)
            updateDateView(context, remoteViews)
            updateLastUpdated(context, remoteViews)

            val manager = AppWidgetManager.getInstance(context)
            manager.updateAppWidget(appWidgetId, remoteViews)
            manager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.hotel_price_appwidget_list)
        }

        private fun updateListView(context: Context, rv: RemoteViews) {
            val hotelIntent = Intent(context, HotelActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, hotelIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT)
            rv.setPendingIntentTemplate(R.id.hotel_price_appwidget_list, pendingIntent)

            val remoteViewServiceIntent = Intent(context, HotelRemoteViewService::class.java)
            rv.setViewVisibility(R.id.hotel_price_appwidget_list, View.VISIBLE)
            rv.setRemoteAdapter(R.id.hotel_price_appwidget_list, remoteViewServiceIntent);
            rv.setEmptyView(R.id.hotel_price_appwidget_list, R.id.hotel_price_appwidget_empty_view)
        }

        private fun updateEditView(context: Context, appWidgetId: Int, rv: RemoteViews) {
            val intent = Intent(context, HotelAppWidgetConfigureActivity::class.java)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            val pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, 0)
            rv.setOnClickPendingIntent(R.id.hotel_appwidget_edit_icon, pendingIntent)
        }

        private fun updateDateView(context: Context, rv: RemoteViews) {
            val checkIn = HotelFavoriteCache.getCheckInDate(context)
            val checkOut = HotelFavoriteCache.getCheckOutDate(context)

            if (checkIn != null && checkOut != null) {
                rv.setViewVisibility(R.id.hotel_appwidget_header_dates, View.VISIBLE)
                val dates = DateFormatUtils.formatAppWidgetDate(context, checkIn, checkOut)
                rv.setTextViewText(R.id.hotel_appwidget_header_dates, dates)
            } else {
                rv.setViewVisibility(R.id.hotel_appwidget_header_dates, View.GONE)
            }
        }

        private fun updateLastUpdated(context: Context, rv: RemoteViews) {
            val lastUpdated = HotelFavoriteCache.getLastUpdated(context)
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
}