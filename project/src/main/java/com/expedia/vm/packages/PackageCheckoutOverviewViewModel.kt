package com.expedia.vm.packages

import android.content.Context
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.HotelMedia
import com.expedia.bookings.utils.Images
import com.expedia.vm.BaseCheckoutOverviewViewModel
import com.mobiata.android.util.AndroidUtils
import rx.subjects.PublishSubject

class PackageCheckoutOverviewViewModel(context: Context) : BaseCheckoutOverviewViewModel(context) {
    val width = AndroidUtils.getScreenSize(context).x / 2
    val tripResponseSubject = PublishSubject.create<OverviewHeaderData>()

    init {
        tripResponseSubject.subscribe { it ->
            setPackageOverviewHeader(it.hotelCity, it.hotelCountry, it.hotelStateProvince, it.checkOutDate, it.checkinDate, it.largeThumbnailUrl)
        }
    }

    fun setPackageOverviewHeader(hotelCity: String, hotelCountry: String, hotelStateProvince: String, checkoutDate: String, checkinDate: String, hotelImageURL: String) {
        val links = HotelMedia(Images.getMediaHost() + hotelImageURL).getBestUrls(width)
        city.onNext(hotelCity)
        val shouldShowCountryName = !hotelCountry.equals("USA") || hotelStateProvince.isNullOrEmpty()
        country.onNext(if (shouldShowCountryName) Db.sharedInstance.packageParams.destination?.hierarchyInfo?.country?.name ?: "" else hotelStateProvince)
        if (checkoutDate != null) {
            checkInAndCheckOutDate.onNext(Pair(checkinDate, checkoutDate))
        } else {
            checkInWithoutCheckoutDate.onNext(checkinDate)
        }
        guests.onNext(Db.sharedInstance.packageParams.guests)

        if (url.value != links) url.onNext(links)
    }
}

data class OverviewHeaderData(val hotelCity: String, val hotelCountry: String, val hotelStateProvince: String, val checkOutDate: String,
                          val checkinDate: String, val largeThumbnailUrl: String) {

}