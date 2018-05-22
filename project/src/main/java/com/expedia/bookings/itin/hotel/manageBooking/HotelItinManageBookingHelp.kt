package com.expedia.bookings.itin.hotel.manageBooking

import android.content.Context
import android.content.pm.PackageManager
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.data.trips.ItinCardDataHotel
import com.expedia.bookings.itin.hotel.common.MessageHotelUtil.getClickListener
import com.expedia.bookings.itin.tripstore.utils.IJsonToItinUtil
import com.expedia.bookings.itin.utils.ActionModeCallbackUtil
import com.expedia.bookings.tracking.TripsTracking
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.setOnClickForSelectableTextView
import com.expedia.bookings.widget.TextView
import com.mobiata.android.SocialUtils
import com.squareup.phrase.Phrase

class HotelItinManageBookingHelp(context: Context, attr: AttributeSet?) : LinearLayout(context, attr) {
    val helpText: TextView by bindView(R.id.itin_more_help_text)
    val confirmationTitle: LinearLayout by bindView(R.id.confirmation_title)
    val confirmationNumber: TextView by bindView(R.id.confirmation_number)
    val callHotelButton: TextView by bindView(R.id.itin_more_help_phone_number)
    private val messageHotel: TextView by bindView(R.id.itin_hotel_manage_booking_message_hotel)
    var readJsonUtil: IJsonToItinUtil = Ui.getApplication(context).tripComponent().jsonUtilProvider()

    init {
        View.inflate(context, R.layout.widget_itin_more_help, this)
        this.orientation = LinearLayout.VERTICAL
        confirmationNumber.customSelectionActionModeCallback = ActionModeCallbackUtil.getActionModeCallBackWithoutPhoneNumberMenuItem()
        callHotelButton.customSelectionActionModeCallback = ActionModeCallbackUtil.getActionModeCallbackWithPhoneNumberClickAction({
            TripsTracking.trackItinHotelCallHotel()
        })
    }

    fun setUpWidget(itinCardDataHotel: ItinCardDataHotel) {
        val phoneNumber: String = itinCardDataHotel.localPhone
        if (phoneNumber.isEmpty()) {
            this.visibility = View.GONE
            return
        }

        val hotelMessagingUrl = itinCardDataHotel.property.epcConversationUrl

        if (hotelMessagingUrl.isNotEmpty()) {
            messageHotel.visibility = View.VISIBLE
            val isGuest = readJsonUtil.getItin(itinCardDataHotel.tripId)?.isGuest
            messageHotel.setOnClickListener(getClickListener(context = context, url = hotelMessagingUrl, fromManageBooking = true, isGuest = isGuest))
        }

        helpText.text = Phrase.from(this, R.string.itin_more_help_text_TEMPLATE)
                .put("supplier", itinCardDataHotel.propertyName).format().toString()
        callHotelButton.text = phoneNumber
        callHotelButton.contentDescription = Phrase.from(context, R.string.itin_hotel_manage_booking_call_hotel_button_content_description_TEMPLATE).put("phonenumber", phoneNumber).format().toString()
        setOnClickForSelectableTextView(callHotelButton, { callHotel(phoneNumber) })
    }

    fun showConfirmationNumberIfAvailable(confirmationNumber: String) {
        if (confirmationNumber.isNotEmpty()) {
            confirmationTitle.visibility = View.VISIBLE
            this.confirmationNumber.text = confirmationNumber
            val contDesc = Phrase.from(this,
                    R.string.itin_more_help_confirmation_number_content_description_TEMPLATE)
                    .put("number", confirmationNumber).format().toString()
            this.confirmationNumber.contentDescription = contDesc
        } else {
            confirmationTitle.visibility = View.GONE
        }
    }

    private fun callHotel(number: String) {
        val pm = context.packageManager
        if (pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY) || ExpediaBookingApp.isRobolectric()) {
            SocialUtils.call(context, number)
            TripsTracking.trackItinHotelCallHotel()
        }
    }
}
