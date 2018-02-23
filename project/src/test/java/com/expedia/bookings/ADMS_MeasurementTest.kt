package com.expedia.bookings

import com.expedia.bookings.test.CustomMatchers.Companion.hasEntries
import com.expedia.bookings.test.CustomMatchers.Companion.matchesPattern
import com.expedia.bookings.test.NullSafeMockitoHamcrest.mapThat
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
class ADMS_MeasurementTest {
    lateinit var adms: ADMS_Measurement

    @Before
    fun setup() {
        adms = ADMS_Measurement()
    }

    @Test
    fun testPurchaseId() {
        val samplePurchaseID = "sample purchase id"
        adms.setPurchaseID(samplePurchaseID)
        assertEquals(samplePurchaseID, adms.getOmnitureDataValue("&&purchaseID"))
    }

    @Test
    fun testCurrencyCode() {
        val sampleCurrencyCode = "sample currency"
        adms.setCurrencyCode(sampleCurrencyCode)
        assertEquals(sampleCurrencyCode, adms.getOmnitureDataValue("&&cc"))
    }

    @Test
    fun testProducts() {
        val sampleProducts = "sample products string"
        adms.setProducts(sampleProducts)
        assertEquals(sampleProducts, adms.getProducts())
        assertEquals(sampleProducts, adms.getOmnitureDataValue("&&products"))
    }

    @Test
    fun testEvents() {
        val sampleEvents = "sample events"
        @Suppress("DEPRECATION")
        adms.setEvents(sampleEvents)
        assertEquals(sampleEvents, adms.getEvents())
        assertEquals(sampleEvents, adms.getOmnitureDataValue("&&events"))
    }

    @Test
    fun testAppendEventsEmpty() {
        adms.appendEvents("")
        assertNull(adms.getEvents())
        assertNull(adms.getOmnitureDataValue("&&events"))
    }

    @Test
    fun testAppendEventsSimple() {
        val event = "event112"
        adms.appendEvents(event)
        assertEquals(event, adms.getEvents())
        assertEquals(event, adms.getOmnitureDataValue("&&events"))
    }

    @Test
    fun testAppendEventsAppending() {
        val event1 = "event1"
        val event2 = "event2"
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
        adms.appendEvents(event1)
        assertEquals(event1, adms.getEvents())
        assertEquals(event1, adms.getOmnitureDataValue("&&events"))

        adms.appendEvents(event2)
        assertEquals("$event1", adms.getEvents())
        assertEquals("$event1", adms.getOmnitureDataValue("&&events"))
    }

    @Test
    fun testProp() {
        val sampleProp = "sample prop"
        adms.setProp(10, sampleProp)
        assertEquals(sampleProp, adms.getProp(10))
        assertEquals(sampleProp, adms.getOmnitureDataValue("&&c10"))
    }

    @Test
    fun testPropNull() {
        adms.setProp(10, null)
        assertEquals("", adms.getOmnitureDataValue("&&c10"))
    }

    @Test
    fun testEvar() {
        val sampleEvar10Value = "sample evar set"
        adms.setEvar(10, sampleEvar10Value)
        assertEquals(sampleEvar10Value, adms.getEvar(10))
        assertEquals(sampleEvar10Value, adms.getOmnitureDataValue("&&v10"))
    }

    @Test
    fun testEvarValueNull() {
        adms.setEvar(10, null)
        assertEquals("", adms.getOmnitureDataValue("&&v10"))
    }

    @Test
    fun visitorIdIsNonNullAndOnlyNumeric() {
        val visitorId = adms.visitorID
        assertNotNull(visitorId)
        assertThat(visitorId, matchesPattern("^[0-9]+$"))
    }

    @Test
    fun trackLinkIncludesCustomLinkType() {
        val mockProvider = OmnitureTestUtils.setMockAnalyticsProvider()

        adms.trackLink("Link.Name")

        val expectedData = mapOf(
                "&&linkType" to "o",
                "&&linkName" to "Link.Name"
        )
        Mockito.verify(mockProvider).trackAction(Mockito.eq("Link.Name"), mapThat(hasEntries(expectedData)))
    }
}
