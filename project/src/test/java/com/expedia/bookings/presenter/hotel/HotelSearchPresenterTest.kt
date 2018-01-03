package com.expedia.bookings.presenter.hotel

import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.SearchSuggestion
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.hotel.tracking.SuggestionTrackingData
import com.expedia.bookings.hotel.util.HotelSearchManager
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.vm.HotelSearchViewModel
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

@RunWith(RobolectricRunner::class)
class HotelSearchPresenterTest {
    lateinit var searchPresenter: HotelSearchPresenter
    private val context = RuntimeEnvironment.application

    private val displayName = "New York"

    @Before
    fun setup() {
        Ui.getApplication(context).defaultHotelComponents()
        context.setTheme(R.style.Theme_Hotels_Default)
        searchPresenter = View.inflate(context, R.layout.hotel_search_presenter_stub, null) as HotelSearchPresenter
        val searchViewModel = HotelSearchViewModel(context, HotelSearchManager(null))
        searchPresenter.searchViewModel = searchViewModel
    }

    @Test
    fun testSuggestionClickNoQuery() {
        // https://eiwork.mingle.thoughtworks.com/projects/ebapp/cards/7644
        assertNotNull(searchPresenter.searchLocationEditText)
        assertEquals("", searchPresenter.searchLocationEditText?.query.toString())

        searchPresenter.suggestionViewModel.suggestionSelectedSubject.onNext(getSuggestion())
        assertNotEquals(displayName, searchPresenter.searchLocationEditText?.query.toString(),
                "FAILURE: Suggestion selection change should not trigger a query change")
    }

    private fun getSuggestion() : SearchSuggestion {
        val mockSuggestion = Mockito.mock(SuggestionV4::class.java)
        mockSuggestion.regionNames = SuggestionV4.RegionNames()
        mockSuggestion.regionNames.displayName = displayName
        val suggestion = SearchSuggestion(mockSuggestion)
        suggestion.trackingData = SuggestionTrackingData()
        return suggestion
    }
}