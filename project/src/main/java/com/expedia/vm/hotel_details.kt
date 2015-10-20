package com.expedia.vm

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.Html
import com.expedia.bookings.R
import com.expedia.bookings.data.HotelMedia
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.User
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.extension.isAirAttached
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.tracking.HotelV2Tracking
import com.expedia.bookings.utils.Amenity
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.Images
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.widget.RecyclerGallery
import com.expedia.bookings.widget.priceFormatter
import com.expedia.util.endlessObserver
import com.mobiata.android.FormatUtils
import com.mobiata.android.SocialUtils
import com.squareup.phrase.Phrase
import rx.Observable
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.math.BigDecimal
import java.util.*
import kotlin.properties.Delegates

class HotelDetailViewModel(val context: Context, val hotelServices: HotelServices, val roomSelectedObserver: Observer<HotelOffersResponse.HotelRoomResponse>) : RecyclerGallery.GalleryItemListener, RecyclerGallery.GalleryItemScrollListener {

    override fun onGalleryItemClicked(item: Any) {
        galleryClickedSubject.onNext(Unit)
    }

    override fun onGalleryItemScrolled(position: Int){
        if(hotelOffersResponse.photos.get(position).displayText !=null )
            galleryItemChangeObservable.onNext(Pair(position,hotelOffersResponse.photos.get(position).displayText))
        else
            galleryItemChangeObservable.onNext(Pair(position,""))

    }

    val hotelOffersSubject = BehaviorSubject.create<HotelOffersResponse>()
    var hotelOffersResponse: HotelOffersResponse by Delegates.notNull()
    var etpOffersList = ArrayList<HotelOffersResponse.HotelRoomResponse>()
    var sectionBody: String by Delegates.notNull()
    var commonList = ArrayList<String>()

    var isSectionExpanded = false
    val sectionBodyObservable = BehaviorSubject.create<String>()
    val sectionImageObservable = BehaviorSubject.create<Boolean>()
    val showBookByPhoneObservable = BehaviorSubject.create<Boolean>()
    val galleryObservable = BehaviorSubject.create<List<HotelMedia>>()

    val commonAmenityTextObservable = BehaviorSubject.create<String>()

    val amenitiesListObservable = BehaviorSubject.create<List<Amenity>>()
    val noAmenityObservable = BehaviorSubject.create<Unit>()

    var hasETPObservable = BehaviorSubject.create<Boolean>()
    var hasFreeCancellationObservable = BehaviorSubject.create<Boolean>()
    var hasBestPriceGuaranteeObservable = BehaviorSubject.create<Boolean>()
    var renovationObservable = BehaviorSubject.create<Unit>()
    val hotelRenovationObservable = BehaviorSubject.create<Pair<String, String>>()
    val hotelPayLaterInfoObservable = BehaviorSubject.create<String>()

    var propertyInfoListObservable = BehaviorSubject.create<List<HotelOffersResponse.HotelText>>(emptyList())

    var roomResponseListObservable = BehaviorSubject.create<Pair<List<HotelOffersResponse.HotelRoomResponse>, List<String>>>()
    var uniqueValueAddForRooms: ArrayList<String> by Delegates.notNull()

    var etpUniqueValueAddForRooms: ArrayList<String> by Delegates.notNull()
    var etpRoomResponseListObservable = BehaviorSubject.create<Pair<List<HotelOffersResponse.HotelRoomResponse>, List<String>>>()

    val lastExpandedRowObservable = BehaviorSubject.create<Int>(0)
    val rowExpandingObservable = BehaviorSubject.create<Int>()
    val hotelRoomRateViewModelsObservable = BehaviorSubject.create<List<com.expedia.vm.HotelRoomRateViewModel>>()

