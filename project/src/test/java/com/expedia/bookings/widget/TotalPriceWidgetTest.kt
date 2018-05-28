package com.expedia.bookings.widget

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.packages.vm.PackageCostSummaryBreakdownViewModel
import com.expedia.bookings.packages.vm.PackageTotalPriceViewModel
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RoboTestHelper
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.BaseTotalPriceWidgetViewModel
import com.expedia.vm.flights.FlightTotalPriceViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.shadows.ShadowAlertDialog
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
class TotalPriceWidgetTest {
    val context = RuntimeEnvironment.application
    lateinit var totalPriceWidget: TotalPriceWidget

    @Before
    fun before() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        totalPriceWidget = LayoutInflater.from(activity).inflate(R.layout.test_total_price_widget, null) as TotalPriceWidget
    }

    @Test
    fun testTotalPriceProgressBarNotShownWhenNotBucketed() {
        val flightTotalPriceViewModel = FlightTotalPriceViewModel(context)
        totalPriceWidget.viewModel = flightTotalPriceViewModel
        totalPriceWidget.resetPriceWidget()
        assertEquals(View.GONE, totalPriceWidget.priceProgressBar.visibility)

        val packageTotalPriceViewModel = PackageTotalPriceViewModel(context)
        totalPriceWidget.viewModel = packageTotalPriceViewModel
        totalPriceWidget.resetPriceWidget()
        assertEquals(View.GONE, totalPriceWidget.priceProgressBar.visibility)
    }

    @Test
    fun testTotalPriceProgressBarShownInFlightsWhenBucketed() {
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppFlightRateDetailsFromCache)
        val flightTotalPriceViewModel = FlightTotalPriceViewModel(context)
        totalPriceWidget.viewModel = flightTotalPriceViewModel
        totalPriceWidget.resetPriceWidget()
        assertEquals(View.VISIBLE, totalPriceWidget.priceProgressBar.visibility)

        totalPriceWidget.priceProgressBar.visibility = View.GONE
        val packageTotalPriceViewModel = PackageTotalPriceViewModel(context)
        totalPriceWidget.viewModel = packageTotalPriceViewModel
        totalPriceWidget.resetPriceWidget()
        assertEquals(View.GONE, totalPriceWidget.priceProgressBar.visibility)
    }

    @Test
    fun testTotalPriceBetterSavingContainerClick() {
        val packageTotalPriceViewModel = PackageTotalPriceViewModel(context)
        totalPriceWidget.viewModel = packageTotalPriceViewModel
        totalPriceWidget.breakdown.viewmodel = PackageCostSummaryBreakdownViewModel(context)

        val testObserver = TestObserver<BaseTotalPriceWidgetViewModel.PriceWidgetEvent>()
        packageTotalPriceViewModel.priceWidgetClick.subscribe(testObserver)
        totalPriceWidget.betterSavingContainer.performClick()
        assertNull(ShadowAlertDialog.getLatestAlertDialog())
        testObserver.assertValuesAndClear(BaseTotalPriceWidgetViewModel.PriceWidgetEvent.SAVINGS_STRIP_CLICK)

        totalPriceWidget.toggleBundleTotalCompoundDrawable(true)
        totalPriceWidget.betterSavingContainer.performClick()
        assertNotNull(ShadowAlertDialog.getLatestAlertDialog())
        testObserver.assertValuesAndClear(BaseTotalPriceWidgetViewModel.PriceWidgetEvent.SAVINGS_STRIP_CLICK)
    }

    @Test
    fun testTotalPriceClick() {
        val packageTotalPriceViewModel = PackageTotalPriceViewModel(context)
        totalPriceWidget.viewModel = packageTotalPriceViewModel
        totalPriceWidget.breakdown.viewmodel = PackageCostSummaryBreakdownViewModel(context)

        val testObserver = TestObserver<BaseTotalPriceWidgetViewModel.PriceWidgetEvent>()
        packageTotalPriceViewModel.priceWidgetClick.subscribe(testObserver)
        totalPriceWidget.performClick()
        assertNull(ShadowAlertDialog.getLatestAlertDialog())
        testObserver.assertValuesAndClear(BaseTotalPriceWidgetViewModel.PriceWidgetEvent.BUNDLE_WIDGET_CLICK)

        totalPriceWidget.toggleBundleTotalCompoundDrawable(true)
        totalPriceWidget.performClick()
        assertNotNull(ShadowAlertDialog.getLatestAlertDialog())
        testObserver.assertValuesAndClear(BaseTotalPriceWidgetViewModel.PriceWidgetEvent.BUNDLE_WIDGET_CLICK)
    }

    @Test
    fun testTotalPriceBetterSavingViewClick() {
        val packageTotalPriceViewModel = PackageTotalPriceViewModel(context)
        totalPriceWidget.viewModel = packageTotalPriceViewModel
        totalPriceWidget.breakdown.viewmodel = PackageCostSummaryBreakdownViewModel(context)

        val testObserver = TestObserver<BaseTotalPriceWidgetViewModel.PriceWidgetEvent>()
        packageTotalPriceViewModel.priceWidgetClick.subscribe(testObserver)
        totalPriceWidget.betterSavingView.performClick()
        assertNull(ShadowAlertDialog.getLatestAlertDialog())
        testObserver.assertValuesAndClear(BaseTotalPriceWidgetViewModel.PriceWidgetEvent.SAVINGS_BUTTON_CLICK)

        totalPriceWidget.toggleBundleTotalCompoundDrawable(true)
        totalPriceWidget.betterSavingView.performClick()
        assertNotNull(ShadowAlertDialog.getLatestAlertDialog())
        testObserver.assertValuesAndClear(BaseTotalPriceWidgetViewModel.PriceWidgetEvent.SAVINGS_BUTTON_CLICK)
    }

    @Test
    fun testTotalPriceBundleTotalTextClick() {
        val packageTotalPriceViewModel = PackageTotalPriceViewModel(context)
        totalPriceWidget.viewModel = packageTotalPriceViewModel
        totalPriceWidget.breakdown.viewmodel = PackageCostSummaryBreakdownViewModel(context)

        val testObserver = TestObserver<BaseTotalPriceWidgetViewModel.PriceWidgetEvent>()
        packageTotalPriceViewModel.priceWidgetClick.subscribe(testObserver)
        totalPriceWidget.bundleTotalText.performClick()
        assertNull(ShadowAlertDialog.getLatestAlertDialog())
        testObserver.assertValuesAndClear(BaseTotalPriceWidgetViewModel.PriceWidgetEvent.INFO_ICON_CLICK)

        totalPriceWidget.toggleBundleTotalCompoundDrawable(true)
        totalPriceWidget.bundleTotalText.performClick()
        assertNotNull(ShadowAlertDialog.getLatestAlertDialog())
        testObserver.assertValuesAndClear(BaseTotalPriceWidgetViewModel.PriceWidgetEvent.INFO_ICON_CLICK)
    }

    @Test
    fun testTotalPriceBundleTotalIncludesTextClick() {
        val packageTotalPriceViewModel = PackageTotalPriceViewModel(context)
        totalPriceWidget.viewModel = packageTotalPriceViewModel
        totalPriceWidget.breakdown.viewmodel = PackageCostSummaryBreakdownViewModel(context)

        val testObserver = TestObserver<BaseTotalPriceWidgetViewModel.PriceWidgetEvent>()
        packageTotalPriceViewModel.priceWidgetClick.subscribe(testObserver)
        totalPriceWidget.bundleTotalIncludes.performClick()
        assertNull(ShadowAlertDialog.getLatestAlertDialog())
        testObserver.assertValuesAndClear(BaseTotalPriceWidgetViewModel.PriceWidgetEvent.INFO_ICON_CLICK)

        totalPriceWidget.toggleBundleTotalCompoundDrawable(true)
        totalPriceWidget.bundleTotalIncludes.performClick()
        assertNotNull(ShadowAlertDialog.getLatestAlertDialog())
        testObserver.assertValuesAndClear(BaseTotalPriceWidgetViewModel.PriceWidgetEvent.INFO_ICON_CLICK)
    }

    @Test
    fun testTotalBundlePriceClick() {
        val packageTotalPriceViewModel = PackageTotalPriceViewModel(context)
        totalPriceWidget.viewModel = packageTotalPriceViewModel
        totalPriceWidget.breakdown.viewmodel = PackageCostSummaryBreakdownViewModel(context)

        val testObserver = TestObserver<BaseTotalPriceWidgetViewModel.PriceWidgetEvent>()
        packageTotalPriceViewModel.priceWidgetClick.subscribe(testObserver)

        totalPriceWidget.priceAndSavingContainer.performClick()
        assertNull(ShadowAlertDialog.getLatestAlertDialog())
        testObserver.assertValuesAndClear(BaseTotalPriceWidgetViewModel.PriceWidgetEvent.BUNDLE_PRICE_CLICK)

        totalPriceWidget.toggleBundleTotalCompoundDrawable(true)
        totalPriceWidget.priceAndSavingContainer.performClick()
        assertNotNull(ShadowAlertDialog.getLatestAlertDialog())
        testObserver.assertValuesAndClear(BaseTotalPriceWidgetViewModel.PriceWidgetEvent.BUNDLE_PRICE_CLICK)
    }
}
