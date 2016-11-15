package com.expedia.vm.test.rail

import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.rail.responses.RailCreateTripResponse
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.rail.RailTotalPriceViewModel
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class RailTotalPriceViewModelTest {
    val context = RuntimeEnvironment.application
    val testVM = RailTotalPriceViewModel(context)

    val mockTotal = Mockito.mock(Money::class.java)
    val testTotalString = "100$"

    @Test
    fun testBundleDefaultDisplayStrings() {
        val bundleTextTestSub = TestSubscriber<String>()
        testVM.bundleTextLabelObservable.subscribe(bundleTextTestSub)

        val totalIncludesTestSub = TestSubscriber<String>()
        testVM.bundleTotalIncludesObservable.subscribe(totalIncludesTestSub)

        assertEquals(context.getString(R.string.total), bundleTextTestSub.onNextEvents[0])
        assertEquals(context.getString(R.string.payment_and_ticket_delivery_fees_may_also_apply), totalIncludesTestSub.onNextEvents[0])
    }

    @Test
    fun assertAccessibility() {
        assertEquals("", testVM.getAccessibleContentDescription(), "Muahaha now you have to write tests.")
    }

    @Test
    fun testUpdatePricing() {
        Mockito.`when`(mockTotal.getFormattedMoneyFromAmountAndCurrencyCode(Mockito.anyInt())).thenReturn(testTotalString)
        val testResponse = RailCreateTripResponse()
        testResponse.totalPrice = mockTotal

        val priceTestSub = TestSubscriber<String>()
        val breakdownTestSub = TestSubscriber<Boolean>()
        testVM.totalPriceObservable.subscribe(priceTestSub)
        testVM.costBreakdownEnabledObservable.subscribe(breakdownTestSub)
        testVM.updatePricing(testResponse)

        assertEquals(testTotalString, priceTestSub.onNextEvents[0])
        assertTrue(breakdownTestSub.onNextEvents[0])
    }
}