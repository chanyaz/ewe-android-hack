package com.expedia.vm

import com.expedia.bookings.R
import com.expedia.bookings.utils.FontCache
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BaseCostSummaryBreakdownViewModelTest {

    @Test
    fun testCostSummaryBreakdownRow() {
        val costSummaryBreakdownRow = BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow
                .Builder()
                .title("Title")
                .cost("$23")
                .color(R.color.packages_primary_color)
                .typeface(FontCache.Font.ROBOTO_BOLD)
                .build()
        assertFalse(costSummaryBreakdownRow.separator)
        assertEquals("Title", costSummaryBreakdownRow.title)
        assertEquals("$23", costSummaryBreakdownRow.cost)
        assertEquals(R.color.packages_primary_color, costSummaryBreakdownRow.titleColor)
        assertEquals(R.color.packages_primary_color, costSummaryBreakdownRow.costColor)
        assertEquals(FontCache.Font.ROBOTO_BOLD, costSummaryBreakdownRow.titleTypeface)
        assertEquals(FontCache.Font.ROBOTO_BOLD, costSummaryBreakdownRow.costTypeface)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCostSummaryBreakdownBlankTitleException() {
        BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow
                .Builder()
                .title("")
                .cost("$23")
                .color(R.color.packages_primary_color)
                .typeface(FontCache.Font.ROBOTO_BOLD)
                .build()
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCostSummaryBreakdownNullCostException() {
        BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow
                .Builder()
                .title("Title")
                .cost("")
                .color(R.color.packages_primary_color)
                .typeface(FontCache.Font.ROBOTO_BOLD)
                .build()
    }

    @Test
    fun testCostSummaryBreakdownRowSeparator() {
        val costSummaryBreakdownRow = BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow.Builder().separator()
        assertTrue(costSummaryBreakdownRow.separator)
    }
}
