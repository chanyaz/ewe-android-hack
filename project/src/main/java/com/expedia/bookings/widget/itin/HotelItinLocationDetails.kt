package com.expedia.bookings.widget.itin

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.expedia.bookings.R
import com.expedia.bookings.data.cars.LatLong
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.utils.ClipboardUtils
import com.expedia.bookings.utils.GoogleMapsUtil
import com.expedia.bookings.utils.NavUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.ItinActionsSection
import com.expedia.bookings.widget.LocationMapImageView
import com.expedia.bookings.widget.TextView
import com.mobiata.android.SocialUtils
import com.squareup.phrase.Phrase

class HotelItinLocationDetails(context: Context, attr: AttributeSet?) : LinearLayout(context, attr) {
    val locationMapImageView: LocationMapImageView by bindView(R.id.widget_hotel_itin_map)
    val address: LinearLayout by bindView(R.id.widget_hotel_itin_address)
    val addressLine1: TextView by bindView(R.id.widget_hotel_itin_address_line_1)
    val addressLine2: TextView by bindView(R.id.widget_hotel_itin_address_line_2)
    val actionButtons: ItinActionsSection by bindView(R.id.action_button_layout)

    init {
        View.inflate(context, R.layout.widget_hotel_itin_location_details, this)
    }

    fun setupWidget(itinCardDataHotel: ItinCardDataHotel) {
        if (itinCardDataHotel.propertyLocation != null) {
            locationMapImageView.setLocation(LatLong(itinCardDataHotel.propertyLocation.latitude, itinCardDataHotel.propertyLocation.longitude))
        }
        addressLine1.text = itinCardDataHotel.propertyLocation.streetAddressString
        addressLine2.text = itinCardDataHotel.propertyLocation.toTwoLineAddressFormattedString()
        val phoneNumber: String = itinCardDataHotel.localPhone
        val callButton: SummaryButton = SummaryButton(R.drawable.itin_call_hotel, phoneNumber, Phrase.from(context, R.string.itin_hotel_details_call_button_content_description_TEMPLATE).put("phonenumber", phoneNumber).format().toString(), View.OnClickListener {
            if (phoneNumber.isNotEmpty()) {
                SocialUtils.call(context, phoneNumber)
            }
        })
        val directionsButton: SummaryButton = SummaryButton(R.drawable.itin_directions_hotel, context.getString(R.string.itin_action_directions), View.OnClickListener {
            val intent = GoogleMapsUtil.getDirectionsIntent(itinCardDataHotel.property.location.toLongFormattedString())
            if (intent != null) {
                NavUtils.startActivitySafe(context, intent)
            }
        })
        if (phoneNumber.isEmpty()) actionButtons.bind(null, directionsButton) else actionButtons.bind(callButton, directionsButton)
        val textToCopy: String = Phrase.from(context, R.string.itin_hotel_details_address_clipboard_TEMPLATE)
                .put("addresslineone", addressLine1.text.toString())
                .put("addresslinetwo", addressLine2.text.toString()).format().toString()
        address.setOnClickListener {
            ClipboardUtils.setText(context, textToCopy)
            Toast.makeText(context, R.string.toast_copied_to_clipboard, Toast.LENGTH_SHORT).show()
        }
        address.contentDescription = Phrase.from(context, R.string.itin_hotel_details_address_copy_content_description_TEMPLATE)
                .put("address", textToCopy).format().toString()
    }
}