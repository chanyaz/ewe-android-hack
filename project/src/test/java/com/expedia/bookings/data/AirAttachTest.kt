package com.expedia.bookings.data

import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class AirAttachTest {

    @Test
    fun fromToJson() {
        val jsonObj = JSONObject()
        val offerExpiresObj = JSONObject()
        offerExpiresObj.put("epochSeconds", 1481660245)
        offerExpiresObj.put("timeZoneOffsetSeconds", -28800)
        jsonObj.put("airAttachQualified", true)
        jsonObj.put("offerExpires", offerExpiresObj)

        val airAttach = AirAttach(jsonObj)
        val toJson = airAttach.toJson()
        val airAttachFromJson = AirAttach()
        airAttachFromJson.fromJson(toJson)

        assertEquals(airAttach, airAttachFromJson)
    }
}
