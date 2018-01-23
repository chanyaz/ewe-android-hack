package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.vm.traveler.AbstractUniversalCKOTravelerEntryWidgetViewModel

class LXTravelerEntryWidget(context: Context, attrs: AttributeSet?) : AbstractTravelerEntryWidget(context, attrs) {
    override fun setUpViewModel(vm: AbstractUniversalCKOTravelerEntryWidgetViewModel) {
    }

    override fun inflateWidget() {
        View.inflate(context, R.layout.lx_traveler_entry_widget, this)
    }
}
