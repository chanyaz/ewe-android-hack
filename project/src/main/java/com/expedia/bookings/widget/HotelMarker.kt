package com.expedia.bookings.widget

import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.NinePatchDrawable
import android.util.TypedValue
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.widget.HotelMarkerPreviewAdapter
import com.expedia.bookings.widget.priceFormatter
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

fun createHotelMarker(resources: Resources, hotel: Hotel, clicked: Boolean): BitmapDescriptor {
    val markerPaddingWidth = resources.getDimensionPixelSize(R.dimen.hotel_marker_padding_width)
    val markerPaddingHeight = resources.getDimensionPixelSize(R.dimen.hotel_marker_padding_height)

    var bounds = Rect()
    var text = priceFormatter(hotel, false)
    var paint: Paint = Paint()
    paint.setColor(Color.WHITE)
    paint.setAntiAlias(true)
    val pxSize: Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 13f, resources.getDisplayMetrics())
    paint.setTextSize(pxSize)
    paint.setTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR))
    paint.setStyle(Paint.Style.FILL)
    paint.getTextBounds(text, 0, text.length(), bounds)

    var outputBitmap = getBitmap(resources, clicked)
    var chunk = outputBitmap.getNinePatchChunk()
    val drawable = NinePatchDrawable(resources, outputBitmap, chunk, Rect(), null)
    val height = bounds.height() + markerPaddingHeight + markerPaddingHeight / 2 + 6
    drawable.setBounds(0, 0, bounds.width() + markerPaddingWidth, height)

    var bitmap = Bitmap.createBitmap(bounds.width() + markerPaddingWidth, height, Bitmap.Config.ARGB_8888)
    var canvas: Canvas = Canvas(bitmap)
    drawable.draw(canvas)

    canvas.drawText(text, markerPaddingWidth / 2f, markerPaddingHeight / 2f + bounds.height(), paint)

    return BitmapDescriptorFactory.fromBitmap(bitmap)

}

fun getBitmap(resources: Resources, clicked: Boolean): Bitmap {
    if (clicked) {
        return BitmapFactory.decodeResource(resources, R.drawable.hotel_tooltip_blue)
    }
    return BitmapFactory.decodeResource(resources, R.drawable.hotel_tooltip)
}
