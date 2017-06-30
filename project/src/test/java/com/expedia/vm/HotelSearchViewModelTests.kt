package com.expedia.vm

import android.app.Activity
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusResponse
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.hotel.vm.HotelResultsViewModel
import org.joda.time.LocalDate
import org.junit.Test
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertFalse

@RunWith(RobolectricRunner::class)
class HotelSearchViewModelTests {
    lateinit var activity : Activity

    lateinit var searchViewModel: HotelSearchViewModel

    @Before
    fun setup() {
        val abacusResponse = AbacusResponse()
        abacusResponse.updateABTestForDebug(AbacusUtils.EBAndroidAppHotelGreedySearch,
                AbacusUtils.DefaultVariant.BUCKETED.ordinal)
        Db.setAbacusResponse(abacusResponse)
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        Ui.getApplication(activity).defaultHotelComponents()
        searchViewModel = HotelSearchViewModel(activity)
    }

    @Test
    fun searchSameParamsTest() {
        val suggestion = getDummySuggestion()
        val testSubscriber = TestSubscriber<HotelSearchParams>()

        var builder = HotelSearchParams.Builder(activity.resources.getInteger(R.integer.calendar_max_days_hotel_stay),
                activity.resources.getInteger(R.integer.max_calendar_selectable_date_range_hotels_only))
                .destination(suggestion)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(3)) as HotelSearchParams.Builder
        builder.shopWithPoints(true)
        builder.adults(1)
        searchViewModel.hotelSearchParams = builder.build()

        searchViewModel.destinationLocationObserver.onNext(suggestion)
        searchViewModel.getParamsBuilder().startDate(LocalDate.now())
        searchViewModel.getParamsBuilder().endDate(LocalDate.now().plusDays(3))
        searchViewModel.getParamsBuilder().adults(1)
        searchViewModel.getParamsBuilder().shopWithPoints(true)
        searchViewModel.searchParamsObservable.subscribe(testSubscriber)
        searchViewModel.searchObserver.onNext(Unit)
        assertTrue(testSubscriber.onNextEvents[0].sameParameters)

        searchViewModel.destinationLocationObserver.onNext(suggestion)
        searchViewModel.getParamsBuilder().startDate(LocalDate.now())
        searchViewModel.getParamsBuilder().endDate(LocalDate.now().plusDays(3))
        searchViewModel.getParamsBuilder().adults(2)
        searchViewModel.getParamsBuilder().shopWithPoints(true)
        searchViewModel.searchParamsObservable.subscribe(testSubscriber)
        searchViewModel.searchObserver.onNext(Unit)
        assertFalse(testSubscriber.onNextEvents[1].sameParameters)

        searchViewModel.destinationLocationObserver.onNext(suggestion)
        searchViewModel.getParamsBuilder().startDate(LocalDate.now())
        searchViewModel.getParamsBuilder().endDate(LocalDate.now().plusDays(3))
        searchViewModel.getParamsBuilder().adults(1)
        searchViewModel.getParamsBuilder().shopWithPoints(false)
        searchViewModel.searchParamsObservable.subscribe(testSubscriber)
        searchViewModel.searchObserver.onNext(Unit)
        assertFalse(testSubscriber.onNextEvents[5].sameParameters)
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

