package com.expedia.vm.test.rail

import com.expedia.bookings.data.rail.responses.RailCheckoutResponse
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.data.rail.responses.RailTripOffer
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.testutils.JSONResourceReader
import com.expedia.vm.rail.RailConfirmationViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RuntimeEnvironment
import com.expedia.bookings.services.TestObserver
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class RailConfirmationViewModelTest {
    val testConfirmationVM = RailConfirmationViewModel(RuntimeEnvironment.application)

    val expectedItinNumber = "1234567890"
    val expectedEmail = "oscar.grouch@seasmestreet.com"
    lateinit var testLegOption: RailLegOption

    @Before
    fun setUp() {
        val pair = Pair<RailCheckoutResponse, String>(buildCheckoutResponse(), expectedEmail)
        testConfirmationVM.confirmationObservable.onNext(pair)
        testLegOption = generateLegOption()
    }

    @Test
    fun testOneWay() {
        val oneWayOffer = getMockOneWayOffer()

        val testItinSub = TestObserver<String>()
        val testInboundVisibleSub = TestObserver<Boolean>()

        testConfirmationVM.itinNumberObservable.subscribe(testItinSub)
        testConfirmationVM.inboundCardVisibility.subscribe(testInboundVisibleSub)

        testConfirmationVM.railOfferObserver.onNext(oneWayOffer)

        assertEquals(getExpectedEmailSent(), testItinSub.values()[0])
        assertFalse(testInboundVisibleSub.values()[0])
    }

    @Test
    fun testOpenReturn() {
        val openReturnOffer = getMockOpenReturnOffer()

        val testInboundVisibleSub = TestObserver<Boolean>()
        testConfirmationVM.inboundCardVisibility.subscribe(testInboundVisibleSub)

        testConfirmationVM.railOfferObserver.onNext(openReturnOffer)

        assertTrue(testInboundVisibleSub.values()[0])
    }

    @Test
    fun testRoundTrip() {
        val roundTripOffer = getMockRoundTripOffer()

        val testInboundVisibleSub = TestObserver<Boolean>()
        testConfirmationVM.inboundCardVisibility.subscribe(testInboundVisibleSub)

        testConfirmationVM.railOfferObserver.onNext(roundTripOffer)

        assertTrue(testInboundVisibleSub.values()[0])
    }

    private fun buildCheckoutResponse(): RailCheckoutResponse {
        val resourceReader = JSONResourceReader("../lib/mocked/templates/m/api/rails/trip/checkout/oneway_happy.json")
        val checkoutResponse = resourceReader.constructUsingGson(RailCheckoutResponse::class.java)

        checkoutResponse.newTrip.itineraryNumber = expectedItinNumber
        return checkoutResponse
    }

    private fun getMockOneWayOffer(): RailTripOffer {
        val mockTripOffer = Mockito.mock(RailTripOffer::class.java)
        Mockito.`when`(mockTripOffer.isRoundTrip).thenReturn(false)
        Mockito.`when`(mockTripOffer.isOpenReturn).thenReturn(false)
        Mockito.`when`(mockTripOffer.outboundLegOption).thenReturn(testLegOption)
        mockTripOffer.passengerList = emptyList()
        return mockTripOffer
    }

    private fun getMockRoundTripOffer(): RailTripOffer {
        val mockTripOffer = Mockito.mock(RailTripOffer::class.java)
        Mockito.`when`(mockTripOffer.isRoundTrip).thenReturn(true)
        Mockito.`when`(mockTripOffer.isOpenReturn).thenReturn(false)
        Mockito.`when`(mockTripOffer.outboundLegOption).thenReturn(testLegOption)
        Mockito.`when`(mockTripOffer.inboundLegOption).thenReturn(testLegOption)
        mockTripOffer.passengerList = emptyList()
        return mockTripOffer
    }

    private fun getMockOpenReturnOffer(): RailTripOffer {
        val mockTripOffer = Mockito.mock(RailTripOffer::class.java)
        Mockito.`when`(mockTripOffer.isRoundTrip).thenReturn(false)
        Mockito.`when`(mockTripOffer.isOpenReturn).thenReturn(true)
        Mockito.`when`(mockTripOffer.outboundLegOption).thenReturn(testLegOption)
        Mockito.`when`(mockTripOffer.inboundLegOption).thenReturn(testLegOption)
        mockTripOffer.passengerList = emptyList()
        return mockTripOffer
    }

    private fun generateLegOption(): RailLegOption {
        val resourceReader = JSONResourceReader("src/test/resources/raw/rail/rail_leg_option_segments_8_9_10.json")
        val legOption = resourceReader.constructUsingGson(RailLegOption::class.java)
        return legOption
    }

    private fun getExpectedEmailSent(): String {
        return "#$expectedItinNumber sent to $expectedEmail"
    }
}
