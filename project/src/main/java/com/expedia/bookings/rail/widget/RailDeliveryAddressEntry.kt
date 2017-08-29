package com.expedia.bookings.rail.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.RailLocation
import com.expedia.bookings.section.RailDeliverySpinnerWithValidationIndicator
import com.expedia.bookings.section.SectionLocation
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.accessibility.AccessibleEditText
import com.expedia.bookings.widget.accessibility.AccessibleSpinner
import rx.subjects.PublishSubject

class RailDeliveryAddressEntry(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs), EntryManager.FormListener {
    val mailDeliverySectionLocation: SectionLocation by bindView(R.id.mail_delivery_address)
    val railDeliverySpinner: RailDeliverySpinnerWithValidationIndicator by bindView(R.id.edit_delivery_option_spinner)
    val addressLineOne: AccessibleEditText by bindView(R.id.edit_address_line_one)
    val addressCity: AccessibleEditText by bindView(R.id.edit_address_city)
    val addressPostalCode: AccessibleEditText by bindView(R.id.edit_address_postal_code)

    val entryManager: EntryManager
    val formsFilledInSubject = PublishSubject.create<Boolean>()

    init {
        View.inflate(context, R.layout.section_rail_mail_delivery_address, this)
        mailDeliverySectionLocation.setLineOfBusiness(LineOfBusiness.RAILS)
        entryManager = EntryManager(listOf(addressLineOne, addressCity, addressPostalCode), this)
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        entryManager.visibilityChanged(visibility == VISIBLE && isShown)
    }

    override fun formFocusChanged(view: View, hasFocus: Boolean) {
        if (hasFocus) {
            mailDeliverySectionLocation.resetValidation(view.id, true)
        } else {
            mailDeliverySectionLocation.validateField(view.id)
        }
        formsFilledInSubject.onNext(areFormsFilledIn())
    }

    override fun formFieldChanged() {
        formsFilledInSubject.onNext(areFormsFilledIn())
    }

    fun isValid() : Boolean {
        return mailDeliverySectionLocation.performValidation()
    }

    fun areFormsFilledIn() : Boolean {
        return addressLineOne.text.isNotEmpty()
                && addressCity.text.isNotEmpty()
                && addressPostalCode.text.isNotEmpty()
                && railDeliverySpinner.hasItemSelected()
    }

    fun focusNext() {
        val currentFocus: View? = findFocus()
        var nextFocus = currentFocus?.focusSearch(FOCUS_FORWARD)
        nextFocus?.requestFocus() ?: addressLineOne.requestFocus()
    }

    fun getLocation() : RailLocation {
        return mailDeliverySectionLocation.location as RailLocation
    }
}