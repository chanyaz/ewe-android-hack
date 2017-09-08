package com.expedia.bookings.test

import android.app.Application
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.services.LoyaltyServices
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.vm.HotelBreakDownViewModel
import com.expedia.vm.HotelBreakDownViewModel.Breakdown
import com.expedia.vm.HotelBreakDownViewModel.BreakdownItem
import com.expedia.vm.HotelCheckoutSummaryViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
<<<<<<< HEAD
import rx.observers.TestSubscriber
import io.reactivex.subjects.PublishSubject
=======
import com.expedia.bookings.services.TestObserver
>>>>>>> 5abc89409b... WIP
import java.math.BigDecimal
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelBreakdownTest {
    var mockHotelServiceTestRule: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get
    var loyaltyServiceRule = ServicesRule(LoyaltyServices::class.java)
        @Rule get

    private var vm: HotelBreakDownViewModel by Delegates.notNull()
    private var hotelCheckoutSummaryViewModel: HotelCheckoutSummaryViewModel by Delegates.notNull()
    private var createTripResponseObservable = PublishSubject.create<HotelCreateTripResponse>()
    private var createTripResponse: HotelCreateTripResponse by Delegates.notNull()
    private var paymentModel: PaymentModel<HotelCreateTripResponse> by Delegates.notNull()
    private var context: Application by Delegates.notNull()

    @Before
    fun before() {
        context = RuntimeEnvironment.application
        paymentModel = PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!)
        hotelCheckoutSummaryViewModel = HotelCheckoutSummaryViewModel(context, paymentModel)
        createTripResponseObservable.subscribe(hotelCheckoutSummaryViewModel.createTripResponseObservable)
        vm = HotelBreakDownViewModel(context, hotelCheckoutSummaryViewModel)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun verifyCostBreakdownForUserWithNoRewardPoints() {
        givenHappyCreateTripResponse()

        val testSubscriber = TestObserver<List<Breakdown>>()
        vm.addRows.subscribe(testSubscriber)

        executeTest(testSubscriber)

        val expected = arrayListOf<List<Breakdown>>()
        expected.add(arrayListOf(Breakdown("1 Night", "$99.00", BreakdownItem.OTHER),
                Breakdown("3/22/2013", "$99.00", BreakdownItem.DATE),
                Breakdown("Taxes & Fees", "$16.81", BreakdownItem.OTHER),
                Breakdown("Trip Total", "$135.81", BreakdownItem.TRIPTOTAL),
                Breakdown("Due to Expedia today", "$0", BreakdownItem.OTHER)))

        testSubscriber.assertReceivedOnNext(expected)
        testSubscriber.dispose()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun verifyCostBreakdownForUserWithRedeemablePoints() {
        givenLoggedInUserWithEnoughRedeemablePointsResponse()

        val testSubscriber = TestObserver<List<Breakdown>>()
        vm.addRows.subscribe(testSubscriber)

        executeTest(testSubscriber, Runnable() {
            val latch1 = CountDownLatch(1)
            paymentModel.burnAmountToPointsApiResponse.subscribe { latch1.countDown() }
            paymentModel.burnAmountSubject.onNext(BigDecimal(32))
            latch1.await(10, TimeUnit.SECONDS)
        })

        val expected = arrayListOf<List<Breakdown>>()
        expected.add(arrayListOf(Breakdown("1 Night", "$99.00", BreakdownItem.OTHER),
                Breakdown("3/22/2013", "$99.00", BreakdownItem.DATE),
                Breakdown("Taxes & Fees", "$16.81", BreakdownItem.OTHER),
                Breakdown("2,500 points", "$1,000.00", BreakdownItem.DISCOUNT),
                Breakdown("Trip Total", "$0.00", BreakdownItem.TRIPTOTAL)))

        expected.add(arrayListOf(Breakdown("1 Night", "$99.00", BreakdownItem.OTHER),
                Breakdown("3/22/2013", "$99.00", BreakdownItem.DATE),
                Breakdown("Taxes & Fees", "$16.81", BreakdownItem.OTHER),
                Breakdown("14,005 points", "$100.00", BreakdownItem.DISCOUNT),
                Breakdown("Trip Total", "$3.70", BreakdownItem.TRIPTOTAL)))

        testSubscriber.assertReceivedOnNext(expected)
        testSubscriber.dispose()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ))
    fun verifyCostBreakDownForFeesWithIncludedTaxes() {
        givenHotelWithFeesAndIncludedTaxesResponse()

        val testSubscriber = TestObserver<List<Breakdown>>()
        vm.addRows.subscribe(testSubscriber)

        executeTest(testSubscriber)

        val expected = arrayListOf<List<Breakdown>>()
        expected.add(arrayListOf(Breakdown("3 Nights", "$110.43", BreakdownItem.OTHER),
                Breakdown("7/05/2016", "$36.81", BreakdownItem.DATE),
                Breakdown("7/06/2016", "$36.81", BreakdownItem.DATE),
                Breakdown("7/07/2016", "$36.81", BreakdownItem.DATE),
                Breakdown("Taxes & Fees", "$19.89", BreakdownItem.OTHER),
                Breakdown("Trip Total", "$130.32", BreakdownItem.TRIPTOTAL)))

        testSubscriber.assertReceivedOnNext(expected)
        testSubscriber.dispose()
    }

    @Test @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun verifyCostBreakDownForFeesPaidAtHotel() {
        givenHotelWithFeesPaidAtHotelResponse()

        val testSubscriber = TestObserver<List<Breakdown>>()
        vm.addRows.subscribe(testSubscriber)

        executeTest(testSubscriber)

        val expected = arrayListOf<List<Breakdown>>()
        expected.add(arrayListOf(Breakdown("1 Night", "$179.31", BreakdownItem.OTHER),
                Breakdown("6/30/2016", "$179.31", BreakdownItem.DATE),
                Breakdown("Taxes & Fees", "$8.97", BreakdownItem.OTHER),
                Breakdown("Fees paid at hotel", "$2.49", BreakdownItem.OTHER),
                Breakdown("Trip Total", "$190.77", BreakdownItem.TRIPTOTAL),
                Breakdown("Due to Expedia", "$188.28", BreakdownItem.OTHER)))

        testSubscriber.assertReceivedOnNext(expected)
        testSubscriber.dispose()
    }

    @Test @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun verifyCostBreakDownForGuestCharge() {
        givenHotelWithGuestChargeResponse()

        val testSubscriber = TestObserver<List<Breakdown>>()
        vm.addRows.subscribe(testSubscriber)

        executeTest(testSubscriber)

        val expected = arrayListOf<List<Breakdown>>()
        expected.add(arrayListOf(Breakdown("2 Nights", "$349.42", BreakdownItem.OTHER),
                Breakdown("6/30/2016", "$135.96", BreakdownItem.DATE),
                Breakdown("7/01/2016", "$213.46", BreakdownItem.DATE),
                Breakdown("Taxes & Fees", "$59.34", BreakdownItem.OTHER),
                Breakdown("Extra Guest Charge", "$120.00", BreakdownItem.OTHER),
                Breakdown("Discount", "$25.00", BreakdownItem.DISCOUNT),
                Breakdown("Fees paid at hotel", "$67.20", BreakdownItem.OTHER),
                Breakdown("Trip Total", "$595.94", BreakdownItem.TRIPTOTAL),
                Breakdown("Due to Expedia", "$528.74", BreakdownItem.OTHER)))

        testSubscriber.assertReceivedOnNext(expected)
        testSubscriber.dispose()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY))
    fun verifyCostBreakDownForIncludedTaxes() {
        givenHotelWithTaxesIncludedResponse()

        val testSubscriber = TestObserver<List<Breakdown>>()
        vm.addRows.subscribe(testSubscriber)

        executeTest(testSubscriber)

        val expected = arrayListOf<List<Breakdown>>()
        expected.add(arrayListOf(Breakdown("1 Night", "$72.96", BreakdownItem.OTHER),
                Breakdown("6/29/2016", "$72.96", BreakdownItem.DATE),
                Breakdown("Taxes & Fees", "Included", BreakdownItem.OTHER),
                Breakdown("Trip Total", "$72.96", BreakdownItem.TRIPTOTAL)))

        testSubscriber.assertReceivedOnNext(expected)
        testSubscriber.dispose()
    }

    private fun executeTest(testSubscriber: TestObserver<List<HotelBreakDownViewModel.Breakdown>>, runBeforeComplete: Runnable? = null) {
        val latch = CountDownLatch(1)
        vm.addRows.subscribe { latch.countDown() }

        paymentModel.createTripSubject.onNext(createTripResponse)
        createTripResponseObservable.onNext(createTripResponse)


        runBeforeComplete?.run()

        assertTrue(latch.await(10, TimeUnit.SECONDS))
        vm.addRows.onComplete()
        testSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS)
    }

    private fun givenHotelWithTaxesIncludedResponse() {
        createTripResponse = mockHotelServiceTestRule.getHotelWithTaxesIncludedResponse()
        createTripResponse.tripId = "happy"
    }

    private fun givenHotelWithGuestChargeResponse() {
        createTripResponse = mockHotelServiceTestRule.getHotelWithGuestChargeResponse()
        createTripResponse.tripId = "happy"
    }

    private fun givenHotelWithFeesPaidAtHotelResponse() {
        createTripResponse = mockHotelServiceTestRule.getHotelWithFeesPaidAtHotelResponse()
        createTripResponse.tripId = "happy"
    }

    private fun givenHotelWithFeesAndIncludedTaxesResponse() {
        createTripResponse = mockHotelServiceTestRule.getHotelWithFeesAndIncludedTaxesResponse()
        createTripResponse.tripId = "happy"
    }

    private fun givenLoggedInUserWithEnoughRedeemablePointsResponse() {
        createTripResponse = mockHotelServiceTestRule.getLoggedInUserWithRedeemablePointsCreateTripResponse()
        createTripResponse.tripId = "happy"
    }

    private fun givenHappyCreateTripResponse() {
        createTripResponse = mockHotelServiceTestRule.getHappyCreateTripResponse()
    }
}
