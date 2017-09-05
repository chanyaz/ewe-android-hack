package com.expedia.vm.test.rail

import android.app.Activity
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.rail.responses.BaseRailOffer
import com.expedia.bookings.data.rail.responses.RailCreateTripResponse
import com.expedia.bookings.data.rail.responses.RailDomainProduct
import com.expedia.bookings.data.rail.responses.RailTripOffer
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.BaseCostSummaryBreakdownViewModel
import com.expedia.vm.rail.RailCostSummaryBreakdownViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import rx.observers.TestSubscriber
import java.math.BigDecimal
import java.util.ArrayList
import kotlin.properties.Delegates

@RunWith(RobolectricRunner::class)
class RailCostSummaryBreakdownViewModelTest {

    lateinit var costBreakdownVM: RailCostSummaryBreakdownViewModel
    private var activity: Activity by Delegates.notNull()

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
    }

    @Test
    fun passengerFareBreakdownIncluded() {
        costBreakdownVM = RailCostSummaryBreakdownViewModel(activity, false)

        val breakdownsSubscriber = TestSubscriber<List<BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow>>()
        costBreakdownVM.addRows.subscribe(breakdownsSubscriber)
        costBreakdownVM.railCostSummaryBreakdownObservable.onNext(buildRailResponseWithPassengerBreakdowns())

        val breakdown1 = BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().title("Journey").cost("$120.00").build()
        val breakdown2 = BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().separator()
        val breakdown3 = BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().title("Total").cost("$120.00").build()
        val expectedBreakdown = listOf(breakdown1, breakdown2, breakdown3)

        breakdownsSubscriber.assertReceivedOnNext(listOf(expectedBreakdown))
    }

    @Test @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun priceBreakdownFeesIncluded() {
        costBreakdownVM = RailCostSummaryBreakdownViewModel(activity, false)

        val breakdownsSubscriber = TestSubscriber<List<BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow>>()
        costBreakdownVM.addRows.subscribe(breakdownsSubscriber)
        costBreakdownVM.railCostSummaryBreakdownObservable.onNext(buildRailResponseWithFees())

        val breakdown1 = BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().title("Journey").cost("$120.00").build()
        val breakdown2 = BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().title("Ticket Delivery Fee").cost("$1.00").build()
        val breakdown3 = BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().title("Card Fee").cost("$10.00").build()
        val breakdown4 = BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().title("Expedia Booking Fee").cost("$15.00").build()
        val breakdown5 = BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().separator()
        val breakdown6 = BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().title("Total").cost("$131.00").build()
        val expectedBreakdown = listOf(breakdown1, breakdown2, breakdown3, breakdown4, breakdown5, breakdown6)

        breakdownsSubscriber.assertReceivedOnNext(listOf(expectedBreakdown))
    }

    private fun buildRailResponseWithPassengerBreakdowns(): RailCreateTripResponse {

        val response = RailCreateTripResponse()
        val domainProduct = RailDomainProduct()
        response.railDomainProduct = domainProduct
        response.totalPrice = Money(BigDecimal(120), "$")
        response.totalPrice.formattedPrice = "$120.00"
        response.totalPriceIncludingFees = Money(BigDecimal(0), "$")
        response.totalPriceIncludingFees?.formattedPrice = "$120.00"

        val offer = RailTripOffer()
        offer.totalPrice = Money()
        offer.totalPrice.formattedPrice = "$120.00"
        offer.priceBreakdown = ArrayList<BaseRailOffer.PriceBreakdown>()

        response.railDomainProduct.railOffer = offer
        return response
    }

    private fun buildRailResponseWithFees(): RailCreateTripResponse {

        val response = RailCreateTripResponse()
        val domainProduct = RailDomainProduct()
        response.railDomainProduct = domainProduct
        response.totalPrice = Money(BigDecimal(120), "$")
        response.totalPrice.formattedPrice = "$120.00"
        response.totalPriceIncludingFees = Money(BigDecimal(131), "$")
        response.totalPriceIncludingFees?.formattedPrice = "$131.00"

        val offer = RailTripOffer()
        offer.totalPrice = Money()
        offer.totalPrice.formattedPrice = "$120.00"
        offer.priceBreakdown = ArrayList<BaseRailOffer.PriceBreakdown>()
        var price = BaseRailOffer.PriceBreakdown()
        price.priceCategoryCode = BaseRailOffer.PriceCategoryCode.TICKET_DELIVERY
        price.amount = BigDecimal(1)
        price.formattedPrice = "$1.00"
        offer.priceBreakdown.add(price)

        price = BaseRailOffer.PriceBreakdown()
        price.priceCategoryCode = BaseRailOffer.PriceCategoryCode.CREDIT_CARD_FEE
        price.amount = BigDecimal(10)
        price.formattedPrice = "$10.00"
        offer.priceBreakdown.add(price)

        price = BaseRailOffer.PriceBreakdown()
        price.priceCategoryCode = BaseRailOffer.PriceCategoryCode.EXPEDIA_SERVICE_FEE
        price.amount = BigDecimal(15)
        price.formattedPrice = "$15.00"
        offer.priceBreakdown.add(price)

        response.railDomainProduct.railOffer = offer

        return response
    }
}