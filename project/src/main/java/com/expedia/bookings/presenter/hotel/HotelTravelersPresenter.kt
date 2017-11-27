package com.expedia.bookings.presenter.hotel

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.presenter.packages.AbstractTravelersPresenter
import com.expedia.vm.traveler.TravelersViewModel

class HotelTravelersPresenter(context: Context, attrs: AttributeSet) : AbstractTravelersPresenter(context, attrs) {

    override fun setUpTravelersViewModel(vm: TravelersViewModel) {
//        TODO setup hotels specific traveler subscriptions as needed
    }

    override fun inflateTravelersView() {
        View.inflate(context, R.layout.hotel_traveler_presenter, this)
    }
}
