package com.expedia.bookings.shared.vm

import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.testutils.builder.TestSuggestionV4Builder
import io.reactivex.observers.TestObserver
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class SuggestionViewModelTest {
    private val viewModel = SuggestionViewModel(RuntimeEnvironment.application)

    val expectedDisplayName = "Chicago and Vicinity"
    val expectedShortName = "ChiCity"

    @Test
    fun testIsChild() {
        val testSubscriber = TestObserver.create<Boolean>()
        viewModel.isChildObservable.subscribe(testSubscriber)

        val suggestion = suggestionBuilder().child(true).build()
        viewModel.bind(suggestion)

        assertTrue(testSubscriber.values()[0])
    }

    @Test
    fun testHistoryNotChild() {
        val testSubscriber = TestObserver.create<Boolean>()
        viewModel.isChildObservable.subscribe(testSubscriber)

        val suggestion = suggestionBuilder().child(true)
                .iconType(SuggestionV4.IconType.HISTORY_ICON).build()
        viewModel.bind(suggestion)

        assertFalse(testSubscriber.values()[0], "FAILURE: History items can not be children")
    }

    @Test
    fun testParentTitle() {
        val testSubscriber = TestObserver.create<String>()
        viewModel.titleObservable.subscribe(testSubscriber)

        val suggestion = suggestionBuilder().regionDisplayName(expectedDisplayName).build()
        viewModel.bind(suggestion)
        assertEquals(expectedDisplayName, testSubscriber.values()[0])
    }

    @Test
    fun testChildHistoryTitle() {
        val testSubscriber = TestObserver.create<String>()
        viewModel.titleObservable.subscribe(testSubscriber)

        val suggestion = suggestionBuilder().regionDisplayName(expectedDisplayName).child(true)
                .regionShortName(expectedShortName).iconType(SuggestionV4.IconType.HISTORY_ICON)
                .build()
        viewModel.bind(suggestion)
        assertEquals(expectedShortName, testSubscriber.values()[0])
    }

    @Test
    fun testSubtitle() {
        val testSubscriber = TestObserver.create<String>()
        viewModel.subtitleObservable.subscribe(testSubscriber)

        val suggestion = suggestionBuilder().regionDisplayName(expectedDisplayName).build()
        viewModel.bind(suggestion)

        assertEquals("", testSubscriber.values()[0])
    }

    @Test
    fun testChildHistoryConflict() {
        val testObserver = TestObserver<Boolean>()
        val suggestion = suggestionBuilder().child(true).iconType(SuggestionV4.IconType.HISTORY_ICON).build()
        viewModel.isChildObservable.subscribe(testObserver)

        viewModel.bind(suggestion)
        assertFalse(testObserver.values()[0], "FAILURE : Should not show child icon when item is history item")
    }

    fun suggestionBuilder(): TestSuggestionV4Builder {
        return TestSuggestionV4Builder().regionDisplayName("display").regionShortName("short")
    }
}
