package com.expedia.vm.test.rail

import com.expedia.bookings.data.Money
import com.expedia.bookings.data.rail.responses.RailDateTime
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.testutils.JSONResourceReader
import com.expedia.vm.rail.RailOutboundDetailsViewModel
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
    val viewModel = RailOutboundDetailsViewModel(RuntimeEnvironment.application)

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
    fun testOpenReturnOffersFiltered() {
        val SEARCH_RESPONSE_RETURNED_OFFERS_SIZE = 6
        val EXPECTED_FILTERED_OFFER_LIST_SIZE = 4

        val offerList = generateOffersForLeg()
        val offerPairSubscriber = TestSubscriber.create<Pair<List<RailSearchResponse.RailOffer>, Money?>>()
        viewModel.railOffersPairSubject.subscribe(offerPairSubscriber)

        val mockSearchResponse = Mockito.mock(RailSearchResponse::class.java)
        mockSearchResponse.legList = emptyList()
        Mockito.`when`(mockSearchResponse.findOffersForLegOption(Mockito.any())).thenReturn(offerList)
        viewModel.railResultsObservable.onNext(mockSearchResponse)

        val leg = mockLegOption()
        viewModel.railLegOptionSubject.onNext(leg)

        assertEquals(SEARCH_RESPONSE_RETURNED_OFFERS_SIZE, viewModel.railResultsObservable.value.findOffersForLegOption(leg).size)
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
        return leg
    }

    private fun generateOffersForLeg(): List<RailSearchResponse.RailOffer> {
        val resourceReader = JSONResourceReader("src/test/resources/raw/stripped_roundtrip_open_return.json")
        val searchResponse = resourceReader.constructUsingGson(RailSearchResponse::class.java)
        return searchResponse.offerList
    }
}