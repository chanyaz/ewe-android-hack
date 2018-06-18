package com.expedia.bookings.tracking

import com.expedia.bookings.data.AbstractItinDetailsResponse
import com.expedia.bookings.utils.TrackingUtils
import org.junit.Test
import kotlin.test.assertEquals

class TrackingUtilsTest {

    @Test
    fun testGetInsuranceProductString() {
        val insurance = getInsurance()
        val productString = TrackingUtils.getInsuranceProductsString(listOf(insurance))
        assertEquals(";Insurance:100004;2;46.70", productString)
    }

    @Test
    fun testGetInsuranceProductStringWithoutTravellerCount() {
        val insurance = getInsurance(hasTravellerCount = false)
        val productString = TrackingUtils.getInsuranceProductsString(listOf(insurance))
        assertEquals(";Insurance:100004;;;46.70", productString)
    }

    @Test
    fun testGetInsuranceProductStringWithoutPrice() {
        val insurance = getInsurance(hasPrice = false)
        val productString = TrackingUtils.getInsuranceProductsString(listOf(insurance))
        assertEquals(";Insurance:100004;2;;", productString)
    }

    @Test
    fun testGetInsuranceProductStringWithoutTravellerCountAndPrice() {
        val insurance = getInsurance(hasTravellerCount = false, hasPrice = false)
        val productString = TrackingUtils.getInsuranceProductsString(listOf(insurance))
        assertEquals(";Insurance:100004;;;;", productString)
    }

    @Test
    fun testGetMultipleInsuranceString() {
        val firstInsurance = getInsurance()
        val secondInsurance = getInsurance(hasTravellerCount = false)
        val insuranceList = listOf(firstInsurance, secondInsurance)
        val productsString = TrackingUtils.getInsuranceProductsString(insuranceList)
        assertEquals(";Insurance:100004;2;46.70,;Insurance:100004;;;46.70", productsString)
    }

    private fun getInsurance(hasTravellerCount: Boolean = true, hasPrice: Boolean = true): AbstractItinDetailsResponse.ResponseData.Insurance {
        val insurance = AbstractItinDetailsResponse.ResponseData.Insurance()
        insurance.insuranceTypeId = 100004
        if (hasTravellerCount) insurance.travellerCount = 2
        if (hasPrice) insurance.price = AbstractItinDetailsResponse.ResponseData.Insurance.Price()
        insurance.price?.total = "46.70"
        return insurance
    }
}
