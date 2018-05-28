package com.expedia.bookings.packages.vm

import android.content.Context
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.PackageTestUtil
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.vm.BaseTotalPriceWidgetViewModel
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
@RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
class PackageTotalPriceViewModelTest {

    lateinit var context: Context
    lateinit var sut: PackageTotalPriceViewModel

    private val totalPrice = "200"
    private val savings = "20"
    private val pricePerPerson = "100"
    private val listOfChildren = listOf<Int>(10, 8)
    private val today = LocalDate.now()
    private val tomorrow = LocalDate.now().plusDays(1)
    private val destination = "New York"

    private val bundleTotalPriceWidgetCostBreakdownContDescText = "Bundle total is $totalPrice. This price includes taxes, fees for both flights and hotel. $savings. Cost Breakdown dialog. Button."
    private val bundleOverviewPriceWidgetText = "Bundle total is $totalPrice. This price includes taxes, fees for both flights and hotel. $savings"
    private val bundleOverviewPriceWidgetTextEmptySavings = "Bundle total is $totalPrice. This price includes taxes, fees for both flights and hotel. "
    private val tripOverviewPriceWidgetExpandedText = "Bundle price is $pricePerPerson per person. This price includes taxes, fees for both flights and hotel. Button to view bundle."
    private val bundleOverviewPriceWidgetButtonOpenText = "Trip to $destination. ${LocaleBasedDateFormatUtils.localDateToMMMd(today)} to ${LocaleBasedDateFormatUtils.localDateToMMMd(tomorrow)}, ${1 + listOfChildren.size} travelers"
    private val emptyString = ""

    private lateinit var mockAnalyticsProvider: AnalyticsProvider

    @Before
    fun setUp() {
        context = RuntimeEnvironment.application
        sut = PackageTotalPriceViewModel(context)
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @Test
    fun testAccessibleContentDescriptionWithAllFalseParameters() {
        val result = sut.getAccessibleContentDescription(false, false, false)
        assertEquals(emptyString, result)
    }

    @Test
    fun testAccessibleContentDescriptionWithCostBreakdownFlagTrueAllElseFalse() {
        sut.totalPriceObservable.onNext(totalPrice)
        sut.savingsPriceObservable.onNext(savings)

        val result = sut.getAccessibleContentDescription(true, false, false)
        assertEquals(bundleTotalPriceWidgetCostBreakdownContDescText, result)
    }

    @Test
    fun testAccessibleContentDescriptionWhenCostBreakdownObservableReturnsTrueAllFlagsFalse() {
        sut.totalPriceObservable.onNext(totalPrice)
        sut.savingsPriceObservable.onNext(savings)
        sut.costBreakdownEnabledObservable.onNext(true)

        val result = sut.getAccessibleContentDescription(false, false, false)
        assertEquals(bundleTotalPriceWidgetCostBreakdownContDescText, result)
    }

    @Test
    fun testAccessibleContentDescriptionWhenCostBreakdownObservableReturnsFalseAllFlagsFalse() {
        sut.costBreakdownEnabledObservable.onNext(false)

        val result = sut.getAccessibleContentDescription(false, false, false)
        assertEquals(emptyString, result)
    }

    @Test
    fun testAccessibleContentDescriptionNonSlidable() {
        sut.totalPriceObservable.onNext(totalPrice)
        sut.savingsPriceObservable.onNext(savings)

        val result = sut.getAccessibleContentDescription(false, false, false)
        assertEquals(bundleOverviewPriceWidgetText, result)
    }

    @Test
    fun testAccesssibleContentDescriptionNonSlidableSavingsObservableReturningNonNullValue() {
        sut.savingsPriceObservable.onNext(savings)

        val result = sut.getAccessibleContentDescription(false, false, false)
        assertEquals(emptyString, result)
    }

    @Test
    fun testAccesssibleContentDescriptionNonSlidableSavingsObservableReturningDefaultValue() {
        sut.totalPriceObservable.onNext(totalPrice)

        val result = sut.getAccessibleContentDescription(false, false, false)
        assertEquals(bundleOverviewPriceWidgetTextEmptySavings, result)
    }

    @Test
    fun testAccessibleContentDescriptionWithExpandableFlagTrue() {
        Db.setPackageParams(PackageTestUtil.getPackageSearchParams(destinationCityName = destination, startDate = today, endDate = tomorrow, childCount = listOfChildren))

        val result = sut.getAccessibleContentDescription(false, false, true)
        assertEquals(bundleOverviewPriceWidgetButtonOpenText, result)
    }

    @Test
    fun testAccessibleContentDescriptionWhenPerPersonObservableReturningNonNullValue() {
        sut.pricePerPersonObservable.onNext(pricePerPerson)

        val result = sut.getAccessibleContentDescription(false, false, false)
        assertEquals(tripOverviewPriceWidgetExpandedText, result)
    }

    @Test
    fun testSavingsFalseBundleSavingsABTestOff() {
        sut.shouldShowSavings.onNext(false)
        sut.priceWidgetClick.onNext(BaseTotalPriceWidgetViewModel.PriceWidgetEvent.SAVINGS_STRIP_CLICK)
        val controlEvar = mapOf(28 to "App.Package.RD.BundleWidget.SSSF")
        OmnitureTestUtils.assertLinkTracked("Rate Details", "App.Package.RD.BundleWidget.SSSF", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    fun testSavingsTrueBundleSavingsABTestOff() {
        sut.shouldShowSavings.onNext(true)
        sut.priceWidgetClick.onNext(BaseTotalPriceWidgetViewModel.PriceWidgetEvent.SAVINGS_STRIP_CLICK)
        val controlEvar = mapOf(28 to "App.Package.RD.BundleWidget.SSST")
        OmnitureTestUtils.assertLinkTracked("Rate Details", "App.Package.RD.BundleWidget.SSST", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    fun testSavingsFalseBundleSavingsABTestOn() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppPackagesBetterSavingsOnRateDetails)
        sut.shouldShowSavings.onNext(false)
        sut.priceWidgetClick.onNext(BaseTotalPriceWidgetViewModel.PriceWidgetEvent.SAVINGS_STRIP_CLICK)
        val controlEvar = mapOf(28 to "App.Package.RD.BundleWidget.SSSF")
        OmnitureTestUtils.assertLinkTracked("Rate Details", "App.Package.RD.BundleWidget.SSSF", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    fun testSavingsTrueBundleSavingsABTestOn() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppPackagesBetterSavingsOnRateDetails)
        sut.shouldShowSavings.onNext(true)
        sut.priceWidgetClick.onNext(BaseTotalPriceWidgetViewModel.PriceWidgetEvent.SAVINGS_STRIP_CLICK)
        val controlEvar = mapOf(28 to "App.Package.RD.SavingsStrip")
        OmnitureTestUtils.assertLinkTracked("Rate Details", "App.Package.RD.SavingsStrip", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }
}
