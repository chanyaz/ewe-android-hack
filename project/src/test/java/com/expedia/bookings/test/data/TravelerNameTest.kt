package com.expedia.bookings.test.data

import com.expedia.bookings.data.TravelerName
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class TravelerNameTest {
    val EMPTY_STRING = ""

    val oscarFirstName = "Oscar"
    val oscarMiddleName = "The"
    val oscarLastName = "Grouch"

    val countFirstName = "Count"
    val countMiddleName = "Von"
    val countLastName = "Count"

    @Test
    fun testGetFullName() {
        val expectedFullName = "$oscarFirstName $oscarMiddleName $oscarLastName"

        val testName = getTestName(oscarFirstName, oscarMiddleName, oscarLastName)

        assertEquals(expectedFullName, testName.fullName)
    }

    @Test
    fun testNotEqualNull() {
        assertFalse(TravelerName().equals(null))
    }

    @Test
    fun testNotEqualFirstName() {
        val oscarGrouch = getTestName(oscarFirstName, EMPTY_STRING, EMPTY_STRING)
        val travelerNull = getTestName(null, EMPTY_STRING, EMPTY_STRING)
        val countVonCount = getTestName(countFirstName, EMPTY_STRING, EMPTY_STRING)

        assertFalse(oscarGrouch.equals(travelerNull))
        assertFalse(oscarGrouch.equals(countVonCount))
        assertFalse(travelerNull.equals(oscarGrouch))
    }

    @Test
    fun testNotEqualMiddleName() {
        val oscarGrouch = getTestName(EMPTY_STRING, oscarMiddleName, EMPTY_STRING)
        val travelerNull = getTestName(EMPTY_STRING, null, EMPTY_STRING)
        val countVonCount = getTestName(EMPTY_STRING, countMiddleName, EMPTY_STRING)

        assertFalse(oscarGrouch.equals(travelerNull))
        assertFalse(oscarGrouch.equals(countVonCount))
        assertFalse(travelerNull.equals(oscarGrouch))
    }

    @Test
    fun testNotEqualLastName() {
        val oscarGrouch = getTestName(EMPTY_STRING, EMPTY_STRING, oscarLastName)
        val travelerNull = getTestName(EMPTY_STRING, EMPTY_STRING, null)
        val countVonCount = getTestName(EMPTY_STRING, EMPTY_STRING, countLastName)

        assertFalse(oscarGrouch.equals(travelerNull))
        assertFalse(oscarGrouch.equals(countVonCount))
        assertFalse(travelerNull.equals(oscarGrouch))
    }

    @Test
    fun testEqualsIgnoresFullName() {
        val oscarGrouch = getTestName(oscarFirstName, oscarMiddleName, oscarLastName)
        oscarGrouch.fullName = "Oscar The Grouch"
        val oscarFullNameWrong = getTestName(oscarFirstName, oscarMiddleName, oscarLastName)
        oscarFullNameWrong.fullName = "Garbage"

        assertTrue(oscarGrouch.equals(oscarFullNameWrong))
    }

    @Test
    fun testEqualsSameInstance() {
        val oscarGrouch = getTestName(oscarFirstName, oscarMiddleName, oscarLastName)

        assertTrue(oscarGrouch.equals(oscarGrouch))
    }

    @Test
    fun testEqualsValid() {
        val oscarOne = getTestName(oscarFirstName, oscarMiddleName, oscarLastName)
        val oscarTwo = getTestName(oscarFirstName, oscarMiddleName, oscarLastName)

        assertTrue(oscarOne.equals(oscarTwo))
    }

    @Test
    fun firstNameEmpty() {
        val oscarOne = getTestName("", oscarMiddleName, oscarLastName)
        val oscarTwo = getTestName(null, oscarMiddleName, oscarLastName)

        assertFalse(oscarOne.isEmpty)
        assertFalse(oscarTwo.isEmpty)
    }

    @Test
    fun middleNameEmpty() {
        val oscarOne = getTestName(oscarFirstName, "", oscarLastName)
        val oscarTwo = getTestName(oscarFirstName, null, oscarLastName)

        assertFalse(oscarOne.isEmpty)
        assertFalse(oscarTwo.isEmpty)
    }

    @Test
    fun lastNameEmpty() {
        val oscarOne = getTestName(oscarFirstName, oscarMiddleName, "")
        val oscarTwo = getTestName(oscarFirstName, oscarMiddleName, null)

        assertFalse(oscarOne.isEmpty)
        assertFalse(oscarTwo.isEmpty)
    }

    @Test
    fun testIsEmpty() {
        val oscarOne = getTestName("", "", "")
        val oscarTwo = getTestName(null, null, null)

        assertTrue(oscarOne.isEmpty)
        assertTrue(oscarTwo.isEmpty)
    }

    private fun getTestName(first: String?, middle: String?, last: String?): TravelerName {
        val travelerName = TravelerName()
        travelerName.firstName = first
        travelerName.middleName = middle
        travelerName.lastName = last
        return travelerName
    }
}
