package com.expedia.bookings.widget.itin

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.squareup.phrase.Phrase

class HotelItinRoomDetails(context: Context, attr: AttributeSet?) : LinearLayout(context, attr) {
    val roomDetailsText: TextView by bindView(R.id.itin_hotel_details_room_details_text)
    val roomDetailsHeaderText: TextView by bindView(R.id.itin_hotel_room_details_header)

    init {
        View.inflate(context, R.layout.widget_hotel_itin_room_details, this)
    }

    fun setUpWidget(itinCardDataHotel: ItinCardDataHotel) {
        roomDetailsText.text = Phrase.from(context, R.string.itin_hotel_details_room_details_text_TEMPLATE)
                .put("roomtype", itinCardDataHotel.property.itinRoomType)
                .put("bedtype", itinCardDataHotel.property.itinBedType).format().toString()
    }
}