    val hotelResortFeeObservable = BehaviorSubject.create<String>(null)
    val hotelNameObservable = BehaviorSubject.create<String>()
    val hotelRatingObservable = BehaviorSubject.create<Float>()
    val hotelRatingObservableVisibility = BehaviorSubject.create<Boolean>()
    val onlyShowTotalPrice = BehaviorSubject.create<Boolean>(false)
    val roomPriceToShowCustomer = BehaviorSubject.create<String>()
    val totalPriceObservable = BehaviorSubject.create<String>()
    val pricePerNightObservable = BehaviorSubject.create<String>()
    val searchInfoObservable = BehaviorSubject.create<String>()
    val userRatingObservable = BehaviorSubject.create<String>()
    val isUserRatingAvailableObservable = BehaviorSubject.create<Boolean>()
    val numberOfReviewsObservable = BehaviorSubject.create<String>()
    val hotelLatLngObservable = BehaviorSubject.create<DoubleArray>()
    val discountPercentageBackgroundObservable = BehaviorSubject.create<Int>()
    val discountPercentageObservable = BehaviorSubject.create<String>()
    val hasDiscountPercentageObservable = BehaviorSubject.create<Boolean>()
    val hasVipAccessObservable = BehaviorSubject.create<Boolean>()
    val promoMessageObservable = BehaviorSubject.create<String>()
    val strikeThroughPriceObservable = BehaviorSubject.create<CharSequence>()
    val galleryItemChangeObservable = BehaviorSubject.create<Pair<Int, String>>()
    var isCurrentLocationSearch = false

    public fun addViewsAfterTransition() {

        uniqueValueAddForRooms = getValueAdd(hotelOffersResponse.hotelRoomResponse)
        roomResponseListObservable.onNext(Pair(hotelOffersResponse.hotelRoomResponse, uniqueValueAddForRooms))

        var listHotelInfo = ArrayList<HotelOffersResponse.HotelText>()

        //Set up entire text for hotel info
        if (hotelOffersResponse.hotelOverviewText != null && hotelOffersResponse.hotelOverviewText.size() > 1 ) {
            for (index in 1..hotelOffersResponse.hotelOverviewText.size() - 1) {
                listHotelInfo.add(hotelOffersResponse.hotelOverviewText.get(index))
            }
        }

        if (hotelOffersResponse.hotelAmenitiesText != null) {
            listHotelInfo.add(hotelOffersResponse.hotelAmenitiesText)
        }

        if (hotelOffersResponse.hotelPoliciesText != null) {
            listHotelInfo.add(hotelOffersResponse.hotelPoliciesText)
        }

        if (hotelOffersResponse.hotelFeesText != null) {
            listHotelInfo.add(hotelOffersResponse.hotelFeesText)
        }

        if (hotelOffersResponse.hotelMandatoryFeesText != null) {
            listHotelInfo.add(hotelOffersResponse.hotelMandatoryFeesText)
        }
        propertyInfoListObservable.onNext(listHotelInfo)

        if (hotelOffersResponse.hotelRenovationText?.content != null) renovationObservable.onNext(Unit)

        // common amenities text
        if (hotelOffersResponse.hotelRoomResponse.size() > 0) {
            val atLeastOneRoomHasNoValueAdds = hotelOffersResponse.hotelRoomResponse.any { it.valueAdds == null }
            if (!atLeastOneRoomHasNoValueAdds) {
                val allValueAdds: List<List<String>> = hotelOffersResponse.hotelRoomResponse
                        .filter { it.valueAdds != null }
                        .map {
                            it.valueAdds.map { it.description }
                        }

                if (!allValueAdds.isEmpty()) {
                    val commonValueAdds: List<String> = allValueAdds
                            .drop(1)
                            .fold(allValueAdds.first().toArrayList(), { initial, nextValueAdds ->
                                initial.retainAll(nextValueAdds)
                                initial
                            })

                    if (!commonValueAdds.isEmpty()) {
                        val commonValueAddsString = context.getString(R.string.common_value_add_template, FormatUtils.series(context, commonValueAdds, ",", FormatUtils.Conjunction.AND)
                                .toLowerCase(Locale.getDefault()))

                        commonAmenityTextObservable.onNext(commonValueAddsString)
                    }
                }
            }
        }

        val hasETPOffer = hasEtpOffer(hotelOffersResponse)
        if (hasETPOffer) {
            etpOffersList = hotelOffersResponse.hotelRoomResponse
                    .filter { it.payLaterOffer != null }.toArrayList()

            etpUniqueValueAddForRooms = getValueAdd(etpOffersList)
        }

        if (hotelOffersResponse.hotelRoomResponse.get(0).rateInfo.chargeableRateInfo.showResortFeeMessage) {
            val rate = hotelOffersResponse.hotelRoomResponse.get(0).rateInfo.chargeableRateInfo
            val hotelResortFee = Money(BigDecimal(rate.totalMandatoryFees.toDouble()), rate.currencyCode)
            hotelResortFeeObservable.onNext(hotelResortFee.getFormattedMoney(Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL))
        }

        showBookByPhoneObservable.onNext(isAvailable() && (hotelOffersResponse.deskTopOverrideNumber != null && !hotelOffersResponse.deskTopOverrideNumber)
                && !Strings.isEmpty(hotelOffersResponse.telesalesNumber))

        HotelV2Tracking().trackPageLoadHotelV2Infosite(hotelOffersResponse, paramsSubject.value, hasETPOffer, isCurrentLocationSearch)

    }

