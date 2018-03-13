package com.expedia.bookings.itin.hotel.manageBooking

import android.content.Context
import android.content.pm.PackageManager
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItinCardDataHotel
import com.expedia.bookings.itin.hotel.common.MessageHotelUtil.getClickListener
import com.expedia.bookings.itin.hotel.common.MessageHotelUtil.isHotelMessagingEnabled
import com.expedia.bookings.tracking.TripsTracking
import com.expedia.bookings.utils.ClipboardUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.mobiata.android.SocialUtils
import com.squareup.phrase.Phrase

class HotelItinManageBookingHelp(context: Context, attr: AttributeSet?) : LinearLayout(context, attr) {
    val helpText: TextView by bindView(R.id.itin_hotel_manage_booking_hotel_help_text)
    val hotelConfirmationNumber: TextView by bindView(R.id.itin_hotel_manage_booking_hotel_help_confirmation_number)
    val callHotelButton: TextView by bindView(R.id.itin_hotel_manage_booking_hotel_help_call_hotel)
    private val messageHotel: TextView by bindView(R.id.itin_hotel_manage_booking_message_hotel)

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

        val hotelMessagingUrl = itinCardDataHotel.property.epcConversationUrl

        if (isHotelMessagingEnabled(context) && hotelMessagingUrl.isNotEmpty()) {
            messageHotel.visibility = View.VISIBLE
            messageHotel.setOnClickListener(getClickListener(context = context, url = hotelMessagingUrl, fromManageBooking = true))
        }

        helpText.text = Phrase.from(this, R.string.itin_hotel_manage_booking_hotel_help_text_TEMPLATE)
                .put("hotelname", itinCardDataHotel.propertyName).format().toString()
        callHotelButton.text = phoneNumber
        callHotelButton.contentDescription = Phrase.from(context, R.string.itin_hotel_manage_booking_call_hotel_button_content_description_TEMPLATE).put("phonenumber", phoneNumber).format().toString()
        callHotelButton.setOnClickListener {
            if (phoneNumber.isNotEmpty()) {
                val pm = context.packageManager
                if (pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
                    SocialUtils.call(context, phoneNumber)
                } else {
                    ClipboardUtils.setText(context, phoneNumber)
                    Toast.makeText(context, R.string.toast_copied_to_clipboard, Toast.LENGTH_SHORT).show()
                }
                TripsTracking.trackItinHotelCallHotel()
            }
        }
    }

    fun showConfirmationNumberIfAvailable(confirmationNumber: String) {
        if (confirmationNumber.isNotEmpty()) {
            hotelConfirmationNumber.text = Phrase.from(this, R.string.itin_hotel_manage_booking_hotel_help_confirmation_number_TEMPLATE)
                    .put("number", confirmationNumber).format().toString()
            hotelConfirmationNumber.contentDescription = Phrase.from(this, R.string.itin_hotel_manage_booking_confirmation_number_content_description_TEMPLATE)
                    .put("number", confirmationNumber.replace(".".toRegex(), "$0 ")).format().toString()
            hotelConfirmationNumber.setOnClickListener {
                ClipboardUtils.setText(context, confirmationNumber)
                Toast.makeText(context, R.string.toast_copied_to_clipboard, Toast.LENGTH_SHORT).show()
            }
        } else {
            hotelConfirmationNumber.visibility = View.GONE
        }
    }
}
