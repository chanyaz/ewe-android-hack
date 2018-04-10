package com.expedia.vm.packages

import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.testutils.builder.TestSuggestionV4Builder
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertNotNull

@RunWith(RobolectricRunner::class)
class PackageSuggestionViewModelTest {
    private val context = RuntimeEnvironment.application
    private val originVM = PackageSuggestionViewModel(true, context)
    private val destVM = PackageSuggestionViewModel(false, context)

    @Test
    fun testSubtitleOrigin() {
        val suggestion = basicSuggestBuilder().regionDisplayName("Midway").build()
        assertNotNull(suggestion.regionNames.displayName, "FAILURE: Required for test to be valid.")
        val subTitleObserver = TestObserver<String>()
        originVM.subtitleObservable.subscribe(subTitleObserver)

        originVM.bind(suggestion)
        subTitleObserver.assertValueAt(0, "Midway")
    }

    @Test
    fun testSubtitleDest() {
        val suggestion = basicSuggestBuilder().regionDisplayName("Midway").build()
        assertNotNull(suggestion.regionNames.displayName, "FAILURE: Required for test to be valid.")
        val subTitleObserver = TestObserver<String>()
        destVM.subtitleObservable.subscribe(subTitleObserver)

        destVM.bind(suggestion)
        subTitleObserver.assertValueAt(0, "")
    }

    @Test
    fun testTitleDestChildAndHistory() {
        val suggestion = basicSuggestBuilder().child(true).iconType(SuggestionV4.IconType.HISTORY_ICON)
                .regionShortName("Midway").build()
        assertNotNull(suggestion.regionNames.shortName, "FAILURE: Required for test to be valid.")
        val titleObserver = TestObserver<CharSequence>()
        destVM.titleObservable.subscribe(titleObserver)

        destVM.bind(suggestion)
        titleObserver.assertValueAt(0, "Midway")
    }

    @Test
    fun testTitleDestHistoryNoChild() {
        val suggestion = basicSuggestBuilder().child(false).iconType(SuggestionV4.IconType.HISTORY_ICON)
                .regionDisplayName("Title").build()
        assertNotNull(suggestion.regionNames.shortName, "FAILURE: Required for test to be valid.")
        val titleObserver = TestObserver<CharSequence>()
        destVM.titleObservable.subscribe(titleObserver)

        destVM.bind(suggestion)
        titleObserver.assertValueAt(0, "Title")
    }

    @Test
    fun testDefaultDestIcon() {
        val testObserver = TestObserver<Int>()
        destVM.iconObservable.subscribe(testObserver)
        val suggestion = basicSuggestBuilder().build()
        destVM.bind(suggestion)
        testObserver.assertValue(R.drawable.search_type_icon)
    }

    @Test
    fun testHistoryItemIcon() {
        val testObserver = TestObserver<Int>()
        destVM.iconObservable.subscribe(testObserver)

        val suggestion = basicSuggestBuilder().iconType(SuggestionV4.IconType.HISTORY_ICON).build()
        destVM.bind(suggestion)
        testObserver.assertValue(R.drawable.recents)
    }

    @Test
    fun testCurrentLocationIcon() {
        val testObserver = TestObserver<Int>()
        destVM.iconObservable.subscribe(testObserver)

        val suggestion = basicSuggestBuilder().iconType(SuggestionV4.IconType.CURRENT_LOCATION_ICON).build()
        destVM.bind(suggestion)
        testObserver.assertValue(R.drawable.ic_suggest_current_location)
    }

    @Test
    fun testOriginAirport() {
        val testObserver = TestObserver<Int>()
        originVM.iconObservable.subscribe(testObserver)

        val suggestion = basicSuggestBuilder().build()
        originVM.bind(suggestion)
        testObserver.assertValue(R.drawable.airport_suggest)
    }

    private fun basicSuggestBuilder(): TestSuggestionV4Builder {
        return TestSuggestionV4Builder().regionDisplayName("notnull").regionShortName("notnull")
    }
}
