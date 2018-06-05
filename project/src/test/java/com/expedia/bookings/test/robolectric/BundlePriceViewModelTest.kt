package com.expedia.bookings.test.robolectric

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.packages.vm.PackageTotalPriceViewModel
import com.squareup.phrase.Phrase
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.ExcludeForBrands
import com.expedia.bookings.test.MultiBrand
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class BundlePriceViewModelTest {
    val context: Context = RuntimeEnvironment.application

    @Test
    fun testBundleSaveLabel() {
        val testSubscriber = TestObserver<String>()
        val testViewModelUniversalCKO = PackageTotalPriceViewModel(context)

        testViewModelUniversalCKO.savingsPriceObservable.subscribe(testSubscriber)

        val someMoney = Money("50.0", "USD")
        val expectedSavingLabel = Phrase.from(context, R.string.bundle_total_savings_TEMPLATE)
                .put("savings", someMoney.getFormattedMoney(Money.F_ALWAYS_TWO_PLACES_AFTER_DECIMAL))
                .format().toString()

        testViewModelUniversalCKO.savings.onNext(someMoney)
        assertEquals(expectedSavingLabel, testSubscriber.values()[1])
    }

    @Test
    fun testNoBundleSaveLabel() {
        val testSubscriber = TestObserver<String>()
        val testViewModelUniversalCKO = PackageTotalPriceViewModel(context)
        testViewModelUniversalCKO.savingsPriceObservable.subscribe(testSubscriber)

        val zeroMoney = Money("0.0", "US")
        testViewModelUniversalCKO.savings.onNext(zeroMoney)
        assertEquals("", testSubscriber.values()[0])

        val someMoney = Money("-50.0", "USD")
        testViewModelUniversalCKO.savings.onNext(someMoney)
        assertEquals("", testSubscriber.values()[1])
    }

    @Test
    @ExcludeForBrands(brands = [MultiBrand.ORBITZ])
    fun testBundleSaveLabelForJP() {
        RoboTestHelper.setPOS(PointOfSaleId.JAPAN)
        val testSubscriber = TestObserver<String>()
        val testViewModelUniversalCKO = PackageTotalPriceViewModel(context)
        testViewModelUniversalCKO.savingsPriceObservable.subscribe(testSubscriber)

        val someMoney = Money("50.00", "JPY")
        val expectedSavingLabel = Phrase.from(context, R.string.bundle_total_savings_TEMPLATE)
                .put("savings", someMoney.getFormattedMoney(Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL))
                .format().toString()

        testViewModelUniversalCKO.savings.onNext(someMoney)

        assertEquals(expectedSavingLabel, testSubscriber.values()[1])
    }

    @Test
    @ExcludeForBrands(brands = [MultiBrand.ORBITZ])
    fun testBundleSaveLabelNonIntegerForJP() {
        RoboTestHelper.setPOS(PointOfSaleId.JAPAN)
        val testSubscriber = TestObserver<String>()
        val testViewModelUniversalCKO = PackageTotalPriceViewModel(context)
        testViewModelUniversalCKO.savingsPriceObservable.subscribe(testSubscriber)

        val someMoney = Money("50.34", "JPY")
        val expectedSavingLabel = Phrase.from(context, R.string.bundle_total_savings_TEMPLATE)
                .put("savings", someMoney.getFormattedMoney(Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL))
                .format().toString()

        testViewModelUniversalCKO.savings.onNext(someMoney)
        assertEquals(expectedSavingLabel, testSubscriber.values()[1])
    }

    @Test
    @ExcludeForBrands(brands = [MultiBrand.ORBITZ])
    fun testBundleTotalIntegerForJP() {
        RoboTestHelper.setPOS(PointOfSaleId.JAPAN)
        val testSubscriber = TestObserver<String>()
        val testViewModelUniversalCKO = PackageTotalPriceViewModel(context)
        testViewModelUniversalCKO.totalPriceObservable.subscribe(testSubscriber)

        val someMoney = Money("1120.00", "JPY")

        testViewModelUniversalCKO.total.onNext(someMoney)
        assertEquals("JPY1,120", testSubscriber.values()[0])
    }

    @Test
    @ExcludeForBrands(brands = [MultiBrand.ORBITZ])
    fun testBundleTotalIntegerNonIntegerForJP() {
        RoboTestHelper.setPOS(PointOfSaleId.JAPAN)
        val testSubscriber = TestObserver<String>()
        val testViewModelUniversalCKO = PackageTotalPriceViewModel(context)
        testViewModelUniversalCKO.totalPriceObservable.subscribe(testSubscriber)

        val someMoney = Money("1120.86", "JPY")

        testViewModelUniversalCKO.total.onNext(someMoney)
        assertEquals("JPY1,120.86", testSubscriber.values()[0])
    }
}
