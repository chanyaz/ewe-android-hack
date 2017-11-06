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
}
