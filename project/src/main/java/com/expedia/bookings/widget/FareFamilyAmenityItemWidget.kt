package com.expedia.bookings.widget

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.flights.FareFamilyAmenityItemViewModel

class FareFamilyAmenityItemWidget(context: Context, attrs: AttributeSet): LinearLayout(context, attrs) {
    val amenityIcon: ImageView by bindView(R.id.fare_family_amenity_icon)
    val amenityTextIcon: android.widget.TextView by bindView(R.id.fare_family_amenity_text_icon)
    val amenityName: TextView by bindView(R.id.fare_family_amenity_text)

    init {
        View.inflate(context, R.layout.fare_family_amenity_item, this)
    }

    var viewModel: FareFamilyAmenityItemViewModel by notNullAndObservable { vm ->
        val(resourceId, displayVal) = vm.resourceType
        if(!displayVal.isNullOrBlank()) {
            amenityTextIcon.background = ContextCompat.getDrawable(context, R.drawable.flight_upsell_oval_icon)
            amenityTextIcon.text = displayVal
            amenityTextIcon.visibility = View.VISIBLE
            amenityIcon.visibility = View.GONE
        } else {
            amenityIcon.setImageDrawable(ContextCompat.getDrawable(context, resourceId))
            amenityIcon.visibility = View.VISIBLE
            amenityTextIcon.visibility = View.GONE
        }
        amenityName.text = vm.amenityDispName
    }
}