package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.data.Money

class RailInboundDetailsViewModel(context: Context) : BaseRailDetailsViewModel(context) {
    var lowestOfferPrice: Money? = null

    init {
        railLegOptionSubject.subscribe { lowestOfferPrice = (it.bestPrice) }
    }

    //TODO https://eiwork.mingle.thoughtworks.com/projects/eb_ad_app/cards/9161
    override fun getComparePrice(): Money? {
        return null
    }
}