package com.expedia.bookings.hotel.activity

import android.app.Activity
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.hotel.fragment.ChangeDatesDialogFragment
import com.expedia.bookings.hotel.util.HotelCalendarDirections
import com.expedia.bookings.hotel.util.HotelFavoriteCache
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.widget.CalendarWidgetV2
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.widget.FrameLayout
import com.expedia.bookings.hotel.deeplink.HotelExtras
import com.expedia.bookings.hotel.service.HotelPriceIntentService
import com.expedia.bookings.hotel.util.HotelAppWidgetUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.TextView


class HotelAppWidgetConfigureActivity: AppCompatActivity() {
    private val calendarCard by lazy {
        findViewById(R.id.hotel_appwidget_config_calendar_card) as CalendarWidgetV2
    }

    private val doneButton by lazy {
        findViewById(R.id.hotel_appwidget_config_done) as TextView
    }

    private val calendarInstructions = HotelCalendarDirections(this)
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = Ui.getApplication(applicationContext)
        app.defaultHotelComponents()
        app.hotelComponent().inject(this)

        setContentView(R.layout.hotel_appwidget_configure_activity)

        if (intent != null) {
            appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID)
        }

        calendarCard.setOnClickListener {
            showChangeDatesDialog()
        }

        doneButton.setOnClickListener {
            val intent = Intent(this, HotelPriceIntentService::class.java)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

            val resultValue = Intent()

            AppWidgetManager.getInstance(this).getAppWidgetInfo(appWidgetId).updatePeriodMillis = 60000
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(Activity.RESULT_OK, resultValue)

            startService(intent)
            finish()
        }
    }

    private fun showChangeDatesDialog() {
        val dialogFragment = ChangeDatesDialogFragment()
        dialogFragment.datesChangedSubject.subscribe { dates ->
            HotelFavoriteCache.saveDates(this, dates.first.toString(), dates.second.toString())
            calendarCard.text = calendarInstructions.getCompleteDateText(dates.first, dates.second, false)
        }
        dialogFragment.show(supportFragmentManager, Constants.TAG_CALENDAR_DIALOG)
    }
}