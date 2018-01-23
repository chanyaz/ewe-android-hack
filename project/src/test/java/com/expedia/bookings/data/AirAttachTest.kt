package com.expedia.bookings.data

import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class AirAttachTest {

    @Test
    fun fromToJson() {
        val jsonObj = JSONObject()
        val offerExpiresObj = makeOfferExpiresTimeJsonObj(1481660245)
        jsonObj.put("airAttachQualified", true)
        jsonObj.put("offerExpiresTime", offerExpiresObj)

        val airAttach = AirAttach(jsonObj)
        val toJson = airAttach.toJson()
        val airAttachFromJson = AirAttach()
        airAttachFromJson.fromJson(toJson)

        assertEquals(airAttach, airAttachFromJson)
    }

    @Test
    fun fromJson() {
        val epochSeconds = 1481660245L

        val jsonObj = JSONObject()
        val offerExpiresObj = makeOfferExpiresTimeJsonObj(epochSeconds)
        jsonObj.put("offerExpiresTime", offerExpiresObj)

        val airAttachFromJson = AirAttach()
        val result = airAttachFromJson.fromJson(jsonObj)

        assertTrue(result)
        assertNotNull(airAttachFromJson.expirationDate, "should be set")
        val expirationDateSecs = airAttachFromJson.expirationDate.millis / 1000
        assertEquals(epochSeconds, expirationDateSecs)
    }

    private fun makeOfferExpiresTimeJsonObj(epochSeconds: Long): JSONObject {
        val offerExpiresObj = JSONObject()
        offerExpiresObj.put("epochSeconds", epochSeconds)
        offerExpiresObj.put("timeZoneOffsetSeconds", -28800)
        return offerExpiresObj
    }
}
