package com.expedia.vm.test.rail

import android.app.Activity
import com.expedia.bookings.data.Money
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.vm.rail.RailCheckoutViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import rx.observers.TestSubscriber
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class RailCheckoutViewModelTest {
    lateinit var testViewModel: RailCheckoutViewModel
    val testPrice = Money(20, "USD")
    val expectedSlideToPurchaseText = "Your card will be charged $20"

    @Before
    fun setUp() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        Ui.getApplication(activity).defaultRailComponents()
        testViewModel = RailCheckoutViewModel(activity)
    }

    @Test
    fun testTotalPriceText() {
        val testSubscriber = TestSubscriber<CharSequence>()
        testViewModel.sliderPurchaseTotalText.subscribe(testSubscriber)

        testViewModel.totalPriceObserver.onNext(testPrice)
        assertEquals(expectedSlideToPurchaseText, testSubscriber.onNextEvents[0])
    }
}