package com.expedia.vm

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.support.annotation.DrawableRes
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import com.expedia.bookings.R
import com.expedia.bookings.data.HotelMedia
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.User
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.extension.isShowAirAttached
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.tracking.HotelV2Tracking
import com.expedia.bookings.utils.Amenity
import com.expedia.bookings.utils.CollectionUtils
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.HotelUtils
import com.expedia.bookings.utils.Images
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.widget.HotelDetailView
import com.expedia.bookings.widget.RecyclerGallery
import com.expedia.bookings.widget.priceFormatter
import com.expedia.util.endlessObserver
import com.expedia.util.getGuestRatingBackgroundDrawable
import com.expedia.util.getGuestRatingRecommendedText
import com.mobiata.android.FormatUtils
import com.mobiata.android.SocialUtils
import com.mobiata.android.text.StrikethroughTagHandler
import com.squareup.phrase.Phrase
import rx.Observable
import rx.Observer
import rx.internal.util.RxRingBuffer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.math.BigDecimal
import java.util.ArrayList
import java.util.Locale
import kotlin.properties.Delegates

class HotelMapViewModel(val context: Context, val selectARoomObserver: Observer<Unit>, val hotelSoldOut: Observable<Boolean>) {
    //Outputs for View
    val hotelName = BehaviorSubject.create<String>()
    val hotelStarRating = BehaviorSubject.create<Float>()
    val hotelStarRatingVisibility = BehaviorSubject.create<Boolean>()
    val strikethroughPrice = BehaviorSubject.create<CharSequence>()
    private val price = BehaviorSubject.create<CharSequence>()
    val fromPrice = BehaviorSubject.create<CharSequence>("")
    val fromPriceVisibility = fromPrice.map { it != null && !it.equals("") }
    val strikethroughPriceVisibility = Observable.combineLatest(fromPriceVisibility,
            Observable.zip(strikethroughPrice, price, { strikethroughPrice, price -> strikethroughPrice.toString() != price.toString() }))
    { fromPriceVisibile, priceAndStrikethroughPriceAreDifferent -> fromPriceVisibile && priceAndStrikethroughPriceAreDifferent }
    val hotelLatLng = BehaviorSubject.create<DoubleArray>()
    val resetCameraPosition = PublishSubject.create<Unit>()
    val selectARoomInvisibility = BehaviorSubject.create<Boolean>(false)

    //Setup the data I need to behave as a View Model for my View
    val offersObserver = endlessObserver<HotelOffersResponse> { response ->
        hotelName.onNext(response.hotelName)
        hotelStarRating.onNext(response.hotelStarRating.toFloat())
        hotelStarRatingVisibility.onNext(response.hotelStarRating > 0)
        price.onNext(priceFormatter(context.resources, response.hotelRoomResponse?.firstOrNull()?.rateInfo?.chargeableRateInfo, false))
        strikethroughPrice.onNext(priceFormatter(context.resources, response.hotelRoomResponse?.firstOrNull()?.rateInfo?.chargeableRateInfo, true))
        hotelLatLng.onNext(doubleArrayOf(response.latitude, response.longitude))

        val firstHotelRoomResponse = response.hotelRoomResponse?.firstOrNull()
        if (firstHotelRoomResponse != null) {
            val firstRoomRate = firstHotelRoomResponse.rateInfo?.chargeableRateInfo
            fromPrice.onNext(fromPriceStyledString(context, firstRoomRate))
        }
    }

    init {
        hotelSoldOut.subscribe(selectARoomInvisibility)
    }

