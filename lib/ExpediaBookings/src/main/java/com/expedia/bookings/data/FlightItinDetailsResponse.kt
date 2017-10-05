package com.expedia.bookings.data

import org.joda.time.DateTime

class FlightItinDetailsResponse: AbstractItinDetailsResponse() {

    lateinit var responseData: FlightResponseData

    class FlightResponseData : ResponseData() {
        var flights = emptyList<Flight>()
        var rewardList: List<Reward>? = null
        lateinit var airAttachQualificationInfo: AirAttachQualificationInfo
        var insurance: List<Insurance>? = null

        class Reward {
            var totalPoints: Int? = null
        }

        class AirAttachQualificationInfo {
            var airAttachQualified: Boolean = false
            lateinit var offerExpiresTime: OfferExpiresTime

            class OfferExpiresTime {
                lateinit var raw: String
                fun airAttachExpirationTime(): DateTime {
                    return DateTime.parse(raw)
                }
            }
        }

        class Insurance
    }

    class Flight {
        lateinit var legs: List<Leg>
        lateinit var passengers: List<Passengers>

        class Leg {
            var numberOfStops: Int? = null
            lateinit var airlineLogoURL: String
            lateinit var sharableFlightLegURL: String
            lateinit var segments: List<Segment>

            class Segment {
                lateinit var departureTime: Time
                lateinit var arrivalTime: Time
                lateinit var arrivalLocation: Location
                lateinit var departureLocation: Location

                class Location {
                    lateinit var airportCode: String
                    lateinit var city: String
                }
            }
        }

        class Passengers {
            var emailAddress: String? = ""
        }
    }

    override fun getResponseDataForItin(): ResponseData? {
        return responseData
    }

    fun getMainTravelerEmail(): String {
        return responseData.flights.firstOrNull()?.passengers?.firstOrNull()?.emailAddress ?: ""
    }

    fun getItinNumber(): String? {
        return responseData.tripNumber?.toString()
    }

    fun getIsAirAttachQualified(): Boolean {
        return responseData.airAttachQualificationInfo.airAttachQualified
    }

    fun getTripTotalPrice(): String? {
        return responseData.totalTripPrice?.totalFormatted
    }

    fun getNumberOfPassengers(): Int {
        return responseData.flights[0].passengers.size
    }

    fun getTotalPoints(): String? {
        return responseData.rewardList?.firstOrNull()?.totalPoints?.toString()
    }

    fun isRoundTrip(): Boolean {
        return responseData.flights.firstOrNull()?.legs?.size ?: 0 > 1
    }

    fun getFirstFlightOutboundLeg(): Flight.Leg? {
        return responseData.flights.firstOrNull()?.legs?.firstOrNull()
    }

    fun getOutboundDepartureCity(): String? {
        return getFirstFlightOutboundLeg()?.segments?.first()?.departureLocation?.city
    }

    fun getOutboundDestinationCity(): String? {
        return getFirstFlightOutboundLeg()?.segments?.last()?.arrivalLocation?.city
    }

    fun getFirstFlightOutboundDepartureDate(): String? {
        return getFirstFlightOutboundLeg()?.segments?.firstOrNull()?.departureTime?.localizedShortDate
    }

    fun getOutboundSharableDetailsURL(): String? {
        return getFirstFlightOutboundLeg()?.sharableFlightLegURL?.replace("/api/", "/m/")
    }

    fun getFirstFlightInboundLeg(): Flight.Leg? {
        return responseData.flights.firstOrNull()?.legs?.getOrNull(1)
    }

    fun getLastFlightInboundLeg(): Flight.Leg? {
        return responseData.flights.lastOrNull()?.legs?.getOrNull(1)
    }

    fun getFirstFlightInboundArrivalDate(): String? {
        val size = getFirstFlightInboundLeg()?.segments?.size ?: 1
        return getFirstFlightInboundLeg()?.segments?.getOrNull(size - 1)?.arrivalTime?.localizedShortDate
    }

    fun getInboundSharableDetailsURL(): String? {
        return getFirstFlightInboundLeg()?.sharableFlightLegURL?.replace("/api/", "/m/")
    }
}
