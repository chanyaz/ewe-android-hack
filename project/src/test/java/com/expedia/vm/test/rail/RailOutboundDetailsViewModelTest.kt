package com.expedia.vm.test.rail

import com.expedia.bookings.data.Money
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.expedia.bookings.data.rail.responses.RailOffer
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.data.rail.responses.RailLeg
import com.expedia.bookings.data.rail.responses.RailDateTime
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.testutils.JSONResourceReader
import com.expedia.vm.rail.RailDetailsViewModel
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class RailOutboundDetailsViewModelTest {
    val viewModel = RailDetailsViewModel(RuntimeEnvironment.application)

    @Test
    fun testOvertaken() {
        val mockSearchResponse = Mockito.mock(RailSearchResponse::class.java)
        mockSearchResponse.legList = emptyList()
        Mockito.`when`(mockSearchResponse.findOffersForLegOption(Mockito.any())).thenReturn(emptyList())
        viewModel.railResultsObservable.onNext(mockSearchResponse)

        val leg = mockLegOption()
        val overtakenTestSubscriber = TestSubscriber.create<Boolean>()
        viewModel.overtaken.subscribe(overtakenTestSubscriber)

        viewModel.railLegOptionSubject.onNext(leg)
        overtakenTestSubscriber.assertValueCount(1)
        assertFalse(overtakenTestSubscriber.onNextEvents[0])

        leg.overtakenJourney = true
        viewModel.railLegOptionSubject.onNext(leg)
        overtakenTestSubscriber.assertValueCount(2)
        assertTrue(overtakenTestSubscriber.onNextEvents[1])
    }

    @Test
    fun testDuplicateOpenReturnOffersFiltered() {
        val OFFERS_FOR_LEG_OPTION = 6
        val EXPECTED_FILTERED_OFFER_LIST_SIZE = 4

        val offerPairSubscriber = TestSubscriber.create<Pair<List<RailOffer>, Money?>>()
        viewModel.railOffersAndInboundCheapestPricePairSubject.subscribe(offerPairSubscriber)

        val mockSearchResponse = mockSearchResponse()
        viewModel.railResultsObservable.onNext(mockSearchResponse)

        val outboundLeg = mockLegOption()
        viewModel.railLegOptionSubject.onNext(outboundLeg)

        assertEquals(OFFERS_FOR_LEG_OPTION, viewModel.railResultsObservable.value.findOffersForLegOption(outboundLeg).size)
        offerPairSubscriber.assertValueCount(1)
        val pair = offerPairSubscriber.onNextEvents[0]
        assertEquals(EXPECTED_FILTERED_OFFER_LIST_SIZE, pair.first.size)
    }

    private fun mockLegOption(): RailLegOption {
        val leg = RailLegOption()
        val railDateTime = RailDateTime()
        railDateTime.raw = "2016-10-08T09:30:00"
        leg.departureDateTime = railDateTime
        leg.arrivalDateTime = railDateTime
        leg.duration = "PT4H31M"
        leg.noOfChanges = 0
        leg.legOptionIndex = 8
        return leg
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
        leg1.legOptionList = listOf(createLegOption(8))
        return arrayListOf(leg1)
    }

    private fun createLegOption(legId: Int): RailLegOption {
        val legOption = RailLegOption()
        legOption.legOptionIndex = legId
        return legOption
    }

    private fun generateOffersForLeg(): List<RailOffer> {
        val resourceReader = JSONResourceReader("src/test/resources/raw/rail/roundtrip_open_return.json")
        val searchResponse = resourceReader.constructUsingGson(RailSearchResponse::class.java)
        return searchResponse.offerList
    }
}