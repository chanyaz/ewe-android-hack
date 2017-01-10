package com.expedia.vm.packages

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.vm.BaseHotelDetailViewModel
import rx.Observer

class PackageHotelDetailViewModel(context: Context, roomSelectedObserver: Observer<HotelOffersResponse.HotelRoomResponse>) :
        BaseHotelDetailViewModel(context, roomSelectedObserver) {

    override fun pricePerDescriptor(): String {
        return " " + context.getString(R.string.per_person)
    }

    override fun getLobPriceObservable(rate: HotelRate) {
        priceToShowCustomerObservable.onNext(rate.packagePricePerPerson.getFormattedMoney(Money.F_NO_DECIMAL))
    }

    override fun showFeeType() : Boolean {
        return true
    }

    override fun showHotelFavorite(): Boolean {
        return false
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
