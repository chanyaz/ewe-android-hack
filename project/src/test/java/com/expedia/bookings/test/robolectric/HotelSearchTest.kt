package com.expedia.bookings.test.robolectric

import android.app.Activity
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.SuggestionV4
import com.expedia.vm.HotelSearchViewModel
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import rx.observers.TestSubscriber
import kotlin.properties.Delegates

@RunWith(RobolectricRunner::class)
public class HotelSearchTest {
    public var vm: HotelSearchViewModel by Delegates.notNull()
    private var LOTS_MORE: Long = 100
    var activity : Activity by Delegates.notNull()

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        vm = HotelSearchViewModel(activity)
    }

    @Test
    fun selectDatesAndSearch() {
        val testSubscriber = TestSubscriber<HotelSearchParams>()
        val expected = arrayListOf<HotelSearchParams>()
        val suggestion = getDummySuggestion()

        vm.searchParamsObservable.subscribe(testSubscriber)

        // Selecting a location suggestion for search, as it is a necessary parameter for search
        vm.suggestionObserver.onNext(suggestion)

        // Selecting only start date should search with end date as the next day
        vm.datesObserver.onNext(Pair(LocalDate.now(), null))
        vm.searchObserver.onNext(Unit)
        expected.add(HotelSearchParams.Builder(activity.resources.getInteger(R.integer.calendar_max_days_hotel_stay)).suggestion(suggestion).checkIn(LocalDate.now()).checkOut(LocalDate.now().plusDays(1)).build())

        // Select both start date and end date and search
        vm.datesObserver.onNext(Pair(LocalDate.now(), LocalDate.now().plusDays(3)))
        vm.searchObserver.onNext(Unit)
        expected.add(HotelSearchParams.Builder(activity.resources.getInteger(R.integer.calendar_max_days_hotel_stay)).suggestion(suggestion).checkIn(LocalDate.now()).checkOut(LocalDate.now().plusDays(3)).build())

        // When neither start date nor end date are selected, search should not fire anything
        vm.datesObserver.onNext(Pair(null, null))
        vm.searchObserver.onNext(Unit)
        vm.searchObserver.onNext(Unit)
        vm.searchObserver.onNext(Unit)

        testSubscriber.requestMore(LOTS_MORE)
        testSubscriber.assertReceivedOnNext(expected)
    }

    private fun getDummySuggestion(): SuggestionV4 {
        val suggestion = SuggestionV4()
        suggestion.gaiaId = ""
        suggestion.regionNames = SuggestionV4.RegionNames()
        suggestion.regionNames.displayName = ""
        suggestion.regionNames.fullName = ""
        suggestion.regionNames.shortName = ""
        return suggestion
    }
}
