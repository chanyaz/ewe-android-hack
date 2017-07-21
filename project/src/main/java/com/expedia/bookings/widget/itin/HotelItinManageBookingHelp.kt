package com.expedia.bookings.widget.itin

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.mobiata.android.SocialUtils
import com.squareup.phrase.Phrase

class HotelItinManageBookingHelp(context: Context, attr: AttributeSet?) : LinearLayout(context, attr) {
    val helpText: TextView by bindView(R.id.itin_hotel_manage_booking_hotel_help_text)
    val hotelConfirmationNumber: TextView by bindView(R.id.itin_hotel_manage_booking_hotel_help_confirmation_number)
    val callHotelButton: TextView by bindView(R.id.itin_hotel_manage_booking_hotel_help_call_hotel)

    init {
        View.inflate(context, R.layout.widget_hotel_itin_manage_booking_help, this)
        this.orientation = LinearLayout.VERTICAL
    }

    fun setUpWidget(itinCardDataHotel: ItinCardDataHotel) {
        val phoneNumber: String = itinCardDataHotel.localPhone
        if (phoneNumber.isEmpty()) {
            this.visibility = View.GONE
            return
        }
        if (!itinCardDataHotel.hasConfirmationNumber()) {
            hotelConfirmationNumber.visibility = View.GONE
        } else {
            hotelConfirmationNumber.text = Phrase.from(this, R.string.itin_hotel_manage_booking_hotel_help_confirmation_number_TEMPLATE)
                    .put("number", itinCardDataHotel.lastConfirmationNumber).format().toString()
        }
        helpText.text = Phrase.from(this, R.string.itin_hotel_manage_booking_hotel_help_text_TEMPLATE)
                .put("hotelname", itinCardDataHotel.propertyName).format().toString()
        callHotelButton.text = phoneNumber
        callHotelButton.setOnClickListener {
            if (phoneNumber.isNotEmpty()) {
                SocialUtils.call(context, phoneNumber)
            }
        }
    }
}