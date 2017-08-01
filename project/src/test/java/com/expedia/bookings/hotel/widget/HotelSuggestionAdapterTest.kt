package com.expedia.bookings.hotel.widget

import android.support.v7.widget.RecyclerView
import android.widget.LinearLayout
import com.expedia.bookings.data.SearchSuggestion
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import com.expedia.bookings.services.TestObserver
import java.util.ArrayList
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelSuggestionAdapterTest {
    val testAdapter = HotelSuggestionAdapter()
    lateinit var testClickObserver: TestObserver<SearchSuggestion>

    @Before
    fun setup() {
        testClickObserver = TestObserver<SearchSuggestion>()
        testAdapter.suggestionClicked.subscribe(testClickObserver)
    }

    @Test
    fun testRecentCounter() {
        val list = buildSuggestionLists(3)
        list[0].iconType = SuggestionV4.IconType.HISTORY_ICON
        list[1].iconType = SuggestionV4.IconType.HISTORY_ICON

        testAdapter.setSuggestions(list)
        val testHolder = createAndBindNewHolder(0)
        testHolder.itemView.callOnClick()

        val historyData = testClickObserver.onNextEvents[0].trackingData!!

        assertEquals(2, historyData.previousSuggestionsShownCount)

        testAdapter.setSuggestions(buildSuggestionLists(2))
        testHolder.itemView.callOnClick()
        val noHistoryData = testClickObserver.onNextEvents[1].trackingData!!
        assertEquals(0, noHistoryData.previousSuggestionsShownCount, "FAILURE: Expected History data to reset after getting new list")
    }

    @Test
    fun testParent() {
        val list = buildSuggestionLists(3)
        list[0].hierarchyInfo!!.isChild = false
        list[1].hierarchyInfo!!.isChild = true
        list[2].hierarchyInfo!!.isChild = true

        testAdapter.setSuggestions(list)
        val testHolder = createAndBindNewHolder(0)
        testHolder.itemView.callOnClick()

        val data = testClickObserver.onNextEvents[0].trackingData!!
        assertFalse(data.isChild)
        assertTrue(data.isParent, "FAILURE: If the current item is not a child and the next item in the list is a child " +
                "then the current item must be a parent")
    }

    @Test
    fun testChildCantBeParent() {
        val list = buildSuggestionLists(3)
        list[0].hierarchyInfo!!.isChild = false
        list[1].hierarchyInfo!!.isChild = true
        list[2].hierarchyInfo!!.isChild = true

        testAdapter.setSuggestions(list)
        val testHolder = createAndBindNewHolder(1)
        testHolder.itemView.callOnClick()

        val data = testClickObserver.onNextEvents[0].trackingData!!
        assertFalse(data.isParent, "FAILURE A Suggestion can not be a parent if it is a child.")
    }

    @Test
    fun testSuggestionTrackingDepth() {
        val list = buildSuggestionLists(3)
        testAdapter.setSuggestions(list)

        val firstItemHolder = createAndBindNewHolder(0)
        firstItemHolder.itemView.callOnClick()

        val firstPositionData = testClickObserver.onNextEvents[0].trackingData!!
        assertEquals(1, firstPositionData.selectedSuggestionPosition)

        val lastItemHolder = createAndBindNewHolder(2)
        lastItemHolder.itemView.callOnClick()

        val secondPositionData = testClickObserver.onNextEvents[1].trackingData!!
        assertEquals(3, secondPositionData.selectedSuggestionPosition)
    }

    @Test
    fun testTrackingTotalCount() {
        val list = buildSuggestionLists(3)
        testAdapter.setSuggestions(list)

        val firstItemHolder = createAndBindNewHolder(0)
        firstItemHolder.itemView.callOnClick()

        val data = testClickObserver.onNextEvents[0].trackingData!!
        assertEquals(3, data.suggestionsShownCount)
    }

    @Test
    fun testTrackingGenericData() {
        val expectedGaiaId = "12345"
        val expectedTypeText = "Neighborhood"
        val expectedDisplayName = "Sesame Street, NY"
        val list = buildSuggestionLists(3)
        list[0].type = expectedTypeText
        list[0].gaiaId = expectedGaiaId
        list[0].regionNames.displayName = expectedDisplayName
        testAdapter.setSuggestions(list)

        val firstItemHolder = createAndBindNewHolder(0)
        firstItemHolder.itemView.callOnClick()

        val firstPositionData = testClickObserver.onNextEvents[0].trackingData!!
        assertEquals(expectedGaiaId, firstPositionData.suggestionGaiaId)
        assertEquals(expectedTypeText, firstPositionData.suggestionType)
        assertEquals(expectedDisplayName, firstPositionData.displayName)
    }

    private fun buildSuggestionLists(count: Int) : List<SuggestionV4> {
        val list = ArrayList<SuggestionV4>()
        for (i in 1..count) {
            val suggestion = SuggestionV4()
            suggestion.regionNames = SuggestionV4.RegionNames()
            suggestion.regionNames.displayName = ""
            suggestion.hierarchyInfo = SuggestionV4.HierarchyInfo()
            list.add(suggestion)
        }
        return list
    }

    private fun createAndBindNewHolder(position: Int): RecyclerView.ViewHolder {
        val testHolder = testAdapter.createViewHolder(LinearLayout(RuntimeEnvironment.application), 0)
        testAdapter.bindViewHolder(testHolder, position)
        return testHolder
    }
}