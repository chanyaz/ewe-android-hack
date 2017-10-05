import com.expedia.bookings.data.AbstractItinDetailsResponse
import com.expedia.bookings.data.FlightItinDetailsResponse
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class FlightItinDetailsResponseTests {
    @Test
    fun testGetResponseDataForItin() {
        assertNotNull(getResponse().getResponseDataForItin())
    }

    @Test
    fun testGetOutboundSharableDetailsURL() {
        assertEquals(getResponse().getOutboundSharableDetailsURL(), "www.expedia_test_outbound.com/m/flights")
    }

    @Test
    fun testGetOutboundSharableDetailsURLNullWhenMissingFlights() {
        assertNull(getResponse(omitFlights = true).getOutboundSharableDetailsURL())
    }

    @Test
    fun testGetInboundShareableDetailsURL() {
        assertEquals(getResponse(isRoundTrip = true).getInboundSharableDetailsURL(), "www.expedia_test_inbound.com/m/flights")
    }

    @Test
    fun testGetInboundShareableDetailsURLWhenNotRoundTrip() {
        assertNull(getResponse().getInboundSharableDetailsURL())
    }

    @Test
    fun testGetOutboundDepartureDate() {
        assertEquals(getResponse().getFirstFlightOutboundDepartureDate(), "5/20/17")
    }

    @Test
    fun testGetOutboundDepartureDateNullWhenMissingFlights() {
        assertNull(getResponse(omitFlights = true).getFirstFlightOutboundDepartureDate())
    }

    @Test
    fun testGetInboundArrivalDate() {
        assertEquals(getResponse(isRoundTrip = true).getFirstFlightInboundArrivalDate(), "5/24/17")
    }

    @Test
    fun testGetInboundArrivalDateNullWhenMissingFlights() {
        assertNull(getResponse(omitFlights = true, isRoundTrip = true).getFirstFlightInboundArrivalDate())
    }

    @Test
    fun testGetInboundArrivalDateNullWhenMissingSegments() {
        assertNull(getResponse(omitSegments = true, isRoundTrip = true).getFirstFlightInboundArrivalDate())
    }

    private fun getResponse(omitFlights: Boolean = false, omitSegments: Boolean = false, isRoundTrip: Boolean = false): FlightItinDetailsResponse {
        val outboundLeg = FlightItinDetailsResponse.Flight.Leg()
        outboundLeg.sharableFlightLegURL = "www.expedia_test_outbound.com/api/flights"
        val outboundSegments = ArrayList<FlightItinDetailsResponse.Flight.Leg.Segment>()
        val outboundSegment = FlightItinDetailsResponse.Flight.Leg.Segment()
        outboundSegment.departureTime = AbstractItinDetailsResponse.Time()
        outboundSegment.departureTime.localizedShortDate = "5/20/17"
        outboundSegments.add(outboundSegment)
        outboundLeg.segments = if (omitSegments) arrayListOf() else outboundSegments

        val legs = ArrayList<FlightItinDetailsResponse.Flight.Leg>()
        legs.add(outboundLeg)

        if (isRoundTrip) {
            val inboundLeg = FlightItinDetailsResponse.Flight.Leg()
            inboundLeg.sharableFlightLegURL = "www.expedia_test_inbound.com/api/flights"
            val inboundSegments = ArrayList<FlightItinDetailsResponse.Flight.Leg.Segment>()
            val inboundSegment = FlightItinDetailsResponse.Flight.Leg.Segment()
            inboundSegment.arrivalTime = AbstractItinDetailsResponse.Time()
            inboundSegment.arrivalTime.localizedShortDate = "5/24/17"
            inboundSegments.add(inboundSegment)
            inboundLeg.segments = if (omitSegments) arrayListOf() else inboundSegments
            legs.add(inboundLeg)
        }

        val flight = FlightItinDetailsResponse.Flight()
        flight.legs = legs

        val flights = ArrayList<FlightItinDetailsResponse.Flight>()
        flights.add(flight)

        val response = FlightItinDetailsResponse()
        response.responseData = FlightItinDetailsResponse.FlightResponseData()
        response.responseData.flights = if (omitFlights) arrayListOf() else flights
        return response
    }
}
