package com.expedia.vm.flights

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.FlightTripDetails.PassengerCategory
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.utils.Ui
import com.expedia.vm.BaseCostSummaryBreakdownViewModel
import com.squareup.phrase.Phrase
import rx.subjects.PublishSubject
import java.util.Collections

class FlightCostSummaryBreakdownViewModel(context: Context) : BaseCostSummaryBreakdownViewModel(context) {
    val flightCostSummaryObservable = PublishSubject.create<FlightCreateTripResponse>()

    init {
        flightCostSummaryObservable.subscribe { tripResponse ->
            val breakdowns = arrayListOf<CostSummaryBreakdown>()
            val flightDetails = tripResponse.details
            val pricePerPassengerList = flightDetails.offer.pricePerPassengerCategory
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
                breakdowns.add(CostSummaryBreakdown.CostSummaryBuilder().title(travelerInfo).cost(passenger.totalPrice.formattedPrice).build())

                breakdowns.add(CostSummaryBreakdown.CostSummaryBuilder().title(context.getString(R.string.Flight)).cost(passenger.basePrice.formattedPrice).build())

                title = context.getString(R.string.cost_summary_breakdown_taxes_fees)
                breakdowns.add(CostSummaryBreakdown.CostSummaryBuilder().title(title).cost(passenger.taxesPrice.formattedPrice).build())

                // insurance
                if (flightDetails.offer.selectedInsuranceProduct != null) {
                    val insurance = flightDetails.offer.selectedInsuranceProduct
                    title = context.getString(R.string.cost_summary_breakdown_flight_insurance)
                    breakdowns.add(CostSummaryBreakdown.CostSummaryBuilder().title(title)
                            .cost(insurance.totalPrice.formattedPrice)
                            .color(Ui.obtainThemeColor(context, R.attr.primary_color)).build())
                }

                // Adding divider line
                breakdowns.add(CostSummaryBreakdown.CostSummaryBuilder().isLine(true).build())

            }

            if (flightDetails.offer.fees != null) {
                title = Phrase.from(context, R.string.brand_booking_fee).put("brand", ProductFlavorFeatureConfiguration.getInstance().getPOSSpecificBrandName(context)).format().toString()
                breakdowns.add(CostSummaryBreakdown.CostSummaryBuilder().title(title).cost(flightDetails.offer.bookingFee.formattedMoney).build())

                // Adding divider line
                breakdowns.add(CostSummaryBreakdown.CostSummaryBuilder().isLine(true).build())
            }

            title = context.getString(R.string.cost_summary_breakdown_total_due_today)
            val totalPrice = tripResponse.totalPrice ?: tripResponse.details.offer.totalFarePrice // TODO - priceChange checkout response does not return totalPrice field!!
            breakdowns.add(CostSummaryBreakdown.CostSummaryBuilder().title(title).cost(totalPrice.formattedPrice).build())

            addRows.onNext(breakdowns)
            iconVisibilityObservable.onNext(true)
        }
    }

}