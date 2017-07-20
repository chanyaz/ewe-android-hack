package com.expedia.vm.test.robolectric

import android.app.Activity

import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.TripDetails
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.data.insurance.InsuranceProduct
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import com.expedia.vm.BaseCostSummaryBreakdownViewModel
import com.expedia.vm.flights.FlightCostSummaryBreakdownViewModel
import com.mobiata.android.util.SettingUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import java.util.ArrayList
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class) @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
class FlightCostSummaryBreakdownViewModelTest {

    lateinit private var activity: Activity
    lateinit private var newTripResponse: FlightCreateTripResponse
    lateinit private var sut: FlightCostSummaryBreakdownViewModel

    private fun setupSystemUnderTest() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.FlightTheme)
        Ui.getApplication(RuntimeEnvironment.application).defaultTravelerComponent()
        sut = FlightCostSummaryBreakdownViewModel(activity)
    }

    @Test
    fun testBreakdownNoAirlineFeeNoInsurance() {
        setupSystemUnderTest()
        setUpFlightSubPubChange()
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
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder()
                .title("Expedia Discount")
                .color(ContextCompat.getColor(activity, R.color.cost_summary_breakdown_savings_cost_color))
                .cost("-$6.24").build())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().separator())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().title("Total Due Today").cost("$55.00").build())

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
    fun testBreakdownWithAirlineFeeNoInsurance() {
        setupSystemUnderTest()
        setUpFlightSubPubChange()
        givenGoodTripResponse()
        givenTripResponseHasFees()

        val breakdownRowsTestObservable = TestSubscriber<kotlin.collections.List<BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow>>()
        sut.addRows.subscribe(breakdownRowsTestObservable)
        sut.flightCostSummaryObservable.onNext(newTripResponse)

        val breakdowns = arrayListOf<BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow>()

        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().title("Adult 1 details").cost("$55.00").build())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().title("Flight").cost("$50.00").build())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().title("Taxes & Fees").cost("$5.00").build())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().separator())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().title("Expedia Booking Fee").cost("$0.00").build())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().title("Payment Method Fee").cost("$2.50").build())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().separator())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder()
                .title("Expedia Discount")
                .color(ContextCompat.getColor(activity, R.color.cost_summary_breakdown_savings_cost_color))
                .cost("-$6.24").build())
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
                breakdowns[7],
                breakdowns[8],
                breakdowns[9]
        )
        val breakdownRows = breakdownRowsTestObservable.onNextEvents[0]
        assertEquals(10, breakdownRows.size)
        assertEvents(expectedBreakdown, breakdownRows)
    }

    @Test
    fun testBreakdownWithInsuranceNoAirlineFee() {
        setupSystemUnderTest()
        setUpFlightSubPubChange()
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
                .color(Ui.obtainThemeColor(activity, R.attr.primary_color)).build())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().separator())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder()
                .title("Expedia Discount")
                .color(ContextCompat.getColor(activity, R.color.cost_summary_breakdown_savings_cost_color))
                .cost("-$6.24").build())
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
                breakdowns[7],
                breakdowns[8],
                breakdowns[9]
        )
        val breakdownRows = breakdownRowsTestObservable.onNextEvents[0]
        assertEquals(10, breakdownRows.size)
        assertEvents(expectedBreakdown, breakdownRows)
    }

    @Test
    fun testBreakdownWithInsuranceAndAirlineFee() {
        setupSystemUnderTest()
        setUpFlightSubPubChange()
        givenGoodTripResponse()
        setupInsuranceFees()
        givenTripResponseHasFees()

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
                .color(Ui.obtainThemeColor(activity, R.attr.primary_color)).build())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().title("Payment Method Fee").cost("$2.50").build())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().separator())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().title("Expedia Discount")
                .color(ContextCompat.getColor(activity, R.color.cost_summary_breakdown_savings_cost_color))
                .cost("-$6.24").build())
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
                breakdowns[8],
                breakdowns[9],
                breakdowns[10]
        )
        val breakdownRows = breakdownRowsTestObservable.onNextEvents[0]

        assertEquals(11, breakdownRows.size)
        assertEvents(expectedBreakdown, breakdownRows)
    }

    @Test
    fun testBreakdownNoAirlineFeeNoInsuranceAndYouthTraveler() {
        setupSystemUnderTest()
        givenGoodTripResponse()
        givenTripResponseHasYouthTraveler()

        val breakdownRowsTestObservable = TestSubscriber<List<BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow>>()
        sut.addRows.subscribe(breakdownRowsTestObservable)
        sut.flightCostSummaryObservable.onNext(newTripResponse)
        val breakdowns = arrayListOf<BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow>()

        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().title("Adult 1 details").cost("$55.00").build())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().title("Flight").cost("$50.00").build())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().title("Taxes & Fees").cost("$5.00").build())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().separator())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().title("Youth 1 details").cost("$55.00").build())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().title("Flight").cost("$50.00").build())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().title("Taxes & Fees").cost("$5.00").build())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().separator())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().title("Expedia Booking Fee").cost("$0.00").build())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().separator())
        breakdowns.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().title("Total Due Today").cost("$55.00").build())

        val expectedBreakdown = listOf(
                breakdowns[0],
                breakdowns[1],
                breakdowns[2],
                breakdowns[3],
                breakdowns[4],
                breakdowns[5],
                breakdowns[6],
                breakdowns[7],
                breakdowns[8],
                breakdowns[9],
                breakdowns[10]
        )

        assertEvents(expectedBreakdown, breakdownRowsTestObservable.onNextEvents[0])
    }

    private fun setupInsuranceFees()  {
       val insurance = Money("10.00", "USD")

        newTripResponse.details.offer.selectedInsuranceProduct = InsuranceProduct()
        newTripResponse.details.offer.selectedInsuranceProduct.totalPrice = insurance

        newTripResponse.details.offer.totalPrice.add(insurance)

        newTripResponse.details.offer.selectedInsuranceProduct.totalPrice.formattedPrice = insurance.formattedMoneyFromAmountAndCurrencyCode
    }

    private fun givenGoodTripResponse() {
        val tripId = "1234"
        val tealeafTransactionId = "tealeaf-1234"
        val priceString = "55.00"
        val currencyCode = "USD"
        val totalPrice = Money(priceString, currencyCode)
        val adultCategory = FlightTripDetails.PassengerCategory.ADULT
        val taxesPrice = Money("5.00", "USD")
        val basePrice = Money("50.00", "USD")
        val discountAmount = Money("-6.24", "USD")


        newTripResponse = FlightCreateTripResponse()
        newTripResponse.tripId = tripId
        newTripResponse.newTrip = TripDetails("", "", tripId)
        newTripResponse.tealeafTransactionId = tealeafTransactionId

        newTripResponse.details = FlightTripDetails()
        newTripResponse.details.PricePerPassengerCategory().passengerCategory = adultCategory
        newTripResponse.details.offer = FlightTripDetails.FlightOffer()

        newTripResponse.details.offer.totalPrice = Money(priceString, currencyCode)
        newTripResponse.details.offer.fees = "0.00"
        newTripResponse.details.offer.discountAmount = discountAmount
        newTripResponse.details.offer.discountAmount.formattedPrice = discountAmount.formattedMoneyFromAmountAndCurrencyCode
        newTripResponse.details.offer.currency = currencyCode
        newTripResponse.details.offer.pricePerPassengerCategory = ArrayList<FlightTripDetails.PricePerPassengerCategory>()
        val list = newTripResponse.details.PricePerPassengerCategory()
        (newTripResponse.details.offer.pricePerPassengerCategory as ArrayList<FlightTripDetails.PricePerPassengerCategory>).add(0, list )
        newTripResponse.details.offer.pricePerPassengerCategory[0].passengerCategory = adultCategory

        newTripResponse.details.offer.pricePerPassengerCategory[0].taxesPrice = taxesPrice
        newTripResponse.details.offer.pricePerPassengerCategory[0].taxesPrice.formattedPrice = taxesPrice.formattedMoneyFromAmountAndCurrencyCode
        newTripResponse.details.offer.pricePerPassengerCategory[0].totalPrice = totalPrice
        newTripResponse.details.offer.pricePerPassengerCategory[0].totalPrice.formattedPrice = totalPrice.formattedMoneyFromAmountAndCurrencyCode
        newTripResponse.details.offer.pricePerPassengerCategory[0].basePrice = basePrice
        newTripResponse.details.offer.pricePerPassengerCategory[0].basePrice.formattedPrice = basePrice.formattedMoneyFromAmountAndCurrencyCode
    }

    private fun assertEvents(expectedBreakdownList: List<BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow>,
                             list: List<BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow>) {
        for (i in expectedBreakdownList.indices) {
            val expected = expectedBreakdownList[i]
            val actual = list[i]
            assertEquals(expected, actual)
        }
    }

    private fun givenTripResponseHasYouthTraveler() {
        val priceString = "55.00"
        val currencyCode = "USD"
        val totalPrice = Money(priceString, currencyCode)
        val youthCategory = FlightTripDetails.PassengerCategory.ADULT_CHILD
        val taxesPrice = Money("5.00", "USD")
        val basePrice = Money("50.00", "USD")
        SettingUtils.save(activity, R.string.preference_flight_traveler_form_revamp, true)
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppFlightTravelerFormRevamp)

        val list = newTripResponse.details.PricePerPassengerCategory()
        (newTripResponse.details.offer.pricePerPassengerCategory as ArrayList<FlightTripDetails.PricePerPassengerCategory>).add(1, list)
        newTripResponse.details.offer.pricePerPassengerCategory[1].passengerCategory = youthCategory

        newTripResponse.details.offer.pricePerPassengerCategory[1].taxesPrice = taxesPrice
        newTripResponse.details.offer.pricePerPassengerCategory[1].taxesPrice.formattedPrice = taxesPrice.formattedMoneyFromAmountAndCurrencyCode
        newTripResponse.details.offer.pricePerPassengerCategory[1].totalPrice = totalPrice
        newTripResponse.details.offer.pricePerPassengerCategory[1].totalPrice.formattedPrice = totalPrice.formattedMoneyFromAmountAndCurrencyCode
        newTripResponse.details.offer.pricePerPassengerCategory[1].basePrice = basePrice
        newTripResponse.details.offer.pricePerPassengerCategory[1].basePrice.formattedPrice = basePrice.formattedMoneyFromAmountAndCurrencyCode
    }

    private fun setUpFlightSubPubChange() {
        SettingUtils.save(activity, R.string.preference_flight_subpub_change, true)
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppFlightSubpubChange)
        Ui.getApplication(activity).defaultFlightComponents()
    }

    private fun givenTripResponseHasFees() {
        val money = Money("2.50", "USD")
        newTripResponse.selectedCardFees = money
    }
}