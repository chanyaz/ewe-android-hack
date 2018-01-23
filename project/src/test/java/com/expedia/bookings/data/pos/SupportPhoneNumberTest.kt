package com.expedia.bookings.data.pos

import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class SupportPhoneNumberTest {

    @Test
    fun singleNumberParsesProperly() {
        val expectedPhoneNumber = "123-456-7890"
        val jsonData = JSONObject()
        jsonData.put("*", expectedPhoneNumber)

        val phoneNumber = SupportPhoneNumber(jsonData)

        assertPhoneNumbers(expectedPhoneNumber, expectedPhoneNumber, phoneNumber)
    }

    @Test
    fun iosSpecificNumbersAreIgnored() {
        val expectedPhoneNumber = "123-456-7890"
        val jsonData = JSONObject()
        jsonData.put("*", expectedPhoneNumber)
        jsonData.put("iPhone", "222-222-2222")
        jsonData.put("iPad", "333-333-3333")

        val phoneNumber = SupportPhoneNumber(jsonData)

        assertPhoneNumbers(expectedPhoneNumber, expectedPhoneNumber, phoneNumber)
    }

    @Test
    fun phoneAndTabletNumbersParseProperly() {
        val expectedPhoneNumber = "123-456-7890"
        val expectedTabletNumber = "456-789-0123"
        val jsonData = JSONObject()
        jsonData.put("*", "111-111-1111")
        jsonData.put("iPhone", "222-222-2222")
        jsonData.put("iPad", "333-333-3333")
        jsonData.put("Android", expectedPhoneNumber)
        jsonData.put("AndroidTablet", expectedTabletNumber)

        val phoneNumber = SupportPhoneNumber(jsonData)

        assertPhoneNumbers(expectedPhoneNumber, expectedTabletNumber, phoneNumber)
    }

    @Test
    fun nullInputParsesAsEmptyStrings() {
        val phoneNumber = SupportPhoneNumber(null)
        assertPhoneNumbers("", "", phoneNumber)
    }

    @Test
    fun unexpectedInputParsesAsEmptyStrings() {
        val jsonData = JSONObject()
        jsonData.put("someRandomThing", "applejuice")

        val phoneNumber = SupportPhoneNumber(jsonData)

        assertPhoneNumbers("", "", phoneNumber)
    }

    private fun assertPhoneNumbers(expectedNumberForPhone: String, expectedNumberForTablet: String, phoneNumber: SupportPhoneNumber) {
        assertEquals(expectedNumberForTablet, phoneNumber.getPhoneNumberForTabletDevice())
        assertEquals(expectedNumberForTablet, phoneNumber.getPhoneNumberForDevice(true))

        assertEquals(expectedNumberForPhone, phoneNumber.getPhoneNumberForPhoneDevice())
        assertEquals(expectedNumberForPhone, phoneNumber.getPhoneNumberForDevice(false))
    }
}