    private val offersObserver = endlessObserver<HotelOffersResponse> { response ->
        hotelOffersResponse = response
        galleryObservable.onNext(Images.getHotelImages(hotelOffersResponse))
        val amenityList = arrayListOf<Amenity>()
        if (response.hotelAmenities != null) {
            amenityList.addAll(Amenity.amenitiesToShow(response.hotelAmenities))
        }

        if (amenityList.isEmpty()) {
            noAmenityObservable.onNext(Unit)
        } else {
            //Here have to pass the list of amenities which we want to show
            amenitiesListObservable.onNext(amenityList)
        }

        hotelNameObservable.onNext(response.hotelName)

        hotelRatingObservable.onNext(response.hotelStarRating.toFloat())
        hotelRatingObservableVisibility.onNext(response.hotelStarRating > 0)

        val firstHotelRoomResponse = response.hotelRoomResponse.first()
        if (firstHotelRoomResponse != null) {
            val rate = firstHotelRoomResponse.rateInfo.chargeableRateInfo;
            val dailyPrice = Money(BigDecimal(rate.averageRate.toDouble()), rate.currencyCode)
            val totalPrice = Money(BigDecimal(rate.total.toDouble()), rate.currencyCode)
            onlyShowTotalPrice.onNext(firstHotelRoomResponse.rateInfo.chargeableRateInfo.getUserPriceType() == HotelRate.UserPriceType.RATE_FOR_WHOLE_STAY_WITH_TAXES)
            pricePerNightObservable.onNext(dailyPrice.getFormattedMoney(Money.F_NO_DECIMAL))
            totalPriceObservable.onNext(totalPrice.getFormattedMoney(Money.F_NO_DECIMAL))
            discountPercentageBackgroundObservable.onNext(if(rate.isAirAttached()) R.drawable.air_attach_background else R.drawable.guest_rating_background)
        }

        userRatingObservable.onNext(response.hotelGuestRating.toString())
        isUserRatingAvailableObservable.onNext(hotelOffersResponse.hotelGuestRating > 0)

        numberOfReviewsObservable.onNext(
                if (hotelOffersResponse.totalReviews > 0)
                    context.resources.getQuantityString(R.plurals.hotel_number_of_reviews, hotelOffersResponse.totalReviews, hotelOffersResponse.totalReviews)
                else context.resources.getString(R.string.zero_reviews))

        val chargeableRateInfo = response.hotelRoomResponse.first()?.rateInfo?.chargeableRateInfo
        var discountPercentage: Int? = chargeableRateInfo?.discountPercent?.toInt()
        discountPercentageObservable.onNext(Phrase.from(context.resources, R.string.hotel_discount_percent_Template)
                .put("discount", discountPercentage ?: 0).format().toString())
        hasDiscountPercentageObservable.onNext(chargeableRateInfo?.isDiscountTenPercentOrBetter())
        hasVipAccessObservable.onNext(response.isVipAccess && PointOfSale.getPointOfSale().supportsVipAccess())
        promoMessageObservable.onNext(getPromoText(firstHotelRoomResponse))
        strikeThroughPriceObservable.onNext(priceFormatter(context.resources, chargeableRateInfo, true))

        hasFreeCancellationObservable.onNext(hasFreeCancellation(response))
        hasBestPriceGuaranteeObservable.onNext(PointOfSale.getPointOfSale().displayBestPriceGuarantee())
        val hasETPOffer = hasEtpOffer(hotelOffersResponse)
        hasETPObservable.onNext(hasETPOffer)

        if (response.firstHotelOverview != null) {
            sectionBody = Html.fromHtml(response.firstHotelOverview).toString()
            sectionBodyObservable.onNext(sectionBody)
        }

        hotelLatLngObservable.onNext(doubleArrayOf(hotelOffersResponse.latitude, hotelOffersResponse.longitude))


    }

