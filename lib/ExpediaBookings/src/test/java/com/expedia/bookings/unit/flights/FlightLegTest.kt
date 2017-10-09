package com.expedia.bookings.unit.flights

import com.expedia.bookings.data.Money
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.multiitem.FlightOffer
import com.expedia.bookings.data.multiitem.MultiItemFlightLeg
import com.expedia.bookings.data.multiitem.MultiItemOffer
import com.google.gson.Gson
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FlightLegTest {

    @Test
    fun testConvertMultiItemFlight() {
        val flightLeg = FlightLeg.convertMultiItemFlightLeg("a13b0ab1eea2bd1d7f4cba76a857ec4b", dummyFlightOffer(), dummyMultiItemFlightLeg(), dummyMultiItemOffer(130))

        assertNotNull(flightLeg)
        assertNotNull(flightLeg.packageOfferModel)
        assertNotNull(flightLeg.packageOfferModel.price)
        assertNotNull(flightLeg.packageOfferModel.urgencyMessage)
        assertNotNull(flightLeg.flightSegments)
        assertNotNull(flightLeg.airlines.size)

        assertEquals(flightLeg.packageOfferModel.price.packageTotalPrice, Money("2387.61", "USD"))
        assertEquals(flightLeg.packageOfferModel.price.tripSavings, Money("237.48", "USD"))
        assertEquals(flightLeg.packageOfferModel.price.differentialPriceFormatted, "$130")
        assertTrue(flightLeg.packageOfferModel.price.deltaPositive)
        assertEquals(flightLeg.packageOfferModel.price.pricePerPerson, Money("2387.61", "USD"))
        assertEquals(flightLeg.packageOfferModel.price.pricePerPersonFormatted, "$2,387.61")

        assertEquals(flightLeg.packageOfferModel.urgencyMessage.ticketsLeft, 7)

        assertEquals(flightLeg.carrierName, "American Airlines")
        assertEquals(flightLeg.arrivalDateTimeISO, "2017-09-12T07:05:00.000-07:00")
        assertEquals(flightLeg.carrierCode, "AA")
        assertEquals(flightLeg.departureDateTimeISO, "2017-09-12T05:00:00.000-07:00")
        assertEquals(flightLeg.durationHour, 5)
        assertEquals(flightLeg.durationMinute, 5)
        assertEquals(flightLeg.elapsedDays, 1)
        assertEquals(flightLeg.hasLayover, false)
        assertEquals(flightLeg.legId, "a13b0ab1eea2bd1d7f4cba76a857ec4b")
        assertEquals(flightLeg.departureLeg, "a13b0ab1eea2bd1d7f4cba76a857ec4b")
        assertEquals(flightLeg.stopCount, 0)

        assertEquals(flightLeg.airlines.size, 1)
        assertEquals(flightLeg.airlines[0].airlineName, "American Airlines")
        assertEquals(flightLeg.airlines[0].airlineLogoUrl, "https://images.trvl-media.com/media/content/expus/graphics/static_content/fusion/v0.1b/images/airlines/s/AA_sq.jpg")

        assertEquals(flightLeg.flightSegments.size, 1)
        assertEquals(flightLeg.flightSegments[0].airplaneType, "Boeing 737")
        assertEquals(flightLeg.flightSegments[0].flightNumber, "6962")
        assertEquals(flightLeg.flightSegments[0].carrier, "American Airlines")
        assertEquals(flightLeg.flightSegments[0].airlineCode, "AA")
        assertEquals(flightLeg.flightSegments[0].airlineLogoURL, "https://images.trvl-media.com/media/content/expus/graphics/static_content/fusion/v0.1b/images/airlines/s/AA_sq.jpg")
        assertEquals(flightLeg.flightSegments[0].departureCity, "San Francisco")
        assertEquals(flightLeg.flightSegments[0].departureAirportCode, "SFO")
        assertEquals(flightLeg.flightSegments[0].departureDateTimeISO, "2017-09-12T05:00:00.000-07:00")
        assertEquals(flightLeg.flightSegments[0].arrivalCity, "Seattle")
        assertEquals(flightLeg.flightSegments[0].arrivalAirportCode, "SEA")
        assertEquals(flightLeg.flightSegments[0].arrivalDateTimeISO, "2017-09-12T07:05:00.000-07:00")
        assertEquals(flightLeg.flightSegments[0].durationHours, 2)
        assertEquals(flightLeg.flightSegments[0].durationMinutes, 5)
        assertEquals(flightLeg.flightSegments[0].layoverDurationHours, 0)
        assertEquals(flightLeg.flightSegments[0].layoverDurationMinutes, 0)
        assertEquals(flightLeg.flightSegments[0].elapsedDays, 0)
    }

    @Test
    fun testConvertMultiItemFlightDeltaPriceNegative() {
        val flightLeg = FlightLeg.convertMultiItemFlightLeg("a13b0ab1eea2bd1d7f4cba76a857ec4b", dummyFlightOffer(), dummyMultiItemFlightLeg(), dummyMultiItemOffer(-130))
        assertEquals(flightLeg.packageOfferModel.price.differentialPriceFormatted, "-$130")
        assertFalse(flightLeg.packageOfferModel.price.deltaPositive)
    }

    private fun dummyFlightOffer(): FlightOffer {
        val multiItemFlightOffer = """
        {
          "bookingSeatCount": 1,
          "legIds": [
            "a13b0ab1eea2bd1d7f4cba76a857ec4b",
            "dd12e9e9032dc2f34fdb7562c96afb5a"
          ],
          "piid": "v5-9812853c4d5fb32e126f368e4305e8f2-3-2-1",
          "productTokens": [
            "AQogCh4IwYIBEgQ2OTYyGLJxIJIBKJDZdjCN2nY4T0AAWAEKIAoeCMGCARIENjg2OBiSASCycSj4q3cw96x3OE9AAFgBEgoIARABGAEqAkFBGAEiBAgBEAEoBCgDKAEoAjAC"
          ],
          "referenceBasePrice": {
            "amount": 91.16
          },
          "referenceTaxesAndFees": {
            "amount": 35.24
          },
          "referenceTotalPrice": {
            "amount": 126.40
          },
          "seatsLeft": 7,
          "splitTicket": false
        }
        """
        return Gson().fromJson(multiItemFlightOffer, FlightOffer::class.java)
    }

    private fun dummyMultiItemFlightLeg(): MultiItemFlightLeg {
        val multiItemFlightLeg = """
        {
          "segments": [
            {
              "airlineCode": "AA",
              "airlineLogoUrl": "/media/content/expus/graphics/static_content/fusion/v0.1b/images/airlines/AA.gif",
              "airlineName": "American Airlines",
              "airplaneType": {
                "code": "737",
                "description": "Boeing 737"
              },
              "arrivalAirportCode": "SEA",
              "arrivalCity": "Seattle",
              "arrivalDateTime": "2017-09-12T07:05:00.000-07:00",
              "bookingCode": " O",
              "cabinClass": "coach",
              "departureAirportCode": "SFO",
              "departureCity": "San Francisco",
              "departureDateTime": "2017-09-12T05:00:00.000-07:00",
              "distance": {
                "distance": 679.0,
                "unit": "mi"
              },
              "flightDuration": "PT2H5M",
              "flightNumber": "6962",
              "operatedByAirlineName": "Alaska Airlines",
              "operatingAirlineCode": "AS"
            }
          ],
          "stops": 0
        }
        """
        return Gson().fromJson(multiItemFlightLeg, MultiItemFlightLeg::class.java)
    }

    private fun dummyMultiItemOffer(deltaAvgPricePerPerson: Int): MultiItemOffer {
        val multiItemOffer = """
        {
          "cancellationPolicy": {
            "isFreeCancellationAvailable": false
          },
          "loyaltyInfo": {
            "earn": {
              "points": {
                "base": 4775,
                "bonus": 4775,
                "total": 9550
              }
            },
            "isBurnApplied": false
          },
          "packageDeal": {
            "markers": [
              {
                "magnitude": "0.0",
                "sticker": "FreeFlight"
              },
              {
                "magnitude": "0.0"
              }
            ],
            "rank": 0,
            "savingsAmount": 237.48,
            "savingsPercentage": 9.05
          },
          "packagedOffers": [
            {
              "productKey": "flight-0",
              "productType": "Air"
            }
          ],
          "price": {
            "basePrice": {
              "amount": 2048.08,
              "currency": "USD"
            },
            "referenceBasePrice": {
              "amount": 2240.56,
              "currency": "USD"
            },
            "referenceTaxesAndFees": {
              "amount": 384.53,
              "currency": "USD"
            },
            "referenceTotalPrice": {
              "amount": 2625.09,
              "currency": "USD"
            },
            "savings": {
              "amount": 237.48,
              "currency": "USD"
            },
            "taxesAndFees": {
              "amount": 339.53,
              "currency": "USD"
            },
            "totalPrice": {
              "amount": 2387.61,
              "currency": "USD"
            },
            "avgPricePerPerson": {
              "amount": 2387.61,
              "currency": "USD"
            },
            "avgReferencePricePerPerson": {
              "amount": 2625.09,
              "currency": "USD"
            },
            "deltaAvgPricePerPerson": {
              "amount": $deltaAvgPricePerPerson,
              "currency": "USD"
            }
          },
          "searchedOffer": {
            "productKey": "hotel-0",
            "productType": "Hotel"
          }
        }
        """
        return Gson().fromJson(multiItemOffer, MultiItemOffer::class.java)
    }
}