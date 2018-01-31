package com.expedia.bookings.hotel.widget

import android.support.v7.widget.RecyclerView
import android.widget.LinearLayout
import com.expedia.bookings.data.SearchSuggestion
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.services.SuggestionV4Services
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.shared.data.SuggestionDataItem
import com.expedia.vm.HotelSuggestionAdapterViewModel
import org.mockito.Mockito
import java.util.ArrayList
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelSuggestionAdapterTest {
    private val testVM = HotelSuggestionAdapterViewModel(RuntimeEnvironment.application,
            Mockito.mock(SuggestionV4Services::class.java), null)
    val testAdapter = HotelSuggestionAdapter(testVM)

    lateinit var testClickObserver: TestObserver<SearchSuggestion>

    @Before
    fun setup() {
        testClickObserver = TestObserver<SearchSuggestion>()
        testAdapter.viewModel.suggestionSelectedSubject.subscribe(testClickObserver)
    }

    @Test
    fun testRecentCounter() {
        val list = buildSuggestionLists(3)
        list[0].suggestion.iconType = SuggestionV4.IconType.HISTORY_ICON
        list[1].suggestion.iconType = SuggestionV4.IconType.HISTORY_ICON

        testVM.suggestionItemsSubject.onNext(list)
        val testHolder = createAndBindNewHolder(0)
        testHolder.itemView.callOnClick()

        val historyData = testClickObserver.values()[0].trackingData!!

        assertEquals(2, historyData.previousSuggestionsShownCount)

        testVM.suggestionItemsSubject.onNext(buildSuggestionLists(2))
        testHolder.itemView.callOnClick()
        val noHistoryData = testClickObserver.values()[1].trackingData!!
        assertEquals(0, noHistoryData.previousSuggestionsShownCount, "FAILURE: Expected History data to reset after getting new list")
    }

    @Test
    fun testParent() {
        val list = buildSuggestionLists(3)
        list[0].suggestion.hierarchyInfo!!.isChild = false
        list[1].suggestion.hierarchyInfo!!.isChild = true
        list[2].suggestion.hierarchyInfo!!.isChild = true

        testVM.suggestionItemsSubject.onNext(list)
        val testHolder = createAndBindNewHolder(0)
        testHolder.itemView.callOnClick()

        val data = testClickObserver.values()[0].trackingData!!
        assertFalse(data.isChild)
        assertTrue(data.isParent, "FAILURE: If the current item is not a child and the next item in the list is a child " +
                "then the current item must be a parent")
    }

    @Test
    fun testChildCantBeParent() {
        val list = buildSuggestionLists(3)
        list[0].suggestion.hierarchyInfo!!.isChild = false
        list[1].suggestion.hierarchyInfo!!.isChild = true
        list[2].suggestion.hierarchyInfo!!.isChild = true

        testVM.suggestionItemsSubject.onNext(list)
        val testHolder = createAndBindNewHolder(1)
        testHolder.itemView.callOnClick()

        val data = testClickObserver.values()[0].trackingData!!
        assertFalse(data.isParent, "FAILURE A Suggestion can not be a parent if it is a child.")
    }

    @Test
    fun testSuggestionTrackingDepth() {
        val list = buildSuggestionLists(3)
        testVM.suggestionItemsSubject.onNext(list)

        val firstItemHolder = createAndBindNewHolder(0)
        firstItemHolder.itemView.callOnClick()

        val firstPositionData = testClickObserver.values()[0].trackingData!!
        assertEquals(1, firstPositionData.selectedSuggestionPosition)

        val lastItemHolder = createAndBindNewHolder(2)
        lastItemHolder.itemView.callOnClick()

        val secondPositionData = testClickObserver.values()[1].trackingData!!
        assertEquals(3, secondPositionData.selectedSuggestionPosition)
    }

    @Test
    fun testTrackingTotalCount() {
        val list = buildSuggestionLists(3)
        testVM.suggestionItemsSubject.onNext(list)

        val firstItemHolder = createAndBindNewHolder(0)
        firstItemHolder.itemView.callOnClick()

        val data = testClickObserver.values()[0].trackingData!!
        assertEquals(3, data.suggestionsShownCount)
    }

    @Test
    fun testTrackingGenericData() {
        val expectedGaiaId = "12345"
        val expectedTypeText = "Neighborhood"
        val expectedDisplayName = "Sesame Street, NY"
        val list = buildSuggestionLists(3)
        list[0].suggestion.type = expectedTypeText
        list[0].suggestion.gaiaId = expectedGaiaId
        list[0].suggestion.regionNames.displayName = expectedDisplayName
        testVM.suggestionItemsSubject.onNext(list)

        val firstItemHolder = createAndBindNewHolder(0)
        firstItemHolder.itemView.callOnClick()

        val firstPositionData = testClickObserver.values()[0].trackingData!!
        assertEquals(expectedGaiaId, firstPositionData.suggestionGaiaId)
        assertEquals(expectedTypeText, firstPositionData.suggestionType)
        assertEquals(expectedDisplayName, firstPositionData.displayName)
    }

    private fun buildSuggestionLists(count: Int): List<SuggestionDataItem.SuggestionDropDown> {
        val list = ArrayList<SuggestionDataItem.SuggestionDropDown>()
        for (i in 1..count) {
            val suggestion = SuggestionV4()
            suggestion.regionNames = SuggestionV4.RegionNames()
            suggestion.regionNames.displayName = ""
            suggestion.hierarchyInfo = SuggestionV4.HierarchyInfo()
            list.add(SuggestionDataItem.SuggestionDropDown(suggestion))
        }
        return list
    }

    private fun createAndBindNewHolder(position: Int): RecyclerView.ViewHolder {
        val testHolder = testAdapter.createViewHolder(LinearLayout(RuntimeEnvironment.application), 0)
        testAdapter.bindViewHolder(testHolder, position)
        return testHolder
    }
}
