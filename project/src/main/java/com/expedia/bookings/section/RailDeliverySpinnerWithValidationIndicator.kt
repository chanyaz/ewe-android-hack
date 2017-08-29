package com.expedia.bookings.section

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.SpinnerAdapterWithHint
import com.expedia.bookings.widget.accessibility.AccessibleSpinner

class RailDeliverySpinnerWithValidationIndicator(context: Context, attrs: AttributeSet?): LinearLayout(context, attrs) {

    val spinner: AccessibleSpinner by bindView(R.id.rail_delivery_spinner)
    val validationIndicator: ImageView by bindView(R.id.validation_indicator)

    private val spinnerFocusListener = SpinnerFocusListener()
    val hint = context.resources.getString(R.string.address_mail_delivery_option_hint)

    init {
        View.inflate(context, R.layout.rail_delivery_spinner_with_validation_indicator, this)
        spinner.isFocusableInTouchMode = true // Does not work when set from xml.
    }

    override fun onVisibilityChanged(changedView: View?, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        spinner.onFocusChangeListener = if (visibility == VISIBLE) spinnerFocusListener else null
    }

    fun hasItemSelected() : Boolean {
        val selection = spinner.selectedItem as SpinnerAdapterWithHint.SpinnerItem
        return !hint.equals(selection.value)
    }

    private class SpinnerFocusListener : View.OnFocusChangeListener {
        override fun onFocusChange(v: View, hasFocus: Boolean) {
            if (v is Spinner && hasFocus) {
                v.performClick()
            }
        }
    }
}