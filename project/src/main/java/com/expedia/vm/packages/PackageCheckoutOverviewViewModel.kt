package com.expedia.vm.packages

import android.content.Context
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.HotelMedia
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.utils.Images
import com.expedia.vm.BaseCheckoutOverviewViewModel
import com.mobiata.android.util.AndroidUtils
import rx.subjects.PublishSubject

class PackageCheckoutOverviewViewModel(context: Context) : BaseCheckoutOverviewViewModel(context) {
    val width = AndroidUtils.getScreenSize(context).x / 2
    val tripResponseSubject = PublishSubject.create<PackageCreateTripResponse>()

    init {
        tripResponseSubject.subscribe { trip ->
            val hotel = trip.packageDetails.hotel
            val links = HotelMedia(Images.getMediaHost() + hotel.largeThumbnailUrl).getBestUrls(width)
            city.onNext(hotel.hotelCity)
            country.onNext(if (hotel.hotelStateProvince.isNullOrEmpty()) Db.getPackageParams().destination?.hierarchyInfo?.country?.name else hotel.hotelStateProvince)
            checkIn.onNext(hotel.checkInDate)
            checkOut.onNext(hotel.checkOutDate)
            guests.onNext(Db.getPackageParams().guests
            )
            url.onNext(links)
        }
    }
}