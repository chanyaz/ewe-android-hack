package com.expedia.bookings.widget

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.NinePatchDrawable
import android.util.TypedValue
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.utils.FontCache
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

fun createHotelMarkerIcon(resources: Resources, hotel: Hotel, isSelected: Boolean, isAirAttached: Boolean, isSoldOut: Boolean): BitmapDescriptor {
    val markerPaddingWidth = resources.getDimensionPixelSize(R.dimen.hotel_marker_padding_width)
    val markerPaddingHeight = resources.getDimensionPixelSize(R.dimen.hotel_marker_padding_height)

    var bounds = Rect()
    var hotelPriceText = priceFormatter(resources, hotel.lowRateInfo, false)
    var paint: Paint = Paint()
    paint.color = Color.WHITE
    paint.isAntiAlias = true
    val pxSize: Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 13f, resources.displayMetrics)
    paint.textSize = pxSize
    paint.setTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR))
    paint.style = Paint.Style.FILL
    paint.getTextBounds(hotelPriceText.toString(), 0, hotelPriceText.length, bounds)
    
    var outputBitmap = getBitmap(resources, isSelected, isAirAttached, isSoldOut)
    var chunk = outputBitmap.getNinePatchChunk()

    val drawable = NinePatchDrawable(resources, outputBitmap, chunk, Rect(), null)
    val height = bounds.height() + markerPaddingHeight + markerPaddingHeight / 2 + 6
    drawable.setBounds(0, 0, bounds.width() + markerPaddingWidth, height)

    var bitmap = Bitmap.createBitmap(bounds.width() + markerPaddingWidth, height, Bitmap.Config.ARGB_8888)
    var canvas: Canvas = Canvas(bitmap)
    drawable.draw(canvas)

    canvas.drawText(hotelPriceText.toString(), markerPaddingWidth / 2f, markerPaddingHeight / 2f + bounds.height(), paint)

    return BitmapDescriptorFactory.fromBitmap(bitmap)

}

fun getBitmap(resources: Resources, isSelected: Boolean, isAirAttached: Boolean, isSoldOut: Boolean): Bitmap {

    val tooltipDrawable = if (isSoldOut) {
        R.drawable.sold_out_pin
    } else if (isAirAttached) {
        R.drawable.hotel_tooltip_airattach
    } else if (isSelected) {
        R.drawable.hotel_tooltip_blue
    } else {
        R.drawable.hotel_tooltip
    }

    return BitmapFactory.decodeResource(resources, tooltipDrawable)
}
