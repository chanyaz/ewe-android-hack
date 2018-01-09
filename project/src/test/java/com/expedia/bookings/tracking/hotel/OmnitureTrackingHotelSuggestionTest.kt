package com.expedia.bookings.tracking.hotel

import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.OmnitureTestUtils.Companion.assertLinkTracked
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.hotel.tracking.SuggestionTrackingData
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.tracking.OmnitureTracking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(RobolectricRunner::class)
class OmnitureTrackingHotelSuggestionTest {
    lateinit var mockAnalyticsProvider: AnalyticsProvider

    private val suggestionBehaviorLinkDefaults = "HTL.UpdateSearch.H.en_US.1"

    @Before
    fun setup() {
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testHotelSuggestionEvar48Prop73_withChild() {
        val dataBuilder = TestTrackingDataBuilder()
        dataBuilder.shown(false).selected(false).child(true).gaiaId("178286").suggestionType("MULTICITY").displayName("Miami, Florida")
        val data = dataBuilder.build()

        val expectedChild = "GAI:${data.suggestionGaiaId}|${data.suggestionType}|R#child|${data.displayName}"

        OmnitureTracking.trackHotelSuggestionBehavior(data)
        assertLinkTracked(OmnitureMatchers.withEvars(mapOf(48 to expectedChild)), mockAnalyticsProvider)
        assertLinkTracked(OmnitureMatchers.withProps(mapOf(73 to expectedChild)), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testHotelSuggestionEvar48Prop73_noChild() {
        val dataBuilder = TestTrackingDataBuilder()
        dataBuilder.shown(false).selected(false).child(false).gaiaId("178286").suggestionType("MULTICITY").displayName("Miami, Florida")
        val data = dataBuilder.build()

        val expectedNoChild = "GAI:${data.suggestionGaiaId}|${data.suggestionType}|R#-|${data.displayName}"

        OmnitureTracking.trackHotelSuggestionBehavior(data)
        assertLinkTracked(OmnitureMatchers.withEvars(mapOf(48 to expectedNoChild)), mockAnalyticsProvider)
        assertLinkTracked(OmnitureMatchers.withProps(mapOf(73 to expectedNoChild)), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testHotelSuggestionEvar48Prop73_userHistory() {
        val dataBuilder = TestTrackingDataBuilder()
        dataBuilder.shown(false).selected(false).history(true).gaiaId("178286").suggestionType("MULTICITY").displayName("Miami, Florida")
        val data = dataBuilder.build()

        val expectedText = "GAI:${data.suggestionGaiaId}|${data.suggestionType}|R#-|${data.displayName}|USERHISTORY"

        OmnitureTracking.trackHotelSuggestionBehavior(data)
        assertLinkTracked(OmnitureMatchers.withEvars(mapOf(48 to expectedText)), mockAnalyticsProvider)
        assertLinkTracked(OmnitureMatchers.withProps(mapOf(73 to expectedText)), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testHotelSuggestionEvents() {
        val dataBuilder = TestTrackingDataBuilder()
        dataBuilder.shown(false).selected(false).charTyped(4).selectedPosition(2)
        val data = dataBuilder.build()

        val expectedEventText = "event45,event44=${data.selectedSuggestionPosition},event46=${data.charactersTypedCount}"

        OmnitureTracking.trackHotelSuggestionBehavior(data)
        assertLinkTracked(OmnitureMatchers.withEventsString(expectedEventText), mockAnalyticsProvider)

        data.charactersTypedCount = 0
        val expectedNoCharText = "event45,event44=${data.selectedSuggestionPosition},event46=0"
        OmnitureTracking.trackHotelSuggestionBehavior(data)
        assertLinkTracked(OmnitureMatchers.withEventsString(expectedNoCharText), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testHotelSuggestionLinkEvar28Prop16_noShowPreviousSearch() {
        val dataBuilder = TestTrackingDataBuilder()
        dataBuilder.shown(false).selected(false).charTyped(0).gaiaId("12356")
        val data = dataBuilder.build()

        val expectedText = "$suggestionBehaviorLinkDefaults.TANoShow.TAPrevSearch.P0C0L0.ESS#0.UH#0"

        OmnitureTracking.trackHotelSuggestionBehavior(data)
        // If User did not focus or select but GAIA is present assume previous search
        assertLinkTracked(OmnitureMatchers.withEvars(mapOf(28 to expectedText)), mockAnalyticsProvider)
        assertLinkTracked(OmnitureMatchers.withProps(mapOf(16 to expectedText)), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testHotelSuggestionLinkEvar28Prop16_showPreviousSearch() {
        val dataBuilder = TestTrackingDataBuilder()
        dataBuilder.shown(true).selected(false).charTyped(3).gaiaId("12356")
        val data = dataBuilder.build()

        val expectedText = "$suggestionBehaviorLinkDefaults.TAShow.TAPrevSearch.P0C0L0.ESS#0.UH#0"

        OmnitureTracking.trackHotelSuggestionBehavior(data)
        // If user DID focus but no select and GAIA is present assume previous search
        assertLinkTracked(OmnitureMatchers.withEvars(mapOf(28 to expectedText)), mockAnalyticsProvider)
        assertLinkTracked(OmnitureMatchers.withProps(mapOf(16 to expectedText)), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testHotelSuggestionLinkEvar28Prop16_showTypeSelection() {
        val dataBuilder = TestTrackingDataBuilder()
        dataBuilder.shown(true).selected(true).charTyped(3).gaiaId("12356")
        val data = dataBuilder.build()

        val expectedText = "$suggestionBehaviorLinkDefaults.TAShow.TASelection.P0C0L0.ESS#0.UH#0"

        OmnitureTracking.trackHotelSuggestionBehavior(data)
        // If User focus and select and has typed something, behavior==TAShow.TASelection
        assertLinkTracked(OmnitureMatchers.withEvars(mapOf(28 to expectedText)), mockAnalyticsProvider)
        assertLinkTracked(OmnitureMatchers.withProps(mapOf(16 to expectedText)), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testHotelSuggestionLinkEvar28Prop16_showNoTypeFocus() {
        val dataBuilder = TestTrackingDataBuilder()
        dataBuilder.shown(true).selected(true).charTyped(0).gaiaId("12356")
        val data = dataBuilder.build()

        val focusExpectedText = "$suggestionBehaviorLinkDefaults.TAShow.TAFocus.P0C0L0.ESS#0.UH#0"

        OmnitureTracking.trackHotelSuggestionBehavior(data)
        // If User focus and select but has not typed anything, behavior==TAShow.TAFocus
        assertLinkTracked(OmnitureMatchers.withEvars(mapOf(28 to focusExpectedText)), mockAnalyticsProvider)
        assertLinkTracked(OmnitureMatchers.withProps(mapOf(16 to focusExpectedText)), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testHotelSuggestionLinkEvar28Prop16_depth() {
        val dataBuilder = TestTrackingDataBuilder()
        dataBuilder.shown(true).selected(true).essCount(10).charTyped(3).selectedPosition(4)
        val data = dataBuilder.build()

        val expectedText = "$suggestionBehaviorLinkDefaults.TAShow.TASelection.P0C0L4.ESS#10.UH#0"

        OmnitureTracking.trackHotelSuggestionBehavior(data)
        // L# should represent depth
        assertLinkTracked(OmnitureMatchers.withEvars(mapOf(28 to expectedText)), mockAnalyticsProvider)
        assertLinkTracked(OmnitureMatchers.withProps(mapOf(16 to expectedText)), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testHotelSuggestionLinkEvar28Prop16_isParentFalse() {
        val dataBuilder = TestTrackingDataBuilder()
        dataBuilder.shown(true).selected(true).parent(false).essCount(10).charTyped(3).selectedPosition(0)
        val data = dataBuilder.build()

        val noParentText = "$suggestionBehaviorLinkDefaults.TAShow.TASelection.P0C0L0.ESS#10.UH#0"

        OmnitureTracking.trackHotelSuggestionBehavior(data)
        // P0 should represent isParent=false
        assertLinkTracked(OmnitureMatchers.withEvars(mapOf(28 to noParentText)), mockAnalyticsProvider)
        assertLinkTracked(OmnitureMatchers.withProps(mapOf(16 to noParentText)), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testHotelSuggestionLinkEvar28Prop16_isParentTrue() {
        val dataBuilder = TestTrackingDataBuilder()
        dataBuilder.shown(true).selected(true).parent(true).essCount(10).charTyped(3).selectedPosition(0)
        val data = dataBuilder.build()

        val parentText = "$suggestionBehaviorLinkDefaults.TAShow.TASelection.P1C0L0.ESS#10.UH#0"

        OmnitureTracking.trackHotelSuggestionBehavior(data)
        // P1 should represent isParent=true
        assertLinkTracked(OmnitureMatchers.withEvars(mapOf(28 to parentText)), mockAnalyticsProvider)
        assertLinkTracked(OmnitureMatchers.withProps(mapOf(16 to parentText)), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testHotelSuggestionLinkEvar28Prop16_isChildFalse() {
        val dataBuilder = TestTrackingDataBuilder()
        dataBuilder.shown(true).selected(true).child(false).essCount(10).charTyped(3).selectedPosition(0)
        val data = dataBuilder.build()

        val noChildText = "$suggestionBehaviorLinkDefaults.TAShow.TASelection.P0C0L0.ESS#10.UH#0"

        OmnitureTracking.trackHotelSuggestionBehavior(data)
        // C0 should represent isChild=false
        assertLinkTracked(OmnitureMatchers.withEvars(mapOf(28 to noChildText)), mockAnalyticsProvider)
        assertLinkTracked(OmnitureMatchers.withProps(mapOf(16 to noChildText)), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testHotelSuggestionLinkEvar28Prop16_isChildTrue() {
        val dataBuilder = TestTrackingDataBuilder()
        dataBuilder.shown(true).selected(true).child(true).essCount(10).charTyped(3).selectedPosition(0)
        val data = dataBuilder.build()

        val childText = "$suggestionBehaviorLinkDefaults.TAShow.TASelection.P0C1L0.ESS#10.UH#0"

        OmnitureTracking.trackHotelSuggestionBehavior(data)
        // C1 should represent isChild=true
        assertLinkTracked(OmnitureMatchers.withEvars(mapOf(28 to childText)), mockAnalyticsProvider)
        assertLinkTracked(OmnitureMatchers.withProps(mapOf(16 to childText)), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testHotelSuggestionLinkEvar28Prop16_withRecents() {
        val dataBuilder = TestTrackingDataBuilder()
        dataBuilder.shown(true).selected(true).essCount(10).historyShownCount(3).charTyped(3)
        val data = dataBuilder.build()

        val essCount = data.suggestionsShownCount - data.previousSuggestionsShownCount
        val expectedText = "$suggestionBehaviorLinkDefaults.TAShow.TASelection.P0C0L0.ESS#$essCount.UH#${data.previousSuggestionsShownCount}"

        OmnitureTracking.trackHotelSuggestionBehavior(data)
        // UH# should represent number of previous suggestions shown
        assertLinkTracked(OmnitureMatchers.withEvars(mapOf(28 to expectedText)), mockAnalyticsProvider)
        assertLinkTracked(OmnitureMatchers.withProps(mapOf(16 to expectedText)), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testHotelSuggestionLinkEvar28Prop16_subtractRecentsFromESS() {
        val dataBuilder = TestTrackingDataBuilder()
        dataBuilder.shown(true).selected(true).essCount(10).historyShownCount(3).charTyped(3)
        val data = dataBuilder.build()

        val essCount = data.suggestionsShownCount - data.previousSuggestionsShownCount
        val expectedText = "$suggestionBehaviorLinkDefaults.TAShow.TASelection.P0C0L0.ESS#$essCount.UH#${data.previousSuggestionsShownCount}"

        OmnitureTracking.trackHotelSuggestionBehavior(data)
        //ESS# should not include previous user history item counts
        assertLinkTracked(OmnitureMatchers.withEvars(mapOf(28 to expectedText)), mockAnalyticsProvider)
        assertLinkTracked(OmnitureMatchers.withProps(mapOf(16 to expectedText)), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testHotelSuggestionLinkEvar28Prop16_historyCantBeChild() {
        val dataBuilder = TestTrackingDataBuilder()
        dataBuilder.shown(true).selected(true).child(true).history(true).essCount(10).charTyped(3).selectedPosition(0)
        val data = dataBuilder.build()

        val childText = "$suggestionBehaviorLinkDefaults.TAShow.TASelection.P0C0L0.ESS#10.UH#0"

        OmnitureTracking.trackHotelSuggestionBehavior(data)
        // If an item is from user history it can't be a child
        assertLinkTracked(OmnitureMatchers.withEvars(mapOf(28 to childText)), mockAnalyticsProvider)
        assertLinkTracked(OmnitureMatchers.withProps(mapOf(16 to childText)), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testHotelSuggestionLinkEvar28Prop16_historyCantBeParent() {
        val dataBuilder = TestTrackingDataBuilder()
        dataBuilder.shown(true).selected(true).parent(true).history(true).essCount(10).charTyped(3).selectedPosition(0)
        val data = dataBuilder.build()

        val noParentText = "$suggestionBehaviorLinkDefaults.TAShow.TASelection.P0C0L0.ESS#10.UH#0"

        OmnitureTracking.trackHotelSuggestionBehavior(data)
        // If an item is from user history it can't be a parent
        assertLinkTracked(OmnitureMatchers.withEvars(mapOf(28 to noParentText)), mockAnalyticsProvider)
        assertLinkTracked(OmnitureMatchers.withProps(mapOf(16 to noParentText)), mockAnalyticsProvider)
    }

    private class TestTrackingDataBuilder {
        private val data = SuggestionTrackingData()

        fun shown(show: Boolean) : TestTrackingDataBuilder{
            data.suggestionsFocused = show
            return this
        }

        fun selected(selected: Boolean) : TestTrackingDataBuilder {
            data.suggestionSelected = selected
            return this
        }

        fun selectedPosition(position: Int) : TestTrackingDataBuilder {
            data.selectedSuggestionPosition = position
            return this
        }

        fun gaiaId(id: String) : TestTrackingDataBuilder {
            data.suggestionGaiaId = id
            return this
        }

        fun essCount(count: Int) : TestTrackingDataBuilder {
            data.suggestionsShownCount = count
            return this
        }

        fun historyShownCount(count: Int) : TestTrackingDataBuilder {
            data.previousSuggestionsShownCount = count
            return this
        }

        fun charTyped(count: Int) : TestTrackingDataBuilder {
            data.charactersTypedCount = count
            return this
        }

        fun displayName(name: String) : TestTrackingDataBuilder {
            data.displayName = name
            return this
        }

        fun suggestionType(type: String) : TestTrackingDataBuilder {
            data.suggestionType = type
            return this
        }

        fun child(child: Boolean) : TestTrackingDataBuilder {
            data.isChild = child
            return this
        }

        fun history(history: Boolean) : TestTrackingDataBuilder {
            data.isHistory = history
            return this
        }

        fun parent(parent: Boolean) : TestTrackingDataBuilder {
            data.isParent = parent
            return this
        }

        fun build() : SuggestionTrackingData {
            return data
        }
    }
}
