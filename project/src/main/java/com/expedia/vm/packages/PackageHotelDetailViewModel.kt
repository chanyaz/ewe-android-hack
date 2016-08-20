package com.expedia.vm.packages

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.util.getControlGuestRatingBackground
import com.expedia.util.getControlGuestRatingText
import com.expedia.vm.BaseHotelDetailViewModel
import rx.Observer

class PackageHotelDetailViewModel(context: Context, roomSelectedObserver: Observer<HotelOffersResponse.HotelRoomResponse>) :
        BaseHotelDetailViewModel(context, roomSelectedObserver) {

    override fun getLOB(): LineOfBusiness {
        return LineOfBusiness.PACKAGES
    }

    override fun trackHotelDetailMapViewClick() {
        PackagesTracking().trackHotelDetailMapViewClick()
    }

    override fun hasMemberDeal(roomOffer: HotelOffersResponse.HotelRoomResponse): Boolean {
        return false
    }

    override fun getGuestRatingRecommendedText(rating: Float, resources: Resources): String {
        return getControlGuestRatingText(rating, resources)
    }

    override fun getGuestRatingBackground(rating: Float, context: Context): Drawable {
        return getControlGuestRatingBackground(rating, context)
    }

    override fun trackHotelResortFeeInfoClick() {
        PackagesTracking().trackHotelResortFeeInfoClick()
    }

    override fun trackHotelRenovationInfoClick() {
        PackagesTracking().trackHotelRenovationInfoClick()
    }

    override fun trackHotelDetailBookPhoneClick() {
        PackagesTracking().trackHotelDetailBookPhoneClick()
    }

    override fun trackHotelDetailSelectRoomClick(isStickyButton: Boolean) {
        PackagesTracking().trackHotelDetailSelectRoomClick(isStickyButton)
    }

    override fun trackHotelViewBookClick() {
        PackagesTracking().trackHotelViewBookClick()
    }

    override fun trackHotelDetailLoad(hotelOffersResponse: HotelOffersResponse, hotelSearchParams: HotelSearchParams, hasEtpOffer: Boolean, currentLocationSearch: Boolean, hotelSoldOut: Boolean, isRoomSoldOut: Boolean) {
        PackagesTracking().trackHotelDetailLoad(hotelOffersResponse.hotelId)
    }

    override fun addViewsAfterTransition(){
        super.addViewsAfterTransition()
        showBookByPhoneObservable.onNext(false)
    }
}
