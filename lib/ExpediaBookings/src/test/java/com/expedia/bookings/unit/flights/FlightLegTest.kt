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

        assertEquals(flightLeg.carrierName, "United")
        assertEquals(flightLeg.arrivalDateTimeISO, "2017-12-14T07:35:00.000+01:00")
        assertEquals(flightLeg.carrierCode, "UA")
        assertEquals(flightLeg.departureDateTimeISO, "2017-12-13T09:10:00.000-08:00")
        assertEquals(flightLeg.durationHour, 13)
        assertEquals(flightLeg.durationMinute, 25)
        assertEquals(flightLeg.elapsedDays, 1)
        assertEquals(flightLeg.hasLayover, true)
        assertEquals(flightLeg.legId, "a13b0ab1eea2bd1d7f4cba76a857ec4b")
        assertEquals(flightLeg.departureLeg, "a13b0ab1eea2bd1d7f4cba76a857ec4b")
        assertEquals(flightLeg.stopCount, 1)

        assertEquals(flightLeg.airlines.size, 2)
        assertEquals(flightLeg.airlines[0].airlineName, "United")
        assertEquals(flightLeg.airlines[0].airlineLogoUrl, "https://images.trvl-media.com/media/content/expus/graphics/static_content/fusion/v0.1b/images/airlines/s/UA_sq.jpg")

        assertEquals(flightLeg.flightSegments.size, 2)
        assertEquals(flightLeg.flightSegments[0].airplaneType, "Boeing 757-200")
        assertEquals(flightLeg.flightSegments[0].flightNumber, "1075")
        assertEquals(flightLeg.flightSegments[0].carrier, "United")
        assertEquals(flightLeg.flightSegments[0].airlineCode, "UA")
        assertEquals(flightLeg.flightSegments[0].airlineLogoURL, "https://images.trvl-media.com/media/content/expus/graphics/static_content/fusion/v0.1b/images/airlines/s/UA_sq.jpg")
        assertEquals(flightLeg.flightSegments[0].departureCity, "Las Vegas")
        assertEquals(flightLeg.flightSegments[0].departureAirportCode, "LAS")
        assertEquals(flightLeg.flightSegments[0].departureDateTimeISO, "2017-12-13T09:10:00.000-08:00")
        assertEquals(flightLeg.flightSegments[0].arrivalCity, "Newark")
        assertEquals(flightLeg.flightSegments[0].arrivalAirportCode, "EWR")
        assertEquals(flightLeg.flightSegments[0].arrivalDateTimeISO, "2017-12-13T16:59:00.000-05:00")
        assertEquals(flightLeg.flightSegments[0].durationHours, 4)
        assertEquals(flightLeg.flightSegments[0].durationMinutes, 49)
        assertEquals(flightLeg.flightSegments[0].layoverDurationHours, 1)
        assertEquals(flightLeg.flightSegments[0].layoverDurationMinutes, 16)
        assertEquals(flightLeg.flightSegments[0].elapsedDays, 0)
    }

    @Test
    fun testConvertMultiItemFlightDeltaPriceNegative() {
        val flightLeg = FlightLeg.convertMultiItemFlightLeg("a13b0ab1eea2bd1d7f4cba76a857ec4b", dummyFlightOffer(), dummyMultiItemFlightLeg(), dummyMultiItemOffer(-130))
        assertEquals("-$130", flightLeg.packageOfferModel.price.differentialPriceFormatted)
        assertFalse(flightLeg.packageOfferModel.price.deltaPositive)
    }

    @Test
    fun testConvertMultiItemFlightDeltaPriceZero() {
        val flightLeg = FlightLeg.convertMultiItemFlightLeg("a13b0ab1eea2bd1d7f4cba76a857ec4b", dummyFlightOffer(), dummyMultiItemFlightLeg(), dummyMultiItemOffer(0))
        assertEquals("$0", flightLeg.packageOfferModel.price.differentialPriceFormatted)
        assertTrue(flightLeg.packageOfferModel.price.deltaPositive)
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
              "departureAirportCode": "LAS",
              "arrivalAirportCode": "EWR",
              "departureCity": "Las Vegas",
              "arrivalCity": "Newark",
              "departureDateTime": "2017-12-13T09:10:00.000-08:00",
              "arrivalDateTime": "2017-12-13T16:59:00.000-05:00",
              "flightNumber": "1075",
              "airlineCode": "UA",
              "bookingCode": " S",
              "airlineName": "United",
              "duration": {
                "minutes": 49,
                "hours": 4
              },
              "layoverDuration": {
                "minutes": 16,
                "hours": 1
              },
              "elapsedDays": 0,
              "distance": {
                "unit": "mi",
                "distance": 2225.0
              },
              "cabinClass": "coach",
              "airplaneType": {
                "code": "752",
                "description": "Boeing 757-200"
              },
              "airlineLogoUrl": "/media/content/expus/graphics/static_content/fusion/v0.1b/images/airlines/UA.gif"
            },
            {
              "departureAirportCode": "EWR",
              "arrivalAirportCode": "CDG",
              "departureCity": "Newark",
              "arrivalCity": "Paris",
              "departureDateTime": "2017-12-13T18:15:00.000-05:00",
              "arrivalDateTime": "2017-12-14T07:35:00.000+01:00",
              "flightNumber": "57",
              "airlineCode": "UA",
              "bookingCode": " S",
              "airlineName": "United",
              "duration": {
                "minutes": 20,
                "hours": 7
              },
              "elapsedDays": 1,
              "distance": {
                "unit": "mi",
                "distance": 3629.0
              },
              "cabinClass": "coach",
              "airplaneType": {
                "code": "764",
                "description": "Boeing 767"
              },
              "airlineLogoUrl": "/media/content/expus/graphics/static_content/fusion/v0.1b/images/airlines/UA.gif"
            }
          ],
          "stops": 1,
          "hasObFees": false,
          "baggageFeesUrl": "/Flights-BagFeesFilterByAC?originapt=LAS&destinationapt=CDG&cabinclass=coach&mktgcarrier=UA&bookingclass=+S&farebasis&opcarrier=UA&traveldate=2017-12-13&flightnumber=1075",
          "airlineCode": "UA",
          "airlineName": "United",
          "duration": {
            "minutes": 25,
            "hours": 13
          },
          "elapsedDays": 1
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
            "showSavings": false,
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