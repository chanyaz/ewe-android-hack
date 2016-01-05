package com.expedia.bookings.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.Hotel
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.ui.IconGenerator

fun createHotelMarkerIcon(context: Context, factory: IconGenerator, hotel: Hotel, isSelected: Boolean, isAirAttached: Boolean, isSoldOut: Boolean): BitmapDescriptor {
    var hotelPriceText = priceFormatter(context.resources, hotel.lowRateInfo, false)
    var outputBitmap = getBitmap(context, isSelected, isAirAttached, isSoldOut)
    factory.setBackground(outputBitmap)
    factory.setTextAppearance(R.style.MarkerTextAppearance)
    return BitmapDescriptorFactory.fromBitmap(factory.makeIcon(hotelPriceText.toString()))
}

fun createHotelMarkerIcon(context: Context, factory: IconGenerator, hotel: com.expedia.bookings.data.packages.Hotel, isSelected: Boolean): BitmapDescriptor {
    var hotelPriceText = priceFormatter(context.resources, hotel.packageOfferModel.price.pricePerPerson, false)
    var outputBitmap = getBitmap(context, isSelected, false, false)
    factory.setBackground(outputBitmap)
    factory.setTextAppearance(R.style.MarkerTextAppearance)
    return BitmapDescriptorFactory.fromBitmap(factory.makeIcon(hotelPriceText.toString()))
}

fun getBitmap(context: Context, isSelected: Boolean, isAirAttached: Boolean, isSoldOut: Boolean): Drawable {

    val tooltipDrawable = if (isSoldOut) {
        R.drawable.sold_out_pin
    } else if (isAirAttached) {
        R.drawable.hotel_tooltip_airattach
    } else if (isSelected) {
        R.drawable.hotel_tooltip_blue
    } else {
        R.drawable.hotel_tooltip
    }

    return ContextCompat.getDrawable(context, tooltipDrawable)
}
