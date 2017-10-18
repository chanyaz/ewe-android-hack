package com.expedia.vm.packages

import android.content.Context
import android.icu.util.CurrencyAmount
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.packages.PackageCreateTripParams
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.FontCache
import com.expedia.vm.BaseCostSummaryBreakdownViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import java.math.BigDecimal
import java.util.Arrays
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
@RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
class PackageCostSummaryBreakdownViewModelTest {
    val packageServiceRule = ServicesRule(PackageServices::class.java)
        @Rule get

    private fun getContext(): Context {
        return RuntimeEnvironment.application
    }

    @Test
    fun testNoResortFee() {
        val rows = getCostSummaryBreakdownRows(false)
        assertEquals(6, rows.count())

        assertEquals("Hotel + Flights", rows[0].title)
        assertEquals("$2,230.34", rows[0].cost)
        assertEquals(null, rows[0].color)
        assertEquals(null, rows[0].typeface)
        assertEquals(false, rows[0].separator)

        assertEquals("1 room, 2 nights, 5 guests", rows[1].title)
        assertEquals(null, rows[1].cost)
        assertEquals(null, rows[1].color)
        assertEquals(null, rows[1].typeface)
        assertEquals(false, rows[1].separator)

        assertEquals("Taxes & Fees Included ($333.08)", rows[2].title)
        assertEquals(null, rows[2].cost)
        assertEquals(null, rows[2].color)
        assertEquals(null, rows[2].typeface)
        assertEquals(false, rows[2].separator)

        assertEquals("Bundle Discount", rows[3].title)
        assertEquals("-$28.00", rows[3].cost)
        assertEquals(ContextCompat.getColor(getContext(), R.color.cost_summary_breakdown_savings_cost_color), rows[3].color)
        assertEquals(null, rows[3].typeface)
        assertEquals(false, rows[3].separator)

        assertEquals(null, rows[4].title)
        assertEquals(null, rows[4].cost)
        assertEquals(null, rows[4].color)
        assertEquals(null, rows[4].typeface)
        assertEquals(true, rows[4].separator)

        assertEquals("Bundle total", rows[5].title)
        assertEquals("$2,202.34", rows[5].cost)
        assertEquals(null, rows[5].color)
        assertEquals(FontCache.Font.ROBOTO_MEDIUM, rows[5].typeface)
        assertEquals(false, rows[5].separator)
    }

    @Test
    fun testHotelWithResortFeesWhenShowBundleTotalWhenResortFeesFalse() {
        PointOfSaleTestConfiguration.configurePointOfSale(getContext(), "MockSharedData/pos_locale_test_config.json", false)

        val rows = getCostSummaryBreakdownRows(true)
        assertEquals(7, rows.count())

        assertEquals("Hotel + Flights", rows[0].title)
        assertEquals("$174.01", rows[0].cost)
        assertEquals(null, rows[0].color)
        assertEquals(null, rows[0].typeface)
        assertEquals(false, rows[0].separator)

        assertEquals("1 room, 1 night, 1 guest", rows[1].title)
        assertEquals(null, rows[1].cost)
        assertEquals(null, rows[1].color)
        assertEquals(null, rows[1].typeface)
        assertEquals(false, rows[1].separator)

        assertEquals("Taxes & Fees Included ($38.63)", rows[2].title)
        assertEquals(null, rows[2].cost)
        assertEquals(null, rows[2].color)
        assertEquals(null, rows[2].typeface)
        assertEquals(false, rows[2].separator)

        assertEquals("Bundle Discount", rows[3].title)
        assertEquals("-$0.33", rows[3].cost)
        assertEquals(ContextCompat.getColor(getContext(), R.color.cost_summary_breakdown_savings_cost_color), rows[3].color)
        assertEquals(null, rows[3].typeface)
        assertEquals(false, rows[3].separator)

        assertEquals(null, rows[4].title)
        assertEquals(null, rows[4].cost)
        assertEquals(null, rows[4].color)
        assertEquals(null, rows[4].typeface)
        assertEquals(true, rows[4].separator)

        assertEquals("Total Due Today", rows[5].title)
        assertEquals("$173.68", rows[5].cost)
        assertEquals(null, rows[5].color)
        assertEquals(FontCache.Font.ROBOTO_MEDIUM, rows[5].typeface)
        assertEquals(false, rows[5].separator)

        assertEquals("Local charges due at hotel", rows[6].title)
        assertEquals("$35.84", rows[6].cost)
        assertEquals(null, rows[6].color)
        assertEquals(null, rows[6].typeface)
        assertEquals(false, rows[6].separator)
    }

