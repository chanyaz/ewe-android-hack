package com.expedia.bookings.test.robolectric

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.vm.packages.BundleTotalPriceViewModel
import com.squareup.phrase.Phrase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class BundlePriceViewModelTest {
    var testViewModel: BundleTotalPriceViewModel by Delegates.notNull()
    val context: Context = RuntimeEnvironment.application

    @Before
    fun before() {
        testViewModel = BundleTotalPriceViewModel(context)
    }

    @Test
    fun testBundleSaveLabel() {
        val testSubscriber = TestSubscriber<String>()
        val zeroMoney = Money("0.0", "US")
        testViewModel.savingsPriceObservable.subscribe(testSubscriber)
        testViewModel.savings.onNext(zeroMoney)
        assertEquals("", testSubscriber.onNextEvents[0])

        val someMoney = Money("50.0", "USD")
        val expectedSavingLabel = Phrase.from(context, R.string.bundle_total_savings_TEMPLATE)
                .put("savings", someMoney.getFormattedMoney(Money.F_ALWAYS_TWO_PLACES_AFTER_DECIMAL))
                .format().toString()

        testViewModel.savings.onNext(someMoney)
        assertEquals(expectedSavingLabel, testSubscriber.onNextEvents[1])

    }
}
