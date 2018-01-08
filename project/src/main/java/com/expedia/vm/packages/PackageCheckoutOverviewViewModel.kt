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
            setPackageOverviewHeader(it.hotelCity, it.checkOutDate, it.checkinDate, it.largeThumbnailUrl)
        }
    }

    fun setPackageOverviewHeader(hotelCity: String, checkoutDate: String, checkinDate: String, hotelImageURL: String) {
        val links = HotelMedia(Images.getMediaHost() + hotelImageURL).getBestUrls(width)
        city.onNext(hotelCity)
        if (checkoutDate != null) {
            checkInAndCheckOutDate.onNext(Pair(checkinDate, checkoutDate))
        } else {
            checkInWithoutCheckoutDate.onNext(checkinDate)
        }
        guests.onNext(Db.sharedInstance.packageParams.guests)

        if (url.value != links) url.onNext(links)
    }
}

data class OverviewHeaderData(val hotelCity: String, val checkOutDate: String, val checkinDate: String, val largeThumbnailUrl: String) {

}
