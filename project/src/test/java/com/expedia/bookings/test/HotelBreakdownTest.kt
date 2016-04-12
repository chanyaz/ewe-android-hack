package com.expedia.bookings.test

import android.app.Application
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.services.LoyaltyServices
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.vm.HotelBreakDownViewModel.Breakdown
import com.expedia.vm.HotelBreakDownViewModel
import com.expedia.vm.HotelBreakDownViewModel.BreakdownItem
import com.expedia.vm.HotelCheckoutSummaryViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
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
    private var createTripResponse: HotelCreateTripResponse by Delegates.notNull()
    private var paymentModel: PaymentModel<HotelCreateTripResponse> by Delegates.notNull()
    private var context: Application by Delegates.notNull()

    @Before
    fun before() {
        context = RuntimeEnvironment.application
        paymentModel = PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!)
        hotelCheckoutSummaryViewModel = HotelCheckoutSummaryViewModel(context, paymentModel)
        vm = HotelBreakDownViewModel(context, hotelCheckoutSummaryViewModel)
    }

    @Test
    fun verifyCostBreakdownForUserWithNoRewardPoints() {
        givenHappyCreateTripResponse()

        val latch = CountDownLatch(1)
        vm.addRows.subscribe { latch.countDown() }
        val testSubscriber = TestSubscriber<List<HotelBreakDownViewModel.Breakdown>>()
        val expected = arrayListOf<List<HotelBreakDownViewModel.Breakdown>>()
        vm.addRows.subscribe(testSubscriber)

        paymentModel.createTripSubject.onNext(createTripResponse)
        expected.add(arrayListOf(Breakdown("1 Night", "$99.00", BreakdownItem.OTHER),
                Breakdown("3/22/2013", "$99.00", BreakdownItem.DATE),
                Breakdown("Taxes & Fees", "$16.81", BreakdownItem.OTHER),
                Breakdown("Trip Total", "$135.81", BreakdownItem.TRIPTOTAL)))

        assertTrue(latch.await(10, TimeUnit.SECONDS))
        vm.addRows.onCompleted()
        testSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS)
        testSubscriber.assertReceivedOnNext(expected)
        testSubscriber.unsubscribe()
    }

    @Test
    fun verifyCostBreakdownForUserWithRedeemablePoints() {
        givenLoggedInUserWithEnoughRedeemablePointsResponse()

        val latch = CountDownLatch(1)
        vm.addRows.subscribe { latch.countDown() }
        val testSubscriber = TestSubscriber<List<Breakdown>>()
        val expected = arrayListOf<List<Breakdown>>()
        vm.addRows.subscribe(testSubscriber)

        paymentModel.createTripSubject.onNext(createTripResponse)

        val latch1 = CountDownLatch(1)
        paymentModel.burnAmountToPointsApiResponse.subscribe { latch1.countDown() }
        paymentModel.burnAmountSubject.onNext(BigDecimal(32))
        latch1.await(10, TimeUnit.SECONDS)

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

        assertTrue(latch.await(10, TimeUnit.SECONDS))
        vm.addRows.onCompleted()
        testSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS)
        testSubscriber.assertReceivedOnNext(expected)
        testSubscriber.unsubscribe()
    }

    private fun givenLoggedInUserWithEnoughRedeemablePointsResponse() {
        createTripResponse = mockHotelServiceTestRule.getLoggedInUserWithRedeemablePointsCreateTripResponse()
        createTripResponse.tripId = "happy"
    }

    private fun givenHappyCreateTripResponse() {
        createTripResponse = mockHotelServiceTestRule.getHappyCreateTripResponse()
    }
}
