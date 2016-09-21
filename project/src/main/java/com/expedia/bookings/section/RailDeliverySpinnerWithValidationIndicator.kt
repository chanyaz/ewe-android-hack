package com.expedia.bookings.section

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView

class RailDeliverySpinnerWithValidationIndicator(context: Context, attrs: AttributeSet?): LinearLayout(context, attrs) {

    val spinner: Spinner by bindView(R.id.spinner)
    val validationIndicator: ImageView by bindView(R.id.validation_indicator)

    init {
        View.inflate(context, R.layout.rail_delivery_spinner_with_validation_indicator, this)
    }
}