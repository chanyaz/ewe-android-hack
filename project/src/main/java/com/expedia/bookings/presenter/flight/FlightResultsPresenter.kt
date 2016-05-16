package com.expedia.bookings.presenter.flight

import android.content.Context
import android.util.AttributeSet

abstract class FlightResultsPresenter(context: Context, attrs: AttributeSet) : BaseFlightPresenter(context, attrs) {

    abstract fun isOutboundResultsPresenter(): Boolean
}
