package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.expedia.bookings.utils.RailUtils
import com.expedia.bookings.widget.RailLegOptionViewModel
import rx.Observable
import rx.subjects.PublishSubject

class RailOutboundHeaderViewModel(context: Context) : RailLegOptionViewModel(context) {
    //input
    val offerSubject = PublishSubject.create<RailSearchResponse.RailOffer>()

    //output
    val offerPriceObservable = Observable.combineLatest(offerSubject, cheapestLegPriceObservable,
            {offer, cheapestPrice -> calculatePrice(offer, cheapestPrice) })

    private fun calculatePrice(offer: RailSearchResponse.RailOffer, cheapestPrice: Money?): String {
        if (cheapestPrice != null) {
            return RailUtils.addAndFormatMoney(offer.totalPrice, cheapestPrice)
        } else {
            return offer.totalPrice.formattedPrice
        }
    }
}