    companion object {
        fun fromPriceStyledString(context: Context, hotelRoomRate: HotelRate?): CharSequence {
            if (hotelRoomRate == null) {
                return ""
            }

            val roomDailyPrice = Money(BigDecimal(hotelRoomRate.averageRate.toDouble()), hotelRoomRate.currencyCode).getFormattedMoney(Money.F_NO_DECIMAL)

            val fromPriceString = context.getString(R.string.map_snippet_price_template, roomDailyPrice)
            val fromPriceStyledString = SpannableString(fromPriceString)
            val startIndex = fromPriceString.indexOf(roomDailyPrice)
            val endIndex = startIndex + roomDailyPrice.length
            fromPriceStyledString.setSpan(StyleSpan(Typeface.BOLD), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            fromPriceStyledString.setSpan(RelativeSizeSpan(1.4f), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            return fromPriceStyledString
        }
    }
}

class HotelDetailViewModel(val context: Context, val hotelServices: HotelServices, val roomSelectedObserver: Observer<HotelOffersResponse.HotelRoomResponse>) : RecyclerGallery.GalleryItemListener, RecyclerGallery.GalleryItemScrollListener {

    override fun onGalleryItemClicked(item: Any) {
        galleryClickedSubject.onNext(Unit)
    }

    override fun onGalleryItemScrolled(position: Int) {
        val havePhotoWithIndex = CollectionUtils.isNotEmpty(hotelOffersResponse.photos) && (position < hotelOffersResponse.photos.count())
        if (havePhotoWithIndex && hotelOffersResponse.photos[position].displayText != null)
            galleryItemChangeObservable.onNext(Pair(position, hotelOffersResponse.photos.get(position).displayText))
        else
            galleryItemChangeObservable.onNext(Pair(position, ""))

    }

    private val allRoomsSoldOut = BehaviorSubject.create<Boolean>(false)
    private val noRoomsInOffersResponse = BehaviorSubject.create<Boolean>(false)
    val hotelSoldOut = BehaviorSubject.create<Boolean>(false)
    val selectedRoomSoldOut = PublishSubject.create<Unit>()

    val toolBarRatingColor = hotelSoldOut.map { if (it) context.resources.getColor(android.R.color.white) else context.resources.getColor(R.color.hotelsv2_detail_star_color) }
    val galleryColorFilter = hotelSoldOut.map { if (it) HotelDetailView.zeroSaturationColorMatrixColorFilter else null }
    val priceWidgetBackground = hotelSoldOut.map { if (it) context.resources.getColor(R.color.hotel_cell_gray_text) else context.resources.getColor(R.color.hotels_primary_color) }

    val hotelOffersSubject = BehaviorSubject.create<HotelOffersResponse>()
    var hotelOffersResponse: HotelOffersResponse by Delegates.notNull()
    var etpOffersList = ArrayList<HotelOffersResponse.HotelRoomResponse>()
    var sectionBody: String by Delegates.notNull()
    var commonList = ArrayList<String>()

    var isSectionExpanded = false
    val sectionBodyObservable = BehaviorSubject.create<String>()
    val sectionImageObservable = BehaviorSubject.create<Boolean>()
    val showBookByPhoneObservable = BehaviorSubject.create<Boolean>(false)
    val galleryObservable = BehaviorSubject.create<ArrayList<HotelMedia>>()

    val commonAmenityTextObservable = BehaviorSubject.create<String>()

    val amenitiesListObservable = BehaviorSubject.create<List<Amenity>>()
    val noAmenityObservable = BehaviorSubject.create<Unit>()

    val hasETPObservable = BehaviorSubject.create<Boolean>(false)
    val hasFreeCancellationObservable = BehaviorSubject.create<Boolean>()
    val hasBestPriceGuaranteeObservable = BehaviorSubject.create<Boolean>()
    val renovationObservable = BehaviorSubject.create<Unit>()
    val hotelRenovationObservable = BehaviorSubject.create<Pair<String, String>>()
    val hotelPayLaterInfoObservable = BehaviorSubject.create<String>()
    val vipAccessInfoObservable = BehaviorSubject.create<Unit>()

    val propertyInfoListObservable = BehaviorSubject.create<List<HotelOffersResponse.HotelText>>(emptyList())

    val roomResponseListObservable = BehaviorSubject.create<Pair<List<HotelOffersResponse.HotelRoomResponse>, List<String>>>()
    var uniqueValueAddForRooms: List<String> by Delegates.notNull()

    var etpUniqueValueAddForRooms: List<String> by Delegates.notNull()
    val etpRoomResponseListObservable = BehaviorSubject.create<Pair<List<HotelOffersResponse.HotelRoomResponse>, List<String>>>()

    val lastExpandedRowObservable = BehaviorSubject.create<Int>()
    val rowExpandingObservable = PublishSubject.create<Int>()
    val hotelRoomRateViewModelsObservable = BehaviorSubject.create<List<com.expedia.vm.HotelRoomRateViewModel>>()

    val hotelResortFeeObservable = BehaviorSubject.create<String>(null as String?)
    val hotelResortFeeIncludedTextObservable = BehaviorSubject.create<String>()
    val hotelNameObservable = BehaviorSubject.create<String>()
    val hotelRatingObservable = BehaviorSubject.create<Float>()
    val hotelRatingObservableVisibility = BehaviorSubject.create<Boolean>()
    val onlyShowTotalPrice = BehaviorSubject.create<Boolean>(false)
    val roomPriceToShowCustomer = BehaviorSubject.create<String>()
    val totalPriceObservable = BehaviorSubject.create<String>()
    val pricePerNightObservable = BehaviorSubject.create<String>()
    val searchInfoObservable = BehaviorSubject.create<String>()
    val userRatingBackgroundColorObservable = BehaviorSubject.create<Drawable>()
    val userRatingObservable = BehaviorSubject.create<String>()
    val isUserRatingAvailableObservable = BehaviorSubject.create<Boolean>()
    val userRatingRecommendationTextObservable = BehaviorSubject.create<String>()
    val ratingContainerBackground = isUserRatingAvailableObservable.map { if (it) context.resources.getDrawable(R.drawable.hotel_detail_ripple) else null }
    val numberOfReviewsObservable = BehaviorSubject.create<String>()
    val hotelLatLngObservable = BehaviorSubject.create<DoubleArray>()
    val discountPercentageBackgroundObservable = BehaviorSubject.create<Int>()
    val discountPercentageObservable = BehaviorSubject.create<String>()
    val hasDiscountPercentageObservable = BehaviorSubject.create<Boolean>(false)
    val hasVipAccessObservable = BehaviorSubject.create<Boolean>(false)
    val promoMessageObservable = BehaviorSubject.create<String>("")
    val strikeThroughPriceObservable = BehaviorSubject.create<CharSequence>()
    val galleryItemChangeObservable = BehaviorSubject.create<Pair<Int, String>>()
    var isCurrentLocationSearch = false
    val scrollToRoom = PublishSubject.create<Unit>()
    val changeDates = PublishSubject.create<Unit>()

    public fun addViewsAfterTransition() {

        if (hotelOffersResponse.hotelRoomResponse != null && hotelOffersResponse.hotelRoomResponse.isNotEmpty()) {
            uniqueValueAddForRooms = getValueAdd(hotelOffersResponse.hotelRoomResponse)
            roomResponseListObservable.onNext(Pair(hotelOffersResponse.hotelRoomResponse, uniqueValueAddForRooms))
        }

        var listHotelInfo = ArrayList<HotelOffersResponse.HotelText>()

        //Set up entire text for hotel info
        if (hotelOffersResponse.hotelOverviewText != null && hotelOffersResponse.hotelOverviewText.size > 1 ) {
            for (index in 1..hotelOffersResponse.hotelOverviewText.size - 1) {
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
        if (CollectionUtils.isNotEmpty(hotelOffersResponse.hotelRoomResponse)) {
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

        val firstRoomDetails = hotelOffersResponse.hotelRoomResponse?.firstOrNull()
        if (firstRoomDetails?.rateInfo?.chargeableRateInfo?.showResortFeeMessage ?: false) {
            val rate = firstRoomDetails!!.rateInfo.chargeableRateInfo
            val hotelResortFee = Money(BigDecimal(rate.totalMandatoryFees.toDouble()), rate.currencyCode)
            hotelResortFeeObservable.onNext(hotelResortFee.getFormattedMoney(Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL))
            val includedNotIncludedStrId = if (rate.resortFeeInclusion) R.string.included_in_the_price else R.string.not_included_in_the_price
            hotelResortFeeIncludedTextObservable.onNext(context.resources.getString(includedNotIncludedStrId))
        } else {
            hotelResortFeeObservable.onNext(null)
            hotelResortFeeIncludedTextObservable.onNext(null)
        }

        showBookByPhoneObservable.onNext(!hotelOffersResponse.deskTopOverrideNumber
                && !Strings.isEmpty(hotelOffersResponse.telesalesNumber))

        HotelV2Tracking().trackPageLoadHotelV2Infosite(hotelOffersResponse, paramsSubject.value, hasETPOffer, isCurrentLocationSearch, hotelSoldOut.value, false)
    }

    private val offersObserver = endlessObserver<HotelOffersResponse> { response ->
        hotelOffersResponse = response

        var galleryUrls = ArrayList<HotelMedia>()

        if (Images.getHotelImages(hotelOffersResponse).isNotEmpty()) {
            galleryUrls.addAll(Images.getHotelImages(hotelOffersResponse).toArrayList())
        } else {
            var placeHolder = HotelMedia()
            placeHolder.setIsPlaceholder(true)
            galleryUrls.add(placeHolder)
        }
        galleryObservable.onNext(galleryUrls)

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

        allRoomsSoldOut.onNext(false)
        lastExpandedRowObservable.onNext(-1)
        noRoomsInOffersResponse.onNext(CollectionUtils.isEmpty(response.hotelRoomResponse))

        val firstHotelRoomResponse = response.hotelRoomResponse?.firstOrNull()
        if (firstHotelRoomResponse != null) {
            val rate = firstHotelRoomResponse.rateInfo.chargeableRateInfo
            onlyShowTotalPrice.onNext(firstHotelRoomResponse.rateInfo.chargeableRateInfo.getUserPriceType() == HotelRate.UserPriceType.RATE_FOR_WHOLE_STAY_WITH_TAXES)
            pricePerNightObservable.onNext(Money(BigDecimal(rate.averageRate.toDouble()), rate.currencyCode).getFormattedMoney(Money.F_NO_DECIMAL))
            totalPriceObservable.onNext(Money(BigDecimal(rate.totalPriceWithMandatoryFees.toDouble()), rate.currencyCode).getFormattedMoney(Money.F_NO_DECIMAL))
            discountPercentageBackgroundObservable.onNext(if (rate.isShowAirAttached()) R.drawable.air_attach_background else R.drawable.guest_rating_background)
        }

        userRatingObservable.onNext(response.hotelGuestRating.toString())
        userRatingBackgroundColorObservable.onNext(getGuestRatingBackgroundDrawable(response.hotelGuestRating.toFloat(), context))
        userRatingRecommendationTextObservable.onNext(getGuestRatingRecommendedText(response.hotelGuestRating.toFloat(), context.resources))
        isUserRatingAvailableObservable.onNext(hotelOffersResponse.hotelGuestRating > 0)

        numberOfReviewsObservable.onNext(
                if (hotelOffersResponse.totalReviews > 0)
                    context.resources.getQuantityString(R.plurals.hotel_number_of_reviews, hotelOffersResponse.totalReviews, HotelUtils.formattedReviewCount(hotelOffersResponse.totalReviews))
                else context.resources.getString(R.string.zero_reviews))

        val chargeableRateInfo = response.hotelRoomResponse?.firstOrNull()?.rateInfo?.chargeableRateInfo
        var discountPercentage: Int? = chargeableRateInfo?.discountPercent?.toInt()
        discountPercentageObservable.onNext(Phrase.from(context.resources, R.string.hotel_discount_percent_Template)
                .put("discount", discountPercentage ?: 0).format().toString())
        hasDiscountPercentageObservable.onNext(chargeableRateInfo?.isDiscountTenPercentOrBetter ?: false)
        hasVipAccessObservable.onNext(response.isVipAccess && PointOfSale.getPointOfSale().supportsVipAccess())
        promoMessageObservable.onNext(getPromoText(firstHotelRoomResponse))
        val priceToShowUsers = chargeableRateInfo?.priceToShowUsers ?: 0f
        val strikethroughPriceToShowUsers = chargeableRateInfo?.strikethroughPriceToShowUsers ?: 0f
        if (priceToShowUsers < strikethroughPriceToShowUsers) {
            strikeThroughPriceObservable.onNext(priceFormatter(context.resources, chargeableRateInfo, true))
        }

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

    val strikeThroughPriceVisibility = Observable.combineLatest(hasDiscountPercentageObservable, hotelSoldOut)
    { hasDiscount, hotelSoldOut -> hasDiscount && !hotelSoldOut }

    val perNightVisibility = Observable.combineLatest(onlyShowTotalPrice, hotelSoldOut) { onlyShowTotalPrice, hotelSoldOut -> onlyShowTotalPrice || hotelSoldOut }

    val payByPhoneContainerVisibility = Observable.combineLatest(showBookByPhoneObservable, hotelSoldOut) { showBookByPhoneObservable, hotelSoldOut -> showBookByPhoneObservable && !hotelSoldOut }

    val hotelMessagingContainerVisibility = Observable.combineLatest(hasDiscountPercentageObservable, hasVipAccessObservable, promoMessageObservable, hotelSoldOut)
    {
        hasDiscount, hasVipAccess, promoMessage, hotelSoldOut ->
        (hasDiscount || hasVipAccess || Strings.isNotEmpty(promoMessage))&& !hotelSoldOut
    }

    val etpContainerVisibility =  Observable.combineLatest(hasETPObservable, hotelSoldOut) { hasETPOffer, hotelSoldOut -> hasETPOffer && !hotelSoldOut }

    val paramsSubject = BehaviorSubject.create<HotelSearchParams>()

    // Every time a new hotel is emitted, emit an Observable<Hotel>
    // that will return the outer hotel every time reviews is clicked
    val reviewsClickedWithHotelData: Observable<HotelOffersResponse> = Observable.switchOnNext(hotelOffersSubject.map { hotel ->
        reviewsClickedSubject.map {
            hotel
        }
    })

    fun hasEtpOffer(response: HotelOffersResponse): Boolean {
        return CollectionUtils.isNotEmpty(response.hotelRoomResponse) && response.hotelRoomResponse.any { it.payLaterOffer != null }
    }

    //get list of unique amenity for hotel room offers
    //we display this unique amenity offered in the expanded room view
    public fun getValueAdd(hotelRooms: List<HotelOffersResponse.HotelRoomResponse>?): List<String> {
        if (CollectionUtils.isEmpty(hotelRooms)) {
            return emptyList()
        }

        var list = Array(hotelRooms!!.size, { i -> "" }).toArrayList()
        for (iRoom in 0..hotelRooms.size - 1) {
            val rate = hotelOffersResponse.hotelRoomResponse.get(iRoom)
            if (rate.valueAdds != null) {
                var unique = rate.valueAdds
                if (!commonList.isEmpty()) {
                    unique.removeAllRaw(commonList)
                }
                if (unique.size > 0) {
                    list.add(iRoom, context.getString(R.string.value_add_template, unique.get(0).description.toLowerCase(Locale.getDefault())))
                }
            }
        }
        return list
    }

    private fun hasFreeCancellation(response: HotelOffersResponse): Boolean {
        return CollectionUtils.isNotEmpty(response.hotelRoomResponse) && response.hotelRoomResponse.any { it.hasFreeCancellation == true }
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
        Observable.combineLatest(allRoomsSoldOut, noRoomsInOffersResponse)
        { allRoomsSoldOut, noRoomsInOffersResponse -> allRoomsSoldOut || noRoomsInOffersResponse }.subscribe(hotelSoldOut)

        selectedRoomSoldOut.subscribe {
            hotelRoomRateViewModelsObservable.value.elementAt(lastExpandedRowObservable.value).collapseRoomObservable.onNext(false)
            hotelRoomRateViewModelsObservable.value.elementAt(lastExpandedRowObservable.value).roomSoldOut.onNext(true)
        }

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
            val hotelRoomRateViewModels = hotelRoomRateViewModelsObservable.value

            if (hotelRoomRateViewModels.isEmpty()) {
                return@subscribe
            }

            //Expand the first item
            hotelRoomRateViewModels.first().expandRoomObservable.onNext(false)

            //Collapse all items except first
            hotelRoomRateViewModels.drop(1).forEach { it.collapseRoomObservable.onNext(false) }

            //Construct allRoomsSoldOut as Observable.combineLatest(hotelRoomRateViewModels.map { it.roomSoldOut }, { obj -> obj.all({ it == true }) })
            //i.e. This will act as a contributing signal to Hotel Being Sold Out b/c All individual rooms sold out leads to hotel sold out.
            //Workaround "Failed to set 'rx.buffer.size' with value "
            //where RxRingBuffer.SIZE is the upper limit of Observables List for operators like combineLatest
            //by splitting view models list into sublists of RxRingBuffer.SIZE size each and use 2 levels of combineLatest
            val listOfListOfRoomRateViewModels = ArrayList<List<HotelRoomRateViewModel>>()
            var hotelRoomRateViewModelsToBeSplit = hotelRoomRateViewModels.toList()
            while (hotelRoomRateViewModelsToBeSplit.size > RxRingBuffer.SIZE) {
                listOfListOfRoomRateViewModels.add(hotelRoomRateViewModelsToBeSplit.take(RxRingBuffer.SIZE))
                hotelRoomRateViewModelsToBeSplit = hotelRoomRateViewModelsToBeSplit.drop(RxRingBuffer.SIZE)
            }
            listOfListOfRoomRateViewModels.add(hotelRoomRateViewModelsToBeSplit.take(RxRingBuffer.SIZE))

            val listOfObservables = listOfListOfRoomRateViewModels.map { listOfRoomRateViewModels -> Observable.combineLatest(listOfRoomRateViewModels.map { it.roomSoldOut }, { obj -> obj.all({ it -> it as Boolean }) }) }
            Observable.combineLatest(listOfObservables, { obj -> obj.all({ it -> it as Boolean }) }).distinctUntilChanged().subscribe(allRoomsSoldOut)

            selectedRoomSoldOut.subscribe {
                for (hotelRoomRateViewModel in hotelRoomRateViewModels.drop(lastExpandedRowObservable.value)) {
                    if (!hotelRoomRateViewModel.roomSoldOut.value) {
                        hotelRoomRateViewModel.expandRoomObservable.onNext(false)
                        return@subscribe
                    }
                }

                for (hotelRoomRateViewModel in hotelRoomRateViewModels.take(lastExpandedRowObservable.value)) {
                    if (!(hotelRoomRateViewModel.roomSoldOut.value)) {
                        hotelRoomRateViewModel.expandRoomObservable.onNext(false)
                        return@subscribe
                    }
                }
            }
        }

        rowExpandingObservable.subscribe { indexOfRowBeingExpanded ->
            //collapse already expanded row if there is one
            if (lastExpandedRowObservable.value >= 0 && lastExpandedRowObservable.value < hotelRoomRateViewModelsObservable.value.size) {
                hotelRoomRateViewModelsObservable.value.elementAt(lastExpandedRowObservable.value).collapseRoomObservable.onNext(true)
            }
            lastExpandedRowObservable.onNext(indexOfRowBeingExpanded)
        }

    }

    private fun getPromoText(roomOffer: HotelOffersResponse.HotelRoomResponse?): String {
        // NOTE: Any changes to this logic should also be made in HotelViewModel (see: urgencyMessageObservable)
        if (roomOffer == null) {
            return ""
        }

        val roomsLeft = roomOffer.currentAllotment.toInt()
        return if (roomsLeft > 0 && roomsLeft <= ROOMS_LEFT_CUTOFF) {
            context.resources.getQuantityString(R.plurals.num_rooms_left, roomsLeft, roomsLeft)
        } else if (roomOffer.isSameDayDRR) {
            context.resources.getString(R.string.tonight_only)
        }
        else if (roomOffer.isDiscountRestrictedToCurrentSourceType) {
            context.resources.getString(R.string.mobile_exclusive)
        }
        else {
            ""
        }
    }

}

val ROOMS_LEFT_CUTOFF = 5


public class HotelRoomRateViewModel(val context: Context, val hotelId: String, val hotelRoomResponse: HotelOffersResponse.HotelRoomResponse, val amenity: String, val rowIndex: Int, val rowExpanding: PublishSubject<Int>) {

    //Output
    val roomSelectedObservable = BehaviorSubject.create<HotelOffersResponse.HotelRoomResponse>()
    val roomTypeObservable = BehaviorSubject.create<String>(hotelRoomResponse.roomTypeDescription)
    val collapsedBedTypeObservable = BehaviorSubject.create<String>()
    val expandedBedTypeObservable = BehaviorSubject.create<String>()
    val expandedAmenityObservable = BehaviorSubject.create<String>()
    val expandedMessageObservable = BehaviorSubject.create<Pair<String, @DrawableRes Int>>()
    val collapsedUrgencyObservable = BehaviorSubject.create<String>()
    val currencyCode = hotelRoomResponse.rateInfo.chargeableRateInfo.currencyCode

    val dailyPrice = Money(BigDecimal(hotelRoomResponse.rateInfo.chargeableRateInfo.priceToShowUsers.toDouble()), currencyCode)
    val strikeThroughPriceObservable = BehaviorSubject.create<CharSequence>()
    val dailyPricePerNightObservable = BehaviorSubject.create<String>()
    val perNightPriceVisibleObservable = BehaviorSubject.create<Boolean>()

    val onlyShowTotalPrice = BehaviorSubject.create<Boolean>()
    val roomHeaderImageObservable = BehaviorSubject.create<String>(Images.getMediaHost() + hotelRoomResponse.roomThumbnailUrl)
    val viewRoomObservable = BehaviorSubject.create<Unit>()
    val roomRateInfoTextObservable = BehaviorSubject.create<String>(hotelRoomResponse.roomLongDescription)
    val roomInfoVisibiltyObservable = roomRateInfoTextObservable.map { it != "" }

    val expandRoomObservable = PublishSubject.create<Boolean>()
    val collapseRoomObservable = PublishSubject.create<Boolean>()
    val expandedMeasurementsDone = PublishSubject.create<Unit>()
    val roomInfoExpandCollapseObservable = PublishSubject.create<Unit>()
    val discountPercentage = BehaviorSubject.create<String>()

    val roomSoldOut = BehaviorSubject.create<Boolean>(false)

    val expandCollapseRoomRateInfoDescription: Observer<Unit> = endlessObserver {
        roomInfoExpandCollapseObservable.onNext(Unit)
    }

    val soldOutButtonLabelObservable: Observable<CharSequence> = roomSoldOut.filter { it == true }.map { context.getString(R.string.trip_bucket_sold_out) }

    val expandCollapseRoomRate: Observer<Boolean> = endlessObserver {
        isChecked ->
        if (!isChecked) {
            roomSelectedObservable.onNext(hotelRoomResponse)
            //don't change the state of toggle button
            viewRoomObservable.onNext(Unit)

            if (hotelRoomResponse.rateInfo.chargeableRateInfo.airAttached) {
                HotelV2Tracking().trackLinkHotelV2AirAttachEligible(hotelRoomResponse, hotelId)
            }
        } else {
            // expand row
            expandRoomObservable.onNext(true)
            HotelV2Tracking().trackLinkHotelV2ViewRoomClick()
        }
    }

    fun makePriceToShowCustomer(): String {
        val sb = StringBuilder(dailyPrice.formattedMoney)
        if (!onlyShowTotalPrice.value) sb.append(context.resources.getString(R.string.per_night))
        return sb.toString()
    }

    init {
        val rateInfo = hotelRoomResponse.rateInfo
        val chargeableRateInfo = rateInfo.chargeableRateInfo
        val discountPercent = HotelUtils.getDiscountPercent(chargeableRateInfo)
        val isPayLater = hotelRoomResponse.isPayLater

        if (discountPercent >= 9.5) {
            // discount is 10% or better
            discountPercentage.onNext(context.resources.getString(R.string.percent_off_TEMPLATE, discountPercent))
            if (!isPayLater && (chargeableRateInfo.priceToShowUsers < chargeableRateInfo.strikethroughPriceToShowUsers)) {
                val strikeThroughPriceToShowUsers = Money(BigDecimal(chargeableRateInfo.strikethroughPriceToShowUsers.toDouble()), currencyCode).formattedMoney
                strikeThroughPriceObservable.onNext(Html.fromHtml(context.resources.getString(R.string.strike_template, strikeThroughPriceToShowUsers), null, StrikethroughTagHandler()))
            }
        }
        onlyShowTotalPrice.onNext(chargeableRateInfo.getUserPriceType() == HotelRate.UserPriceType.RATE_FOR_WHOLE_STAY_WITH_TAXES)

        if (isPayLater) {
            val depositAmount = chargeableRateInfo.depositAmountToShowUsers?.toDouble() ?: 0.0
            val depositAmountMoney = Money(BigDecimal(depositAmount), currencyCode)
            val payLaterText = Phrase.from(context, R.string.room_rate_pay_later_due_now).put("amount", depositAmountMoney.formattedMoney).format().toString()
            dailyPricePerNightObservable.onNext(payLaterText)
            perNightPriceVisibleObservable.onNext(false)
            // we show price per night in strikeThroughPriceObservable in case of pay later option
            strikeThroughPriceObservable.onNext(makePriceToShowCustomer())
        }
        else {
            perNightPriceVisibleObservable.onNext(true)
            dailyPricePerNightObservable.onNext(dailyPrice.formattedMoney)
        }

        val bedTypes = (hotelRoomResponse.bedTypes ?: emptyList()).map { it.description }.joinToString("")
        collapsedBedTypeObservable.onNext(bedTypes)
        expandedBedTypeObservable.onNext(bedTypes)
        var expandedPair: Pair<String, Int>
        if (hotelRoomResponse.hasFreeCancellation) {
            expandedPair = Pair(context.resources.getString(R.string.free_cancellation), R.drawable.room_checkmark)
        } else {
            expandedPair = Pair(context.resources.getString(R.string.non_refundable), R.drawable.room_non_refundable)
        }
        expandedMessageObservable.onNext(expandedPair)
        val roomLeft = hotelRoomResponse.currentAllotment.toInt()
        if (hotelRoomResponse.currentAllotment != null && roomLeft > 0 && roomLeft <= ROOMS_LEFT_CUTOFF) {
            collapsedUrgencyObservable.onNext(context.resources.getQuantityString(R.plurals.num_rooms_left, roomLeft, roomLeft))
        } else {
            collapsedUrgencyObservable.onNext(expandedPair.first)
        }
        if (Strings.isNotEmpty(amenity)) expandedAmenityObservable.onNext(amenity)
    }
}
