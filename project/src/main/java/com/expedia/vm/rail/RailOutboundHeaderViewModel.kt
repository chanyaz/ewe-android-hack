package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.rail.responses.RailOffer
import com.expedia.bookings.utils.rail.RailUtils
import com.expedia.bookings.widget.RailLegOptionViewModel
import rx.Observable

class RailOutboundHeaderViewModel(context: Context) : RailLegOptionViewModel(context, false) {
    //output
    val offerPriceObservable = Observable.combineLatest(offerSubject, cheapestLegPriceObservable,
            { offer, cheapestPrice -> calculatePrice(offer, cheapestPrice) })

    private fun calculatePrice(offer: RailOffer, cheapestPrice: Money?): String {
        if (cheapestPrice == null || offer.isOpenReturn) {
            return offer.totalPrice.formattedPrice
        }
        return RailUtils.addAndFormatMoney(offer.totalPrice, cheapestPrice)
    }
}