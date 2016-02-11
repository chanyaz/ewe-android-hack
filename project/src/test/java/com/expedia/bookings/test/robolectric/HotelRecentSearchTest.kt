package com.expedia.bookings.test.robolectric

import android.app.Activity
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.services.LocalDateTypeAdapter
import com.expedia.bookings.utils.HotelSearchParamsUtil
import com.expedia.vm.HotelSearchViewModel
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.mobiata.android.Log
import com.mobiata.android.util.IoUtils
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import java.io.IOException
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
public class HotelRecentSearchTest {
    public var vm: HotelSearchViewModel by Delegates.notNull()
    var activity: Activity by Delegates.notNull()

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        vm = HotelSearchViewModel(activity)
        val builder = GsonBuilder().registerTypeAdapter(LocalDate::class.java, LocalDateTypeAdapter(HotelSearchParamsUtil.PATTERN))
        val type = object : TypeToken<List<HotelSearchParams>>() {}.type
        val emptyList: List<HotelSearchParams> = emptyList()
        val searchJson = builder.create().toJson(emptyList, type)
        try {
            IoUtils.writeStringToFile(HotelSearchParamsUtil.RECENT_HOTEL_SEARCHES_FILE, searchJson, activity)
        } catch (e: IOException) {
            Log.e("Unable to create recent searches file: ", e)
        }
    }

    @Test
    fun testRecentSearch() {
        //Initially there should be no saved searches.
        assertEquals(0, HotelSearchParamsUtil.loadSearchHistory(activity).size)

        val suggestion = getDummySuggestion("location")
        val anotherSuggestion = getDummySuggestion("anotherSuggestion")

        // Searching once should save a new search.
        vm.suggestionObserver.onNext(suggestion)
        vm.datesObserver.onNext(Pair(LocalDate.now(), null))
        vm.searchObserver.onNext(Unit)
        Thread.sleep(2000)
        assertEquals(1, HotelSearchParamsUtil.loadSearchHistory(activity).size)

        // Searching with the same params should not add another saved search.
        vm.suggestionObserver.onNext(suggestion)
        vm.datesObserver.onNext(Pair(LocalDate.now(), null))
        vm.searchObserver.onNext(Unit)
        Thread.sleep(2000)
        assertEquals(1, HotelSearchParamsUtil.loadSearchHistory(activity).size)

        // Selecting another suggestion and searching should add another saved search.
        vm.suggestionObserver.onNext(anotherSuggestion)
        vm.datesObserver.onNext(Pair(LocalDate.now(), null))
        vm.searchObserver.onNext(Unit)
        Thread.sleep(2000)
        assertEquals(2, HotelSearchParamsUtil.loadSearchHistory(activity).size)
        // New search should be added at the top.
        assertEquals(anotherSuggestion.regionNames.displayName, HotelSearchParamsUtil.loadSearchHistory(activity).get(0).suggestion.regionNames.displayName)

        // Re-doing an old search should bring that search to top.
        vm.suggestionObserver.onNext(suggestion)
        vm.datesObserver.onNext(Pair(LocalDate.now(), null))
        vm.searchObserver.onNext(Unit)
        Thread.sleep(2000)
        assertEquals(2, HotelSearchParamsUtil.loadSearchHistory(activity).size)
        assertEquals(suggestion.regionNames.displayName, HotelSearchParamsUtil.loadSearchHistory(activity).get(0).suggestion.regionNames.displayName)

        // Selecting another date and searching should add another saved search.
        vm.suggestionObserver.onNext(anotherSuggestion)
        vm.datesObserver.onNext(Pair(LocalDate.now().plusDays(1), null))
        vm.searchObserver.onNext(Unit)
        Thread.sleep(2000)
        assertEquals(3, HotelSearchParamsUtil.loadSearchHistory(activity).size)

        // Changing traveler info should add another saved search.
        vm.suggestionObserver.onNext(anotherSuggestion)
        vm.datesObserver.onNext(Pair(LocalDate.now().plusDays(1), null))
        vm.searchObserver.onNext(Unit)
        Thread.sleep(2000)
        assertEquals(3, HotelSearchParamsUtil.loadSearchHistory(activity).size)
    }

    private fun getDummySuggestion(location: String): SuggestionV4 {
        val suggestion = SuggestionV4()
        suggestion.gaiaId = ""
        suggestion.regionNames = SuggestionV4.RegionNames()
        suggestion.regionNames.displayName = location
        suggestion.regionNames.fullName = location
        suggestion.regionNames.shortName = location
        return suggestion
    }
}
