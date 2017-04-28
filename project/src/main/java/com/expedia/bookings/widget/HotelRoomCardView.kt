package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView

class HotelRoomCardView(context: Context, attrs: AttributeSet) : CardView(context, attrs) {

    val hotelRoomInfoContainer: LinearLayout by bindView(R.id.hotel_room_info_container)

    fun addViewToContainer(view: View) {
        hotelRoomInfoContainer.addView(view)
    }
}
