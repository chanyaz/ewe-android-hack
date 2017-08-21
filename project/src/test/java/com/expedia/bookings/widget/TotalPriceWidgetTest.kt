package com.expedia.bookings.widget

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.test.robolectric.RoboTestHelper
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.packages.FlightTotalPriceViewModel
import com.expedia.vm.packages.PackageTotalPriceViewModel
import com.mobiata.android.util.SettingUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

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
    fun testTotalPriceProgressBarShownInFLightsWhenBucketed() {
        SettingUtils.save(context, R.string.preference_flight_rate_detail_from_cache, true)
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
}