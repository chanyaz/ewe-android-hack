package com.expedia.bookings.test.data.hotel

import com.expedia.bookings.data.hotel.HotelValueAdd
import com.expedia.bookings.data.hotel.ValueAddsEnum
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Test
import org.junit.runner.RunWith
import java.util.ArrayList
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelValueAddTest {

    @Test
    fun testHotelValueAddPriority() {
        val valueAddsList = ArrayList<HotelValueAdd>()

        valueAddsList.add(HotelValueAdd(ValueAddsEnum.BREAKFAST, "0"))
        valueAddsList.add(HotelValueAdd(ValueAddsEnum.FREE_AIRPORT_SHUTTLE, "1"))
        valueAddsList.add(HotelValueAdd(ValueAddsEnum.BREAKFAST, "2"))
        valueAddsList.add(HotelValueAdd(ValueAddsEnum.INTERNET, "3"))
        valueAddsList.add(HotelValueAdd(ValueAddsEnum.PARKING, "4"))

        valueAddsList.sort()

        assertEquals(5, valueAddsList.count())

        assertEquals(ValueAddsEnum.INTERNET, valueAddsList[0].valueAddsEnum)
        assertEquals(ValueAddsEnum.BREAKFAST, valueAddsList[1].valueAddsEnum)
        assertEquals("0", valueAddsList[1].apiDescription)
        assertEquals(ValueAddsEnum.BREAKFAST, valueAddsList[2].valueAddsEnum)
        assertEquals("2", valueAddsList[2].apiDescription)
        assertEquals(ValueAddsEnum.PARKING, valueAddsList[3].valueAddsEnum)
        assertEquals(ValueAddsEnum.FREE_AIRPORT_SHUTTLE, valueAddsList[4].valueAddsEnum)
    }
}
