package com.expedia.bookings.hotel.map

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.extensions.isShowAirAttached
import com.expedia.bookings.features.Features
import com.expedia.bookings.hotel.widget.adapter.priceFormatter
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.TextView
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.ui.IconGenerator

class HotelMarkerIconGenerator(context: Context) {

    val iconFactory = IconGenerator(context)

    val hotelMarkerTextView: TextView by lazy {
        LayoutInflater.from(context).inflate(R.layout.fav_hotel_marker, null) as TextView
    }

    fun getBitmap(context: Context, isSelected: Boolean, isAirAttached: Boolean, isSoldOut: Boolean): Drawable {
        val tooltipDrawable = if (isSoldOut) {
            R.drawable.sold_out_pin
        } else if (isAirAttached) {
            if (isSelected) {
                R.drawable.hotel_tooltip_airattach_selected
            } else {
                R.drawable.hotel_tooltip_airattach
            }
        } else if (isSelected) {
            Ui.obtainThemeResID(context, R.attr.hotel_map_tooltip_pressed_drawable)
        } else {
            Ui.obtainThemeResID(context, R.attr.hotel_map_tooltip_drawable)
        }
        return ContextCompat.getDrawable(context, tooltipDrawable)
    }

    fun createHotelMarkerIcon(context: Context, hotel: Hotel, isSelected: Boolean): BitmapDescriptor {
        val hotelPriceText =
                if (hotel.isSoldOut) {
                    context.getString(R.string.sold_out)
                } else {
                    priceFormatter(context.resources, hotel.lowRateInfo, false, !hotel.isPackage)
                }
        val shouldShowAirAttached = if (hotel.isSoldOut || isGenericAttachEnabled()) false else hotel.lowRateInfo.isShowAirAttached()
        val outputBitmap = getBitmap(context, isSelected, shouldShowAirAttached, hotel.isSoldOut)
        iconFactory.setBackground(outputBitmap)
        hotelMarkerTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        iconFactory.setTextAppearance(R.style.MarkerTextAppearance)
        return BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(hotelPriceText.toString()))
    }

    private fun isGenericAttachEnabled(): Boolean {
        return Features.all.genericAttach.enabled()
    }
}
