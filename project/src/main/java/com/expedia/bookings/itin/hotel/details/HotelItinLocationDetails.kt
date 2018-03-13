package com.expedia.bookings.itin.hotel.details

import android.content.Context
import android.support.v4.app.ActivityOptionsCompat
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import com.expedia.bookings.R
import com.expedia.bookings.itin.hotel.common.HotelItinExpandedMapActivity
import com.expedia.bookings.data.trips.ItinCardDataHotel
import com.expedia.bookings.itin.common.GoogleMapsLiteViewModel
import com.expedia.bookings.itin.common.GoogleMapsLiteMapView
import com.expedia.bookings.tracking.TripsTracking
import com.expedia.bookings.utils.ClipboardUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.google.android.gms.maps.model.LatLng
import com.squareup.phrase.Phrase

class HotelItinLocationDetails(context: Context, attr: AttributeSet?) : LinearLayout(context, attr) {
    val mapView by bindView<GoogleMapsLiteMapView>(R.id.widget_hotel_itin_map)
    val address: LinearLayout by bindView(R.id.widget_hotel_itin_address)
    val addressLine1: TextView by bindView(R.id.widget_hotel_itin_address_line_1)
    val addressLine2: TextView by bindView(R.id.widget_hotel_itin_address_line_2)
    val directionsButton: ImageView by bindView(R.id.hotel_directions_button)

    init {
        View.inflate(context, R.layout.widget_hotel_itin_location_details, this)
    }

    fun setupWidget(itinCardDataHotel: ItinCardDataHotel) {
        if (itinCardDataHotel.propertyLocation != null) {
            val mapVm = GoogleMapsLiteViewModel(
                    listOf(LatLng(itinCardDataHotel.propertyLocation.latitude, itinCardDataHotel.propertyLocation.longitude))
            )
            mapView.setViewModel(mapVm)
            mapView.setOnClickListener {
                context.startActivity(HotelItinExpandedMapActivity.createIntent(context, itinCardDataHotel.id), ActivityOptionsCompat.makeCustomAnimation(context, R.anim.slide_in_right, R.anim.slide_out_left_complete).toBundle())
                TripsTracking.trackItinHotelExpandMap()
            }
            directionsButton.setOnClickListener {
                context.startActivity(HotelItinExpandedMapActivity.createIntent(context, itinCardDataHotel.id), ActivityOptionsCompat.makeCustomAnimation(getContext(), R.anim.slide_in_right, R.anim.slide_out_left_complete).toBundle())
                TripsTracking.trackItinHotelDirections()
            }
        }

        addressLine1.text = itinCardDataHotel.propertyLocation.streetAddressString
        addressLine2.text = itinCardDataHotel.propertyLocation.toTwoLineAddressFormattedString()
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
