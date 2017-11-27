package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.vm.traveler.AbstractUniversalCKOTravelerEntryWidgetViewModel

class HotelTravelerEntryWidget(context: Context, attrs: AttributeSet?) : AbstractTravelerEntryWidget(context, attrs) {
    override fun setUpViewModel(vm: AbstractUniversalCKOTravelerEntryWidgetViewModel) {
//        TODO setup subscriptions and other VM logic specific to Hotels
    }

    override fun inflateWidget() {
        View.inflate(context, R.layout.lx_traveler_entry_widget, this)
    }
}
