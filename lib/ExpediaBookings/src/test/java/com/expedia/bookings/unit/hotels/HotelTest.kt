package com.expedia.bookings.unit.hotels

import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelOffersResponse.convertMidHotelRoomResponse
import com.expedia.bookings.data.multiitem.HotelOffer
import com.expedia.bookings.data.multiitem.MultiItemOffer
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.utils.Constants
import com.google.gson.Gson
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class HotelTest {

    @Test
    fun testConvertMultiItemHotel() {
        val hotelOffer = dummyHotelOffer()
        val multiItemOffer = dummyMultiItemOffer()
        val hotel = Hotel.convertMultiItemHotel(hotelOffer, multiItemOffer)

        assertEquals(hotel.isPackage, true)
        assertEquals(hotel.hotelId, "3818880")
        assertEquals(hotel.localizedName, "The Cosmopolitan Of Las Vegas")
        assertEquals(hotel.address, "3708 Las Vegas Blvd S")
        assertEquals(hotel.city, "Las Vegas")
        assertEquals(hotel.stateProvinceCode, "NV")
        assertEquals(hotel.countryCode, "USA")
        assertEquals(hotel.postalCode, "89109")
        assertEquals(hotel.hotelStarRating, 5f)
        assertEquals(hotel.hotelGuestRating, 4.5f)
        assertEquals(hotel.locationDescription, "Set in the center of the Strip, this unique resort provides an enchanting destination amid the attractions of Las Vegas. The Cosmopolitan of Las Vegas is less than 5 miles from McCarran International Airport.")
        assertEquals(hotel.latitude, 36.109118)
        assertEquals(hotel.longitude, -115.173163)
        assertEquals(hotel.largeThumbnailUrl, "/hotels/4000000/3820000/3818900/3818880/3818880_243_t.jpg")
        assertEquals(hotel.thumbnailUrl, "/hotels/4000000/3820000/3818900/3818880/3818880_243_t.jpg")
        assertEquals(hotel.isVipAccess, false)

        assertNotNull(hotel.packageOfferModel)
        assertTrue(hotel.packageOfferModel.featuredDeal)
        assertNotNull(hotel.packageOfferModel.brandedDealData)
        assertEquals(hotel.packageOfferModel.brandedDealData.dealVariation, PackageOfferModel.DealVariation.FreeFlight)
        assertEquals(hotel.packageOfferModel.brandedDealData.savingsAmount, "899.39")
        assertEquals(hotel.packageOfferModel.brandedDealData.savingPercentageOverPackagePrice, "20.75")
        assertEquals(hotel.packageOfferModel.brandedDealData.freeNights, "0")

        assertNotNull(hotel.packageOfferModel.loyaltyInfo)
        assertEquals(hotel.packageOfferModel.loyaltyInfo.earn.points?.base, 6869)
        assertEquals(hotel.packageOfferModel.loyaltyInfo.earn.points?.bonus, 0)
        assertEquals(hotel.packageOfferModel.loyaltyInfo.earn.points?.total, 6869)

        assertNotNull(hotel.lowRateInfo)
        assertEquals(hotel.lowRateInfo.strikethroughPriceToShowUsers, 4333.87f)
        assertEquals(hotel.lowRateInfo.priceToShowUsers, 3434.48f)
        assertEquals(hotel.lowRateInfo.currencyCode, "USD")
    }

    @Test
    fun testConvertMidHotelOfferResonse() {
        val hotelOffer = dummyMidHotelRoomOffer()
        val multiItemOffer = dummyMultiItemRoomOffer()

        val room = convertMidHotelRoomResponse(hotelOffer, multiItemOffer)

        assertEquals(room.productKey, null)
        assertEquals(room.packageHotelDeltaPrice, multiItemOffer.price.deltaPricePerPerson())
        assertEquals(room.rateInfo.chargeableRateInfo.priceToShowUsers, multiItemOffer.price.deltaPricePerPerson()?.amount?.toFloat())
        assertEquals(room.rateInfo.chargeableRateInfo.currencyCode, multiItemOffer.price.deltaPricePerPerson()?.currency)
        assertEquals(room.rateInfo.chargeableRateInfo.strikethroughPriceToShowUsers, 0f)
        assertEquals(room.rateInfo.chargeableRateInfo.userPriceType, Constants.PACKAGE_HOTEL_DELTA_PRICE_TYPE)
        assertEquals(room.rateInfo.chargeableRateInfo.averageRate, 10f)
        assertEquals(room.rateInfo.chargeableRateInfo.taxStatusType, "None")
        assertEquals(room.rateInfo.chargeableRateInfo.surchargeTotal, 10f)
        assertEquals(room.rateInfo.chargeableRateInfo.surchargeTotalForEntireStay, 10f)
        assertEquals(room.rateInfo.chargeableRateInfo.discountPercent, multiItemOffer.price.savings.amount.toFloat())
        assertEquals(room.rateInfo.chargeableRateInfo.total, multiItemOffer.price.totalPrice.amount.toFloat())
        assertEquals(room.rateInfo.chargeableRateInfo.surchargesWithoutPropertyFeeForEntireStay, 10f)
        assertEquals(room.rateInfo.chargeableRateInfo.checkoutPriceType, "totalPriceWithMandatoryFees")
        assertEquals(room.rateInfo.chargeableRateInfo.airAttached, false)
        assertEquals(room.rateInfo.chargeableRateInfo.currencyCode, multiItemOffer.price.totalPrice.currency)
        assertEquals(room.rateInfo.chargeableRateInfo.showResortFeeMessage, true)
        assertEquals(room.rateInfo.chargeableRateInfo.resortFeeInclusion, true)
        assertEquals(room.rateInfo.chargeableRateInfo.packagePricePerPerson, multiItemOffer.price.pricePerPerson())
        assertEquals(room.rateInfo.chargeableRateInfo.packageSavings, multiItemOffer.price.packageSavings())
        assertEquals(room.rateInfo.chargeableRateInfo.packageTotalPrice, multiItemOffer.price.pricePerPerson())

        assertEquals(room.isPayLater, false)
        assertEquals(room.hasFreeCancellation, multiItemOffer.cancellationPolicy.freeCancellationAvailable)
        assertEquals(room.packageLoyaltyInformation, multiItemOffer.loyaltyInfo)
        assertEquals(room.currentAllotment, hotelOffer.roomsLeft.toString())
        assertEquals(room.isSameDayDRR, hotelOffer.sameDayDRR)
        assertEquals(room.isDiscountRestrictedToCurrentSourceType, hotelOffer.sourceTypeRestricted)
        assertEquals(room.isMemberDeal, hotelOffer.memberDeal)
        assertEquals(room.roomTypeCode, hotelOffer.roomTypeCode)
        assertEquals(room.roomLongDescription, hotelOffer.roomLongDescription)
        assertEquals(room.roomThumbnailUrl, hotelOffer.thumbnailUrl)
        assertEquals(room.roomTypeDescription, hotelOffer.roomRatePlanDescription)
        assertEquals(room.supplierType, hotelOffer.inventoryType);
        assertEquals(room.packageLoyaltyInformation, multiItemOffer.loyaltyInfo)

    }

    private fun dummyMultiItemOffer(): MultiItemOffer{
        val multiItemOfferJson = """
        {
            "price": {
                "basePrice": {
                    "amount": 2935.94,
                    "currency": "USD"
                },
                "taxesAndFees": {
                    "amount": 498.54,
                    "currency": "USD"
                },
                "totalPrice": {
                    "amount": 3434.48,
                    "currency": "USD"
                },
                "referenceBasePrice": {
                    "amount": 3737.68,
                    "currency": "USD"
                },
                "referenceTaxesAndFees": {
                    "amount": 596.19,
                    "currency": "USD"
                },
                "referenceTotalPrice": {
                    "amount": 4333.87,
                    "currency": "USD"
                },
                "savings": {
                    "amount": 899.39,
                    "currency": "USD"
                },
                "avgPricePerPerson": {
                  "amount": 3434.48,
                  "currency": "USD"
                },
                "avgReferencePricePerPerson": {
                  "amount": 4333.87,
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
            "packageDeal": {
                "markers": [
                    {
                        "sticker": "FreeFlight",
                        "magnitude": 0,
                        "content": {
                            "description": "Book this and &lt;strong&gt;save 100% on your flight&lt;/strong&gt;",
                            "title": "Unreal Deal",
                            "legal": "Over 271,000 hotels and 400 airlines compete for your business every day. When you book a flight and hotel together, we negotiate incredible deals and pass the savings on to you. The savings can be so great they cover the cost of your airfare."
                        },
                        "contentVariants": {}
                    },
                    {
                        "sticker": "FreeHotelNights",
                        "magnitude": 2,
                        "content": {
                            "description": "&lt;strong&gt;Get 2 nights free&lt;/strong&gt; by booking together.",
                            "title": "Unreal Deal",
                            "legal": "Over 271,000 hotels and 400 airlines compete for your business every day. When you book a flight and hotel together, we negotiate incredible deals and pass the savings on to you. The savings can be so great they cover the cost of 2 hotel nights."
                        },
                        "contentVariants": {}
                    },
                    {
                        "sticker": "FlightDeal",
                        "magnitude": 0,
                        "content": {
                            "description": "Book this and save $899 (20%)",
                            "title": "Unreal Deal",
                            "legal": ""
                        },
                        "contentVariants": {}
                    },
                    {
                        "sticker": "HotelSavingsFreeNights",
                        "magnitude": 2,
                        "content": {
                            "description": "&lt;strong&gt;Get 2 nights free&lt;/strong&gt; by booking together.",
                            "title": "Unreal Deal",
                            "legal": "Over 271,000 hotels and 400 airlines compete for your business every day. When you book a flight and hotel together, we negotiate incredible deals and pass the savings on to you. The savings can be so great they cover the cost of 2 hotel nights."
                        },
                        "contentVariants": {}
                    }
                ],
                "savingsPercentage": 20.75,
                "savingsAmount": 899.39,
                "rank": 0
            },
            "loyaltyInfo": {
                "earn": {
                    "points": {
                        "base": 6869,
                        "bonus": 0,
                        "total": 6869
                    }
                }
            },
            "detailsUrl": "/Details?action=UnifiedDetailsWidget@showDetailsForDeepLink&ptyp=multiitem&langid=1033&crom=1&cadt=R1:4&dcty=L1:SFO%7CL2:LAS&acty=L1:LAS%7CL2:SFO&ddte=L1:2017-10-28%7CL2:2017-11-06&destinationId=178276&hotelId=3818880&ratePlanCode=200356319&roomTypeCode=200062768&inventoryType=1&checkInDate=2017-10-28&checkOutDate=2017-11-06&tokens=AQohCh8I1rABEgQxOTE4GLJxIIuQASjt5Howw-V6OE5AAFgBEgoIARABGAEqAlZYGAQiBAgBEAQoBCgDKAEoAjAB,AQohCh8IwaYBEgQxOTE3GIuQASCycSj3y3sw2cx7OFJAAFgBEgoIARABGAEqAkFTGAQiBAgBEAQoBCgDKAEoAjAB&price=3434.48&ccyc=USD",
            "changeHotelUrl": "/changehotel?packageType=fh&langid=1033&originId=5195347&ftla=SFO&destinationId=178276&ttla=LAS&fromDate=2017-10-28&toDate=2017-11-06&numberOfRooms=1&adultsPerRoom%5B1%5D=4&flightPIID=v5-04e272aa4153ac371c3285b02f61dd80-0-1-st-v5-40391eee42e174a5a1c055aa7386db44-0-1&hotelPIID=3818880,200356319,200062768&adjustedCheckin=2017-10-28&hotelInventoryType=MERCHANT",
            "changeRoomUrl": "/hotel.h3818880.Hotel-Information?packageType=fh&langid=1033&originId=5195347&ftla=SFO&destinationId=178276&ttla=LAS&fromDate=2017-10-28&toDate=2017-11-06&numberOfRooms=1&adultsPerRoom%5B1%5D=4&action=changeRoom&hotelId=3818880&currentRatePlan=200062768200356319&flightPIID=v5-04e272aa4153ac371c3285b02f61dd80-0-1-st-v5-40391eee42e174a5a1c055aa7386db44-0-1&hotelPIID=3818880,200356319,200062768&adjustedCheckin=2017-10-28&hotelInventoryType=MERCHANT",
            "cancellationPolicy": {
                "freeCancellationAvailable": true
            }
        }
        """
        return Gson().fromJson(multiItemOfferJson, MultiItemOffer::class.java)
    }

    private fun dummyHotelOffer(): HotelOffer {
        val hotelOfferJson = """
        {
            "id": "3818880",
            "name": "The Cosmopolitan Of Las Vegas",
            "englishName": "The Cosmopolitan Of Las Vegas EnglishName",
            "shortDescription": "Set in the center of the Strip, this unique resort provides an enchanting destination amid the attractions of Las Vegas. The Cosmopolitan of Las Vegas is less than 5 miles from McCarran International Airport.",
            "regionId": 800045,
            "address": {
                "firstAddressLine": "3708 Las Vegas Blvd S",
                "secondAddressLine": "secondAddressLine",
                "city": "Las Vegas",
                "provinceCode": "NV",
                "threeLetterCountryCode": "USA",
                "postalCode": "89109",
                "latitude": 36.109118,
                "longitude": -115.173163
            },
            "starRating": 5,
            "averageReview": 4.4530926,
            "reviewCount": 9850,
            "recommendationPercentage": 88.314316,
            "thumbnailUrl": "/hotels/4000000/3820000/3818900/3818880/3818880_243_t.jpg",
            "roomRatePlanDescription": "City Room, 2 Queen Beds",
            "ratePlanCode": "200356319",
            "roomTypeCode": "200062768",
            "vip": false,
            "neighborhood": "Las Vegas Strip",
            "roomsLeft": 75,
            "referenceBasePrice": {
                "amount": 3440,
                "currency": "USD"
            },
            "referenceTaxesAndFees": {
                "amount": 460.27,
                "currency": "USD"
            },
            "referenceTotalPrice": {
                "amount": 3900.27,
                "currency": "USD"
            },
            "checkInDate": "2017-10-28",
            "checkOutDate": "2017-11-06",
            "nights": 9,
            "avgReferencePricePerNight": {
                "amount": 433.36,
                "currency": "USD"
            },
            "rateRuleId": 230441923,
            "promotion": {
                "description": "Book now and save (on select nights)",
                "amount": {
                    "amount": 140.67,
                    "currency": "USD"
                }
            },
            "inventoryType": "MERCHANT",
            "mandatoryFees": {
                "totalMandatoryFeesSupplyCurrency": {
                    "amount": 357.21,
                    "currency": "USD"
                },
                "dailyResortFeePOSCurrency": {
                    "amount": 39.69,
                    "currency": "USD"
                },
                "displayType": "DAILY"
            },
            "memberDeal": false,
            "sourceTypeRestricted": false,
            "sameDayDRR": false
        }
        """
        return Gson().fromJson(hotelOfferJson, HotelOffer::class.java)
    }

    private fun dummyMidHotelRoomOffer() : HotelOffer {
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

    private fun dummyMultiItemRoomOffer(): MultiItemOffer {
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
}
