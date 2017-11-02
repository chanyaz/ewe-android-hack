package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.rail.responses.BaseRailOffer
import com.expedia.bookings.data.rail.responses.RailCreateTripResponse
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.tracking.RailTracking
import com.expedia.vm.BaseCostSummaryBreakdownViewModel
import com.squareup.phrase.Phrase
import io.reactivex.subjects.PublishSubject
import java.util.ArrayList

class RailCostSummaryBreakdownViewModel(context: Context, val isCheckout: Boolean) : BaseCostSummaryBreakdownViewModel(context) {
    val railCostSummaryBreakdownObservable = PublishSubject.create<RailCreateTripResponse>()

    init {
        railCostSummaryBreakdownObservable.subscribe { response ->
            val breakdowns = arrayListOf<CostSummaryBreakdownRow>()

            val offer = response.railDomainProduct.railOffer

            addPassengerPriceBreakdown(breakdowns, response.totalPrice)
            addFees(breakdowns, offer.priceBreakdownByCode)

            // Adding divider line
            breakdowns.add(CostSummaryBreakdownRow.Builder().separator())

            val title = context.getString(R.string.total)
            breakdowns.add(CostSummaryBreakdownRow.Builder().title(title).cost(response.totalPayablePrice.formattedPrice).build())

            addRows.onNext(breakdowns)
            iconVisibilityObservable.onNext(true)
        }
    }

    private fun addFees(breakdowns: ArrayList<CostSummaryBreakdownRow>, priceBreakdowns: Map<BaseRailOffer.PriceCategoryCode, BaseRailOffer.PriceBreakdown>) {
        // show fee line item if non zero
        val ticketDeliveryFee = priceBreakdowns[BaseRailOffer.PriceCategoryCode.TICKET_DELIVERY]
        if (ticketDeliveryFee != null && !ticketDeliveryFee.isZero) {
            breakdowns.add(CostSummaryBreakdownRow.Builder()
                    .title(context.getString(R.string.rail_ticket_delivery_fee))
                    .cost(ticketDeliveryFee.formattedPrice).build())
        }

        val creditCardFee = priceBreakdowns[BaseRailOffer.PriceCategoryCode.CREDIT_CARD_FEE]
        if (creditCardFee != null && !creditCardFee.isZero) {
            breakdowns.add(CostSummaryBreakdownRow.Builder()
                    .title(context.getString(R.string.rail_credit_card_fee))
                    .cost(creditCardFee.formattedPrice).build())
        }

        val bookingFee = priceBreakdowns[BaseRailOffer.PriceCategoryCode.EXPEDIA_SERVICE_FEE]
        if (bookingFee != null && !bookingFee.isZero) {
            val title = Phrase.from(context, R.string.brand_booking_fee)
                    .put("brand", ProductFlavorFeatureConfiguration.getInstance().getPOSSpecificBrandName(context))
                    .format().toString()
            breakdowns.add(CostSummaryBreakdownRow.Builder().title(title).cost(bookingFee.formattedPrice).build())
        }
    }

    private fun addPassengerPriceBreakdown(breakdowns: ArrayList<CostSummaryBreakdownRow>, totalTicketsPriceWithoutFees: Money ) {
        val title: String = context.getString(R.string.rail_price_breakdown_journey)
        breakdowns.add(CostSummaryBreakdownRow.Builder().title(title).cost(totalTicketsPriceWithoutFees.formattedPrice).build())
    }

    override fun trackBreakDownClicked() {
        if (isCheckout) {
            RailTracking().trackRailCheckoutTotalCostToolTip()
        }
        else {
            RailTracking().trackRailDetailsTotalCostToolTip()
        }
    }
}