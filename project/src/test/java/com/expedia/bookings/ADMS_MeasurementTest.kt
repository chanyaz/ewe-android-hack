package com.expedia.bookings

import com.expedia.bookings.test.CustomMatchers.Companion.matchesPattern
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
class ADMS_MeasurementTest {

    @Test
    fun testPurchaseId() {
        val sharedInstance = ADMS_Measurement.sharedInstance()
        val samplePurchaseID = "sample purchase id"
        sharedInstance.setPurchaseID(samplePurchaseID)
        assertEquals(samplePurchaseID, sharedInstance.getOmnitureDataValue("&&purchaseID"))
    }

    @Test
    fun testCurrencyCode() {
        val sharedInstance = ADMS_Measurement.sharedInstance()
        val sampleCurrencyCode = "sample currency"
        sharedInstance.setCurrencyCode(sampleCurrencyCode)
        assertEquals(sampleCurrencyCode, sharedInstance.getOmnitureDataValue("&&cc"))
    }

    @Test
    fun testProducts() {
        val sharedInstance = ADMS_Measurement.sharedInstance()
        val sampleProducts = "sample products string"
        sharedInstance.setProducts(sampleProducts)
        assertEquals(sampleProducts, sharedInstance.getProducts())
        assertEquals(sampleProducts, sharedInstance.getOmnitureDataValue("&&products"))
    }

    @Test
    fun testEvents() {
        val sharedInstance = ADMS_Measurement.sharedInstance()
        val sampleEvents = "sample events"
        sharedInstance.setEvents(sampleEvents)
        assertEquals(sampleEvents, sharedInstance.getEvents())
        assertEquals(sampleEvents, sharedInstance.getOmnitureDataValue("&&events"))
    }

    @Test
    fun testAppendEventsEmpty() {
        val adms = ADMS_Measurement()
        adms.appendEvents("")
        assertNull(adms.getEvents())
        assertNull(adms.getOmnitureDataValue("&&events"))
    }

    @Test
    fun testAppendEventsSimple() {
        val event = "event112"
        val adms = ADMS_Measurement()
        adms.appendEvents(event)
        assertEquals(event, adms.getEvents())
        assertEquals(event, adms.getOmnitureDataValue("&&events"))
    }

    @Test
    fun testAppendEventsAppending() {
        val event1 = "event1"
        val event2 = "event2"
        val adms = ADMS_Measurement()
        adms.appendEvents(event1)
        assertEquals(event1, adms.getEvents())
        assertEquals(event1, adms.getOmnitureDataValue("&&events"))

        adms.appendEvents(event2)
        assertEquals("$event1,$event2", adms.getEvents())
        assertEquals("$event1,$event2", adms.getOmnitureDataValue("&&events"))
    }

    @Test
    fun testAppendEventsAppendingEmpty() {
        val event1 = "event1"
        val event2 = ""
        val adms = ADMS_Measurement()
        adms.appendEvents(event1)
        assertEquals(event1, adms.getEvents())
        assertEquals(event1, adms.getOmnitureDataValue("&&events"))

        adms.appendEvents(event2)
        assertEquals("$event1", adms.getEvents())
        assertEquals("$event1", adms.getOmnitureDataValue("&&events"))
    }

    @Test
    fun testProp() {
        val sharedInstance = ADMS_Measurement.sharedInstance()
        val sampleProp = "sample prop"
        sharedInstance.setProp(10, sampleProp)
        assertEquals(sampleProp, sharedInstance.getProp(10))
        assertEquals(sampleProp, sharedInstance.getOmnitureDataValue("&&c10"))
    }

    @Test
    fun testPropNull() {
        val sharedInstance = ADMS_Measurement.sharedInstance()
        sharedInstance.setProp(10, null)
        assertEquals("", sharedInstance.getOmnitureDataValue("&&c10"))
    }

    @Test
    fun testEvar() {
        val sharedInstance = ADMS_Measurement.sharedInstance()
        val sampleEvar10Value = "sample evar set"
        sharedInstance.setEvar(10, sampleEvar10Value)
        assertEquals(sampleEvar10Value, sharedInstance.getEvar(10))
        assertEquals(sampleEvar10Value, sharedInstance.getOmnitureDataValue("&&v10"))
    }

    @Test
    fun testEvarValueNull() {
        val sharedInstance = ADMS_Measurement.sharedInstance()
        sharedInstance.setEvar(10, null)
        assertEquals("", sharedInstance.getOmnitureDataValue("&&v10"))
    }

    @Test
    fun visitorIdIsNonNullAndOnlyNumeric() {
        val visitorId = ADMS_Measurement.sharedInstance().visitorID
        assertNotNull(visitorId)
        assertThat(visitorId, matchesPattern("^[0-9]+$"))
    }
}
