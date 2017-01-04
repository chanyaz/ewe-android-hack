package com.expedia.bookings.fragment

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.presenter.Presenter

class ItinCardDetailsPresenter(context: Context, attributeSet: AttributeSet) : Presenter(context, attributeSet) {

    init {
        View.inflate(context, R.layout.itin_card_details, this)
    }

}
