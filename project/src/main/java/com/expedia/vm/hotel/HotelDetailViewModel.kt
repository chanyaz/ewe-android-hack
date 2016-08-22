package com.expedia.vm.hotel

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.User
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.tracking.HotelTracking
import com.expedia.util.getABTestGuestRatingBackground
import com.expedia.util.getABTestGuestRatingText
import com.expedia.vm.BaseHotelDetailViewModel
import rx.Observer

open class HotelDetailViewModel(context: Context, roomSelectedObserver: Observer<HotelOffersResponse.HotelRoomResponse>) :
        BaseHotelDetailViewModel(context, roomSelectedObserver) {

    override fun trackHotelDetailLoad(hotelOffersResponse: HotelOffersResponse, hotelSearchParams: HotelSearchParams, hasEtpOffer: Boolean, currentLocationSearch: Boolean, hotelSoldOut: Boolean, isRoomSoldOut: Boolean) {
        HotelTracking().trackPageLoadHotelInfosite(hotelOffersResponse, hotelSearchParams, hasEtpOffer, currentLocationSearch, hotelSoldOut, isRoomSoldOut)
    }

    override fun getLOB(): LineOfBusiness {
        return LineOfBusiness.HOTELS
    }

    override fun hasMemberDeal(roomOffer: HotelOffersResponse.HotelRoomResponse): Boolean {
        val isUserBucketedForTest = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelsMemberDealTest)
        return roomOffer.isMemberDeal && isUserBucketedForTest && User.isLoggedIn(context)
    }

    override fun getGuestRatingRecommendedText(rating: Float, resources: Resources): String {
        return getABTestGuestRatingText(rating, context.resources)
    }

    override fun getGuestRatingBackground(rating: Float, context: Context): Drawable {
        return getABTestGuestRatingBackground(rating, context)
    }

    override fun trackHotelResortFeeInfoClick() {
        HotelTracking().trackHotelResortFeeInfo()
    }

    override fun trackHotelRenovationInfoClick() {
        HotelTracking().trackHotelRenovationInfo()
    }

    override fun trackHotelDetailBookPhoneClick() {
        HotelTracking().trackLinkHotelDetailBookPhoneClick()
    }

    override fun trackHotelDetailSelectRoomClick(isStickyButton: Boolean) {
        HotelTracking().trackLinkHotelDetailSelectRoom()
    }

    override fun trackHotelViewBookClick() {
        HotelTracking().trackLinkHotelViewRoomClick()
    }

    override fun trackHotelDetailMapViewClick() {
        HotelTracking().trackHotelDetailMapView()
    }
}
