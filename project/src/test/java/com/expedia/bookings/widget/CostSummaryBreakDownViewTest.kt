package com.expedia.bookings.widget

import android.content.Context
import android.support.v4.content.ContextCompat
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.FontCache
import com.expedia.vm.BaseCostSummaryBreakdownViewModel
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.util.ArrayList
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class CostSummaryBreakDownViewTest {

    fun getContext(): Context {
        return RuntimeEnvironment.application
    }

    @Test
    fun testRow() {
        val costSummaryBreakDownView = CostSummaryBreakDownView(getContext(), null)
        costSummaryBreakDownView.viewmodel = object: BaseCostSummaryBreakdownViewModel(getContext()) {
            override fun trackBreakDownClicked() {
            }
        }

        val list = ArrayList<BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow>()
        list.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow
                .Builder()
                .title("Hotel + Flights")
                .cost("$230")
                .color(ContextCompat.getColor(getContext(), R.color.packages_primary_color))
                .typeface(FontCache.Font.ROBOTO_BOLD)
                .build())
        costSummaryBreakDownView.viewmodel.addRows.onNext(list)

        assertEquals(1, costSummaryBreakDownView.linearLayout.childCount)

        val priceDescription = costSummaryBreakDownView.linearLayout.getChildAt(0).findViewById<View>(R.id.price_type_text_view) as TextView
        val priceValue = costSummaryBreakDownView.linearLayout.getChildAt(0).findViewById<View>(R.id.price_text_view) as TextView

        assertEquals("Hotel + Flights", priceDescription.text.toString())
        assertEquals("$230", priceValue.text.toString())

        assertEquals(ContextCompat.getColor(getContext(), R.color.packages_primary_color), priceDescription.currentTextColor)
        assertEquals(ContextCompat.getColor(getContext(), R.color.packages_primary_color), priceValue.currentTextColor)

        assertEquals(FontCache.getTypeface(FontCache.Font.ROBOTO_BOLD), priceDescription.typeface)
        assertEquals(FontCache.getTypeface(FontCache.Font.ROBOTO_BOLD), priceValue.typeface)
    }

    @Test
    fun testSeparatorRow() {
        val costSummaryBreakDownView = CostSummaryBreakDownView(getContext(), null)
        costSummaryBreakDownView.viewmodel = object: BaseCostSummaryBreakdownViewModel(getContext()) {
            override fun trackBreakDownClicked() {
            }
        }

        val list = ArrayList<BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow>()
        list.add(BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow
                .Builder()
                .separator())
        costSummaryBreakDownView.viewmodel.addRows.onNext(list)

        assertEquals(1, costSummaryBreakDownView.linearLayout.childCount)
    }
}