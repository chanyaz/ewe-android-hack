package com.expedia.bookings.unit.rail

import com.expedia.bookings.data.Money
import com.expedia.bookings.data.rail.responses.RailLeg
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.data.rail.responses.RailOffer
import com.expedia.bookings.data.rail.responses.RailProduct
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import org.junit.Test
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RailSearchResponseTest {

    @Test
    fun testInboundLegsNotFilteredForNonOpenReturnOffer() {
        val response = getMockRailSearchResponse()
        val offer = createOffer(listOf(1))
        val legOptions = response.getInboundLegOptionsForOffer(offer)
        assertTrue(legOptions.size == 3, "3 inbound leg options should be returned")
        assertLegOptions(legOptions, listOf(3, 4, 5))
    }

    @Test
    fun testInboundLegsFilteredForOpenReturnOffer() {
        val response = getMockRailSearchResponse()
        var offer = createOffer("First class", listOf(1, 3), true, Money(BigDecimal(121.00), "$"))

        var legOptions = response.getInboundLegOptionsForOffer(offer)
        assertTrue(legOptions.size == 1, "1 inbound leg option should be returned")
        assertLegOptions(legOptions, listOf(3))

        offer = createOffer("Standard", listOf(1, 3), true, Money(BigDecimal(99.00), "$"))

        legOptions = response.getInboundLegOptionsForOffer(offer)
        assertTrue(legOptions.size == 2, "2 inbound leg option should be returned")
        assertLegOptions(legOptions, listOf(3, 4))
    }

    @Test
    fun testFilterOutboundOffers() {
        val response = getMockRailSearchResponse()
        val filteredOffers = response.filterOutboundOffers(response.offerList)
        assertEquals(response.offerList.size, 8)
        assertEquals(filteredOffers.size, 7)
    }

    @Test
    fun testFilterInboundOffersForNonOpenReturn() {
        val response = getMockRailSearchResponse()
        val outboundOffer = createOffer("First class", listOf(1), false, Money(BigDecimal(121.00), "$"))

        val filteredOffers = response.filterInboundOffers(response.offerList, outboundOffer)
        assertEquals(response.offerList.size, 8)
        assertEquals(filteredOffers.size, 5)
    }

    @Test
    fun testFilterInboundOffersForOpenReturn() {
        val response = getMockRailSearchResponse()
        val outboundOffer = createOffer("First class", listOf(1, 3), true, Money(BigDecimal(121.00), "$"))

        val filteredOffers = response.filterInboundOffers(response.offerList, outboundOffer)
        assertEquals(response.offerList.size, 8)
        assertEquals(filteredOffers.size, 1)
    }

    private fun assertLegOptions(legOptions: List<RailLegOption>, legOptionIds: List<Int>) {
        var valid = false
        for ((index, legOption) in legOptions.withIndex()) {
            valid = legOption.legOptionIndex == legOptionIds[index]
        }
        assertTrue(valid, "Invalid leg options returned")
    }

    private fun getMockRailSearchResponse(): RailSearchResponse {
        val response = RailSearchResponse()
        response.legList = getLegs()
        response.offerList = getOffers()

        return response
    }

    private fun getOffers(): List<RailOffer> {
        return listOf(createOffer(listOf(1)), createOffer(listOf(2)), createOffer(listOf(3)),
                createOffer(listOf(4)), createOffer(listOf(5)),
                createOffer("Standard", listOf(1, 3), true, Money(BigDecimal(99.00), "$")),
                createOffer("Standard", listOf(1, 4), true, Money(BigDecimal(99.00), "$")),
                createOffer("First class", listOf(1, 3), true, Money(BigDecimal(121.00), "$")))
    }

    private fun createOffer(legOptionIds: List<Int>): RailOffer {
        return createOffer("Standard", legOptionIds, false, Money(BigDecimal(99.00), "$"))
    }

    private fun createOffer(serviceClass: String, legOptionIds: List<Int>, openReturn: Boolean, price: Money): RailOffer {
        val offer = RailOffer()
        val product = RailProduct()
        product.openReturn = openReturn
        product.legOptionIndexList = legOptionIds
        product.aggregatedCarrierServiceClassDisplayName = serviceClass
        product.aggregatedCarrierFareClassDisplayName = "Off-Peak Return"
        offer.railProductList = listOf(product)
        offer.totalPrice = price
        return offer
    }

    private fun getLegs(): MutableList<RailLeg> {
        val leg1 = RailLeg()
        leg1.legBoundOrder = 1
        leg1.legOptionList = listOf(createLegOption(1), createLegOption(2))

        val leg2 = RailLeg()
        leg2.legBoundOrder = 2
        leg2.legOptionList = listOf(createLegOption(3), createLegOption(4), createLegOption(5))
        return arrayListOf(leg1, leg2)
    }

    private fun createLegOption(legId: Int): RailLegOption {
        val legOption = RailLegOption()
        legOption.legOptionIndex = legId
        return legOption
    }
}
