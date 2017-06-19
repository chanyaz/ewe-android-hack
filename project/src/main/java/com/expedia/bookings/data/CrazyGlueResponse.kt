package com.expedia.bookings.data

import android.content.Context
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.packages.PackageOfferModel

class CrazyGlueResponse () {


    fun getTheseHotels() : List<Hotel>{
        val hotel1 = getCrazyHotel("15237", "Luxor Hotel and Casino", 3.5f, 3.6f, 107f, 61f, "https://images.trvl-media.com//hotels/1000000/20000/15300/15237/15237_227_l.jpg")
        val hotel2 = getCrazyHotel("58201", "Monte Carlo Resort and Casino", 4.0f, 3.7f, 141f, 82f, "https://images.trvl-media.com//hotels/1000000/60000/58300/58201/58201_244_l.jpg")
        val hotel3 = getCrazyHotel("41308", "Excalibur Hotel Casino", 3.0f, 3.6f, 70f, 42f, "https://images.trvl-media.com//hotels/1000000/50000/41400/41308/41308_135_l.jpg")
        val hotel4 = Hotel()
        return listOf(hotel1, hotel2, hotel3, hotel4)
    }

    private fun getCrazyHotel(
            id: String,
            name: String,
            starRating: Float,
            guestRating: Float,
            standAlonePrice: Float,
            airAttachPrice: Float,
            hotelImage: String) : Hotel {
        val hotel = Hotel()
        hotel.hotelId = id
        hotel.localizedName = name
        hotel.hotelStarRating = starRating
        hotel.hotelGuestRating = guestRating
        hotel.packageOfferModel = PackageOfferModel()
        hotel.packageOfferModel.price = PackageOfferModel.PackagePrice()
        hotel.lowRateInfo = HotelRate()
        hotel.lowRateInfo.priceToShowUsers = airAttachPrice
        hotel.lowRateInfo.strikethroughPriceToShowUsers = standAlonePrice
        hotel.lowRateInfo.currencyCode = "USD"
        hotel.lowRateInfo.currencySymbol = "USD"
        hotel.thumbnailUrl = hotelImage
        hotel.isPackage = true
        return hotel
    }

}