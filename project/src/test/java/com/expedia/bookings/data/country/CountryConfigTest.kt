package com.expedia.bookings.data.country

import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class CountryConfigTest {

    @Test
    fun testCountryConfigContainsBillingCountryConfigs() {
        val countryConfig = CountryConfig.countryConfig!!
        val billingCountryConfigs = countryConfig.billingCountryConfigs
        assertTrue(billingCountryConfigs.containsKey("1"))
    }

    @Test
    fun testCountryConfigContainsCountryConfigs() {
        val countryConfig = CountryConfig.countryConfig!!
        val countryName = "IN"
        val expectedConfigForIndia = HashMap<String, String>()
        expectedConfigForIndia.put("configID", "1")
        assertEquals(expectedConfigForIndia, countryConfig.countries[countryName])
    }
}
