package com.expedia.bookings.rail.presenter

import android.content.Context
import android.util.AttributeSet

class RailInboundDetailsPresenter(context: Context, attrs: AttributeSet) : RailDetailsPresenter(context, attrs) {
    // TODO https://eiwork.mingle.thoughtworks.com/projects/eb_ad_app/cards/9235
    override fun showDeltaPricing(): Boolean {
        return true
    }
}
