package com.expedia.bookings.hotel.widget

import android.content.Context
import android.support.annotation.VisibleForTesting
import android.support.v7.widget.GridLayout
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import android.util.TypedValue
import com.expedia.bookings.hotel.data.Amenity

interface OnHotelAmenityFilterChangedListener {
    fun onHotelAmenityFilterChanged(amenity: Amenity, selected: Boolean, doTracking: Boolean)
}

class HotelAmenityGridItem(context: Context, val amenity: Amenity) : LinearLayout(context, null) {

    @VisibleForTesting
    val icon by bindView<ImageView>(R.id.hotel_filter_grid_item_image)
    @VisibleForTesting
    val textView by bindView<TextView>(R.id.hotel_filter_grid_item_text)

    private var listener: OnHotelAmenityFilterChangedListener? = null

    init {
        View.inflate(context, R.layout.hotel_amenity_grid_item, this)
        orientation = VERTICAL

        val params = GridLayout.LayoutParams(GridLayout.spec(GridLayout.UNDEFINED, 1f),
                GridLayout.spec(GridLayout.UNDEFINED, 1f))
        params.width = 0
        val sideMargins = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f,
                context.resources.displayMetrics).toInt()
        params.setMargins(sideMargins, params.topMargin, sideMargins, params.bottomMargin)
        layoutParams = params

        icon.setImageResource(amenity.drawableRes)
        icon.setOnClickListener {
            icon.isSelected = !icon.isSelected
            listener?.onHotelAmenityFilterChanged(amenity, icon.isSelected, true)
        }

        amenity.filterDescriptionId?.let { id -> textView.text = context.getString(id) }
    }

    fun setOnHotelAmenityFilterChangedListener(listener: OnHotelAmenityFilterChangedListener?) {
        this.listener = listener
    }

    fun select() {
        icon.isSelected = true
        listener?.onHotelAmenityFilterChanged(amenity, icon.isSelected, false)
    }

    fun deselect() {
        icon.isSelected = false
    }

    fun disable() {
        icon.isEnabled = false
        textView.isEnabled = false
    }

    fun enable() {
        icon.isEnabled = true
        textView.isEnabled = true
    }

    fun isAmenityEnabled(): Boolean {
        return icon.isEnabled
    }
}
