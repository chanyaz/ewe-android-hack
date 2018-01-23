package com.expedia.bookings.hotel.widget

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.HotelSuggestionViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelSuggestionViewHolderTest {
    lateinit var testViewHolder: HotelSuggestionViewHolder

    private lateinit var rootView: ViewGroup

    @Before
    fun setUp() {
        rootView = LayoutInflater.from(RuntimeEnvironment.application).inflate(R.layout.hotel_dropdown_item, null, false) as ViewGroup
    }

    @Test
    fun testRecentChildSuggestion() {
        val mockViewModel = Mockito.mock(HotelSuggestionViewModel::class.java)

        Mockito.`when`(mockViewModel.getTitle()).thenReturn("")
        Mockito.`when`(mockViewModel.isChild()).thenReturn(true)
        Mockito.`when`(mockViewModel.isHistoryItem()).thenReturn(true)

        testViewHolder = HotelSuggestionViewHolder(rootView, mockViewModel)

        testViewHolder = HotelSuggestionViewHolder(rootView, mockViewModel)

        testViewHolder.bind(SuggestionV4())

        assertEquals(View.VISIBLE, testViewHolder.icon.visibility, "FAILURE : History Icon should override child icon")
        assertEquals(View.GONE, testViewHolder.hierarchyIcon.visibility, "FAILURE : Should not show child icon when item is history item")
    }

    @Test
    fun testChildSuggestion() {
        val mockViewModel = Mockito.mock(HotelSuggestionViewModel::class.java)

        Mockito.`when`(mockViewModel.getTitle()).thenReturn("")
        Mockito.`when`(mockViewModel.isChild()).thenReturn(true)
        Mockito.`when`(mockViewModel.isHistoryItem()).thenReturn(false)

        testViewHolder = HotelSuggestionViewHolder(rootView, mockViewModel)

        testViewHolder.bind(SuggestionV4())

        assertEquals(View.VISIBLE, testViewHolder.hierarchyIcon.visibility)
        assertEquals(View.GONE, testViewHolder.icon.visibility)
    }
}
