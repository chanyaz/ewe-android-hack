package com.expedia.bookings.data

class FlightItinDetailsResponse: AbstractItinDetailsResponse() {

    lateinit var responseData: FlightResponseData

    class FlightResponseData : ResponseData() {
        var flights = emptyList<Flight>()
    }

    class Flight {
        lateinit var legs: List<Leg>

        class Leg {
            lateinit var sharableFlightLegURL: String
            lateinit var segments: List<Segment>

            class Segment {
                lateinit var departureTime: Time
                lateinit var arrivalTime: Time
            }
        }
    }

    override fun getResponseDataForItin(): ResponseData? {
        return responseData
    }

    fun getOutboundSharableDetailsURL(): String? {
        return getOutboundLeg()?.sharableFlightLegURL?.replace("/api/", "/m/")
    }

    fun getInboundSharableDetailsURL(): String? {
        return getInboundLeg()?.sharableFlightLegURL?.replace("/api/", "/m/")
    }

    fun getOutboundDepartureDate(): String? {
        return getOutboundLeg()?.segments?.firstOrNull()?.departureTime?.localizedShortDate
    }

    fun getInboundArrivalDate(): String? {
        val size = getInboundLeg()?.segments?.size ?: 1
        return getInboundLeg()?.segments?.getOrNull(size - 1)?.arrivalTime?.localizedShortDate
    }

    private fun getOutboundLeg(): Flight.Leg? {
        return responseData.flights.firstOrNull()?.legs?.firstOrNull()
    }

    private fun getInboundLeg(): Flight.Leg? {
        return responseData.flights.firstOrNull()?.legs?.getOrNull(1)
    }
}
