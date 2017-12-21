package com.expedia.bookings.test.robolectric

import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.multiitem.FlightOffer
import com.expedia.bookings.data.multiitem.HotelOffer
import com.expedia.bookings.data.multiitem.MultiItemApiSearchResponse
import com.expedia.bookings.data.multiitem.MultiItemError
import com.expedia.bookings.data.multiitem.MultiItemFlightLeg
import com.expedia.bookings.data.multiitem.MultiItemOffer
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.data.packages.PackageSearchParams
import com.google.gson.Gson
import org.joda.time.LocalDate

class PackageTestUtil {
    companion object {
        @JvmStatic
        fun getPackageSearchParams() : PackageSearchParams {
            return PackageSearchParams.Builder(maxRange = 1, maxStay = 1)
                    .startDate(LocalDate.now().plusDays(1))
                    .endDate(LocalDate.now().plusDays(2))
                    .destination(getSuggestion("San Francisco", "SFO"))
                    .origin(getSuggestion("Seattle", "SEA"))
                    .children(listOf(0))
                    .adults(1)
                    .build() as PackageSearchParams
        }

        @JvmStatic
        fun getSuggestion(cityName: String, airportCode: String) : SuggestionV4 {
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
        fun dummyMidHotelRoomOffer(): HotelOffer {
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
              "checkOutDate": "2017-09-08",
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
                "displayType": "NONE"
              },
              "memberDeal": true,
              "sourceTypeRestricted": false,
              "sameDayDRR": false
            }"""
            return Gson().fromJson(hotelRoomOfferJson, HotelOffer::class.java)
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
        fun setDbPackageSelectedHotel() {
            val hotel = Hotel()
            hotel.packageOfferModel = PackageOfferModel()
            hotel.packageOfferModel.piid = "123"
            hotel.hotelId = "999"
            val roomResponse = HotelOffersResponse.HotelRoomResponse()
            roomResponse.ratePlanCode = "test"
            roomResponse.roomTypeCode = "penthouse"
            Db.setPackageSelectedHotel(hotel, roomResponse)
        }
    }
}
