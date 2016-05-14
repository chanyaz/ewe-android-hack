package com.expedia.vm

import android.content.Context
import android.text.Html
import android.text.SpannableStringBuilder
import com.expedia.bookings.R
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.services.FlightServices
import com.squareup.phrase.Phrase

class FlightCheckoutViewModel(context: Context, val flightServices: FlightServices) : BaseCheckoutViewModel(context) {

    init {
        tripResponseObservable.subscribe {

            var depositText = Html.fromHtml("")
            depositPolicyText.onNext(depositText)

            legalText.onNext(SpannableStringBuilder(PointOfSale.getPointOfSale().stylizedFlightBookingStatement))
            sliderPurchaseTotalText.onNext(Phrase.from(context, R.string.your_card_will_be_charged_template).put("dueamount", it.getTripTotalExcludingFee().formattedPrice).format())
        }
    }
}

