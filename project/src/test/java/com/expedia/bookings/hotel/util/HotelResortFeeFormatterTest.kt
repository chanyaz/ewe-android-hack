package com.expedia.bookings.hotel.util

import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelResortFeeFormatterTest {
    private val formatter = HotelResortFeeFormatter()
    private val context = RuntimeEnvironment.application

    private val expectedFee = 25f
    private val expectedCurrency = "GBP"
    private val expectedCurrencyPOSu = "USD"

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testInclusionTextEmpty() {
        assertEquals("", formatter.getResortFeeInclusionText(context, null))
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testInclusionTextShowFeesFalse() {
        val response = happyResponse()
        response.rateInfo.chargeableRateInfo.showResortFeeMessage = false
        assertEquals("", formatter.getResortFeeInclusionText(context, response))
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testInclusionTextShowFeesIncluded() {
        val response = happyResponse()
        response.rateInfo.chargeableRateInfo.showResortFeeMessage = true
        response.rateInfo.chargeableRateInfo.resortFeeInclusion = true
        assertEquals(context.getString(R.string.included_in_the_price),
                formatter.getResortFeeInclusionText(context, response))
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testInclusionTextShowFeesNotIncluded() {
        val response = happyResponse()
        response.rateInfo.chargeableRateInfo.showResortFeeMessage = true
        response.rateInfo.chargeableRateInfo.resortFeeInclusion = false
        assertEquals(context.getString(R.string.not_included_in_the_price),
                formatter.getResortFeeInclusionText(context, response))
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testResortFeeEmpty() {
        val fee = formatter.getResortFee(context, null, false, hotelCountry = "")
        assertEquals("", fee)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testResortFeeShowFeesFalse() {
        val response = happyResponse()
        response.rateInfo.chargeableRateInfo.showResortFeeMessage = false
        val fee = formatter.getResortFee(context, response, false, hotelCountry = "")
        assertEquals("", fee)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testResortFeeShowFeesDefault() {
        val response = happyResponse()
        response.rateInfo.chargeableRateInfo.showResortFeeMessage = true
        response.rateInfo.chargeableRateInfo.totalMandatoryFees = expectedFee
        response.rateInfo.chargeableRateInfo.currencyCode = expectedCurrency

        val fee = formatter.getResortFee(context, response, false, hotelCountry = "")
        assertEquals("GBP25", fee)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testResortFeeShowFeesNewCurrencyPOS() {
        val response = happyResponse()
        response.rateInfo.chargeableRateInfo.showResortFeeMessage = true
        response.rateInfo.chargeableRateInfo.totalMandatoryFees = expectedFee
        response.rateInfo.chargeableRateInfo.currencyCode = expectedCurrency
        response.rateInfo.chargeableRateInfo.currencyCodePOSu = expectedCurrencyPOSu

        val fee = formatter.getResortFee(context, response, false, hotelCountry = "")
        assertEquals("$25", fee, "FAILURE: Expected api value 'currencyCodePOSu' to override `currencyCode` when present")
    }

    private fun happyResponse(): HotelOffersResponse.HotelRoomResponse {
        val response = HotelOffersResponse.HotelRoomResponse()
        response.rateInfo = HotelOffersResponse.RateInfo()
        response.rateInfo.chargeableRateInfo = HotelRate()
        return response
    }
}
