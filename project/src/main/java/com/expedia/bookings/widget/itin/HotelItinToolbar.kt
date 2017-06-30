package com.expedia.bookings.widget.itin

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.Toolbar
import com.activeandroid.Cache
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItinCardData
import com.expedia.bookings.data.trips.TripComponent
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.itin.ItinShareTargetBroadcastReceiver
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.TextView
import com.mobiata.android.util.SettingUtils
import com.squareup.phrase.Phrase
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter


class HotelItinToolbar(context: Context, attr: AttributeSet?) : Toolbar(context, attr) {

    val hotelNameTextView: TextView by lazy {
        findViewById(R.id.hotel_itin_name) as TextView
    }
    val hotelTripDatesTextView: TextView by lazy {
        findViewById(R.id.hotel_itin_dates) as TextView
    }
    private var mItinContentGenerator: ItinContentGenerator<out ItinCardData>? = null

    init {
        View.inflate(context, R.layout.widget_hotel_itin_toolbar, this)
    }

    fun setUpWidget(itinCardDataHotel: ItinCardDataHotel) {
        mItinContentGenerator = ItinContentGenerator.createGenerator(context, itinCardDataHotel)
        this.navigationIcon = context.getDrawable(R.drawable.ic_arrow_back_white_24dp)
        this.navigationContentDescription = context.getText(R.string.toolbar_nav_icon_cont_desc)

        val hotelName = itinCardDataHotel?.propertyName
        val formatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")
        val tripStartDate = DateUtils.localDateToMMMd(formatter.parseLocalDate(itinCardDataHotel?.startDate.toString().substringBefore("T")))
        val tripEndDate = DateUtils.localDateToMMMd(formatter.parseLocalDate(itinCardDataHotel?.endDate.toString().substringBefore("T")))
        val tripDateString = Phrase.from(this, R.string.calendar_instructions_date_range_TEMPLATE)
                .put("startdate", tripStartDate)
                .put("enddate", tripEndDate)
                .format().toString()
        hotelNameTextView.text = hotelName
        hotelTripDatesTextView.text = tripDateString

        if (ProductFlavorFeatureConfiguration.getInstance().shouldShowItinShare()) {
            val shareTextView = findViewById(R.id.itin_share_button) as TextView
            shareTextView.visibility = View.VISIBLE
            shareTextView.setTintedDrawable(context.getDrawable(R.drawable.ic_itin_share), ContextCompat.getColor(context, R.color.itin_toolbar_text))
            Ui.setOnClickListener(this, R.id.itin_share_button, mOnClickListener)
        }
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

        val receiver = Intent(Cache.getContext(), ItinShareTargetBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(Cache.getContext(), 0, receiver, PendingIntent.FLAG_UPDATE_CURRENT)
        val chooserIntent = Intent.createChooser(shareIntent, "", pendingIntent.intentSender)
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, shareIntent)
        Cache.getContext().startActivity(chooserIntent)
    }

}