package com.expedia.vm.packages

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.util.getControlGuestRatingBackground
import com.expedia.util.getControlGuestRatingText
import com.expedia.vm.BaseHotelDetailViewModel
import rx.Observer
import java.util.Locale

class PackageHotelDetailViewModel(context: Context, roomSelectedObserver: Observer<HotelOffersResponse.HotelRoomResponse>) :
        BaseHotelDetailViewModel(context, roomSelectedObserver) {

    override fun showFeeType() : Boolean {
        return true
    }

    override fun getFeeTypeText() : Int {
        return if (PointOfSale.getPointOfSale().pointOfSaleId == PointOfSaleId.UNITED_STATES) R.string.rate_per_night else R.string.total_fee
    }

    override fun showFeesIncludedNotIncluded() : Boolean {
        return false
    }

    override fun getResortFeeText() : Int {
        return R.string.additional_fees_at_check_in
    }

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
}
