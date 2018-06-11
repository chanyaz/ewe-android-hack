package com.expedia.bookings.packages.vm

import android.content.Context
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.data.packages.PackageCostSummaryBreakdownModel
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.FontCache
import com.expedia.vm.BaseCostSummaryBreakdownViewModel
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RoboTestHelper.setPOS
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
@RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
class PackageCostSummaryBreakdownViewModelTest {

    private fun getContext(): Context {
        return RuntimeEnvironment.application
    }

    @Test
    fun testCostSummaryBreakdownRows() {
        validateCostSummaryBreakdownRows(false)
    }

    @Test
    fun testCostSummaryBreakdownRowsForJP() {
        setPOS(PointOfSaleId.JAPAN)
        validateCostSummaryBreakdownRows(true)
    }

    private fun validateCostSummaryBreakdownRows(isJP: Boolean) {
        val textSize = getContext().getResources().getDimension(R.dimen.type_200_text_size)
        val rowsAndContDesc = getCostSummaryBreakdownRowsAndContDesc()
        val rows = rowsAndContDesc.first
        val contDesc = rowsAndContDesc.second
        assertEquals(8, rows.count())

        assertEquals(null, rows[0].title)
        assertEquals(null, rows[0].cost)
        assertEquals(true, rows[0].separator)

        assertEquals("Hotel", rows[1].title)
        assertEquals("$200.00", rows[1].cost)
        assertEquals(FontCache.Font.ROBOTO_MEDIUM, rows[1].titleTypeface)
        assertEquals(FontCache.Font.ROBOTO_REGULAR, rows[1].costTypeface)
        assertEquals(false, rows[1].separator)
        assertEquals(textSize, rows[1].titleTextSize)
        assertEquals(textSize, rows[1].costTextSize)

        assertEquals("Roundtrip flights", rows[2].title)
        assertEquals("$100.00", rows[2].cost)
        assertEquals(FontCache.Font.ROBOTO_MEDIUM, rows[2].titleTypeface)
        assertEquals(FontCache.Font.ROBOTO_REGULAR, rows[2].costTypeface)
        assertEquals(false, rows[2].separator)
        assertEquals(textSize, rows[2].titleTextSize)
        assertEquals(textSize, rows[2].costTextSize)

        assertEquals("Total", rows[3].title)
        assertEquals("$300.00", rows[3].cost)
        assertEquals(FontCache.Font.ROBOTO_MEDIUM, rows[3].titleTypeface)
        assertEquals(FontCache.Font.ROBOTO_MEDIUM, rows[3].costTypeface)
        assertEquals(false, rows[3].separator)
        assertEquals(textSize, rows[3].titleTextSize)
        assertEquals(textSize, rows[3].costTextSize)

        assertEquals("Savings for booking together", rows[4].title)
        assertEquals("-$120.00", rows[4].cost)
        assertEquals(ContextCompat.getColor(getContext(), R.color.cost_summary_breakdown_savings_cost_color), rows[4].titleColor)
        assertEquals(ContextCompat.getColor(getContext(), R.color.cost_summary_breakdown_savings_cost_color), rows[4].costColor)
        assertEquals(FontCache.Font.ROBOTO_MEDIUM, rows[4].titleTypeface)
        assertEquals(FontCache.Font.ROBOTO_MEDIUM, rows[4].costTypeface)
        assertEquals(false, rows[4].separator)
        assertEquals(textSize, rows[4].titleTextSize)
        assertEquals(textSize, rows[4].costTextSize)

        assertEquals(null, rows[5].title)
        assertEquals(null, rows[5].cost)
        assertEquals(true, rows[5].separator)

        val titleText = if (isJP) "Trip total (with taxes & fee)" else "Bundle total"
        assertEquals(titleText, rows[6].title)
        assertEquals("$300.00", rows[6].cost)
        assertEquals(ContextCompat.getColor(getContext(), R.color.background_holo_dark), rows[6].titleColor)
        assertEquals(ContextCompat.getColor(getContext(), R.color.text_dark), rows[6].costColor)
        assertEquals(FontCache.Font.ROBOTO_MEDIUM, rows[6].titleTypeface)
        assertEquals(FontCache.Font.ROBOTO_REGULAR, rows[6].costTypeface)
        assertEquals(false, rows[6].separator)
        assertEquals(true, rows[6].strikeThrough)

        assertEquals("includes hotel and flights", rows[7].title)
        assertEquals("$180.00", rows[7].cost)
        assertEquals(null, rows[7].titleColor)
        assertEquals(ContextCompat.getColor(getContext(), R.color.background_holo_dark), rows[7].costColor)
        assertEquals(FontCache.Font.ROBOTO_REGULAR, rows[7].titleTypeface)
        assertEquals(FontCache.Font.ROBOTO_MEDIUM, rows[7].costTypeface)
        assertEquals(false, rows[7].separator)
        assertEquals(false, rows[7].strikeThrough)
        assertEquals(textSize, rows[7].titleTextSize)

        assertEquals("Price Summary Dialog. Here is the price summary for your bundle. Hotels is $200.00. Roundtrip flights is $100.00. Total is $300.00. Savings for booking together is $120.00. Bundle total for booking together is $180.00. This price includes taxes, fees for both flights and hotels.", contDesc)
    }

    private fun getCostSummaryBreakdownRowsAndContDesc(): Pair<List<BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow>, String> {
        val viewModel = PackageCostSummaryBreakdownViewModel(getContext())
        val rowsObserver = TestObserver<List<BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow>>()
        val priceSummaryContDescObserver = TestObserver<String>()
        viewModel.addRows.subscribe(rowsObserver)
        viewModel.priceSummaryContainerDescription.subscribe(priceSummaryContDescObserver)
        viewModel.packageCostSummaryObservable.onNext(PackageCostSummaryBreakdownModel("$200.00", "$100.00", "$300.00", "$120.00", "$180.00"))
        return Pair(rowsObserver.values()[0], priceSummaryContDescObserver.values()[0])
    }
}
