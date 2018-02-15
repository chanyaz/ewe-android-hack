package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.extensions.ObservableOld
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.data.rail.responses.RailOffer
import com.expedia.bookings.rail.util.RailUtils
import com.expedia.bookings.rail.widget.RailLegOptionViewModel

class RailOutboundHeaderViewModel(context: Context) : RailLegOptionViewModel(context, false) {
    //output
    val offerPriceObservable = ObservableOld.combineLatest(offerSubject, cheapestLegPriceObservable,
            { offer, cheapestPrice -> calculatePrice(offer.value, cheapestPrice.value) })

    override fun getContentDescription(legOption: RailLegOption, price: String, stopsAndDuration: String): String {
        val result = StringBuffer(context.getString(R.string.rail_selected_outbound_cont_desc))
        result.append(" ").append(super.getContentDescription(legOption, price, stopsAndDuration))
        return result.toString()
    }

    private fun calculatePrice(offer: RailOffer?, cheapestPrice: Money?): String {
        if (offer == null) {
            throw RuntimeException("RailOffer should not be null")
        }
        if (cheapestPrice == null || offer.isOpenReturn) {
            return offer.totalPrice.formattedPrice
        }
        return RailUtils.addAndFormatMoney(offer.totalPrice, cheapestPrice)
    }
}
