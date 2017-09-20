package com.expedia.bookings.itin.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoHelper
import com.expedia.bookings.data.HotelMedia
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView

class HotelItinImage(context: Context, attr: AttributeSet?) : LinearLayout(context, attr) {

    val hotelImageView: ImageView by bindView(R.id.hotel_image)
    val hotelNameTextView: TextView by bindView(R.id.hotel_name)

    init {
        View.inflate(context, R.layout.hotel_itin_image, this)
    }

    fun setUpWidget(itinCardDataHotel: ItinCardDataHotel) {
        if (itinCardDataHotel.property.thumbnail.originalUrl.isNotBlank()) {
            val hotelMedia = HotelMedia(itinCardDataHotel.property.thumbnail.originalUrl)
            PicassoHelper.Builder(hotelImageView)
                    .setPlaceholder(R.drawable.room_fallback)
                    .fit()
                    .centerCrop()
                    .build()
                    .load(hotelMedia.getBestUrls(Ui.getScreenSize(context).x / 2))
        }
        hotelNameTextView.text = itinCardDataHotel.propertyName
    }
}
