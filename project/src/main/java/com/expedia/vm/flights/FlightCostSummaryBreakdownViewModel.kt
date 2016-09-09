package com.expedia.vm.flights

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.flights.AbstractFlightOfferResponse
import com.expedia.bookings.data.flights.FlightTripDetails.PassengerCategory
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.utils.Ui
import com.expedia.vm.BaseCostSummaryBreakdownViewModel
import com.squareup.phrase.Phrase
import rx.subjects.PublishSubject
import java.util.Collections

class FlightCostSummaryBreakdownViewModel(context: Context) : BaseCostSummaryBreakdownViewModel(context) {
    val flightCostSummaryObservable = PublishSubject.create<TripResponse>()

    init {
        flightCostSummaryObservable.subscribe { tripResponse -> tripResponse as AbstractFlightOfferResponse
            val breakdowns = arrayListOf<CostSummaryBreakdownRow>()
            val flightDetails = tripResponse.getDetails()
            val flightOffer = flightDetails.offer
            val pricePerPassengerList = flightOffer.pricePerPassengerCategory
            var title: String
            var travelerInfo: String = ""
            var numAdultsAdded = 0
            var numChildrenAdded = 0
            var numInfantsInSeat = 0
            var numInfantsInLap = 0

            Collections.sort(pricePerPassengerList)
            pricePerPassengerList.forEachIndexed { index, passenger ->
                when (passenger.passengerCategory) {
                    PassengerCategory.ADULT,
                    PassengerCategory.SENIOR -> {
                        travelerInfo = Phrase.from(context, R.string.flight_add_adult_number_TEMPLATE).put("number", ++numAdultsAdded).format().toString()
                    }

                    PassengerCategory.CHILD,
                    PassengerCategory.ADULT_CHILD -> {
                        travelerInfo = Phrase.from(context, R.string.flight_add_child_number_TEMPLATE).put("number", ++numChildrenAdded).format().toString()
                    }

                    PassengerCategory.INFANT_IN_LAP -> {
                        travelerInfo = Phrase.from(context, R.string.flight_add_infant_in_lap_number_TEMPLATE).put("number", ++numInfantsInSeat).format().toString()
                    }

                    PassengerCategory.INFANT_IN_SEAT -> {
                        travelerInfo = Phrase.from(context, R.string.flight_add_infant_in_seat_number_TEMPLATE).put("number", ++numInfantsInLap).format().toString()
                    }
                }
                breakdowns.add(CostSummaryBreakdownRow.Builder().title(travelerInfo).cost(passenger.totalPrice.formattedPrice).build())

                breakdowns.add(CostSummaryBreakdownRow.Builder().title(context.getString(R.string.Flight)).cost(passenger.basePrice.formattedPrice).build())

                title = context.getString(R.string.cost_summary_breakdown_taxes_fees)
                breakdowns.add(CostSummaryBreakdownRow.Builder().title(title).cost(passenger.taxesPrice.formattedPrice).build())

                // Adding divider line
                breakdowns.add(CostSummaryBreakdownRow.Builder().separator())

            }

            if (flightOffer.fees != null) {
                title = Phrase.from(context, R.string.brand_booking_fee).put("brand", ProductFlavorFeatureConfiguration.getInstance().getPOSSpecificBrandName(context)).format().toString()
                breakdowns.add(CostSummaryBreakdownRow.Builder().title(title).cost(flightOffer.bookingFee.formattedMoney).build())

                // insurance
                if (flightOffer.selectedInsuranceProduct != null) {
                    val insurance = flightOffer.selectedInsuranceProduct
                    val insuranceTitle = context.getString(R.string.cost_summary_breakdown_flight_insurance)

                    breakdowns.add(CostSummaryBreakdownRow.Builder().title(insuranceTitle)
                            .cost(insurance.totalPrice.formattedPrice)
                            .color(Ui.obtainThemeColor(context, R.attr.primary_color))
                            .build())
                }

                val selectedCardFees = tripResponse.selectedCardFees
                if (selectedCardFees != null && !selectedCardFees.isZero) {
                    title = context.getString(R.string.airline_card_fee)
                    val airlineCardFee = selectedCardFees.formattedMoneyFromAmountAndCurrencyCode
                    breakdowns.add(CostSummaryBreakdownRow.Builder().title(title).cost(airlineCardFee).build())
                }

                // Adding divider line
                breakdowns.add(CostSummaryBreakdownRow.Builder().separator())
            }

            title = context.getString(R.string.cost_summary_breakdown_total_due_today)
            val totalPrice = tripResponse.tripTotalPayableIncludingFeeIfZeroPayableByPoints()
            breakdowns.add(CostSummaryBreakdownRow.Builder().title(title).cost(totalPrice.formattedMoneyFromAmountAndCurrencyCode).build())

            addRows.onNext(breakdowns)
            iconVisibilityObservable.onNext(true)
        }
    }
}
