package com.expedia.bookings

import org.junit.Test
import kotlin.test.assertEquals

class ADMS_MeasurementTest {

    @Test
    fun testEvar() {
        val sharedInstance = ADMS_Measurement.sharedInstance()
        val sampleEvar10Value = "sample evar set"
        sharedInstance.setEvar(10, sampleEvar10Value)
        assertEquals(sampleEvar10Value, sharedInstance.getOmnitureDataValue("&&v10"))
    }

    @Test
    fun testEvarValueNull() {
        val sharedInstance = ADMS_Measurement.sharedInstance()
        sharedInstance.setEvar(10, null)
        assertEquals("", sharedInstance.getOmnitureDataValue("&&v10"))
    }
}