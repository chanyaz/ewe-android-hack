package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.RailPassenger
import com.expedia.bookings.data.rail.responses.BaseRailOffer
import com.expedia.bookings.data.rail.responses.RailCreateTripResponse
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.vm.BaseCostSummaryBreakdownViewModel
import com.squareup.phrase.Phrase
import rx.subjects.PublishSubject
import java.util.ArrayList

class RailCostSummaryBreakdownViewModel(context: Context) : BaseCostSummaryBreakdownViewModel(context) {
    val railCostSummaryBreakdownObservable = PublishSubject.create<RailCreateTripResponse.RailTripOffer>()

    init {
        railCostSummaryBreakdownObservable.subscribe { offer ->
            val breakdowns = arrayListOf<CostSummaryBreakdownRow>()

            addPassengerPriceBreakdown(breakdowns, offer.passengerList)
            addFees(breakdowns, offer.priceBreakdownByCode)

            // Adding divider line
            breakdowns.add(CostSummaryBreakdownRow.Builder().separator())

            var title = context.getString(R.string.total)
            breakdowns.add(CostSummaryBreakdownRow.Builder().title(title).cost(offer.totalPrice.formattedPrice).build())

            addRows.onNext(breakdowns)
            iconVisibilityObservable.onNext(true)
        }
    }

    private fun addFees(breakdowns: ArrayList<CostSummaryBreakdownRow>, priceBreakdowns: Map<BaseRailOffer.PriceCategoryCode, BaseRailOffer.PriceBreakdown>) {
        val ticketDeliveryFee = priceBreakdowns[BaseRailOffer.PriceCategoryCode.TICKET_DELIVERY]
        if (ticketDeliveryFee != null) {
            breakdowns.add(CostSummaryBreakdownRow.Builder()
                    .title(context.getString(R.string.rail_ticket_delivery_fee))
                    .cost(ticketDeliveryFee.formattedPrice).build())
        }

        val creditCardFee = priceBreakdowns[BaseRailOffer.PriceCategoryCode.CREDIT_CARD_FEE]
        if (creditCardFee != null) {
            breakdowns.add(CostSummaryBreakdownRow.Builder()
                    .title(context.getString(R.string.rail_credit_card_fee))
                    .cost(creditCardFee.formattedPrice).build())
        }

        val bookingFee = priceBreakdowns[BaseRailOffer.PriceCategoryCode.EXPEDIA_SERVICE_FEE]
        if (bookingFee != null) {
            val title = Phrase.from(context, R.string.brand_booking_fee)
                    .put("brand", ProductFlavorFeatureConfiguration.getInstance().getPOSSpecificBrandName(context))
                    .format().toString()
            breakdowns.add(CostSummaryBreakdownRow.Builder().title(title).cost(bookingFee.formattedPrice).build())
        }
    }

    private fun addPassengerPriceBreakdown(breakdowns: ArrayList<CostSummaryBreakdownRow>, passengers: MutableList<RailPassenger>) {
        var title: String
        var travelerNumber: Int = 0
        passengers.forEach { passenger ->
            when (passenger.passengerAgeGroup) {
                RailPassenger.PassengerAgeGroup.ADULT -> {
                    title = Phrase.from(context, R.string.rail_adult_number_TEMPLATE).put("number", ++travelerNumber).format().toString()
                }

                RailPassenger.PassengerAgeGroup.CHILD -> {
                    title = Phrase.from(context, R.string.rail_child_number_TEMPLATE).put("number", ++travelerNumber).format().toString()
                }

                RailPassenger.PassengerAgeGroup.YOUTH -> {
                    title = Phrase.from(context, R.string.rail_youth_number_TEMPLATE).put("number", ++travelerNumber).format().toString()
                }

                RailPassenger.PassengerAgeGroup.SENIOR -> {
                    title = Phrase.from(context, R.string.rail_senior_number_TEMPLATE).put("number", ++travelerNumber).format().toString()
                }
            }
            breakdowns.add(CostSummaryBreakdownRow.Builder().title(title).cost(passenger.price.formattedPrice).build())
        }
    }
}