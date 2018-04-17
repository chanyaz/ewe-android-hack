package com.expedia.bookings.unit.hotels

import com.expedia.bookings.data.hotels.HotelPriceInfo
import com.expedia.bookings.data.hotels.PriceScheme
import com.expedia.bookings.data.hotels.PriceType
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HotelPriceInfoTest {

    private lateinit var priceInfo: HotelPriceInfo

    @Before
    fun before() {
        priceInfo = NewHotelSearchResponseTestUtils.createHotelPriceInfo()
    }

    @Test
    fun testConvertToHotelRateDisplayPrice() {
        var hotelRate = priceInfo.convertToHotelRate()
        assertEquals(100f, hotelRate.averageRate)
        assertEquals(100f, hotelRate.total)
        assertEquals(100f, hotelRate.priceToShowUsers)
        assertEquals(100f, hotelRate.totalPriceWithMandatoryFees)
    }

    @Test
    fun testConvertToHotelRateDisplayPriceNull() {
        priceInfo = NewHotelSearchResponseTestUtils.createHotelPriceInfo(displayPrice = null)
        var hotelRate = priceInfo.convertToHotelRate()
        assertEquals(0f, hotelRate.averageRate)
        assertEquals(0f, hotelRate.total)
        assertEquals(0f, hotelRate.priceToShowUsers)
        assertEquals(0f, hotelRate.totalPriceWithMandatoryFees)
    }

    @Test
    fun testConvertToHotelRateStrikeThroughPrice() {
        var hotelRate = priceInfo.convertToHotelRate()
        assertEquals(200f, hotelRate.strikethroughPriceToShowUsers)
    }

    @Test
    fun testConvertToHotelRateStrikeThroughPriceNull() {
        priceInfo = NewHotelSearchResponseTestUtils.createHotelPriceInfo(strikeThroughPrice = null)
        var hotelRate = priceInfo.convertToHotelRate()
        assertEquals(0f, hotelRate.strikethroughPriceToShowUsers)
    }

    @Test
    fun testConvertToHotelRateDiscountPercentage() {
        var hotelRate = priceInfo.convertToHotelRate()
        assertEquals(50f, hotelRate.discountPercent)
    }

    @Test
    fun testConvertToHotelRateDiscountPercentageNull() {
        priceInfo = NewHotelSearchResponseTestUtils.createHotelPriceInfo(discountPercentage = null)
        var hotelRate = priceInfo.convertToHotelRate()
        assertEquals(0f, hotelRate.discountPercent)
    }

    @Test
    fun testConvertToHotelRateCurrencySymbol() {
        var hotelRate = priceInfo.convertToHotelRate()
        assertEquals("$", hotelRate.currencySymbol)
    }

    @Test
    fun testConvertToHotelRateCurrencySymbolNull() {
        priceInfo = NewHotelSearchResponseTestUtils.createHotelPriceInfo(currencySymbol = null)
        var hotelRate = priceInfo.convertToHotelRate()
        assertNull(hotelRate.currencySymbol)
    }

    @Test
    fun testConvertToHotelRateCurrencyCode() {
        var hotelRate = priceInfo.convertToHotelRate()
        assertEquals("USD", hotelRate.currencyCode)
    }

    @Test
    fun testConvertToHotelRateCurrencyCodeNull() {
        priceInfo = NewHotelSearchResponseTestUtils.createHotelPriceInfo(currencyCode = null)
        var hotelRate = priceInfo.convertToHotelRate()
        assertNull(hotelRate.currencyCode)
    }

    @Test
    fun testConvertToHotelRatePricingScheme() {
        var hotelRate = priceInfo.convertToHotelRate()
        assertEquals("PerNightRateNoTaxes", hotelRate.userPriceType)
        assertEquals("PerNightRateNoTaxes", hotelRate.checkoutPriceType)
        assertFalse(hotelRate.resortFeeInclusion)
    }

    @Test
    fun testConvertToHotelRatePricingSchemeWithResortFee() {
        priceInfo = NewHotelSearchResponseTestUtils.createHotelPriceInfo(pricingScheme = PriceScheme(PriceType.TOTAL, true, true))
        var hotelRate = priceInfo.convertToHotelRate()
        assertEquals("RateForWholeStayWithTaxes", hotelRate.userPriceType)
        assertEquals("RateForWholeStayWithTaxes", hotelRate.checkoutPriceType)
        assertTrue(hotelRate.resortFeeInclusion)
    }

    @Test
    fun testConvertToHotelRatePricingSchemeNull() {
        priceInfo = NewHotelSearchResponseTestUtils.createHotelPriceInfo(pricingScheme = null)
        var hotelRate = priceInfo.convertToHotelRate()
        assertNull(hotelRate.userPriceType)
        assertNull(hotelRate.checkoutPriceType)
        assertFalse(hotelRate.resortFeeInclusion)
    }
}
