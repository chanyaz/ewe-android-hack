package com.expedia.vm.hotel

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.User
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.vm.BaseHotelDetailViewModel
import com.expedia.vm.HotelDetailToolbarViewModel
import com.squareup.phrase.Phrase
import org.joda.time.format.DateTimeFormat
import rx.Observer
import rx.subjects.BehaviorSubject
import java.math.BigDecimal

open class HotelDetailViewModel(context: Context) : BaseHotelDetailViewModel(context) {
    init {
        paramsSubject.subscribe { params ->
            searchInfoObservable.onNext(Phrase.from(context, R.string.calendar_instructions_date_range_with_guests_TEMPLATE).put("startdate",
                    DateUtils.localDateToMMMd(params.checkIn)).put("enddate",
                    DateUtils.localDateToMMMd(params.checkOut)).put("guests", StrUtils.formatGuestString(context, params.guests))
                    .format()
                    .toString())

            isCurrentLocationSearch = params.suggestion.isCurrentLocationSearch
        }
    }

    override fun pricePerDescriptor(): String {
        return context.getString(R.string.per_night)
    }

    override fun getLobPriceObservable(rate: HotelRate) {
        priceToShowCustomerObservable.onNext(Money(BigDecimal(rate.averageRate.toDouble()), rate.currencyCode).getFormattedMoney(Money.F_NO_DECIMAL))
    }

    override fun showFeeType() : Boolean {
        return false
    }

    override fun getFeeTypeText() : Int {
       return R.string.total_fee
    }

    override fun showHotelFavorite(): Boolean {
        return true
    }

    override fun showFeesIncludedNotIncluded() : Boolean {
        return true
    }

    override fun getResortFeeText() : Int {
        return R.string.hotel_fees_for_this_stay
    }

    override fun trackHotelDetailLoad(hotelOffersResponse: HotelOffersResponse, hotelSearchParams: HotelSearchParams, hasEtpOffer: Boolean, currentLocationSearch: Boolean, hotelSoldOut: Boolean, isRoomSoldOut: Boolean) {
        HotelTracking.trackPageLoadHotelInfosite(hotelOffersResponse, hotelSearchParams, hasEtpOffer, currentLocationSearch, hotelSoldOut, isRoomSoldOut)
    }

    override fun getLOB(): LineOfBusiness {
        return LineOfBusiness.HOTELS
    }

    override fun hasMemberDeal(roomOffer: HotelOffersResponse.HotelRoomResponse): Boolean {
        return roomOffer.isMemberDeal && User.isLoggedIn(context)
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
    companion object {
        @JvmStatic fun convertToToolbarViewModel(detailViewModel: BaseHotelDetailViewModel) : HotelDetailToolbarViewModel {
            val viewModel = HotelDetailToolbarViewModel(detailViewModel.context, detailViewModel.hotelNameObservable.value, detailViewModel.hotelRatingObservable.value, detailViewModel.showHotelFavorite(), detailViewModel.hotelSoldOut.value)
            return viewModel
        }
     }
}
