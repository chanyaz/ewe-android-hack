package com.expedia.vm.packages

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.packages.PackagesPageUsableData
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Strings
import com.expedia.vm.BaseHotelDetailViewModel
import com.squareup.phrase.Phrase
import org.joda.time.format.DateTimeFormat
import io.reactivex.subjects.BehaviorSubject
import java.math.BigDecimal

class PackageHotelDetailViewModel(context: Context) : BaseHotelDetailViewModel(context) {

    val bundlePricePerPersonObservable = BehaviorSubject.create<Money>()
    val bundleTotalPriceObservable = BehaviorSubject.create<Money>()
    val bundleSavingsObservable = BehaviorSubject.create<Money>()

    init {
        paramsSubject.subscribe { params ->
            val dtf = DateTimeFormat.forPattern("yyyy-MM-dd")
            val dates = Phrase.from(context, R.string.start_dash_end_date_range_TEMPLATE)
                    .put("startdate", LocaleBasedDateFormatUtils.localDateToMMMd(dtf.parseLocalDate(Db.getPackageResponse().getHotelCheckInDate())))
                    .put("enddate", LocaleBasedDateFormatUtils.localDateToMMMd(dtf.parseLocalDate(Db.getPackageResponse().getHotelCheckOutDate())))
                    .format().toString()
            searchInfoObservable.onNext(dates)
            searchDatesObservable.onNext(dates)
            searchInfoGuestsObservable.onNext(StrUtils.formatGuestString(context, params.guests))

            isCurrentLocationSearch = params.suggestion.isCurrentLocationSearch
        }
    }

    override fun offerReturned(offerResponse: HotelOffersResponse) {
        super.offerReturned(offerResponse)

        val firstHotelRoomResponse = offerResponse.hotelRoomResponse?.firstOrNull()
        if (firstHotelRoomResponse != null) {
            val rate = firstHotelRoomResponse.rateInfo.chargeableRateInfo
            if (rate.packagePricePerPerson != null && rate.packageTotalPrice != null && rate.packageSavings != null) {
                bundlePricePerPersonObservable.onNext(Money(BigDecimal(rate.packagePricePerPerson.amount.toDouble()), rate.packagePricePerPerson.currencyCode))
                bundleTotalPriceObservable.onNext(rate.packageTotalPrice)
                bundleSavingsObservable.onNext(rate.packageSavings)
            }
        }
    }

    override fun isChangeDatesEnabled(): Boolean {
        return false
    }

    override fun pricePerDescriptor(): String {
        return context.getString(R.string.price_per_person)
    }

    override fun getLobPriceObservable(rate: HotelRate) {
        priceToShowCustomerObservable.onNext(rate.packagePricePerPerson.getFormattedMoney(Money.F_NO_DECIMAL))
    }

    override fun showFeeType(): Boolean {
        return true
    }

    override fun getFeeTypeText(): Int {
        return if (PointOfSale.getPointOfSale().pointOfSaleId == PointOfSaleId.UNITED_STATES) R.string.rate_per_night else R.string.total_fee
    }

    override fun showFeesIncludedNotIncluded(): Boolean {
        return false
    }

    override fun getResortFeeText(): Int {
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

    override fun trackHotelDetailLoad(isRoomSoldOut: Boolean) {
        PackagesTracking().trackHotelDetailLoad(hotelOffersResponse.hotelId, PackagesPageUsableData.HOTEL_INFOSITE.pageUsableData)
    }

    override fun trackHotelDetailGalleryClick() {
        PackagesTracking().trackHotelDetailGalleryClick()
    }

    override fun trackHotelDetailRoomGalleryClick() {
        PackagesTracking().trackHotelDetailRoomGalleryClick()
    }

    override fun getHotelPriceContentDescription(showStrikeThrough: Boolean): String {
        return priceToShowCustomerObservable.value + context.getString(R.string.price_per_person)
    }

    override fun shouldShowBookByPhone(): Boolean {
        return if (PointOfSale.getPointOfSale().pointOfSaleId == PointOfSaleId.UNITED_STATES)
            !Strings.isEmpty(hotelOffersResponse.packageTelesalesNumber) else false
    }

    override fun getTelesalesNumber(): String {
        return hotelOffersResponse.packageTelesalesNumber
    }
}
