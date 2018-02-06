package com.expedia.bookings.test.robolectric

import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.flights.Airline
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.multiitem.FlightOffer
import com.expedia.bookings.data.multiitem.HotelOffer
import com.expedia.bookings.data.multiitem.MandatoryFees
import com.expedia.bookings.data.multiitem.MultiItemApiSearchResponse
import com.expedia.bookings.data.multiitem.MultiItemError
import com.expedia.bookings.data.multiitem.MultiItemFlightLeg
import com.expedia.bookings.data.multiitem.MultiItemOffer
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.data.packages.PackageSearchParams
import com.google.gson.Gson
import org.joda.time.LocalDate
import java.util.ArrayList

class PackageTestUtil {
    companion object {
        @JvmStatic
        fun getPackageSearchParams(startDate: LocalDate = LocalDate.now().plusDays(1),
                                   endDate: LocalDate = LocalDate.now().plusDays(2),
                                   destinationCityName: String = "San Francisco",
                                   childCount: List<Int> = listOf(0)): PackageSearchParams {
            return PackageSearchParams.Builder(maxRange = 1, maxStay = 1)
                    .startDate(startDate)
                    .endDate(endDate)
                    .destination(getSuggestion(destinationCityName, "SFO"))
                    .origin(getSuggestion("Seattle", "SEA"))
                    .children(childCount)
                    .adults(1)
                    .build() as PackageSearchParams
        }

        // Add more fields when needed
        @JvmStatic fun getPackageSelectedOutboundFlight(
                flightFareTypeString: String = "M",
                carrierCode: String = "000"): FlightLeg {
            val flight = FlightLeg()
            flight.flightFareTypeString = flightFareTypeString
            flight.carrierCode = carrierCode
            return flight
        }

        @JvmStatic fun getDummyPackageFlightLeg(): FlightLeg {
            val flightLeg = FlightLeg()
            flightLeg.elapsedDays = 1
            flightLeg.segments = arrayListOf()
            flightLeg.flightSegments = arrayListOf()
            flightLeg.durationHour = 19
            flightLeg.durationMinute = 10
            flightLeg.departureTimeShort = "1:10AM"
            flightLeg.arrivalTimeShort = "12:20PM"
            flightLeg.departureDateTimeISO = "2016-07-10T01:10:00.000-05:00"
            flightLeg.arrivalDateTimeISO = "2016-07-10T12:20:00.000-07:00"
            flightLeg.stopCount = 1
            flightLeg.packageOfferModel = PackageOfferModel()
            flightLeg.packageOfferModel.price = PackageOfferModel.PackagePrice()
            flightLeg.packageOfferModel.price.packageTotalPrice = Money("111", "USD")
            flightLeg.packageOfferModel.price.deltaPositive = true
            flightLeg.packageOfferModel.price.differentialPriceFormatted = "$11"
            flightLeg.packageOfferModel.price.pricePerPersonFormatted = "200.0"
            flightLeg.packageOfferModel.price.averageTotalPricePerTicket = Money("200.0", "USD")
            flightLeg.packageOfferModel.price.pricePerPerson = Money("200.0", "USD")
            flightLeg.baggageFeesUrl = ""

            val airlines = ArrayList<Airline>()
            val airline1 = Airline("United", null)
            val airline2 = Airline("Delta", null)
            airlines.add(airline1)
            airlines.add(airline2)
            flightLeg.airlines = airlines
            return flightLeg
        }

        @JvmStatic
        fun getSuggestion(cityName: String, airportCode: String): SuggestionV4 {
            val suggestion = SuggestionV4()
            suggestion.gaiaId = ""
            suggestion.regionNames = SuggestionV4.RegionNames()
            suggestion.regionNames.displayName = cityName
            suggestion.regionNames.fullName = ""
            suggestion.regionNames.shortName = ""
            suggestion.hierarchyInfo = SuggestionV4.HierarchyInfo()
            suggestion.hierarchyInfo!!.airport = SuggestionV4.Airport()
            suggestion.hierarchyInfo!!.airport!!.airportCode = airportCode
            suggestion.hierarchyInfo!!.airport!!.multicity = "happy"
            return suggestion
        }

        @JvmStatic
        fun dummyMidHotelRoomOffer(displayType: MandatoryFees.DisplayType = MandatoryFees.DisplayType.NONE,
                                   displayCurrency: MandatoryFees.DisplayCurrency = MandatoryFees.DisplayCurrency.POINT_OF_SALE): HotelOffer {
            val hotelRoomOfferJson = """
                {
              "thumbnailUrl": "/hotels/1000000/30000/26500/26432/26432_223_t.jpg",
              "roomRatePlanDescription": "Room, 1 Queen Bed, Non Smoking (Upgraded)",
              "roomLongDescription": " 1 Queen Bed  300 sq feet (28 sq meters)   Internet  - Free WiFi    Entertainment  - 37-inch flat-screen TV with satellite channels and pay movies  Food & Drink  - Refrigerator, microwave, and coffee/tea maker  Sleep  - Blackout drapes/curtains   Bathroom  - Private bathroom, bathtub or shower, free toiletries, and a hair dryer  Practical  - Laptop-compatible safe, iron/ironing board, and desk; rollaway/extra beds and free cribs/infant beds available on request  Comfort  - Air conditioning and daily housekeeping Non-Smoking Connecting/adjoining rooms can be requested, subject to availability  &nbsp;",
              "ratePlanCode": "208290304",
              "roomTypeCode": "201660950",
              "vip": false,
              "bedTypes": [
                {
                  "id": 6201,
                  "name": "1 queen bed\r"
                }
              ],
              "roomsLeft": 2,
              "referenceBasePrice": {
                "amount": 87.57,
                "currency": "USD"
              },
              "referenceTaxesAndFees": {
                "amount": 16.09,
                "currency": "USD"
              },
              "referenceTotalPrice": {
                "amount": 103.66,
                "currency": "USD"
              },
              "checkInDate": "2017-09-07",
              "checkOutDate": "2017-09-10",
              "nights": 1,
              "avgReferencePricePerNight": {
                "amount": 103.66,
                "currency": "USD"
              },
              "rateRuleId": 229100808,
              "promotion": {
                "description": "Member’s exclusive price",
                "amount": {
                  "amount": 22.36,
                  "currency": "USD"
                }
              },
              "inventoryType": "MERCHANT",
              "mandatoryFees": {
                "totalMandatoryFeesPOSCurrency": {
                  "amount": 158.62,
                  "currency": "GBP"
                },
                "totalMandatoryFeesSupplyCurrency": {
                  "amount": 221.1,
                  "currency": "USD"
                },
                "dailyResortFeePOSCurrency": {
                  "amount": 31.72,
                  "currency": "GBP"
                },
                "displayType": "$displayType",
                "displayCurrency": "$displayCurrency"
              },
              "memberDeal": true,
              "sourceTypeRestricted": false,
              "sameDayDRR": false
            }"""
            return Gson().fromJson(hotelRoomOfferJson, HotelOffer::class.java)
        }

        @JvmStatic
        fun dummyMIDItemRoomOffer(): MultiItemOffer {
            val hotelRoomMultiItemOfferJson = """
        {
          "price": {
            "basePrice": {
              "amount": 255.00,
              "currency": "USD"
            },
            "taxesAndFees": {
              "amount": 57.06,
              "currency": "USD"
            },
            "totalPrice": {
              "amount": 312.06,
              "currency": "USD"
            },
            "referenceBasePrice": {
              "amount": 255.00,
              "currency": "USD"
            },
            "referenceTaxesAndFees": {
              "amount": 57.06,
              "currency": "USD"
            },
            "referenceTotalPrice": {
              "amount": 312.06,
              "currency": "USD"
            },
            "savings": {
              "amount": 0.00,
              "currency": "USD"
            },
            "avgPricePerPerson": {
              "amount": 312.06,
              "currency": "USD"
            },
            "showSavings": false,
            "avgReferencePricePerPerson": {
              "amount": 312.06,
              "currency": "USD"
            },
            "deltaAvgPricePerPerson": {
              "amount": 0.00,
              "currency": "USD"
            }
          },
          "searchedOffer": {
            "productType": "Hotel",
            "productKey": "hotel-0"
          },
          "packagedOffers": [
            {
              "productType": "Air",
              "productKey": "flight-0"
            }
          ],
          "loyaltyInfo": {
            "earn": {
              "points": {
                "base": 624,
                "bonus": 624,
                "total": 1248
              }
            }
          },
          "detailsUrl": "/Details?action=UnifiedDetailsWidget@showDetailsForDeepLink&ptyp=multiitem&langid=1033&crom=1&cadt=R1:1&dcty=L1:LAS%7CL2:BWI&acty=L1:BWI%7CL2:LAS&ddte=L1:2017-09-06%7CL2:2017-09-08&destinationId=178235&hotelId=26432&ratePlanCode=208290304&roomTypeCode=201660950&inventoryType=1&checkInDate=2017-09-07&checkOutDate=2017-09-08&tokens=AQogCh4IzpYBEgM2OTYYi5ABIMFFKLuedjDNoHY4VUAAWAEKIAoeCM6WARIDNjk1GMFFIIuQASjHsXYwgrR2OFJAAFgBEgoIAhABGAIqAk5LGAEiBAgBEAEoBCgDKAEoAjAC&price=312.06&ccyc=USD",
          "changeHotelUrl": "/changehotel?packageType=fh&langid=1033&originId=6139100&ftla=LAS&destinationId=178235&ttla=BWI&fromDate=2017-09-06&toDate=2017-09-08&numberOfRooms=1&adultsPerRoom%5B1%5D=1&hotelIds=26432&flightPIID=v5-a696232f4ee05b20474d5d87f967586c-0-0-2&hotelPIID=26432,208290304,201660950&adjustedCheckin=2017-09-07&hotelInventoryType=MERCHANT",
          "changeRoomUrl": "/hotel.h26432.Hotel-Information?packageType=fh&langid=1033&originId=6139100&ftla=LAS&destinationId=178235&ttla=BWI&fromDate=2017-09-06&toDate=2017-09-08&numberOfRooms=1&adultsPerRoom%5B1%5D=1&hotelIds=26432&action=changeRoom&hotelId=26432&currentRatePlan=201660950208290304&flightPIID=v5-a696232f4ee05b20474d5d87f967586c-0-0-2&hotelPIID=26432,208290304,201660950&adjustedCheckin=2017-09-07&hotelInventoryType=MERCHANT",
          "cancellationPolicy": {
            "freeCancellationAvailable": false
          }
        }
        """
            return Gson().fromJson(hotelRoomMultiItemOfferJson, MultiItemOffer::class.java)
        }

        @JvmStatic
        fun getMockMIDResponse(offers: List<MultiItemOffer> = emptyList(),
                               hotels: Map<String, HotelOffer> = emptyMap(),
                               flights: Map<String, FlightOffer> = emptyMap(),
                               flightLegs: Map<String, MultiItemFlightLeg> = emptyMap(),
                               errors: List<MultiItemError>? = null): MultiItemApiSearchResponse {
            return MultiItemApiSearchResponse(offers = offers, hotels = hotels, flights = flights, flightLegs = flightLegs, errors = errors)
        }

        @JvmStatic
        fun setDbPackageSelectedHotel(displayType: MandatoryFees.DisplayType = MandatoryFees.DisplayType.NONE,
                                      displayCurrency: MandatoryFees.DisplayCurrency = MandatoryFees.DisplayCurrency.POINT_OF_SALE) {
            val hotel = Hotel()
            hotel.packageOfferModel = PackageOfferModel()
            hotel.city = "Detroit"
            hotel.countryCode = "USA"
            hotel.stateProvinceCode = "MI"
            hotel.largeThumbnailUrl = "https://"
            hotel.packageOfferModel = PackageOfferModel()
            hotel.packageOfferModel.piid = "123"
            hotel.hotelId = "999"
            hotel.city = "Detroit"
            hotel.countryCode = "USA"
            hotel.stateProvinceCode = "MI"
            hotel.largeThumbnailUrl = "https://"
            val roomResponse = HotelOffersResponse.HotelRoomResponse()
            roomResponse.supplierType = "MERCHANT"
            roomResponse.ratePlanCode = "test"
            roomResponse.roomTypeCode = "penthouse"
            roomResponse.rateInfo = setRateInfo(displayType, displayCurrency)
            Db.setPackageSelectedHotel(hotel, roomResponse)
        }

        @JvmStatic
        fun getMIDPackageSearchParams(): PackageSearchParams {
            val packageParams = getPackageSearchParams(LocalDate.parse("2017-12-07"), LocalDate.parse("2017-12-08"))
            packageParams.hotelId = "1111"
            packageParams.latestSelectedFlightPIID = "mid_create_trip"
            packageParams.inventoryType = "AA"
            packageParams.ratePlanCode = "AAA"
            packageParams.roomTypeCode = "AA"
            packageParams.latestSelectedProductOfferPrice = PackageOfferModel.PackagePrice()
            packageParams.latestSelectedProductOfferPrice?.packageTotalPrice = Money(100, "USD")
            return packageParams
        }

        @JvmStatic
        fun getCreateTripResponse(tripId: String = "00000", currency: String = "USD", bundleTotal: Int = 0, packageTotal: Int = 0,
                                  hotelLargeThumbnailUrl: String = "", hotelCity: String = "", hotelStateProvince: String = "",
                                  hotelCountry: String = "", hotelCheckinDate: String = "", hotelCheckoutOutDate: String = "",
                                  hotelNumberOfNights: String = ""): PackageCreateTripResponse {
            val trip = PackageCreateTripResponse()
            val packageDetails = PackageCreateTripResponse.PackageDetails()
            packageDetails.tripId = tripId
            packageDetails.pricing = PackageCreateTripResponse.Pricing()
            packageDetails.pricing.bundleTotal = Money(bundleTotal, currency)
            packageDetails.pricing.packageTotal = Money(packageTotal, currency)
            val hotel = HotelCreateTripResponse.HotelProductResponse()
            hotel.largeThumbnailUrl = hotelLargeThumbnailUrl
            hotel.hotelCity = hotelCity
            hotel.hotelStateProvince = hotelStateProvince
            hotel.hotelCountry = hotelCountry
            hotel.checkInDate = hotelCheckinDate
            hotel.checkOutDate = hotelCheckoutOutDate
            hotel.numberOfNights = hotelNumberOfNights
            hotel.hotelRoomResponse = HotelOffersResponse.HotelRoomResponse()
            hotel.hotelRoomResponse.bedTypes = ArrayList<HotelOffersResponse.BedTypes>()

            trip.packageDetails = packageDetails
            packageDetails.hotel = hotel

            return trip
        }

        private fun setRateInfo(displayType: MandatoryFees.DisplayType = MandatoryFees.DisplayType.NONE,
                                displayCurrency: MandatoryFees.DisplayCurrency = MandatoryFees.DisplayCurrency.POINT_OF_SALE): HotelOffersResponse.RateInfo {
            val rateInfo = HotelOffersResponse.RateInfo()
            rateInfo.chargeableRateInfo = HotelRate()
            rateInfo.chargeableRateInfo.showResortFeeMessage = true
            rateInfo.chargeableRateInfo.mandatoryDisplayType = displayType
            rateInfo.chargeableRateInfo.mandatoryDisplayCurrency = displayCurrency
            rateInfo.chargeableRateInfo.totalMandatoryFees = 50F
            return rateInfo
        }
    }
}
