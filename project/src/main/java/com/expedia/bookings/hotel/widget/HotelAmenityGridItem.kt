package com.expedia.bookings.hotel.widget

import android.content.Context
import android.support.v7.widget.GridLayout
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import android.util.TypedValue
import com.expedia.bookings.hotel.data.Amenity
import io.reactivex.subjects.PublishSubject

class HotelAmenityGridItem(context: Context, val amenity: Amenity) : LinearLayout(context, null) {
    val amenitySelected = PublishSubject.create<Amenity>()
    val amenityDeselected = PublishSubject.create<Amenity>()

    private val icon by bindView<ImageView>(R.id.hotel_filter_grid_item_image)
    private val textView by bindView<TextView>(R.id.hotel_filter_grid_item_text)

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
            if (icon.isSelected) {
                amenitySelected.onNext(amenity)
            } else {
                amenityDeselected.onNext(amenity)
            }
        }

        amenity.filterDescriptionId?.let { id -> textView.text = context.getString(id) }
    }

    fun select() {
        icon.isSelected = true
        amenitySelected.onNext(amenity)
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
