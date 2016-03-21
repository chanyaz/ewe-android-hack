package com.expedia.bookings.presenter.rail

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.Ui

class RailCheckoutPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {

    init {
        Ui.getApplication(getContext()).railComponent().inject(this)
        View.inflate(context, R.layout.rail_checkout_presenter, this)
    }
}


