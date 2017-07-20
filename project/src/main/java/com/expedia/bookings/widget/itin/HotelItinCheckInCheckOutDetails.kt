package com.expedia.bookings.widget.itin

import android.content.Context
import android.text.format.DateFormat
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import java.util.Locale

class HotelItinCheckInCheckOutDetails(context: Context, attr: AttributeSet?) : LinearLayout(context, attr) {
    val checkInDateView: TextView by bindView(R.id.hotel_itin_checkin_date_text)
    val checkInTimeView: TextView by bindView(R.id.hotel_itin_checkin_time_text)
    val checkOutDateView: TextView by bindView(R.id.hotel_itin_checkout_date_text)
    val checkOutTimeView: TextView by bindView(R.id.hotel_itin_checkout_time_text)

    init {
        View.inflate(context, R.layout.widget_hotel_itin_checkin_checkout_details, this)
    }

    fun setUpWidget(itinCardDataHotel: ItinCardDataHotel) {

        val formatPattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), "EEE, MMM d")
        checkInDateView.text = itinCardDataHotel?.startDate.toString(formatPattern)
        checkOutDateView.text = itinCardDataHotel?.endDate.toString(formatPattern)
        checkInTimeView.text = itinCardDataHotel.checkInTime?.toLowerCase()
        checkOutTimeView.text = itinCardDataHotel.checkOutTime?.toLowerCase()
    }
}