    @Test
    fun testHotelWithResortFeesWhenShowBundleTotalWhenResortFeesTrue() {
        PointOfSaleTestConfiguration.configurePointOfSale(getContext(), "MockSharedData/pos_test_config.json", false)

        val rows = getCostSummaryBreakdownRows(true)
        assertEquals(8, rows.count())

        assertEquals("Hotel + Flights", rows[0].title)
        assertEquals("$174.01", rows[0].cost)
        assertEquals(null, rows[0].color)
        assertEquals(null, rows[0].typeface)
        assertEquals(false, rows[0].separator)

        assertEquals("1 room, 1 night, 1 guest", rows[1].title)
        assertEquals(null, rows[1].cost)
        assertEquals(null, rows[1].color)
        assertEquals(null, rows[1].typeface)
        assertEquals(false, rows[1].separator)

        assertEquals("Taxes & Fees Included ($38.63)", rows[2].title)
        assertEquals(null, rows[2].cost)
        assertEquals(null, rows[2].color)
        assertEquals(null, rows[2].typeface)
        assertEquals(false, rows[2].separator)

        assertEquals("Bundle Discount", rows[3].title)
        assertEquals("-$0.33", rows[3].cost)
        assertEquals(ContextCompat.getColor(getContext(), R.color.cost_summary_breakdown_savings_cost_color), rows[3].color)
        assertEquals(null, rows[3].typeface)
        assertEquals(false, rows[3].separator)

        assertEquals("Local charges due at hotel", rows[4].title)
        assertEquals("$35.84", rows[4].cost)
        assertEquals(null, rows[4].color)
        assertEquals(null, rows[4].typeface)
        assertEquals(false, rows[4].separator)

        assertEquals(null, rows[5].title)
        assertEquals(null, rows[5].cost)
        assertEquals(null, rows[5].color)
        assertEquals(null, rows[5].typeface)
        assertEquals(true, rows[5].separator)

        assertEquals("Bundle total", rows[6].title)
        assertEquals("$209.52", rows[6].cost)
        assertEquals(null, rows[6].color)
        assertEquals(FontCache.Font.ROBOTO_MEDIUM, rows[6].typeface)
        assertEquals(false, rows[6].separator)

        assertEquals("Total Due Today", rows[7].title)
        assertEquals("$173.68", rows[7].cost)
        assertEquals(null, rows[7].color)
        assertEquals(null, rows[7].typeface)
        assertEquals(false, rows[7].separator)
    }

    @Test
    fun testNoBundleDiscount() {
        val rows = getCostSummaryBreakdownRowsNoBundleDiscount()
        assertEquals(5, rows.count())

        assertEquals("Hotel + Flights", rows[0].title)
        assertEquals("$2,230.34", rows[0].cost)
        assertEquals(null, rows[0].color)
        assertEquals(null, rows[0].typeface)
        assertEquals(false, rows[0].separator)

        assertEquals("1 room, 2 nights, 5 guests", rows[1].title)
        assertEquals(null, rows[1].cost)
        assertEquals(null, rows[1].color)
        assertEquals(null, rows[1].typeface)
        assertEquals(false, rows[1].separator)

        assertEquals("Taxes & Fees Included ($333.08)", rows[2].title)
        assertEquals(null, rows[2].cost)
        assertEquals(null, rows[2].color)
        assertEquals(null, rows[2].typeface)
        assertEquals(false, rows[2].separator)

        assertEquals(null, rows[3].title)
        assertEquals(null, rows[3].cost)
        assertEquals(null, rows[3].color)
        assertEquals(null, rows[3].typeface)
        assertEquals(true, rows[3].separator)

        assertEquals("Bundle total", rows[4].title)
        assertEquals("$2,202.34", rows[4].cost)
        assertEquals(null, rows[4].color)
        assertEquals(FontCache.Font.ROBOTO_MEDIUM, rows[4].typeface)
        assertEquals(false, rows[4].separator)
    }

    @Test
    fun testAirlineFee() {
        val rows = getCostSummaryBreakdownRowsWithAirlineFee(5.20)
        assertEquals(6, rows.count())

        assertEquals("Hotel + Flights", rows[0].title)
        assertEquals("$2,230.34", rows[0].cost)
        assertEquals(null, rows[0].color)
        assertEquals(null, rows[0].typeface)
        assertEquals(false, rows[0].separator)

        assertEquals("1 room, 2 nights, 5 guests", rows[1].title)
        assertEquals(null, rows[1].cost)
        assertEquals(null, rows[1].color)
        assertEquals(null, rows[1].typeface)
        assertEquals(false, rows[1].separator)

        assertEquals("Taxes & Fees Included ($333.08)", rows[2].title)
        assertEquals(null, rows[2].cost)
        assertEquals(null, rows[2].color)
        assertEquals(null, rows[2].typeface)
        assertEquals(false, rows[2].separator)

        assertEquals("Payment Method Fee", rows[3].title)
        assertEquals("$5.20", rows[3].cost)
        assertEquals(null, rows[3].color)
        assertEquals(null, rows[3].typeface)
        assertEquals(false, rows[3].separator)

        assertEquals(null, rows[4].title)
        assertEquals(null, rows[4].cost)
        assertEquals(null, rows[4].color)
        assertEquals(null, rows[4].typeface)
        assertEquals(true, rows[4].separator)

        assertEquals("Bundle total", rows[5].title)
        assertEquals("$2,202.34", rows[5].cost)
        assertEquals(null, rows[5].color)
        assertEquals(FontCache.Font.ROBOTO_MEDIUM, rows[5].typeface)
        assertEquals(false, rows[5].separator)
    }

