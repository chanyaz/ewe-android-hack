package com.expedia.vm.test.rail

import com.expedia.bookings.data.Money
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.expedia.bookings.data.rail.responses.RailOffer
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.data.rail.responses.RailLeg
import com.expedia.bookings.data.rail.responses.RailDateTime
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.testutils.JSONResourceReader
import com.expedia.vm.rail.RailInboundDetailsViewModel
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import com.expedia.bookings.services.TestObserver
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


@RunWith(RobolectricRunner::class)
class RailInboundDetailsViewModelTest {
    val viewModel = RailInboundDetailsViewModel(RuntimeEnvironment.application)

    @Test
    fun testOpenReturnOffersNotShown() {
        val OFFERS_FOR_LEG_OPTION = 6
        val EXPECTED_FILTERED_OFFER_LIST_SIZE = 2

        val mockSearchResponse = mockSearchResponse()

        // non open return offer (8)
        val selectedOutboundOffer = mockSearchResponse.offerList[0]
        viewModel.selectedOutboundOfferSubject.onNext(selectedOutboundOffer)

        //selected inbound leg with legOptionIndex: 8
        val selectedInboundLeg = mockSearchResponse.inboundLeg!!.legOptionList[0] //mockInboundLeg()

        val offerPairSubscriber = TestObserver.create<Pair<List<RailOffer>, Money?>>()
        viewModel.railOffersAndInboundCheapestPricePairSubject.subscribe(offerPairSubscriber)

        viewModel.railResultsObservable.onNext(mockSearchResponse)
        viewModel.railLegOptionSubject.onNext(selectedInboundLeg)

        assertEquals(OFFERS_FOR_LEG_OPTION, viewModel.railResultsObservable.value.findOffersForLegOption(selectedInboundLeg).size)
        offerPairSubscriber.assertValueCount(1)
        val pair = offerPairSubscriber.values()[0]
        assertEquals(EXPECTED_FILTERED_OFFER_LIST_SIZE, pair.first.size)
        assertFalse(pair.first[0].isOpenReturn)
        assertFalse(pair.first[1].isOpenReturn)
    }

    @Test
    fun testMatchingOpenReturnOfferShown() {
        val OFFERS_FOR_LEG_OPTION = 6
        val EXPECTED_FILTERED_OFFER_LIST_SIZE = 1

        val mockSearchResponse = mockSearchResponse()

        // open return offer (1,8)
        val selectedOutboundOffer = mockSearchResponse.offerList[3]
        viewModel.selectedOutboundOfferSubject.onNext(selectedOutboundOffer)

        //selected inbound leg with legOptionIndex: 8
        val selectedInboundLeg = mockSearchResponse.inboundLeg!!.legOptionList[0] //mockInboundLeg()

        val offerPairSubscriber = TestObserver.create<Pair<List<RailOffer>, Money?>>()
        viewModel.railOffersAndInboundCheapestPricePairSubject.subscribe(offerPairSubscriber)

        viewModel.railResultsObservable.onNext(mockSearchResponse)
        viewModel.railLegOptionSubject.onNext(selectedInboundLeg)

        assertEquals(OFFERS_FOR_LEG_OPTION, viewModel.railResultsObservable.value.findOffersForLegOption(selectedInboundLeg).size)
        offerPairSubscriber.assertValueCount(1)
        val pair = offerPairSubscriber.values()[0]
        assertEquals(EXPECTED_FILTERED_OFFER_LIST_SIZE, pair.first.size)
        assertTrue(pair.first[0].isOpenReturn)
    }

    private fun mockSearchResponse():RailSearchResponse {
        val response = RailSearchResponse()
        response.legList = getLegs()
        response.offerList = generateOffersForLeg()
        return response
    }

    private fun getLegs(): MutableList<RailLeg> {
        val leg1 = RailLeg()
        leg1.legBoundOrder = 1
        leg1.legOptionList = listOf(createLegOption(1))

        val leg2 = RailLeg()
        leg2.legBoundOrder = 2
        leg2.legOptionList = listOf(createLegOption(8))
        return arrayListOf(leg2)
    }

    private fun createLegOption(legId: Int): RailLegOption {
        val legOption = RailLegOption()
        legOption.legOptionIndex = legId
        val railDateTime = RailDateTime()
        railDateTime.raw = "2016-10-08T09:30:00"
        legOption.departureDateTime = railDateTime
        legOption.arrivalDateTime = railDateTime
        legOption.duration = "PT4H31M"
        legOption.noOfChanges = 0
        return legOption
    }

    private fun generateOffersForLeg(): List<RailOffer> {
        val resourceReader = JSONResourceReader("src/test/resources/raw/rail/roundtrip_open_return.json")
        val searchResponse = resourceReader.constructUsingGson(RailSearchResponse::class.java)
        return searchResponse.offerList
    }
}
