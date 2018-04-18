package com.expedia.bookings.widget.suggestions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.rail.vm.RailSuggestionViewModel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.testutils.builder.TestSuggestionV4Builder
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class SuggestionViewHolderTest {
    lateinit var testViewHolder: SuggestionViewHolder

    private lateinit var rootView: ViewGroup
    private val context = RuntimeEnvironment.application

    @Before
    fun setUp() {
        rootView = LayoutInflater.from(context).inflate(R.layout.suggestion_dropdown_item, null, false) as ViewGroup
    }

    @Test
    fun testRecentChildSuggestion() {
        val testVM = RailSuggestionViewModel(context)

        testViewHolder = SuggestionViewHolder(rootView, testVM)
        testVM.isChildObservable.onNext(false)

        assertEquals(View.VISIBLE, testViewHolder.icon.visibility)
        assertEquals(View.GONE, testViewHolder.hierarchyIcon.visibility)
    }

    @Test
    fun testChildSuggestion() {
        val testVM = RailSuggestionViewModel(context)

        testViewHolder = SuggestionViewHolder(rootView, testVM)

        testVM.isChildObservable.onNext(true)

        assertEquals(View.VISIBLE, testViewHolder.hierarchyIcon.visibility)
        assertEquals(View.GONE, testViewHolder.icon.visibility)
    }

    @Test
    fun testSuggestionIconTypeContentDescription() {
        val testVM = RailSuggestionViewModel(context)

        testViewHolder = SuggestionViewHolder(rootView, testVM)

        val suggestion = TestSuggestionV4Builder().regionDisplayName("notnull").iconType(SuggestionV4.IconType.SEARCH_TYPE_ICON).build()
        testVM.bind(suggestion)
        val iconContentDescription = if (testVM.isIconContentDescriptionRequired()) "SEARCH_TYPE_ICON" else ""
        assertEquals(iconContentDescription, testViewHolder.icon.contentDescription)
    }
}
