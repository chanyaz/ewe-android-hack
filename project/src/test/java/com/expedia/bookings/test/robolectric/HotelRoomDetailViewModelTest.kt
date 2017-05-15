package com.expedia.bookings.test.robolectric

import com.expedia.bookings.data.hotel.ValueAddsEnum
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.testutils.JSONResourceReader
import com.expedia.vm.HotelRoomDetailViewModel
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.util.ArrayList
import kotlin.comparisons.compareBy
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelRoomDetailViewModelTest {

    val context = RuntimeEnvironment.application

    @Test
    fun testBreakfastValueAddsToShow() {
        val ids = intArrayOf(512, 2, 8, 4, 8192, 4096, 16777216, 33554432, 67108864, 1073742786, 1073742857, 2111, 2085, 4363,
                2102, 2207, 2206, 2194, 2193, 2205, 2103, 2105, 2104, 3969, 4647, 4646, 4648, 4649, 4650, 4651, 2001).asList()
        testValueAddsToShow(ids, ValueAddsEnum.BREAKFAST)
    }

    @Test
    fun testInternetValueAddsToShow() {
        val ids = intArrayOf(2048, 1024, 1073742787, 4347, 2403, 4345, 2405, 2407, 4154, 2191, 2192, 2404, 2406).asList()
        testValueAddsToShow(ids, ValueAddsEnum.INTERNET)
    }

    @Test
    fun testParkingValueAddsToShow() {
        val ids = intArrayOf(16384, 128, 2195, 2109, 4449, 4447, 4445, 4443, 3863, 3861, 2011).asList()
        testValueAddsToShow(ids, ValueAddsEnum.PARKING)
    }

    @Test
    fun testAirportShuttleValueAddsToShow() {
        val ids = intArrayOf(2196, 32768, 10).sorted()
        testValueAddsToShow(ids, ValueAddsEnum.FREE_AIRPORT_SHUTTLE)
    }

    @Test
    fun testInvalidValueAddsToShow() {
        val ids = intArrayOf(946, 42, 420, 69, 34, 404, 7242728).toList()

        val roomResponse = createRoomResponse(ids)
        val vm = createViewModel(roomResponse, 0)
        val toShow = vm.getValueAdds()

        assertEquals(0, toShow.count())
    }

    @Test
    fun testValueAddsToShowPriority() {
        val ids = intArrayOf(128, 1024, 2, 2196).toList()

        val roomResponse = createRoomResponse(ids)
        val vm = createViewModel(roomResponse, 0)
        val toShow = vm.getValueAdds()

        assertEquals(ids.count(), toShow.count())

        assertEquals(ValueAddsEnum.INTERNET, toShow[0].valueAddsEnum)
        assertEquals(ValueAddsEnum.BREAKFAST, toShow[1].valueAddsEnum)
        assertEquals(ValueAddsEnum.PARKING, toShow[2].valueAddsEnum)
        assertEquals(ValueAddsEnum.FREE_AIRPORT_SHUTTLE, toShow[3].valueAddsEnum)
    }

    @Test
    fun testDontShowExactDuplicateValueAdds() {
        val valueAdds = ArrayList<HotelOffersResponse.ValueAdds>()

        var valueAdd = HotelOffersResponse.ValueAdds()
        valueAdd.id = "2"
        valueAdd.description = "1"
        valueAdds.add(valueAdd)

        valueAdd = HotelOffersResponse.ValueAdds()
        valueAdd.id = "2"
        valueAdd.description = "2"
        valueAdds.add(valueAdd)

        valueAdd = HotelOffersResponse.ValueAdds()
        valueAdd.id = "2"
        valueAdd.description = "1"
        valueAdds.add(valueAdd)

        val roomResponse = createRoomResponse(ArrayList())
        roomResponse.valueAdds = valueAdds
        val vm = createViewModel(roomResponse, 0)
        val toShow = vm.getValueAdds()

        assertEquals(2, toShow.count())

        assertEquals(ValueAddsEnum.BREAKFAST, toShow[0].valueAddsEnum)
        assertEquals("1", toShow[0].apiDescription)
        assertEquals(ValueAddsEnum.BREAKFAST, toShow[1].valueAddsEnum)
        assertEquals("2", toShow[1].apiDescription)
    }

    @Test
    fun testGroupedInternetValueAdds() {
        val ids = intArrayOf(2048, 1024, 1073742787, 4347, 2403, 4345, 2405, 2407, 4154, 2191, 2192, 2404, 2406).toList()

        val roomResponse = createRoomResponse(ids)
        val vm = createViewModel(roomResponse, 0)
        val toShow = vm.getValueAdds()

        assertEquals(1, toShow.count())

        assertEquals(ValueAddsEnum.INTERNET, toShow[0].valueAddsEnum)
        assertEquals(context.resources.getString(ValueAddsEnum.INTERNET.descriptionId), toShow[0].apiDescription)
    }

    private fun createRoomResponse(valueAddIds: List<Int>): HotelOffersResponse.HotelRoomResponse {
         val resourceReader = JSONResourceReader("../lib/mocked/templates/m/api/hotel/offers/happypath.json")
        val checkoutResponse = resourceReader.constructUsingGson(HotelOffersResponse::class.java)

        val roomResponse = checkoutResponse.hotelRoomResponse[0]

        val valueAdds = ArrayList<HotelOffersResponse.ValueAdds>()
        valueAddIds.forEachIndexed { i, id ->
            val valueAdd = HotelOffersResponse.ValueAdds()
            valueAdd.id = id.toString()
            valueAdd.description = id.toString()

            valueAdds.add(valueAdd)
        }
        roomResponse.valueAdds = valueAdds

        return roomResponse
    }

    private fun createViewModel(roomResponse: HotelOffersResponse.HotelRoomResponse, optionIndex: Int): HotelRoomDetailViewModel {
        return HotelRoomDetailViewModel(context, roomResponse, "id", 0, optionIndex, false)
    }

    private fun testValueAddsToShow(ids: List<Int>, enum: ValueAddsEnum) {
        for (id in ids) {
            val roomResponse = createRoomResponse(intArrayOf(id).toList())
            val vm = createViewModel(roomResponse, 0)
            val toShow = vm.getValueAdds()

            assertEquals(1, toShow.count())

            assertEquals(enum, toShow[0].valueAddsEnum)
        }
    }
}
