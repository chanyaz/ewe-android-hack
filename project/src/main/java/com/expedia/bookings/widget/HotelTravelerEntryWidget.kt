package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.CheckBox
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.util.subscribeChecked
import com.expedia.util.subscribeText
import com.expedia.util.subscribeVisibility
import com.expedia.vm.traveler.AbstractUniversalCKOTravelerEntryWidgetViewModel
import com.expedia.vm.traveler.HotelTravelerEntryWidgetViewModel

class HotelTravelerEntryWidget(context: Context, attrs: AttributeSet?) : AbstractTravelerEntryWidget(context, attrs) {

    val merchandiseOptCheckBox: CheckBox by bindView(R.id.merchandise_guest_opt_checkbox)

    override fun setUpViewModel(vm: AbstractUniversalCKOTravelerEntryWidgetViewModel) {
        vm as HotelTravelerEntryWidgetViewModel
        vm.checkboxTextSubject.subscribeText(merchandiseOptCheckBox)
        vm.checkBoxVisibilitySubject.subscribeVisibility(merchandiseOptCheckBox)
        vm.emailOptInSubject.subscribeChecked(merchandiseOptCheckBox)
        vm.createTripOptInStatus.subscribe(vm.optInEmailStatusSubject)
    }

    override fun inflateWidget() {
        View.inflate(context, R.layout.hotel_traveler_entry_widget, this)
    }
}
