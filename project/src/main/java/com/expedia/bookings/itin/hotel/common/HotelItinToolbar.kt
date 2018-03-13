package com.expedia.bookings.itin.hotel.common

import android.content.Context
import android.support.annotation.VisibleForTesting
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.Toolbar
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItinCardData
import com.expedia.bookings.data.trips.TripComponent
import com.expedia.bookings.data.trips.ItinCardDataHotel
import com.expedia.bookings.itin.utils.ItinShareTripHelper
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.itin.ItinContentGenerator
import com.squareup.phrase.Phrase

class HotelItinToolbar(context: Context, attr: AttributeSet?) : Toolbar(context, attr) {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val toolbarTitleTextView: TextView by bindView(R.id.itin_toolbar_title)
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val toolbarSubtitleTextView: TextView by bindView(R.id.itin_toolbar_subtitle)

    private var mItinContentGenerator: ItinContentGenerator<out ItinCardData>? = null
    lateinit var itinCardDataHotel: ItinCardDataHotel

    init {
        View.inflate(context, R.layout.widget_itin_toolbar, this)
    }

    fun setUpWidget(itinCardDataHotel: ItinCardDataHotel, toolbarTitle: String, toolbarSubtitle: String? = null) {
        this.itinCardDataHotel = itinCardDataHotel
        mItinContentGenerator = ItinContentGenerator.createGenerator(context, itinCardDataHotel)
        this.navigationIcon = context.getDrawable(R.drawable.ic_arrow_back_white_24dp)
        this.navigationContentDescription = context.getText(R.string.toolbar_nav_icon_cont_desc)
        if (toolbarSubtitle == null) {
            buildToolbarSubtitleForDates(itinCardDataHotel)
        } else {
            toolbarSubtitleTextView.text = toolbarSubtitle
        }
        toolbarTitleTextView.text = toolbarTitle
    }

    fun showShare() {
        val shareTextView = findViewById<TextView>(R.id.itin_share_button)
        shareTextView.visibility = View.VISIBLE
        shareTextView.setTintedDrawable(context.getDrawable(R.drawable.ic_itin_share), ContextCompat.getColor(context, R.color.itin_toolbar_text))
        Ui.setOnClickListener(this, R.id.itin_share_button, mOnClickListener)
    }

    private fun buildToolbarSubtitleForDates(itinCardDataHotel: ItinCardDataHotel) {
        val tripStartDate = LocaleBasedDateFormatUtils.dateTimeToMMMd(itinCardDataHotel.startDate)
        val tripEndDate = LocaleBasedDateFormatUtils.dateTimeToMMMd(itinCardDataHotel.endDate)
        val tripDateString = Phrase.from(this, R.string.start_dash_end_date_range_TEMPLATE)
                .put("startdate", tripStartDate)
                .put("enddate", tripEndDate)
                .format().toString()
        toolbarSubtitleTextView.text = tripDateString
        val tripDateContDesc = Phrase.from(this, R.string.start_to_end_date_range_cont_desc_TEMPLATE)
                .put("startdate", tripStartDate)
                .put("enddate", tripEndDate)
                .format().toString()
        toolbarSubtitleTextView.contentDescription = tripDateContDesc
    }

    private val mOnClickListener = View.OnClickListener { v ->
        when (v.id) {
            R.id.itin_share_button -> {
                val shareHelper = ItinShareTripHelper(context, itinCardDataHotel)
                shareHelper.fetchShortShareUrlShowShareDialog()
                OmnitureTracking.trackItinShareStart(TripComponent.Type.HOTEL)
            }
        }
    }
}
