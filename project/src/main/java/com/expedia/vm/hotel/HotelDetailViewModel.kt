package com.expedia.vm.hotel

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.bookings.utils.Strings
import com.expedia.vm.BaseHotelDetailPriceViewModel
import com.expedia.vm.BaseHotelDetailViewModel
import com.expedia.vm.HotelDetailPriceViewModel
import com.expedia.vm.HotelDetailToolbarViewModel

open class HotelDetailViewModel(context: Context) : BaseHotelDetailViewModel(context) {
    private var swpEnabled = false

    init {
        paramsSubject.subscribe { params ->
            isCurrentLocationSearch = params.suggestion.isCurrentLocationSearch
            swpEnabled = params.shopWithPoints
        }
    }

    override fun showFeeType(): Boolean {
        return false
    }

    override fun getFeeTypeText(): Int {
        return R.string.total_fee
    }

    override fun showFeesIncludedNotIncluded(): Boolean {
        return true
    }

    override fun getResortFeeText(): Int {
        return R.string.hotel_fees_for_this_stay
    }

    override fun trackHotelDetailLoad(isRoomSoldOut: Boolean) {
        HotelTracking.trackPageLoadHotelInfosite(hotelOffersResponse, paramsSubject.value, hasEtpOffer(hotelOffersResponse),
                isCurrentLocationSearch, hotelSoldOut.value, isRoomSoldOut, loadTimeData, swpEnabled)
    }

    override fun getLOB(): LineOfBusiness {
        return LineOfBusiness.HOTELS
    }

    override fun hasMemberDeal(roomOffer: HotelOffersResponse.HotelRoomResponse): Boolean {
        return roomOffer.isMemberDeal && userStateManager.isUserAuthenticated()
    }

    override fun trackHotelResortFeeInfoClick() {
        HotelTracking.trackHotelResortFeeInfo()
    }

    override fun trackHotelRenovationInfoClick() {
        HotelTracking.trackHotelRenovationInfo()
    }

    override fun trackHotelDetailBookPhoneClick() {
        HotelTracking.trackLinkHotelDetailBookPhoneClick()
    }

    override fun trackHotelDetailSelectRoomClick(isStickyButton: Boolean) {
        HotelTracking.trackLinkHotelDetailSelectRoom()
    }

    override fun trackHotelViewBookClick() {
        HotelTracking.trackLinkHotelViewRoomClick()
    }

    override fun trackHotelDetailMapViewClick() {
        HotelTracking.trackHotelDetailMapView()
    }

    override fun trackHotelDetailGalleryClick() {
        HotelTracking.trackHotelDetailGalleryClick()
    }

    override fun shouldShowBookByPhone(): Boolean {
        return !hotelOffersResponse.deskTopOverrideNumber
                && !Strings.isEmpty(hotelOffersResponse.telesalesNumber)
    }

    override fun getTelesalesNumber(): String {
        return hotelOffersResponse.telesalesNumber
    }

    override fun getHotelDetailPriceViewModel(): BaseHotelDetailPriceViewModel {
        return HotelDetailPriceViewModel(context)
    }

    companion object {
        @JvmStatic fun convertToToolbarViewModel(detailViewModel: BaseHotelDetailViewModel): HotelDetailToolbarViewModel {
            val viewModel = HotelDetailToolbarViewModel(detailViewModel.context, detailViewModel.hotelNameObservable.value, detailViewModel.hotelRatingObservable.value, detailViewModel.hotelSoldOut.value)
            return viewModel
        }
    }
}
