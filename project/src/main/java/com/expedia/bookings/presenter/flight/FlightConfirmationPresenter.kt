package com.expedia.bookings.presenter.flight

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.presenter.Presenter

class FlightConfirmationPresenter(context: Context, attrs: AttributeSet?) : Presenter(context, attrs) {

    init {
        View.inflate(context, R.layout.package_confirmation_presenter, this)
    }
}
