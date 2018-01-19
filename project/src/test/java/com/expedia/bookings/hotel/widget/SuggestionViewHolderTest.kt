package com.expedia.bookings.hotel.widget

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.hotel.vm.HotelSuggestionViewModel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.suggestions.SuggestionViewHolder
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when` as whenever
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class SuggestionViewHolderTest {
    lateinit var testViewHolder: SuggestionViewHolder

    private lateinit var rootView: ViewGroup

    @Before
    fun setUp() {
        rootView = LayoutInflater.from(RuntimeEnvironment.application).inflate(R.layout.suggestion_dropdown_item, null, false) as ViewGroup
    }

    @Test
    fun testRecentChildSuggestion() {
        val testVM = HotelSuggestionViewModel()

        testViewHolder = SuggestionViewHolder(rootView, testVM)
        testVM.isChildObservable.onNext(false)

        assertEquals(View.VISIBLE, testViewHolder.icon.visibility)
        assertEquals(View.GONE, testViewHolder.hierarchyIcon.visibility)
    }

    @Test
    fun testChildSuggestion() {
        val testVM = HotelSuggestionViewModel()

        testViewHolder = SuggestionViewHolder(rootView, testVM)

        testVM.isChildObservable.onNext(true)

        assertEquals(View.VISIBLE, testViewHolder.hierarchyIcon.visibility)
        assertEquals(View.GONE, testViewHolder.icon.visibility)
    }
}
