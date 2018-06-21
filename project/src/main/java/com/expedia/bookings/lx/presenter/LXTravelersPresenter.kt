package com.expedia.bookings.lx.presenter

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.presenter.shared.AbstractTravelersPresenter
import com.expedia.bookings.lx.widget.LXTravelerEntryWidget
import com.expedia.vm.traveler.TravelersViewModel

class LXTravelersPresenter(context: Context, attrs: AttributeSet) : AbstractTravelersPresenter(context, attrs) {

    private val selectToEntryLX = object : SelectToEntryTransition(LXTravelerEntryWidget::class.java) {}

    init {
        addTransition(selectToEntryLX)
    }

    override fun setUpTravelersViewModel(vm: TravelersViewModel) {
    }

    override fun inflateTravelersView() {
        View.inflate(context, R.layout.simple_traveler_presenter, this)
    }
}
