package com.expedia.bookings.widget.itin

import android.content.Context
import android.support.annotation.VisibleForTesting
import android.support.design.widget.CoordinatorLayout
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView

class HotelItinRoomAmenity(context: Context) : CoordinatorLayout(context) {
    private val params = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1.0f)
    private val amenityIcon : ImageView by bindView(R.id.itin_hotel_room_amenity_icon)
    private val amenityLabel: TextView by bindView(R.id.itin_hotel_room_amenity_label)

    init {
        View.inflate(context, R.layout.widget_itin_hotel_room_amenity, this)
    }

    fun setUp(label: String, icon: Int) {
        this.layoutParams = params
        amenityIcon.setImageResource(icon)
        amenityLabel.text = label
    }

    @VisibleForTesting (otherwise = VisibleForTesting.PRIVATE)
    fun getLabel() : TextView{
        return amenityLabel
    }

    @VisibleForTesting (otherwise = VisibleForTesting.PRIVATE)
    fun getIcon() : ImageView{
        return amenityIcon
    }
}