    val hotelDescriptionContainerObserver: Observer<Unit> = endlessObserver {
        expandSection()
    }

    val bookByPhoneContainerClickObserver: Observer<Unit> = endlessObserver {
        val number = when (User.getLoggedInLoyaltyMembershipTier(context)) {
            Traveler.LoyaltyMembershipTier.SILVER -> PointOfSale.getPointOfSale().getSupportPhoneNumberSilver()
            Traveler.LoyaltyMembershipTier.GOLD -> PointOfSale.getPointOfSale().getSupportPhoneNumberGold()
            else -> hotelOffersResponse.telesalesNumber
        }
        SocialUtils.call(context, number)
        HotelV2Tracking().trackLinkHotelV2DetailBookPhoneClick()
    }

    fun isAvailable(): Boolean {
        return hotelOffersResponse.hotelRoomResponse.size() > 0
    }

    val mapClickedSubject = PublishSubject.create<Unit>()

    val reviewsClickedSubject = PublishSubject.create<Unit>()


    val galleryClickedSubject = PublishSubject.create<Unit>()

    val renovationContainerClickObserver: Observer<Unit> = endlessObserver {
        var renovationInfo = Pair<String, String>(context.resources.getString(R.string.renovation_notice),
                hotelOffersResponse.hotelRenovationText?.content ?: "")
        hotelRenovationObservable.onNext(renovationInfo)
        HotelV2Tracking().trackHotelV2RenovationInfo()
    }

    val resortFeeContainerClickObserver: Observer<Unit> = endlessObserver {
        var renovationInfo = Pair<String, String>(context.getResources().getString(R.string.additional_fees),
                hotelOffersResponse.hotelMandatoryFeesText?.content ?: "")
        hotelRenovationObservable.onNext(renovationInfo)
        HotelV2Tracking().trackHotelV2ResortFeeInfo()
    }

    val payLaterInfoContainerClickObserver: Observer<Unit> = endlessObserver {
        hotelPayLaterInfoObservable.onNext(hotelOffersResponse.hotelCountry)
        HotelV2Tracking().trackHotelV2EtpInfo()
    }

    val paramsSubject = BehaviorSubject.create<HotelSearchParams>()

    // Every time a new hotel is emitted, emit an Observable<Hotel>
    // that will return the outer hotel every time the map is clicked
    val mapClickedWithHotelData: Observable<HotelOffersResponse> = Observable.switchOnNext(hotelOffersSubject.map { hotel ->
        mapClickedSubject.map {
            hotel
        }
    })

    val startMapWithIntentObservable: Observable<Intent> = mapClickedWithHotelData.map { hotel ->
        val uri = "geo:" + hotel.latitude + "," + hotel.longitude
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        intent
    }

    // Every time a new hotel is emitted, emit an Observable<Hotel>
    // that will return the outer hotel every time reviews is clicked
    val reviewsClickedWithHotelData: Observable<HotelOffersResponse> = Observable.switchOnNext(hotelOffersSubject.map { hotel ->
        reviewsClickedSubject.map {
            hotel
        }
    })

    private fun hasEtpOffer(response: HotelOffersResponse): Boolean {
        return response.hotelRoomResponse
                .any { it.payLaterOffer != null }
    }

    //get list of unique amenity for hotel room offers
    //we display this unique amenity offered in the expanded room view
    public fun getValueAdd(hotelRoomResponse: List<HotelOffersResponse.HotelRoomResponse>): ArrayList<String> {
        var list = Array(hotelRoomResponse.size(), { i -> "" }).toArrayList()
        for (roomResponseIndex in 0..hotelRoomResponse.size() - 1) {
            val rate = hotelOffersResponse.hotelRoomResponse.get(roomResponseIndex)
            if (rate.valueAdds != null) {
                var unique = rate.valueAdds
                if (!commonList.isEmpty()) {
                    unique.removeAll(commonList)
                }
                if (unique.size() > 0) {
                    list.add(roomResponseIndex, context.getString(R.string.value_add_template, unique.get(0).description.toLowerCase(Locale.getDefault())))
                }
            }
        }
        return list
    }

    private fun hasFreeCancellation(response: HotelOffersResponse): Boolean {
        return response.hotelRoomResponse
                .any { it.hasFreeCancellation == true }
    }

