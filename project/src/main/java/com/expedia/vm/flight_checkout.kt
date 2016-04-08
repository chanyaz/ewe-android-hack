package com.expedia.vm

import android.content.Context
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import com.expedia.bookings.R
import com.expedia.bookings.data.BaseCheckoutParams
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.utils.StrUtils
import com.squareup.phrase.Phrase
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class FlightCheckoutViewModel(val context: Context, val flightServices: FlightServices) {

    val tripResponseObservable = BehaviorSubject.create<FlightCreateTripResponse>()
    val baseParams = PublishSubject.create<BaseCheckoutParams>()

    // Outputs
    val depositPolicyText = PublishSubject.create<Spanned>()
    val legalText = PublishSubject.create<SpannableStringBuilder>()
    val sliderPurchaseTotalText = PublishSubject.create<CharSequence>()

    init {
        tripResponseObservable.subscribe {

            var depositText = Html.fromHtml("")
            depositPolicyText.onNext(depositText)

            legalText.onNext(SpannableStringBuilder(PointOfSale.getPointOfSale().stylizedFlightBookingStatement))
            sliderPurchaseTotalText.onNext(Phrase.from(context, R.string.your_card_will_be_charged_template).put("dueamount", it.getTripTotal().formattedPrice).format())
        }
    }

}

