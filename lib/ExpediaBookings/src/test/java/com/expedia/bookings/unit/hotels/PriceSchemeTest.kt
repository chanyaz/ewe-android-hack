package com.expedia.bookings.unit.hotels

import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.hotels.PriceScheme
import com.expedia.bookings.data.hotels.PriceType
import org.junit.Test
import kotlin.test.assertEquals

class PriceSchemeTest {

    private val totalPriceScheme = PriceScheme(PriceType.TOTAL, true, true)
    private val perNightPriceScheme = PriceScheme(PriceType.PER_NIGHT, false, false)

    @Test
    fun testConvertToUserPriceTypeTotal() {
        assertEquals(HotelRate.UserPriceType.RATE_FOR_WHOLE_STAY_WITH_TAXES, totalPriceScheme.convertToUserPriceType())
    }

    @Test
    fun testConvertToUserPriceTypeTotalIgnoreTaxFeeInclusion() {
        var priceScheme = PriceScheme(PriceType.TOTAL, false, false)
        assertEquals(HotelRate.UserPriceType.RATE_FOR_WHOLE_STAY_WITH_TAXES, priceScheme.convertToUserPriceType())
        priceScheme = PriceScheme(PriceType.TOTAL, false, true)
        assertEquals(HotelRate.UserPriceType.RATE_FOR_WHOLE_STAY_WITH_TAXES, priceScheme.convertToUserPriceType())
        priceScheme = PriceScheme(PriceType.TOTAL, true, false)
        assertEquals(HotelRate.UserPriceType.RATE_FOR_WHOLE_STAY_WITH_TAXES, priceScheme.convertToUserPriceType())
    }

    @Test
    fun testConvertToUserPriceTypePerNight() {
        assertEquals(HotelRate.UserPriceType.PER_NIGHT_RATE_NO_TAXES, perNightPriceScheme.convertToUserPriceType())
    }

    @Test
    fun testConvertToUserPriceTypePerNightIgnoreTaxFeeInclusion() {
        var priceScheme = PriceScheme(PriceType.PER_NIGHT, false, true)
        assertEquals(HotelRate.UserPriceType.PER_NIGHT_RATE_NO_TAXES, priceScheme.convertToUserPriceType())
        priceScheme = PriceScheme(PriceType.PER_NIGHT, true, false)
        assertEquals(HotelRate.UserPriceType.PER_NIGHT_RATE_NO_TAXES, priceScheme.convertToUserPriceType())
        priceScheme = PriceScheme(PriceType.PER_NIGHT, true, true)
        assertEquals(HotelRate.UserPriceType.PER_NIGHT_RATE_NO_TAXES, priceScheme.convertToUserPriceType())
    }

    @Test
    fun testConvertToUserPriceTypeStringTotal() {
        assertEquals("RateForWholeStayWithTaxes", totalPriceScheme.convertToUserPriceTypeString())
    }

    @Test
    fun testConvertToUserPriceTypeStringTotalIgnoreTaxFeeInclusion() {
        var priceScheme = PriceScheme(PriceType.TOTAL, false, false)
        assertEquals("RateForWholeStayWithTaxes", priceScheme.convertToUserPriceTypeString())
        priceScheme = PriceScheme(PriceType.TOTAL, false, true)
        assertEquals("RateForWholeStayWithTaxes", priceScheme.convertToUserPriceTypeString())
        priceScheme = PriceScheme(PriceType.TOTAL, true, false)
        assertEquals("RateForWholeStayWithTaxes", priceScheme.convertToUserPriceTypeString())
    }

    @Test
    fun testConvertToUserPriceTypeStringPerNight() {
        assertEquals("PerNightRateNoTaxes", perNightPriceScheme.convertToUserPriceTypeString())
    }

    @Test
    fun testConvertToUserPriceTypeStringPerNightIgnoreTaxFeeInclusion() {
        var priceScheme = PriceScheme(PriceType.PER_NIGHT, false, true)
        assertEquals("PerNightRateNoTaxes", priceScheme.convertToUserPriceTypeString())
        priceScheme = PriceScheme(PriceType.PER_NIGHT, true, false)
        assertEquals("PerNightRateNoTaxes", priceScheme.convertToUserPriceTypeString())
        priceScheme = PriceScheme(PriceType.PER_NIGHT, true, true)
        assertEquals("PerNightRateNoTaxes", priceScheme.convertToUserPriceTypeString())
    }
}