    private fun expandSection() {
        if (!isSectionExpanded) {
            isSectionExpanded = true
        } else {
            isSectionExpanded = false
        }
        sectionImageObservable.onNext(isSectionExpanded)
    }

    init {
        onlyShowTotalPrice.subscribe { onlyShowTotalPrice ->
            (if (onlyShowTotalPrice) totalPriceObservable else pricePerNightObservable).subscribe(roomPriceToShowCustomer)
        }

        paramsSubject.subscribe { params ->
            searchInfoObservable.onNext(Phrase.from(context, R.string.calendar_instructions_date_range_with_guests_TEMPLATE).put("startdate",
                    DateUtils.localDateToMMMd(params.checkIn)).put("enddate",
                    DateUtils.localDateToMMMd(params.checkOut)).put("guests", StrUtils.formatGuestString(context, params.guests()))
                    .format()
                    .toString())
            isCurrentLocationSearch = params.suggestion.isCurrentLocationSearch
        }

        hotelOffersSubject.subscribe(offersObserver)

        hotelRoomRateViewModelsObservable.subscribe {
            val hotelRoomRateViewModels = hotelRoomRateViewModelsObservable.getValue()

            //Expand the first item
            hotelRoomRateViewModels.first().expandRoomObservable.onNext(false)

            //Collapse all items except the first
            for (hotelRoomRateViewModel in hotelRoomRateViewModels.drop(1)) {
                hotelRoomRateViewModel.collapseRoomObservable.onNext(false)
            }

            //Hide all Room Info Description Texts
            for (hotelRoomRateViewModel in hotelRoomRateViewModels) {
                hotelRoomRateViewModel.roomInfoExpandCollapseObservable.onNext(Unit)
            }
        }

        rowExpandingObservable.subscribe { indexOfRowBeingExpanded ->
            //collapse already expanded row
            hotelRoomRateViewModelsObservable.value.elementAt(lastExpandedRowObservable.value).collapseRoomObservable.onNext(true)
            lastExpandedRowObservable.onNext(indexOfRowBeingExpanded)
        }

    }

    private fun getPromoText(hotel: HotelOffersResponse.HotelRoomResponse): String {
        val roomsLeft = hotel.currentAllotment.toInt();
        if (roomsLeft > 0 && roomsLeft <= ROOMS_LEFT_CUTOFF)
            return context.getResources().getQuantityString(R.plurals.num_rooms_left, roomsLeft, roomsLeft)
        else if (hotel.isDiscountRestrictedToCurrentSourceType) return context.getResources().getString(R.string.mobile_exclusive)
        else if (hotel.isSameDayDRR) return context.getResources().getString(R.string.tonight_only)
        else return ""

    }

}

val ROOMS_LEFT_CUTOFF = 5


public class HotelRoomRateViewModel(val context: Context, val hotelRoomResponse: HotelOffersResponse.HotelRoomResponse, val amenity: String, val rowIndex: Int, val hotelDetailViewModel: HotelDetailViewModel) {

    //Output
    val rateObservable = BehaviorSubject.create(hotelRoomResponse)
    val roomBackgroundViewObservable = BehaviorSubject.create<Drawable>()
    val roomSelectedObservable = BehaviorSubject.create<HotelOffersResponse.HotelRoomResponse>()
    val roomTypeObservable = BehaviorSubject.create<String>(hotelRoomResponse.roomTypeDescription)
    val collapsedBedTypeObservable = BehaviorSubject.create<String>()
    val expandedBedTypeObservable = BehaviorSubject.create<String>()
    val expandedAmenityObservable = BehaviorSubject.create<String>()
    val expandedMessageObservable = BehaviorSubject.create<Pair<String, Drawable>>()
    val collapsedUrgencyObservable = BehaviorSubject.create<String>()
    val currencyCode = hotelRoomResponse.rateInfo.chargeableRateInfo.currencyCode

    val dailyPrice = Money(BigDecimal(hotelRoomResponse.rateInfo.chargeableRateInfo.priceToShowUsers.toDouble()), currencyCode)
    var dailyPricePerNightObservable = BehaviorSubject.create<String>()
    var perNightObservable = BehaviorSubject.create<Boolean>()

