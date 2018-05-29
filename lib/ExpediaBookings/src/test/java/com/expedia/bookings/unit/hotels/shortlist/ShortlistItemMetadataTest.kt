package com.expedia.bookings.unit.hotels.shortlist

import com.expedia.bookings.data.hotels.shortlist.ShortlistItemMetadata
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ShortlistItemMetadataTest {

    private lateinit var metadata: ShortlistItemMetadata
    private val formatter = DateTimeFormat.forPattern("yyyyMMdd")

    private val dateTestSuite = mapOf("19460614" to LocalDate.parse("19460614", formatter), //0
            "19610804" to LocalDate.parse("19610804", formatter), //1
            "00011225" to LocalDate.parse("00011225", formatter), //2
            "22330322" to LocalDate.parse("22330322", formatter), //3
            null to null, //4
            "" to null, //5
            " " to null, //6
            "   " to null, //7
            "~" to null, //8
            "No Date!" to null, //9
            "-19460614" to LocalDate.parse("-19460614", formatter), //10
            "06/14/1946" to null, //11
            "1946-06-14" to null, //12
            "1946.06.14" to null, //13
            "196184" to null, //14
            "1961Aug04" to null //15
    )

    private val ageTestSuite = mapOf("6|1-9" to Pair<Int?, List<Int>>(6, listOf(1, 9)), //0
            "6|1-9-9-3-99" to Pair(6, listOf(1, 9, 9, 3, 99)), //1
            "5|" to Pair(5, emptyList()), //2
            "6|0" to Pair(6, listOf(0)), //3
            "7|2-" to Pair(7, emptyList()), //4
            "8|-3" to Pair(8, emptyList()), //5
            "9|-4-" to Pair(9, emptyList()), //6
            "9|-5-6" to Pair(9, emptyList()), //7
            "1|7-8-" to Pair(1, emptyList()), //8
            "2|-9-10-" to Pair(2, emptyList()), //9
            "3|-----" to Pair(3, emptyList()), //10
            "4|--7-8--" to Pair(4, emptyList()), //11
            "1" to Pair(1, emptyList()), //12
            "0" to Pair(0, emptyList()), //13
            "911" to Pair(911, emptyList()), //14
            "-2" to Pair(-2, emptyList()), //15
            "-2|" to Pair(-2, emptyList()), //16
            "0.9|9-9" to Pair(null, listOf(9, 9)), //17
            "3|1.4" to Pair(3, emptyList()), //18
            "2-|" to Pair(null, emptyList()), //19
            "|1" to Pair(null, emptyList()), //20
            "|-" to Pair(null, emptyList()), //21
            "-" to Pair(null, emptyList()), //22
            "|" to Pair(null, emptyList()), //23
            null to Pair(null, emptyList()), //24
            "" to Pair(null, emptyList()), //25
            " " to Pair(null, emptyList()), //26
            "   " to Pair(null, emptyList()), //27
            "~" to Pair(null, emptyList()), //28
            "No Human!" to Pair(null, emptyList()), //29
            "x|1" to Pair(null, listOf(1)), //30
            "1|x" to Pair(1, emptyList()) //31
    )

    @Before
    fun before() {
        metadata = ShortlistItemMetadata().apply {
            hotelId = "hotelId"
            chkIn = "19460614"
            chkOut = "19610804"
            roomConfiguration = "6|1-9"
        }
    }

    @Test
    fun testShortlistItemMetaDataNull() {
        val metadata = ShortlistItemMetadata()
        assertNull(metadata.hotelId)
        assertNull(metadata.chkIn)
        assertNull(metadata.chkOut)
        assertNull(metadata.roomConfiguration)
    }

    @Test
    fun testGetCheckInLocalDate() {
        var i = 0
        dateTestSuite.forEach { checkInString, expectedDate ->
            assertGetCheckInLocalDate(checkInString, expectedDate, i)
            i++
        }
    }

    @Test
    fun testGetCheckOutLocalDate() {
        var i = 0
        dateTestSuite.forEach { checkOutString, expectedDate ->
            assertGetCheckOutLocalDate(checkOutString, expectedDate, i)
            i++
        }
    }

    @Test
    fun testGetNumberOfAdults() {
        var i = 0
        ageTestSuite.forEach { roomConfigurationString, expectedAdults ->
            assertGetNumberOfAdults(roomConfigurationString, expectedAdults.first, i)
            i++
        }
    }

    @Test
    fun testGetNumberOfChildren() {
        var i = 0
        ageTestSuite.forEach { roomConfigurationString, expectedAges ->
            assertGetChildrenAges(roomConfigurationString, expectedAges.second, i)
            i++
        }
    }

    private fun assertGetCheckInLocalDate(checkInString: String?, expectedDate: LocalDate?, i: Int) {
        metadata.chkIn = checkInString
        val date = metadata.getCheckInLocalDate()
        assertEquals(expectedDate, date, "Failed at number $i")
    }

    private fun assertGetCheckOutLocalDate(checkOutString: String?, expectedDate: LocalDate?, i: Int) {
        metadata.chkOut = checkOutString
        val date = metadata.getCheckOutLocalDate()
        assertEquals(expectedDate, date, "Failed at number $i")
    }

    private fun assertGetNumberOfAdults(roomConfigurationString: String?, expectedAdults: Int?, i: Int) {
        metadata.roomConfiguration = roomConfigurationString
        val adults = metadata.getNumberOfAdults()
        assertEquals(expectedAdults, adults, "Failed at number $i")
    }

    private fun assertGetChildrenAges(roomConfigurationString: String?, expectedAges: List<Int>?, i: Int) {
        metadata.roomConfiguration = roomConfigurationString
        val ages = metadata.getChildrenAges()
        assertEquals(expectedAges, ages, "Failed at number $i")
    }
}
