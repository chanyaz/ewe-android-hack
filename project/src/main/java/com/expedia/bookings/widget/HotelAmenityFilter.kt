package com.expedia.bookings.widget

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.utils.FilterAmenity
import com.expedia.bookings.utils.bindView
import com.expedia.util.endlessObserver
import com.expedia.vm.hotel.BaseHotelFilterViewModel
import io.reactivex.Observer
import kotlin.properties.Delegates

class HotelAmenityFilter(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    val amenityTextView: TextView by bindView(R.id.amenity_label)
    val amenityIconView: ImageView by bindView(R.id.amenity_icon)
    var amenitySelected: Boolean = false
    var amenity: FilterAmenity ?= null
    var amenityId: Int ? = null

    var hotelFilterViewModel: BaseHotelFilterViewModel by Delegates.notNull()

    fun bind(amenity: FilterAmenity, id:Int, vm: BaseHotelFilterViewModel) {
        this.hotelFilterViewModel = vm
        this.amenity = amenity
        this.amenityId = id
        amenityTextView.text = context.getString(amenity.strId)
        amenityTextView.setTextColor(R.color.hotelsv2_checkout_text_color)
        val drawable = ContextCompat.getDrawable(context, amenity.resId)
        DrawableCompat.setTint(drawable, R.color.hotelsv2_checkout_text_color)
        amenityIconView.setImageDrawable(drawable)
    }

    val selectObserver : Observer<Unit> = endlessObserver {
        amenitySelected = !amenitySelected
        if (amenitySelected) {
            changeColor(ContextCompat.getColor(context, R.color.hotels_primary_color))
        } else {
            changeColor(ContextCompat.getColor(context, R.color.hotelsv2_checkout_text_color))
        }
        amenityId?.let {
            hotelFilterViewModel.selectAmenity.onNext(it)
        }
    }

    fun changeColor(color: Int){
        amenityTextView.setTextColor(color)
        amenityIconView.setColorFilter(color)
    }
}
