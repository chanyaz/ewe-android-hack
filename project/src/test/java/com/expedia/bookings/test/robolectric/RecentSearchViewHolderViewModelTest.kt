package com.expedia.bookings.test.robolectric

import com.expedia.bookings.data.flights.RecentSearch
import com.expedia.bookings.services.TestObserver
import com.expedia.vm.flights.RecentSearchViewHolderViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class RecentSearchViewHolderViewModelTest {
    lateinit var sut: RecentSearchViewHolderViewModel
    private val context = RuntimeEnvironment.application

    @Before
    fun setup() {
        sut = RecentSearchViewHolderViewModel(context)
    }

    @Test
    fun testRecentSearchRoundTripContentDescription() {
        val recentSearch = RecentSearch("SFO", "LAS", "{\"coordinates\"}".toByteArray(),
                "{\"coordinates\"}".toByteArray(), "2018-05-10", "2018-05-31", "COACH",
                1520490226L, 668, "USD", 1, "10,12",
                false, true)
        val testObserver = TestObserver<String>()
        sut.contentDescriptionObservable.subscribe(testObserver)
        sut.recentSearchObservable.onNext(recentSearch)
        assertEquals("SFO to LAS, Roundtrip search, from May 10 to May 31, for 3 travelers, Economy class, " +
                "Price starting from $668 per person  ", testObserver.values().get(0))
    }

    @Test
    fun testRecentSearchOneWayContentDescription() {
        val recentSearch = RecentSearch("SFO", "LAS", "{\"coordinates\"}".toByteArray(),
                "{\"coordinates\"}".toByteArray(), "2018-05-10", "", "PREMIUM_COACH",
                1520490226L, 668, "USD", 1, "",
                false, false)
        val testObserver = TestObserver<String>()
        sut.contentDescriptionObservable.subscribe(testObserver)
        sut.recentSearchObservable.onNext(recentSearch)
        assertEquals("SFO to LAS, One way search, from May 10, for 1 traveler, Premium Economy class, Price starting from $668 per person  ", testObserver.values().get(0))
    }
}
