package com.expedia.bookings.widget.itin

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.content.ContextCompat
import android.text.format.DateFormat
import android.util.AttributeSet
import android.view.View
import android.widget.Toolbar
import com.activeandroid.Cache
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItinCardData
import com.expedia.bookings.data.trips.TripComponent
import com.expedia.bookings.itin.ItinShareTargetBroadcastReceiver
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.TextView
import com.mobiata.android.util.SettingUtils
import com.squareup.phrase.Phrase
import java.util.Locale


class HotelItinToolbar(context: Context, attr: AttributeSet?) : Toolbar(context, attr) {

    val toolbarTitleTextView: TextView by lazy {
        findViewById(R.id.itin_toolbar_title) as TextView
    }
    val toolbarSubtitleTextView: TextView by lazy {
        findViewById(R.id.itin_toolbar_subtitle) as TextView
    }
    private var mItinContentGenerator: ItinContentGenerator<out ItinCardData>? = null

    init {
        View.inflate(context, R.layout.widget_hotel_itin_toolbar, this)
    }

    fun setUpWidget(itinCardDataHotel: ItinCardDataHotel, toolbarTitle: String) {
        mItinContentGenerator = ItinContentGenerator.createGenerator(context, itinCardDataHotel)
        this.navigationIcon = context.getDrawable(R.drawable.ic_arrow_back_white_24dp)
        this.navigationContentDescription = context.getText(R.string.toolbar_nav_icon_cont_desc)
        toolbarSubtitle(itinCardDataHotel)
        toolbarTitleTextView.text = toolbarTitle
    }

    fun showShare() {
        val shareTextView = findViewById(R.id.itin_share_button) as TextView
        shareTextView.visibility = View.VISIBLE
        shareTextView.setTintedDrawable(context.getDrawable(R.drawable.ic_itin_share), ContextCompat.getColor(context, R.color.itin_toolbar_text))
        Ui.setOnClickListener(this, R.id.itin_share_button, mOnClickListener)
    }


    private fun toolbarSubtitle(itinCardDataHotel: ItinCardDataHotel) {
        val formatPattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), "MMM d")
        val tripStartDate = itinCardDataHotel.startDate.toString(formatPattern)
        val tripEndDate = itinCardDataHotel.endDate.toString(formatPattern)
        val tripDateString = Phrase.from(this, R.string.calendar_instructions_date_range_TEMPLATE)
                .put("startdate", tripStartDate)
                .put("enddate", tripEndDate)
                .format().toString()
        toolbarSubtitleTextView.text = tripDateString
        val tripDateContDesc = Phrase.from(this, R.string.calendar_instructions_date_range_cont_desc_TEMPLATE)
                .put("startdate", tripStartDate)
                .put("enddate", tripEndDate)
                .format().toString()
        toolbarSubtitleTextView.contentDescription = tripDateContDesc
    }


    private val mOnClickListener = View.OnClickListener { v ->
        when (v.id) {
            R.id.itin_share_button -> {
                showNativeShareDialog()
                OmnitureTracking.trackItinShareStart(TripComponent.Type.HOTEL)
            }
        }
    }

    private fun showNativeShareDialog() {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_TEXT, mItinContentGenerator?.shareTextShort)
        shareIntent.type = "text/plain"

        SettingUtils.save(Cache.getContext(), "TripType", mItinContentGenerator?.type.toString())

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            context.startActivity(shareIntent)
        } else {
            val receiver = Intent(Cache.getContext(), ItinShareTargetBroadcastReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(Cache.getContext(), 0, receiver, PendingIntent.FLAG_UPDATE_CURRENT)
            val chooserIntent = Intent.createChooser(shareIntent, "", pendingIntent.intentSender)
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, shareIntent)
            Cache.getContext().startActivity(chooserIntent)
        }
    }
}