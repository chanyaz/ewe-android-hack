package com.expedia.bookings.widget.rail

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.expedia.bookings.utils.bindView
import rx.subjects.PublishSubject

class RailOfferView : FrameLayout {

    val priceView: TextView by bindView(R.id.price)
    val fareTitle: TextView by bindView(R.id.fare_title)
    val fareDescription: TextView by bindView(R.id.fare_description)
    val selectButton: View by bindView(R.id.select_button)

    constructor(context: Context, offer: RailSearchResponse.RailOffer, offerSelectedObservable: PublishSubject<RailSearchResponse.RailOffer>) : super(context) {
        View.inflate(context, R.layout.widget_rail_details_fare_option, this)
        priceView.text = offer.totalPrice.formattedDisplayPrice
        fareTitle.text = offer.railProductList.first().aggregatedCarrierServiceClassDisplayName
        fareDescription.text = offer.railProductList.first().aggregatedFareDescription

        selectButton.setOnClickListener { offerSelectedObservable.onNext(offer) }
    }
}
