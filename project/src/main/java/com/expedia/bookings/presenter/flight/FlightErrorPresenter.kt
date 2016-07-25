package com.expedia.bookings.presenter.flight

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.presenter.BaseErrorPresenter
import com.expedia.vm.flights.FlightErrorViewModel

class FlightErrorPresenter(context: Context, attr: AttributeSet?) : BaseErrorPresenter(context, attr) {

    override fun getViewModel(): FlightErrorViewModel {
        return viewmodel as FlightErrorViewModel
    }
}
