package com.expedia.bookings.presenter.hotel

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.presenter.packages.AbstractTravelersPresenter
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.HotelTravelerEntryWidget
import com.expedia.vm.traveler.TravelersViewModel

class HotelTravelersPresenter(context: Context, attrs: AttributeSet) : AbstractTravelersPresenter(context, attrs) {

    override fun setUpTravelersViewModel(vm: TravelersViewModel) {
//        TODO setup hotels specific traveler subscriptions as needed
    }

    override fun inflateTravelersView() {
        View.inflate(context, R.layout.hotel_traveler_presenter, this)
        val toolbarHeightAndStatusBarHeight = Ui.getStatusBarHeight(context) + Ui.getToolbarSize(context)
        setPadding(0, toolbarHeightAndStatusBarHeight, 0, 0)
    }

    private val selectToHotelEntry = object : SelectToEntryTransition(HotelTravelerEntryWidget::class.java) {}


    init {
        addTransition(selectToHotelEntry)
    }
}
