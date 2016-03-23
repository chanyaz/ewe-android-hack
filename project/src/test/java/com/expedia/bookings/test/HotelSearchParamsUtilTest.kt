package com.expedia.bookings.test

import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.HotelSearchParamsUtil
import org.joda.time.LocalDate
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.util.ArrayList

@RunWith(RobolectricRunner::class)
class HotelSearchParamsUtilTest {
    val context = RuntimeEnvironment.application

    @Test
    fun saveAndLoadSearchForPastDate() {
        // Delete all recent searches
        HotelSearchParamsUtil.deleteCachedSearches(context)
        Assert.assertEquals(0, HotelSearchParamsUtil.loadSearchHistory(context).size)

        //saving valid search params
        val suggestionV4 = SuggestionV4()
        suggestionV4.gaiaId = "1234"
        val regionNames = SuggestionV4.RegionNames()
        regionNames.displayName = "San Francisco"
        regionNames.shortName = "SFO"
        suggestionV4.regionNames = regionNames
        val childList = ArrayList<Int>()
        childList.add(2)
        childList.add(4)
        var checkIn = LocalDate.now().plusDays(2)
        var checkOut = LocalDate.now().plusDays(5)
        val numAdults = 2
        val v2params = HotelSearchParams.Builder(0).departure(suggestionV4).checkIn(checkIn).checkOut(checkOut).adults(numAdults).children(childList).build() as HotelSearchParams
        HotelSearchParamsUtil.saveSearchHistory(context, v2params)

        Thread.sleep(1000)
        Assert.assertEquals(1, HotelSearchParamsUtil.loadSearchHistory(context).size)

        //save to past date searches
        checkIn = LocalDate.now().minusDays(4)
        checkOut = LocalDate.now().plusDays(5)
        val v2paramsPastDate = HotelSearchParams.Builder(0).departure(suggestionV4).checkIn(checkIn).checkOut(checkOut).adults(numAdults).children(childList).build() as HotelSearchParams
        HotelSearchParamsUtil.saveSearchHistory(context, v2paramsPastDate)

        Thread.sleep(1000)
        Assert.assertEquals(1, HotelSearchParamsUtil.loadSearchHistory(context).size)

        // clearing the searches again
        HotelSearchParamsUtil.deleteCachedSearches(context)
        Assert.assertEquals(0, HotelSearchParamsUtil.loadSearchHistory(context).size)

    }

}
