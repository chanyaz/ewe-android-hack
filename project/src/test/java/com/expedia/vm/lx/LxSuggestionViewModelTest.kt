package com.expedia.vm.lx

import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.testutils.builder.TestSuggestionV4Builder
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertNotNull

@RunWith(RobolectricRunner::class)
class LxSuggestionViewModelTest {
    private val testVM = LxSuggestionViewModel()
    private val testObserver = TestObserver<Int>()

    @Before
    fun setup() {
        testVM.iconObservable.subscribe(testObserver)
    }

    @Test
    fun testCurrentLocationNoSubtitle() {
        val suggestion = basicSuggestBuilder().iconType(SuggestionV4.IconType.CURRENT_LOCATION_ICON).build()
        assertNotNull(suggestion.regionNames.shortName, "FAILURE: Required for test to be valid.")
        val subTitleObserver = TestObserver<String>()
        testVM.subtitleObservable.subscribe(subTitleObserver)

        testVM.bind(suggestion)
        subTitleObserver.assertValueAt(0, "")
    }

    @Test
    fun testSubtitle() {
        val suggestion = basicSuggestBuilder().regionShortName("ORD").build()
        assertNotNull(suggestion.regionNames.shortName, "FAILURE: Required for test to be valid.")
        val subTitleObserver = TestObserver<String>()
        testVM.subtitleObservable.subscribe(subTitleObserver)

        testVM.bind(suggestion)
        subTitleObserver.assertValueAt(0, "ORD")
    }

    @Test
    fun testDefaultIcon() {
        val suggestion = basicSuggestBuilder().build()
        testVM.bind(suggestion)
        testObserver.assertValue(R.drawable.search_type_icon)
    }

    @Test
    fun testHistoryItemIcon() {
        val suggestion = basicSuggestBuilder().iconType(SuggestionV4.IconType.HISTORY_ICON).build()
        testVM.bind(suggestion)
        testObserver.assertValue(R.drawable.recents)
    }

    @Test
    fun testCurrentLocationIcon() {
        val suggestion = basicSuggestBuilder().iconType(SuggestionV4.IconType.CURRENT_LOCATION_ICON).build()
        testVM.bind(suggestion)
        testObserver.assertValue(R.drawable.ic_suggest_current_location)
    }

    private fun basicSuggestBuilder(): TestSuggestionV4Builder {
        return TestSuggestionV4Builder().regionDisplayName("notnull").regionShortName("notnull")
    }
}
