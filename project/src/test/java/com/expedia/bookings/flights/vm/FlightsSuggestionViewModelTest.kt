package com.expedia.bookings.flights.vm

import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.testutils.builder.TestSuggestionV4Builder
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class FlightsSuggestionViewModelTest {
    private val context = RuntimeEnvironment.application
    private var viewModel = FlightsSuggestionViewModel(context)

    val expectedDisplayName = "Chicago and Vicinity"
    val expectedShortName = "ChiCity"

    private val testIconObserver = TestObserver<Int>()
    private val testIconContentDescriptionObserver = TestObserver<String>()

    @Before
    fun setup() {
        viewModel.iconObservable.subscribe(testIconObserver)
        viewModel.iconContentDescriptionObservable.subscribe(testIconContentDescriptionObserver)
    }

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
        testIconObserver.assertValue(R.drawable.recents)
        val iconContentDescription = if (viewModel.isIconContentDescriptionRequired()) "HISTORY_ICON" else ""
        testIconContentDescriptionObserver.assertValue(iconContentDescription)
    }

    @Test
    fun testParentTitle() {
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppFlightsContentHighlightInTypeahead)
        val testSubscriber = TestObserver.create<CharSequence>()
        viewModel.titleObservable.subscribe(testSubscriber)

        val suggestion = suggestionBuilder().regionDisplayName(expectedDisplayName).build()
        viewModel.bind(suggestion)
        assertEquals(expectedDisplayName, testSubscriber.values()[0])
    }

    @Test
    fun testParentTitleWithTags() {
        AbacusTestUtils.bucketTestsAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppFlightsContentHighlightInTypeahead)
        val testSubscriber = TestObserver.create<CharSequence>()
        viewModel = FlightsSuggestionViewModel(context)
        viewModel.titleObservable.subscribe(testSubscriber)

        val suggestion = suggestionBuilder().regionDisplayName("<B>$expectedDisplayName</B>").build()
        viewModel.bind(suggestion)
        var spannedText = testSubscriber.values()[0] as Spanned
        val styleSpans = spannedText.getSpans(0, spannedText.length, StyleSpan::class.java)
        val colorSpans = spannedText.getSpans(0, spannedText.length, ForegroundColorSpan::class.java)

        assertEquals(1, styleSpans.size)
        assertEquals(1, colorSpans.size)

        // Ensure history suggestions don't have tags
        suggestion.iconType = SuggestionV4.IconType.HISTORY_ICON
        viewModel.bind(suggestion)
        assertFalse(testSubscriber.values()[1] is Spanned)
        assertTrue(testSubscriber.values()[1] is String)
    }

    @Test
    fun testChildHistoryTitle() {
        val testSubscriber = TestObserver.create<CharSequence>()
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

    @Test
    fun testCurrentLocationIcon() {
        val suggestion = suggestionBuilder().iconType(SuggestionV4.IconType.CURRENT_LOCATION_ICON).build()
        viewModel.bind(suggestion)
        testIconObserver.assertValue(R.drawable.ic_suggest_current_location)
        val iconContentDescription = if (viewModel.isIconContentDescriptionRequired()) "CURRENT_LOCATION_ICON" else ""
        testIconContentDescriptionObserver.assertValue(iconContentDescription)
    }

    private fun suggestionBuilder(): TestSuggestionV4Builder {
        return TestSuggestionV4Builder().regionDisplayName("display").regionShortName("short")
    }
}