    @Test
    fun testZeroAirlineFee() {
        val rows = getCostSummaryBreakdownRowsWithAirlineFee(0.00)
        assertEquals(5, rows.count())

        assertEquals("Hotel + Flights", rows[0].title)
        assertEquals("$2,230.34", rows[0].cost)
        assertEquals(null, rows[0].color)
        assertEquals(null, rows[0].typeface)
        assertEquals(false, rows[0].separator)

        assertEquals("1 room, 2 nights, 5 guests", rows[1].title)
        assertEquals(null, rows[1].cost)
        assertEquals(null, rows[1].color)
        assertEquals(null, rows[1].typeface)
        assertEquals(false, rows[1].separator)

        assertEquals("Taxes & Fees Included ($333.08)", rows[2].title)
        assertEquals(null, rows[2].cost)
        assertEquals(null, rows[2].color)
        assertEquals(null, rows[2].typeface)
        assertEquals(false, rows[2].separator)

        assertEquals(null, rows[3].title)
        assertEquals(null, rows[3].cost)
        assertEquals(null, rows[3].color)
        assertEquals(null, rows[3].typeface)
        assertEquals(true, rows[3].separator)

        assertEquals("Bundle total", rows[4].title)
        assertEquals("$2,202.34", rows[4].cost)
        assertEquals(null, rows[4].color)
        assertEquals(FontCache.Font.ROBOTO_MEDIUM, rows[4].typeface)
        assertEquals(false, rows[4].separator)
    }

    private fun getCostSummaryBreakdownRows(hasFees: Boolean): List<BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow> {
        val viewModel = PackageCostSummaryBreakdownViewModel(getContext())
        val prodID = if (hasFees) "create_trip_with_resort_fee" else "create_trip_multitraveler"

        val observer = TestSubscriber<PackageCreateTripResponse>()
        val params = PackageCreateTripParams(prodID, "6139057", 2, false, Arrays.asList(0, 8, 12))

        packageServiceRule.services!!.createTrip(params).subscribe(observer)

        observer.awaitTerminalEvent(10, TimeUnit.SECONDS)
        observer.assertNoErrors()
        observer.assertComplete()

        val rowsObserver = TestSubscriber<List<BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow>>()
        viewModel.addRows.subscribe(rowsObserver)
        viewModel.packageCostSummaryObservable.onNext(observer.onNextEvents[0])
        return rowsObserver.onNextEvents[0]
    }

    private fun getCostSummaryBreakdownRowsNoBundleDiscount(): List<BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow> {
        val viewModel = PackageCostSummaryBreakdownViewModel(getContext())
        val prodID = "create_trip_with_negative_savings"

        val observer = TestSubscriber<PackageCreateTripResponse>()
        val params = PackageCreateTripParams(prodID, "6139057", 2, false, Arrays.asList(0, 8, 12))

        packageServiceRule.services!!.createTrip(params).subscribe(observer)

        observer.awaitTerminalEvent(10, TimeUnit.SECONDS)
        observer.assertNoErrors()
        observer.assertComplete()

        val rowsObserver = TestSubscriber<List<BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow>>()
        viewModel.addRows.subscribe(rowsObserver)
        viewModel.packageCostSummaryObservable.onNext(observer.onNextEvents[0])
        return rowsObserver.onNextEvents[0]
    }

    private fun getCostSummaryBreakdownRowsWithAirlineFee(airlineFee: Double): List<BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow> {
        val viewModel = PackageCostSummaryBreakdownViewModel(getContext())
        val prodID = "create_trip_with_negative_savings"

        val observer = TestSubscriber<PackageCreateTripResponse>()
        val params = PackageCreateTripParams(prodID, "6139057", 2, false, Arrays.asList(0, 8, 12))

        packageServiceRule.services!!.createTrip(params).subscribe(observer)

        observer.awaitTerminalEvent(10, TimeUnit.SECONDS)
        observer.assertNoErrors()
        observer.assertComplete()

        val createTrip = observer.onNextEvents[0]
        createTrip.selectedCardFees = Money(BigDecimal(airlineFee), "USD")

        val rowsObserver = TestSubscriber<List<BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow>>()
        viewModel.addRows.subscribe(rowsObserver)
        viewModel.packageCostSummaryObservable.onNext(createTrip)
        return rowsObserver.onNextEvents[0]
    }
}
