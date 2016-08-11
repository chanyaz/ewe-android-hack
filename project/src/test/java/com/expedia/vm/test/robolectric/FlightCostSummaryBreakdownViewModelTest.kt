package com.expedia.vm.test.robolectric

import android.app.Activity
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.TripDetails
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.data.insurance.InsuranceProduct
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.vm.BaseCostSummaryBreakdownViewModel
import com.expedia.vm.flights.FlightCostSummaryBreakdownViewModel
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import java.util.ArrayList
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class FlightCostSummaryBreakdownViewModelTest {

    private val context = RuntimeEnvironment.application
    lateinit private var newTripResponse: FlightCreateTripResponse
    lateinit private var sut: FlightCostSummaryBreakdownViewModel

    private fun setupSystemUnderTest() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.FlightTheme)
        Ui.getApplication(context).defaultTravelerComponent()
        sut = FlightCostSummaryBreakdownViewModel(context)
    }

    @Test
    fun testBreakdownNoAirlineFeeNoInsurance() {
        setupSystemUnderTest()
        givenGoodTripResponse()

        val breakdownRowsTestObservable = TestSubscriber<List<BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow>>()
        sut.addRows.subscribe(breakdownRowsTestObservable)
        sut.flightCostSummaryObservable.onNext(newTripResponse)
        val breakdowns = arrayListOf<BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow>()

        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().title("Adult 1 details").cost("$55.00").build())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().title("Flight").cost("$50.00").build())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().title("Taxes & Fees").cost("$5.00").build())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().separator())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().title("Expedia Booking Fee").cost("$0.00").build())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().separator())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().title("Total Due Today").cost("$55.00").build())

        val expectedBreakdown = listOf(breakdowns[0], breakdowns[1], breakdowns[2], breakdowns[3], breakdowns[4], breakdowns[5], breakdowns[6])

        assertEvents(expectedBreakdown, breakdownRowsTestObservable.onNextEvents[0])
    }

    @Test
    fun testBreakdownWithAirlineFeeNoInsurance() {
        setupSystemUnderTest()
        givenGoodTripResponse()
        newTripResponse.selectedCardFees = Money("2.50", "USD")

        val breakdownRowsTestObservable = TestSubscriber<kotlin.collections.List<BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow>>()
        sut.addRows.subscribe(breakdownRowsTestObservable)
        sut.flightCostSummaryObservable.onNext(newTripResponse)

        val breakdowns = arrayListOf<BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow>()

        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().title("Adult 1 details").cost("$55.00").build())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().title("Flight").cost("$50.00").build())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().title("Taxes & Fees").cost("$5.00").build())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().separator())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().title("Expedia Booking Fee").cost("$0.00").build())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().title("Airline Card Fee").cost("$2.50").build())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().separator())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().title("Total Due Today").cost("$57.50").build())

        val expectedBreakdown = listOf(
                breakdowns[0],
                breakdowns[1],
                breakdowns[2],
                breakdowns[3],
                breakdowns[4],
                breakdowns[5],
                breakdowns[6],
                breakdowns[7]
        )
        assertEvents(expectedBreakdown, breakdownRowsTestObservable.onNextEvents[0])
    }

    @Test
    fun testBreakdownWithInsuranceNoAirlineFee() {
        setupSystemUnderTest()
        givenGoodTripResponse()
        setupInsuranceFees()

        val breakdownRowsTestObservable = TestSubscriber<kotlin.collections.List<BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow>>()
        sut.addRows.subscribe(breakdownRowsTestObservable)
        sut.flightCostSummaryObservable.onNext(newTripResponse)

        val breakdowns = arrayListOf<BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow>()

        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().title("Adult 1 details").cost("$55.00").build())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().title("Flight").cost("$50.00").build())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().title("Taxes & Fees").cost("$5.00").build())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().separator())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().title("Expedia Booking Fee").cost("$0.00").build())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder()
                .title("Flight Protection")
                .cost("$10.00")
                .color(Ui.obtainThemeColor(context, R.attr.primary_color)).build())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().separator())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().title("Total Due Today").cost("$65.00").build())

        val expectedBreakdown = listOf<BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow>(
                breakdowns[0],
                breakdowns[1],
                breakdowns[2],
                breakdowns[3],
                breakdowns[4],
                breakdowns[5],
                breakdowns[6],
                breakdowns[7]
        )

        assertEvents(expectedBreakdown, breakdownRowsTestObservable.onNextEvents[0])
    }

    @Test
    fun testBreakdownWithInsuranceAndAirlineFee() {
        setupSystemUnderTest()
        givenGoodTripResponse()
        setupInsuranceFees()
        newTripResponse.selectedCardFees = Money("2.50", "USD")

        val breakdownRowsTestObservable = TestSubscriber<kotlin.collections.List<BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow>>()
        sut.addRows.subscribe(breakdownRowsTestObservable)
        sut.flightCostSummaryObservable.onNext(newTripResponse)

        val breakdowns = arrayListOf<BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow>()

        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().title("Adult 1 details").cost("$55.00").build())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().title("Flight").cost("$50.00").build())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().title("Taxes & Fees").cost("$5.00").build())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().separator())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().title("Expedia Booking Fee").cost("$0.00").build())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder()
                .title("Flight Protection")
                .cost("$10.00")
                .color(Ui.obtainThemeColor(context, R.attr.primary_color)).build())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().title("Airline Card Fee").cost("$2.50").build())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().separator())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().title("Total Due Today").cost("$67.50").build())

        val expectedBreakdown = listOf(
                breakdowns[0],
                breakdowns[1],
                breakdowns[2],
                breakdowns[3],
                breakdowns[4],
                breakdowns[5],
                breakdowns[6],
                breakdowns[7],
                breakdowns[8]
        )

        assertEvents(expectedBreakdown, breakdownRowsTestObservable.onNextEvents[0])
    }

    private fun setupInsuranceFees()  {
       val insurance = Money("10.00", "USD")

        newTripResponse.details.offer.selectedInsuranceProduct = InsuranceProduct()
        newTripResponse.details.offer.selectedInsuranceProduct.totalPrice = insurance

        newTripResponse.totalPrice.add(insurance)

        newTripResponse.details.offer.selectedInsuranceProduct.totalPrice.formattedPrice = insurance.getFormattedMoneyFromAmountAndCurrencyCode()
    }

    private fun givenGoodTripResponse() {
        val tripId = "1234"
        val tealeafTransactionId = "tealeaf-1234"
        val priceString = "55.00"
        val currencyCode = "USD"
        val totalPrice = Money(priceString, currencyCode)
        val adultCategory = FlightTripDetails.PassengerCategory.ADULT
        var taxesPrice = Money("5.00", "USD")
        var basePrice = Money("50.00", "USD")


        newTripResponse = FlightCreateTripResponse()
        newTripResponse.tripId = tripId
        newTripResponse.newTrip = TripDetails("", "", tripId)
        newTripResponse.totalPrice = Money(priceString, currencyCode)
        newTripResponse.tealeafTransactionId = tealeafTransactionId

        val details = newTripResponse.javaClass.getDeclaredField("details")
        val tripDetails = FlightTripDetails()
        details.isAccessible = true
        details.set(newTripResponse, tripDetails)

        newTripResponse.details.PricePerPassengerCategory().passengerCategory = adultCategory
        newTripResponse.details.offer = FlightTripDetails.FlightOffer()

        newTripResponse.details.offer.fees = "0.00"
        newTripResponse.details.offer.currency = currencyCode
        newTripResponse.details.offer.pricePerPassengerCategory = ArrayList<FlightTripDetails.PricePerPassengerCategory>()
        val list = newTripResponse.details.PricePerPassengerCategory()
        (newTripResponse.details.offer.pricePerPassengerCategory as ArrayList<FlightTripDetails.PricePerPassengerCategory>).add(0, list )
        newTripResponse.details.offer.pricePerPassengerCategory[0].passengerCategory = adultCategory

        newTripResponse.details.offer.pricePerPassengerCategory[0].taxesPrice = taxesPrice
        newTripResponse.details.offer.pricePerPassengerCategory[0].taxesPrice.formattedPrice = taxesPrice.getFormattedMoneyFromAmountAndCurrencyCode()
        newTripResponse.details.offer.pricePerPassengerCategory[0].totalPrice = totalPrice
        newTripResponse.details.offer.pricePerPassengerCategory[0].totalPrice.formattedPrice = totalPrice.getFormattedMoneyFromAmountAndCurrencyCode()
        newTripResponse.details.offer.pricePerPassengerCategory[0].basePrice = basePrice
        newTripResponse.details.offer.pricePerPassengerCategory[0].basePrice.formattedPrice = basePrice.getFormattedMoneyFromAmountAndCurrencyCode()
    }

    private fun assertEvents(expectedBreakdown: List<BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow>,
                             list: List<BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow>) {
        for (index in 0..expectedBreakdown.size - 1) {
            assertTrue(expectedBreakdown[index].equals(list[index]))
        }
    }
}