package com.expedia.vm.flights

import android.content.Context
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.data.FlightTripResponse
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightTripDetails.PassengerCategory
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.utils.Ui
import com.expedia.vm.BaseCostSummaryBreakdownViewModel
import com.squareup.phrase.Phrase
import rx.subjects.PublishSubject
import java.util.Collections

class FlightCostSummaryBreakdownViewModel(context: Context) : BaseCostSummaryBreakdownViewModel(context) {
    val flightCostSummaryObservable = PublishSubject.create<TripResponse>()

    init {
        flightCostSummaryObservable.subscribe { tripResponse -> tripResponse as FlightTripResponse
            val breakdowns = arrayListOf<CostSummaryBreakdownRow>()
            val flightDetails = tripResponse.details
            val flightOffer = flightDetails.offer
            val pricePerPassengerList = flightOffer.pricePerPassengerCategory
            var title: String
            var travelerInfo: String = ""
            var numAdultsAdded = 0
            var numYouthAdded = 0
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

                    PassengerCategory.ADULT_CHILD -> {
                        if (AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightTravelerFormRevamp)) {
                            travelerInfo = Phrase.from(context, R.string.flight_add_youth_number_TEMPLATE).put("number", ++numYouthAdded).format().toString()
                        } else {
                            travelerInfo = Phrase.from(context, R.string.flight_add_child_number_TEMPLATE).put("number", ++numChildrenAdded).format().toString()
                        }
                    }

                    PassengerCategory.CHILD -> {
                        travelerInfo = Phrase.from(context, R.string.flight_add_child_number_TEMPLATE).put("number", ++numChildrenAdded).format().toString()
                    }

                    PassengerCategory.INFANT_IN_LAP -> {
                        travelerInfo = Phrase.from(context, R.string.flight_add_infant_in_lap_number_TEMPLATE).put("number", ++numInfantsInSeat).format().toString()
                    }

                    PassengerCategory.INFANT_IN_SEAT -> {
                        travelerInfo = Phrase.from(context, R.string.flight_add_infant_in_seat_number_TEMPLATE).put("number", ++numInfantsInLap).format().toString()
                    }
                }
                breakdowns.add(CostSummaryBreakdownRow.Builder().title(travelerInfo).cost(passenger.totalPrice.formattedMoneyFromAmountAndCurrencyCode).build())

                breakdowns.add(CostSummaryBreakdownRow.Builder().title(context.getString(R.string.Flight)).cost(passenger.basePrice.formattedMoneyFromAmountAndCurrencyCode).build())

                title = context.getString(R.string.cost_summary_breakdown_taxes_fees)
                breakdowns.add(CostSummaryBreakdownRow.Builder().title(title).cost(passenger.taxesPrice.formattedMoneyFromAmountAndCurrencyCode).build())

                // Adding divider line
                breakdowns.add(CostSummaryBreakdownRow.Builder().separator())

            }

            if (flightOffer.fees != null) {
                val flightLeg = flightDetails.legs?.firstOrNull()
                if (flightLeg != null && flightLeg.isEvolable && isEvolableEnabled()) {
                    title = context.getString(R.string.booking_fee)
                } else {
                    title = Phrase.from(context, R.string.brand_booking_fee).put("brand", ProductFlavorFeatureConfiguration.getInstance().getPOSSpecificBrandName(context)).format().toString()
                }
                breakdowns.add(CostSummaryBreakdownRow.Builder().title(title).cost(flightOffer.bookingFee.formattedMoneyFromAmountAndCurrencyCode).build())

                // insurance
                if (flightOffer.selectedInsuranceProduct != null) {
                    val insurance = flightOffer.selectedInsuranceProduct
                    val insuranceTitle = context.getString(R.string.cost_summary_breakdown_flight_insurance)

                    breakdowns.add(CostSummaryBreakdownRow.Builder().title(insuranceTitle)
                            .cost(insurance.totalPrice.formattedMoneyFromAmountAndCurrencyCode)
                            .color(Ui.obtainThemeColor(context, R.attr.primary_color))
                            .build())
                }

                val selectedCardFees = tripResponse.selectedCardFees
                if (selectedCardFees != null && !selectedCardFees.isZero) {
                    title = context.getString(R.string.payment_method_fee)
                    val airlineCardFee = selectedCardFees.formattedMoneyFromAmountAndCurrencyCode
                    breakdowns.add(CostSummaryBreakdownRow.Builder().title(title).cost(airlineCardFee).build())
                }

                // Adding divider line
                breakdowns.add(CostSummaryBreakdownRow.Builder().separator())
            }

            if (AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightSubpubChange)) {
                if (flightOffer.discountAmount != null && !flightOffer.discountAmount.isZero) {
                    title = Phrase.from(context, R.string.cost_summary_breakdown_discount_TEMPLATE).put("brand", ProductFlavorFeatureConfiguration.getInstance().getPOSSpecificBrandName(context)).format().toString()
                    breakdowns.add(CostSummaryBreakdownRow.Builder()
                            .title(title)
                            .cost(flightOffer.discountAmount.formattedMoneyFromAmountAndCurrencyCode)
                            .color(ContextCompat.getColor(context, R.color.cost_summary_breakdown_savings_cost_color)).build())

                    // Adding divider line
                    breakdowns.add(CostSummaryBreakdownRow.Builder().separator())
                }
            }

            title = context.getString(R.string.cost_summary_breakdown_total_due_today)
            val totalPrice = tripResponse.tripTotalPayableIncludingFeeIfZeroPayableByPoints()
            breakdowns.add(CostSummaryBreakdownRow.Builder().title(title).cost(totalPrice.formattedMoneyFromAmountAndCurrencyCode).build())

            addRows.onNext(breakdowns)
            iconVisibilityObservable.onNext(true)
        }
    }

    override fun trackBreakDownClicked() {
        FlightsV2Tracking.trackFlightCostBreakdownClick()
    }

    fun isEvolableEnabled(): Boolean {
        return AbacusFeatureConfigManager.isUserBucketedForTest(context, AbacusUtils.EBAndroidAppFlightsEvolable)
    }
}
