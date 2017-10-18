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
import com.expedia.bookings.services.TestObserver
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class
RailTotalPriceViewModelTest {
    val context = RuntimeEnvironment.application
    val testVM = RailTotalPriceViewModel(context)

    val mockTotal = Mockito.mock(Money::class.java)
    val testTotalString = "$100"

    @Test
    fun testBundleDefaultDisplayStrings() {
        val bundleTextTestSub = TestObserver<String>()
        testVM.bundleTextLabelObservable.subscribe(bundleTextTestSub)

        val totalIncludesTestSub = TestObserver<String>()
        testVM.bundleTotalIncludesObservable.subscribe(totalIncludesTestSub)

        assertEquals(context.getString(R.string.total), bundleTextTestSub.values()[0])
        assertEquals(context.getString(R.string.payment_and_ticket_delivery_fees_may_also_apply), totalIncludesTestSub.values()[0])
    }

    @Test
    fun assertAccessibility() {
        Mockito.`when`(mockTotal.getFormattedMoneyFromAmountAndCurrencyCode(Mockito.anyInt())).thenReturn(testTotalString)
        testVM.total.onNext(mockTotal)
        assertEquals("Total, Payment and ticket delivery fees may also apply, $testTotalString. Cost breakdown dialog. Button.",
                testVM.getAccessibleContentDescription())
    }

    @Test
    fun testUpdatePricing() {
        Mockito.`when`(mockTotal.getFormattedMoneyFromAmountAndCurrencyCode(Mockito.anyInt())).thenReturn(testTotalString)
        val testResponse = RailCreateTripResponse()
        testResponse.totalPrice = mockTotal

        val priceTestSub = TestObserver<String>()
        val breakdownTestSub = TestObserver<Boolean>()
        testVM.totalPriceObservable.subscribe(priceTestSub)
        testVM.costBreakdownEnabledObservable.subscribe(breakdownTestSub)
        testVM.updatePricing(testResponse)

        assertEquals(testTotalString, priceTestSub.values()[0])
        assertTrue(breakdownTestSub.values()[0])
    }
}