    val onlyShowTotalPrice = BehaviorSubject.create<Boolean>()
    val pricePerNightObservable = BehaviorSubject.create<String>()
    val roomHeaderImageObservable = BehaviorSubject.create<String>(Images.getMediaHost() + hotelRoomResponse.roomThumbnailUrl)
    val viewRoomObservable = BehaviorSubject.create<Unit>()
    val roomRateInfoTextObservable = BehaviorSubject.create<String>(hotelRoomResponse.roomLongDescription)
    val roomInfoVisibiltyObservable = roomRateInfoTextObservable.map { it != "" }

    val expandRoomObservable = BehaviorSubject.create<Boolean>()
    val collapseRoomObservable = BehaviorSubject.create<Boolean>()
    val expandedMeasurementsDone = PublishSubject.create<Unit>()
    val roomInfoExpandCollapseObservable = PublishSubject.create<Unit>()

    val expandCollapseRoomRateInfoDescription: Observer<Unit> = endlessObserver {
        roomInfoExpandCollapseObservable.onNext(Unit)
    }

    val expandCollapseRoomRate: Observer<Boolean> = endlessObserver {
        isChecked ->
        if (!isChecked) {
            roomSelectedObservable.onNext(hotelRoomResponse)
            //don't change the state of toggle button
            viewRoomObservable.onNext(Unit)
        } else {
            // expand row
            expandRoomObservable.onNext(true)

            // let parent know that a row is being expanded
            hotelDetailViewModel.rowExpandingObservable.onNext(rowIndex)
            HotelV2Tracking().trackLinkHotelV2ViewRoomClick()
        }
    }

    val payLaterObserver: Observer<Unit> = endlessObserver {
        val depositAmount = hotelRoomResponse.payLaterOffer?.rateInfo?.chargeableRateInfo?.depositAmountToShowUsers?.toDouble() ?: 0.0
        val depositAmountMoney = Money(BigDecimal(depositAmount), currencyCode)
        val payLaterText = Phrase.from(context, R.string.room_rate_pay_later_due_now).put("amount", depositAmountMoney.formattedMoney).format().toString()
        dailyPricePerNightObservable.onNext(payLaterText)
        perNightObservable.onNext(false)
        // show price per night
        pricePerNightObservable.onNext(makePriceToShowCustomer())
    }

    fun makePriceToShowCustomer(): String {
        val sb = StringBuilder(dailyPrice.formattedMoney)
        if (!onlyShowTotalPrice.value) sb.append(context.resources.getString(R.string.per_night))
        return sb.toString()
    }

    init {
        onlyShowTotalPrice.onNext(hotelRoomResponse.rateInfo.chargeableRateInfo.getUserPriceType() == HotelRate.UserPriceType.RATE_FOR_WHOLE_STAY_WITH_TAXES)

        //show total price per night
        pricePerNightObservable.onNext(context.resources.getString(R.string.cars_total_template, Money.getFormattedMoneyFromAmountAndCurrencyCode(BigDecimal(hotelRoomResponse.rateInfo.chargeableRateInfo.total.toDouble()), currencyCode, Money.F_NO_DECIMAL)))
        dailyPricePerNightObservable.onNext(dailyPrice.formattedMoney)
        perNightObservable.onNext(true)
        rateObservable.subscribe { hotelRoom ->
            val bedTypes = (hotelRoom.bedTypes ?: emptyList()).map { it.description }.join("")
            collapsedBedTypeObservable.onNext(bedTypes)
            expandedBedTypeObservable.onNext(bedTypes)
            var expandedPair: Pair<String, Drawable>
            if (hotelRoom.hasFreeCancellation) {
                expandedPair = Pair(context.getResources().getString(R.string.free_cancellation), context.getResources().getDrawable(R.drawable.room_checkmark))
            } else {
                expandedPair = Pair(context.getResources().getString(R.string.non_refundable), context.getResources().getDrawable(R.drawable.room_non_refundable))
            }

            expandedMessageObservable.onNext(expandedPair)

            val roomLeft = hotelRoom.currentAllotment.toInt()
            if (hotelRoom.currentAllotment != null && roomLeft > 0 && roomLeft <= ROOMS_LEFT_CUTOFF) {
                collapsedUrgencyObservable.onNext(context.getResources().getQuantityString(R.plurals.num_rooms_left, roomLeft, roomLeft))
            } else {
                collapsedUrgencyObservable.onNext(expandedPair.first)
            }

        }

        if (Strings.isNotEmpty(amenity)) expandedAmenityObservable.onNext(amenity)
    }
}
