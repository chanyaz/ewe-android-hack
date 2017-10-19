package com.expedia.vm.hotel

import android.content.Context
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.dialog.DialogFactory
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.hotel.util.HotelCalendarRules
import com.expedia.bookings.hotel.util.HotelInfoManager
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Strings
import com.expedia.vm.BaseHotelDetailViewModel
import com.expedia.vm.HotelDetailToolbarViewModel
import com.squareup.phrase.Phrase
import org.joda.time.LocalDate
import rx.subjects.PublishSubject
import rx.subscriptions.CompositeSubscription
import java.math.BigDecimal

open class HotelDetailViewModel(context: Context,
                                private val hotelInfoManager: HotelInfoManager) : BaseHotelDetailViewModel(context) {
    val fetchInProgressSubject = PublishSubject.create<Unit>()
    val fetchCancelledSubject = PublishSubject.create<Unit>()

    private var swpEnabled = false
    private var cachedParams: HotelSearchParams? = null
    private var apiSubscriptions = CompositeSubscription()

    init {
        paramsSubject.subscribe { params ->
            cachedParams = params
            searchInfoObservable.onNext(Phrase.from(context, R.string.calendar_instructions_date_range_TEMPLATE).put("startdate",
                    LocaleBasedDateFormatUtils.localDateToMMMd(params.checkIn)).put("enddate",
                    LocaleBasedDateFormatUtils.localDateToMMMd(params.checkOut))
                    .format()
                    .toString())

            searchInfoGuestsObservable.onNext(StrUtils.formatGuestString(context, params.guests))

            isCurrentLocationSearch = params.suggestion.isCurrentLocationSearch
            swpEnabled = params.shopWithPoints
        }
        searchInfoTextColorObservable.onNext(getSearchInfoTextColor())
        apiSubscriptions.add(hotelInfoManager.offerSuccessSubject.subscribe(hotelOffersSubject))
        apiSubscriptions.add(hotelInfoManager.infoSuccessSubject.subscribe(hotelOffersSubject))
    }

    fun fetchOffers(params: HotelSearchParams, hotelId: String) {
        this.hotelId = hotelId
        paramsSubject.onNext(params)

        fetchInProgressSubject.onNext(Unit)

        apiSubscriptions.add(hotelInfoManager.offersNoInternetSubject.subscribe {
            handleNoInternet(retryFun = {
                fetchOffers(params, hotelId)
            })
        })

        apiSubscriptions.add(hotelInfoManager.infoNoInternetSubject.subscribe {
            handleNoInternet(retryFun = {
                hotelInfoManager.fetchInfo(params, hotelId)
            })
        })

        apiSubscriptions.add(hotelInfoManager.soldOutSubject.subscribe {
            hotelInfoManager.fetchInfo(params, hotelId)
        })

        hotelInfoManager.fetchOffers(params, hotelId)
    }

    fun changeDates(newStartDate: LocalDate, newEndDate: LocalDate) {
        cachedParams?.let {
            val rules = HotelCalendarRules(context)
            val builder = HotelSearchParams.Builder(rules.getMaxDateRange(), rules.getMaxSearchDurationDays())
            builder.from(cachedParams!!).startDate(newStartDate).endDate(newEndDate)
            val params = builder.build()

            fetchOffers(params, hotelId)
        }
    }

    fun clearSubscriptions() {
        apiSubscriptions.clear()
    }

    override fun isChangeDatesEnabled(): Boolean {
        return FeatureToggleUtil.isFeatureEnabled(context, R.string.preference_dateless_infosite)
    }

    override fun pricePerDescriptor(): String {
        val firstHotelRoomResponse = hotelOffersResponse.hotelRoomResponse?.firstOrNull()
        val bucketedToShowPriceDescriptorProminence = AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelPriceDescriptorProminence)
        if (firstHotelRoomResponse != null && bucketedToShowPriceDescriptorProminence) {
            val priceType = firstHotelRoomResponse?.rateInfo?.chargeableRateInfo?.getUserPriceType()
            return when (priceType) {
                HotelRate.UserPriceType.RATE_FOR_WHOLE_STAY_WITH_TAXES -> context.getString(R.string.total_stay)
                else -> context.getString(R.string.per_night)
            }
        }
        return context.getString(R.string.per_night)
    }

    override fun getLobPriceObservable(rate: HotelRate) {
        priceToShowCustomerObservable.onNext(Money(BigDecimal(rate.averageRate.toDouble()), rate.currencyCode).getFormattedMoney(Money.F_NO_DECIMAL))
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

    companion object {
        @JvmStatic
        fun convertToToolbarViewModel(detailViewModel: BaseHotelDetailViewModel): HotelDetailToolbarViewModel {
            val viewModel = HotelDetailToolbarViewModel(detailViewModel.context, detailViewModel.hotelNameObservable.value, detailViewModel.hotelRatingObservable.value, detailViewModel.hotelSoldOut.value)
            return viewModel
        }
    }

    private fun getSearchInfoTextColor(): Int {
        if (isChangeDatesEnabled()) {
            return ContextCompat.getColor(context, R.color.hotel_search_info_selectable_color)
        }
        return ContextCompat.getColor(context, R.color.hotel_search_info_color)
    }

    private fun handleNoInternet(retryFun: () -> Unit) {
        val cancelFun = fun() {
            fetchCancelledSubject.onNext(Unit)
        }
        DialogFactory.showNoInternetRetryDialog(context, retryFun, cancelFun)
    }

}
