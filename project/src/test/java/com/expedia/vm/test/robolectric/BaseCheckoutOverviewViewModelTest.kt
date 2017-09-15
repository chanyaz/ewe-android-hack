package com.expedia.vm.test.robolectric

import android.content.Context
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.BaseCheckoutOverviewViewModel
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class BaseCheckoutOverviewViewModelTest {

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testCheckInWithoutCheckoutDate() {
        val datesTitleTestSubscriber = TestSubscriber<String>()
        val datesTitleContDescTestSubscriber = TestSubscriber<String>()

        val viewmodel = BaseCheckoutOverviewViewModel(getContext())

        viewmodel.datesTitle.subscribe(datesTitleTestSubscriber)
        viewmodel.datesTitleContDesc.subscribe(datesTitleContDescTestSubscriber)

        viewmodel.checkInWithoutCheckoutDate.onNext("2021-09-06")

        datesTitleTestSubscriber.assertValueCount(1)
        datesTitleContDescTestSubscriber.assertValueCount(1)

        assertEquals("Mon Sep 06, 2021", datesTitleTestSubscriber.onNextEvents.first())
        assertEquals("Mon Sep 06, 2021", datesTitleContDescTestSubscriber.onNextEvents.first())
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testCheckInAndCheckoutDate() {
        val datesTitleTestSubscriber = TestSubscriber<String>()
        val datesTitleContDescTestSubscriber = TestSubscriber<String>()

        val viewmodel = BaseCheckoutOverviewViewModel(getContext())

        viewmodel.datesTitle.subscribe(datesTitleTestSubscriber)
        viewmodel.datesTitleContDesc.subscribe(datesTitleContDescTestSubscriber)

        viewmodel.checkInAndCheckOutDate.onNext(Pair("1989-09-06", "2021-09-06"))

        datesTitleTestSubscriber.assertValueCount(1)
        datesTitleContDescTestSubscriber.assertValueCount(1)

        assertEquals("Wed Sep 06, 1989 - Mon Sep 06, 2021", datesTitleTestSubscriber.onNextEvents.first())
        assertEquals("Wed Sep 06, 1989 to Mon Sep 06, 2021", datesTitleContDescTestSubscriber.onNextEvents.first())
    }

    private fun getContext(): Context {
        return RuntimeEnvironment.application
    }
}