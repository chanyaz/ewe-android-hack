package com.expedia.bookings.data

import org.joda.time.DateTime


class HotelItinDetailsResponse : AbstractItinDetailsResponse() {

    lateinit var responseData: HotelResponseData

    class HotelResponseData : ResponseData() {
        val hotels = emptyList<Hotels>()
    }

    class Hotels {
        var bookingStatus: String? = null
        var hotelId: String? = null
        lateinit var checkInDateTime: DateTime
        lateinit var checkOutDateTime: DateTime
        lateinit var inventoryType: String
        lateinit var totalPriceDetails: TotalPriceDetails
        lateinit var hotelPropertyInfo: HotelPropertyInfo
        lateinit var rooms: List<Rooms>
        lateinit var orderNumber: String

        class TotalPriceDetails {
            lateinit var primaryCurrencyCode: String
        }

        class HotelPropertyInfo {
            lateinit var name: String
            lateinit var photoThumbnailURL: String
            lateinit var address: Address

            class Address {
                lateinit var addressLine1: String
                lateinit var city: String
                lateinit var countrySubdivisionCode: String
                lateinit var postalCode: String
                lateinit var countryName: String
                lateinit var fullAddress: String
                lateinit var airportCode: String
            }
        }

        class Rooms {
            lateinit var roomPreferences: RoomPreferences

            class RoomPreferences {
                lateinit var primaryOccupant: PrimaryOccupant

                class PrimaryOccupant {
                    lateinit var email: String
                }
            }
        }
    }

    override fun getResponseDataForItin(): ResponseData? {
        return responseData
    }

}

