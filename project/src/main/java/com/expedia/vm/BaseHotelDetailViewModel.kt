package com.expedia.vm

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.annotation.CallSuper
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.HotelMedia
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.LoyaltyMembershipTier
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.extension.isShowAirAttached
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.bookings.tracking.hotel.PageUsableData
import com.expedia.bookings.utils.Amenity
import com.expedia.bookings.utils.CollectionUtils
import com.expedia.bookings.utils.CurrencyUtils
import com.expedia.bookings.utils.HotelUtils
import com.expedia.bookings.utils.HotelsV2DataUtil
import com.expedia.bookings.utils.Images
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.Ui
import com.expedia.util.endlessObserver
import com.expedia.util.getGuestRatingBackground
import com.expedia.util.getGuestRatingText
import com.mobiata.android.FormatUtils
import com.mobiata.android.SocialUtils
import com.squareup.phrase.Phrase
import rx.Observable
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import rx.subscriptions.CompositeSubscription
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.ArrayList
import java.util.LinkedHashMap
import java.util.Locale
import kotlin.properties.Delegates

abstract class BaseHotelDetailViewModel(val context: Context) {

    abstract fun getFeeTypeText(): Int
    abstract fun getResortFeeText(): Int
    abstract fun showFeesIncludedNotIncluded(): Boolean
    abstract fun showFeeType(): Boolean
    abstract fun getLOB(): LineOfBusiness
    abstract fun hasMemberDeal(roomOffer: HotelOffersResponse.HotelRoomResponse): Boolean
    abstract fun trackHotelResortFeeInfoClick()
    abstract fun trackHotelRenovationInfoClick()
    abstract fun trackHotelDetailBookPhoneClick()
    abstract fun trackHotelDetailSelectRoomClick(isStickyButton: Boolean)
    abstract fun trackHotelViewBookClick()
    abstract fun trackHotelDetailMapViewClick()
    abstract fun trackHotelDetailGalleryClick()
    abstract fun trackHotelDetailLoad(isRoomSoldOut: Boolean)
    abstract fun shouldShowBookByPhone(): Boolean
    abstract fun getTelesalesNumber(): String
    abstract fun getHotelDetailPriceViewModel(): BaseHotelDetailPriceViewModel

    val hotelOffersResponseSubject = PublishSubject.create<HotelOffersResponse>()

    val roomSelectedSubject = BehaviorSubject.create<HotelOffersResponse.HotelRoomResponse>()
    val hotelSoldOut = BehaviorSubject.create<Boolean>(false)
    val selectedRoomSoldOut = PublishSubject.create<Unit>()

    val hotelOffersSubject = BehaviorSubject.create<HotelOffersResponse>()
    var hotelOffersResponse: HotelOffersResponse by Delegates.notNull()
    var etpOffersList = ArrayList<HotelOffersResponse.HotelRoomResponse>()

    val sectionBodyObservable = BehaviorSubject.create<String>()
    val showBookByPhoneObservable = BehaviorSubject.create<Boolean>(false)
    val galleryObservable = PublishSubject.create<ArrayList<HotelMedia>>()

    val commonAmenityTextObservable = BehaviorSubject.create<String>()

    val amenitiesListObservable = BehaviorSubject.create<List<Amenity>>()
    val noAmenityObservable = BehaviorSubject.create<Unit>()

    val hasETPObservable = BehaviorSubject.create<Boolean>(false)
    val hasFreeCancellationObservable = BehaviorSubject.create<Boolean>()
    val hasBestPriceGuaranteeObservable = BehaviorSubject.create<Boolean>()
    val renovationObservable = BehaviorSubject.create<Boolean>()
    val hotelRenovationObservable = BehaviorSubject.create<Pair<String, String>>()
    val hotelPayLaterInfoObservable = BehaviorSubject.create<Pair<String, List<HotelOffersResponse.HotelRoomResponse>>>()
    val vipAccessInfoObservable = BehaviorSubject.create<Unit>()
    val propertyInfoListObservable = BehaviorSubject.create<List<HotelOffersResponse.HotelText>>(emptyList())

    val roomResponseListObservable = BehaviorSubject.create<Pair<List<HotelOffersResponse.HotelRoomResponse>, List<String>>>()
    var uniqueValueAddForRooms: List<String> by Delegates.notNull()

    var etpUniqueValueAddForRooms: List<String> by Delegates.notNull()
    val etpRoomResponseListObservable = BehaviorSubject.create<Pair<List<HotelOffersResponse.HotelRoomResponse>, List<String>>>()

    val lastExpandedRowIndexObservable = BehaviorSubject.create<Int>()
    val rowExpandingObservable = PublishSubject.create<Int>()
    val hotelRoomRateViewModelsObservable = BehaviorSubject.create<ArrayList<HotelRoomRateViewModel>>()
    val hotelRoomDetailViewModelsObservable = BehaviorSubject.create<ArrayList<HotelRoomDetailViewModel>>()

    val hotelResortFeeObservable = BehaviorSubject.create<String>("")
    val hotelResortFeeIncludedTextObservable = BehaviorSubject.create<String>()
    val hotelNameObservable = BehaviorSubject.create<String>()
    val hotelRatingObservable = BehaviorSubject.create<Float>()
    val hotelRatingContentDescriptionObservable = BehaviorSubject.create<String>()
    val hotelRatingObservableVisibility = BehaviorSubject.create<Boolean>()
    val userRatingBackgroundColorObservable = BehaviorSubject.create<Drawable>()
    val userRatingObservable = BehaviorSubject.create<String>()
    val isUserRatingAvailableObservable = BehaviorSubject.create<Boolean>()
    val userRatingRecommendationTextObservable = BehaviorSubject.create<String>()
    val ratingContainerBackground = isUserRatingAvailableObservable.map { ratingAvailable ->
        if (ratingAvailable) ContextCompat.getDrawable(context, R.drawable.gray_background_ripple)
        else (ContextCompat.getDrawable(context, R.color.gray1))
    }
    val numberOfReviewsObservable = BehaviorSubject.create<String>()
    val hotelLatLngObservable = BehaviorSubject.create<DoubleArray>()
    val discountPercentageBackgroundObservable = BehaviorSubject.create<Int>()
    val discountPercentageObservable = BehaviorSubject.create<Pair<String, String>>()
    val showDiscountPercentageObservable = BehaviorSubject.create<Boolean>(false)
    val shopWithPointsObservable = PublishSubject.create<Boolean>()
    val showAirAttachedObservable = PublishSubject.create<Boolean>()
    val showAirAttachSWPImageObservable = BehaviorSubject.create<Boolean>(false)
    val hasVipAccessObservable = BehaviorSubject.create<Boolean>(false)
    val hasVipAccessLoyaltyObservable = BehaviorSubject.create<Boolean>(false)
    val hasRegularLoyaltyPointsAppliedObservable = BehaviorSubject.create<Boolean>(false)
    val promoMessageObservable = BehaviorSubject.create<String>("")
    val promoImageObservable = BehaviorSubject.create<Int>(0)

    val depositInfoContainerClickObservable = BehaviorSubject.create<Pair<String, HotelOffersResponse.HotelRoomResponse>>()
    val scrollToRoom = PublishSubject.create<Unit>()
    val changeDates = PublishSubject.create<Unit>()
    val hotelSelectedObservable = PublishSubject.create<Unit>()
    private val allRoomsSoldOut = BehaviorSubject.create<Boolean>(false)

    var isCurrentLocationSearch = false
    var selectedRoomIndex = -1
    val loadTimeData = PageUsableData()

    private var roomSubscriptions: CompositeSubscription? = null

    private val offersObserver = endlessObserver<HotelOffersResponse> { response ->
        offerReturned(response)
    }

    val bookByPhoneContainerClickObserver: Observer<Unit> = endlessObserver {
        var supportPhoneNumber = when (userStateManager.getCurrentUserLoyaltyTier()) {
            LoyaltyMembershipTier.BASE -> PointOfSale.getPointOfSale().supportPhoneNumberBaseTier
            LoyaltyMembershipTier.MIDDLE -> PointOfSale.getPointOfSale().supportPhoneNumberMiddleTier
            LoyaltyMembershipTier.TOP -> PointOfSale.getPointOfSale().supportPhoneNumberTopTier
            else -> getTelesalesNumber()
        }

        if (supportPhoneNumber == null) {
            supportPhoneNumber = PointOfSale.getPointOfSale().defaultSupportPhoneNumber
        }

        SocialUtils.call(context, supportPhoneNumber)
        trackHotelDetailBookPhoneClick()
    }

    val mapClickedSubject = PublishSubject.create<Unit>()

    val reviewsClickedSubject = PublishSubject.create<Unit>()

    val renovationContainerClickObserver: Observer<Unit> = endlessObserver {
        val renovationInfo = Pair<String, String>(context.resources.getString(R.string.renovation_notice),
                hotelOffersResponse.hotelRenovationText?.content ?: "")
        hotelRenovationObservable.onNext(renovationInfo)
        trackHotelRenovationInfoClick()
    }

    val resortFeeContainerClickObserver: Observer<Unit> = endlessObserver {
        val renovationInfo = Pair<String, String>(context.resources.getString(R.string.additional_fees),
                hotelOffersResponse.hotelMandatoryFeesText?.content ?: "")
        hotelRenovationObservable.onNext(renovationInfo)
        trackHotelResortFeeInfoClick()
    }

    val payLaterInfoContainerClickObserver: Observer<Unit> = endlessObserver {
        hotelPayLaterInfoObservable.onNext(Pair(hotelOffersResponse.hotelCountry, hotelOffersResponse.hotelRoomResponse))
        HotelTracking.trackHotelEtpInfo()
    }

    val payByPhoneContainerVisibility = Observable.combineLatest(showBookByPhoneObservable, hotelSoldOut) { showBookByPhoneObservable, hotelSoldOut -> showBookByPhoneObservable && !hotelSoldOut }

    val hotelMessagingContainerVisibility = Observable.combineLatest(showDiscountPercentageObservable, hasVipAccessObservable, promoMessageObservable, hotelSoldOut, hasRegularLoyaltyPointsAppliedObservable, showAirAttachSWPImageObservable)
    {
        hasDiscount, hasVipAccess, promoMessage, hotelSoldOut, hasRegularLoyaltyPointsApplied, shouldShowAirAttachSWPImage ->
        (hasDiscount || hasVipAccess || Strings.isNotEmpty(promoMessage) || hasRegularLoyaltyPointsApplied || shouldShowAirAttachSWPImage) && !hotelSoldOut
    }

    val etpContainerVisibility = Observable.combineLatest(hasETPObservable, hotelSoldOut) { hasETPOffer, hotelSoldOut -> hasETPOffer && !hotelSoldOut }

    val paramsSubject = BehaviorSubject.create<HotelSearchParams>()

    // Every time a new hotel is emitted, emit an Observable<Hotel>
    // that will return the outer hotel every time reviews is clicked
    val reviewsClickedWithHotelData: Observable<HotelOffersResponse> = Observable.switchOnNext(hotelOffersSubject.map { hotel ->
        reviewsClickedSubject.map {
            hotel
        }
    })

    protected val userStateManager = Ui.getApplication(context).appComponent().userStateManager()

    init {
        allRoomsSoldOut.subscribe(hotelSoldOut)

        selectedRoomSoldOut.subscribe {
            if (selectedRoomIndex != -1) {
                if (shouldGroupAndSortRoom()) {
                    hotelRoomDetailViewModelsObservable.value.elementAt(selectedRoomIndex).roomSoldOut.onNext(true)
                } else {
                    hotelRoomRateViewModelsObservable.value.elementAt(selectedRoomIndex).collapseRoomObservable.onNext(Unit)
                    hotelRoomRateViewModelsObservable.value.elementAt(selectedRoomIndex).roomSoldOut.onNext(true)
                }
            }
        }

        hotelOffersSubject.subscribe(offersObserver)

        hotelRoomRateViewModelsObservable.subscribe { roomRateViewModels ->
            roomSubscriptions?.unsubscribe()
            if (roomRateViewModels.isEmpty()) {
                return@subscribe
            }

            roomSubscriptions = CompositeSubscription()
            roomRateViewModels.forEach { roomViewModel ->
                roomSubscriptions?.add(roomViewModel.roomSoldOut.subscribe {
                    if (areAllRoomsSoldOut(roomRateViewModels)) {
                        // In the situation where all once available rooms become sold out, update the experience
                        allRoomsSoldOut.onNext(true)
                    }
                })
            }

            allRoomsSoldOut.onNext(areAllRoomsSoldOut(roomRateViewModels))
        }

        hotelRoomDetailViewModelsObservable.subscribe { roomDetailViewModels ->
            roomSubscriptions?.unsubscribe()
            if (roomDetailViewModels.isEmpty()) {
                return@subscribe
            }

            roomSubscriptions = CompositeSubscription()
            roomDetailViewModels.forEach { roomViewModel ->
                roomSubscriptions?.add(roomViewModel.roomSoldOut.subscribe {
                    if (areAllRoomDetailsSoldOut(roomDetailViewModels)) {
                        allRoomsSoldOut.onNext(true)
                    }
                })
            }

            allRoomsSoldOut.onNext(areAllRoomDetailsSoldOut(roomDetailViewModels))
        }

        rowExpandingObservable.subscribe { indexOfRowBeingExpanded ->
            //collapse already expanded row if there is one
            if (!AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelRoomRateExpanded)) {
                val previousRowIndex = lastExpandedRowIndexObservable.value
                if (previousRowIndex >= 0
                        && previousRowIndex < hotelRoomRateViewModelsObservable.value.size
                        && previousRowIndex != indexOfRowBeingExpanded) {
                    hotelRoomRateViewModelsObservable.value.elementAt(previousRowIndex).collapseRoomWithAnimationObservable.onNext(Unit)
                }
                lastExpandedRowIndexObservable.onNext(indexOfRowBeingExpanded)
            }
        }

        hotelSelectedObservable.subscribe { loadTimeData.markPageLoadStarted(System.currentTimeMillis()) }
    }

    fun shouldGroupAndSortRoom(): Boolean {
        return getLOB() == LineOfBusiness.HOTELS &&
                AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelGroupRoomRate)
    }

    fun groupAndSortRoomList(roomList: List<HotelOffersResponse.HotelRoomResponse>): LinkedHashMap<String, ArrayList<HotelOffersResponse.HotelRoomResponse>> {
        val roomOrderedMap = LinkedHashMap<String, ArrayList<HotelOffersResponse.HotelRoomResponse>>()
        val sortedRoomList = roomList.sortedWith(compareBy({ it.rateInfo.chargeableRateInfo.priceToShowUsers }, { it.hasFreeCancellation }, { if (it.valueAdds != null) -it.valueAdds.count() else 0 }))

        sortedRoomList.forEach { room ->
            if (roomOrderedMap[room.roomTypeCode] == null) {
                roomOrderedMap[room.roomTypeCode] = ArrayList<HotelOffersResponse.HotelRoomResponse>()
            }

            roomOrderedMap[room.roomTypeCode]?.add(room)
        }

        return roomOrderedMap
    }

    private fun areAllRoomDetailsSoldOut(viewModels: ArrayList<HotelRoomDetailViewModel>): Boolean {
        for (vm in viewModels) {
            if (!vm.roomSoldOut.value) return false
        }
        return true
    }

    private fun areAllRoomsSoldOut(viewModels: ArrayList<HotelRoomRateViewModel>): Boolean {
        var soldOutCount = 0
        for (vm in viewModels) {
            if (vm.roomSoldOut.value) soldOutCount++
        }
        return soldOutCount == viewModels.size
    }

    fun addViewsAfterTransition() {
        if (hotelOffersResponse.hotelRoomResponse != null && hotelOffersResponse.hotelRoomResponse.isNotEmpty()) {
            uniqueValueAddForRooms = getValueAdd(hotelOffersResponse.hotelRoomResponse)
            roomResponseListObservable.onNext(Pair(hotelOffersResponse.hotelRoomResponse, uniqueValueAddForRooms))
        }

        val listHotelInfo = ArrayList<HotelOffersResponse.HotelText>()
        //Set up entire text for hotel info
        if (hotelOffersResponse.hotelOverviewText != null && hotelOffersResponse.hotelOverviewText.size > 1) {
            for (index in 1..hotelOffersResponse.hotelOverviewText.size - 1) {
                listHotelInfo.add(hotelOffersResponse.hotelOverviewText[index])
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

        renovationObservable.onNext(hotelOffersResponse.hotelRenovationText?.content != null)

        // common amenities text
        if (CollectionUtils.isNotEmpty(hotelOffersResponse.hotelRoomResponse)) {
            val atLeastOneRoomHasNoValueAdds = hotelOffersResponse.hotelRoomResponse.any { it.valueAdds == null }
            if (!atLeastOneRoomHasNoValueAdds) {
                val allValueAdds: List<List<String>> = getAllValueAdds(hotelOffersResponse)
                if (!allValueAdds.isEmpty()) {
                    val commonValueAdds: List<String> = getCommonValueAdds(allValueAdds)

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
                    .filter { it.payLaterOffer != null }.toCollection(ArrayList())

            etpUniqueValueAddForRooms = getValueAdd(etpOffersList)
        }

        val firstRoomDetails = hotelOffersResponse.hotelRoomResponse?.firstOrNull()
        if (firstRoomDetails?.rateInfo?.chargeableRateInfo?.showResortFeeMessage ?: false) {
            val rate = firstRoomDetails!!.rateInfo.chargeableRateInfo
            val resortText: String

            if (hotelOffersResponse.isPackage && PointOfSale.getPointOfSale().showResortFeesInHotelLocalCurrency()) {
                val df = DecimalFormat("#.00")
                val resortFees = Money(BigDecimal(rate.totalMandatoryFees.toDouble()), CurrencyUtils.currencyForLocale(hotelOffersResponse.hotelCountry))
                resortText = Phrase.from(context, R.string.non_us_resort_fee_format_TEMPLATE)
                        .put("amount", df.format(resortFees.amount)).put("currency", resortFees.currencyCode).format().toString()
            } else {
                val resortFees = Money(BigDecimal(rate.totalMandatoryFees.toDouble()), rate.currencyCode)
                resortText = resortFees.getFormattedMoney(Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL)
            }

            hotelResortFeeObservable.onNext(resortText)
            val includedNotIncludedStrId = if (rate.resortFeeInclusion) R.string.included_in_the_price else R.string.not_included_in_the_price
            hotelResortFeeIncludedTextObservable.onNext(context.resources.getString(includedNotIncludedStrId))
        } else {
            hotelResortFeeObservable.onNext("")
            hotelResortFeeIncludedTextObservable.onNext("")
        }

        showBookByPhoneObservable.onNext(shouldShowBookByPhone())

        loadTimeData.markAllViewsLoaded(System.currentTimeMillis())
        trackHotelDetailLoad(false)
    }

    fun hasEtpOffer(response: HotelOffersResponse): Boolean {
        return CollectionUtils.isNotEmpty(response.hotelRoomResponse) && response.hotelRoomResponse.any { it.payLaterOffer != null }
    }

    //get list of unique amenity for hotel room offers
    //we display this unique amenity offered in the expanded room view
    fun getValueAdd(hotelRooms: List<HotelOffersResponse.HotelRoomResponse>?): List<String> {
        if (CollectionUtils.isEmpty(hotelRooms)) {
            return emptyList()
        }
        val commonValueAdds = getCommonValueAdds(hotelOffersResponse)
        val list = Array(hotelRooms!!.size, { i -> "" }).toMutableList()
        for (iRoom in 0..hotelRooms.size - 1) {
            val rate = hotelOffersResponse.hotelRoomResponse[iRoom]
            if (rate.valueAdds != null) {
                val unique = rate.valueAdds.filter { !commonValueAdds.contains(it.description) }
                if (unique.size > 0) {
                    list.add(iRoom, context.getString(R.string.value_add_template, unique[0].description.toLowerCase(Locale.getDefault())))
                }
            }
        }
        return list
    }

    @CallSuper
    protected open fun offerReturned(offerResponse: HotelOffersResponse) {
        hotelOffersResponse = offerResponse
        hotelOffersResponseSubject.onNext(hotelOffersResponse)

        galleryObservable.onNext(getGalleryUrls())

        val amenityList = arrayListOf<Amenity>()
        if (offerResponse.hotelAmenities != null) {
            amenityList.addAll(Amenity.amenitiesToShow(offerResponse.hotelAmenities))
        }

        if (amenityList.isEmpty()) {
            noAmenityObservable.onNext(Unit)
        } else {
            //Here have to pass the list of amenities which we want to show
            amenitiesListObservable.onNext(amenityList)
        }

        hotelNameObservable.onNext(offerResponse.hotelName)

        hotelRatingObservable.onNext(offerResponse.hotelStarRating.toFloat())
        hotelRatingObservableVisibility.onNext(offerResponse.hotelStarRating > 0)
        hotelRatingContentDescriptionObservable.onNext(HotelsV2DataUtil.getHotelRatingContentDescription(context, offerResponse.hotelStarRating.toInt()))

        allRoomsSoldOut.onNext(CollectionUtils.isEmpty(offerResponse.hotelRoomResponse))
        lastExpandedRowIndexObservable.onNext(-1)

        val firstHotelRoomResponse = offerResponse.hotelRoomResponse?.firstOrNull()
        if (firstHotelRoomResponse != null) {
            val rate = firstHotelRoomResponse.rateInfo.chargeableRateInfo
            discountPercentageBackgroundObservable.onNext(if (rate.isShowAirAttached()) R.drawable.air_attach_background else R.drawable.guest_rating_background)
            showAirAttachSWPImageObservable.onNext(rate.loyaltyInfo?.isBurnApplied ?: false && rate.isShowAirAttached())
            showAirAttachedObservable.onNext(rate.isShowAirAttached())
        }

        userRatingObservable.onNext(offerResponse.hotelGuestRating.toString())
        userRatingBackgroundColorObservable.onNext(getGuestRatingBackground(context))
        userRatingRecommendationTextObservable.onNext(getGuestRatingText(offerResponse.hotelGuestRating.toFloat(), context.resources))
        isUserRatingAvailableObservable.onNext(offerResponse.hotelGuestRating > 0)

        numberOfReviewsObservable.onNext(
                if (offerResponse.totalReviews > 0)
                    context.resources.getQuantityString(R.plurals.hotel_number_of_reviews, offerResponse.totalReviews, HotelUtils.formattedReviewCount(offerResponse.totalReviews))
                else context.resources.getString(R.string.zero_reviews))

        val chargeableRateInfo = offerResponse.hotelRoomResponse?.firstOrNull()?.rateInfo?.chargeableRateInfo
        val isRateShopWithPoints = chargeableRateInfo?.loyaltyInfo?.isBurnApplied ?: false
        val discountPercentage: Int? = chargeableRateInfo?.discountPercent?.toInt()
        discountPercentageObservable.onNext(Pair(Phrase.from(context.resources, R.string.hotel_discount_percent_Template)
                .put("discount", discountPercentage ?: 0).format().toString(),
                Phrase.from(context, R.string.hotel_discount_cont_desc_TEMPLATE)
                        .put("percent", Math.abs(discountPercentage ?: 0)).format().toString()))

        showDiscountPercentageObservable.onNext(!offerResponse.isPackage && !isRateShopWithPoints && chargeableRateInfo?.isDiscountPercentNotZero ?: false)
        shopWithPointsObservable.onNext(isRateShopWithPoints)
        val isVipAccess = offerResponse.isVipAccess && PointOfSale.getPointOfSale().supportsVipAccess()
        hasVipAccessObservable.onNext(isVipAccess)
        hasVipAccessLoyaltyObservable.onNext(isVipAccess && offerResponse.doesAnyHotelRateOfAnyRoomHaveLoyaltyInfo)
        hasRegularLoyaltyPointsAppliedObservable.onNext(!isVipAccess && offerResponse.doesAnyHotelRateOfAnyRoomHaveLoyaltyInfo)
        promoMessageObservable.onNext(getPromoText(firstHotelRoomResponse))
        promoImageObservable.onNext(getPromoImage(firstHotelRoomResponse))
        hasFreeCancellationObservable.onNext(hasFreeCancellation(offerResponse))
        hasBestPriceGuaranteeObservable.onNext(PointOfSale.getPointOfSale().displayBestPriceGuarantee())
        val hasETPOffer = hasEtpOffer(offerResponse)
        hasETPObservable.onNext(hasETPOffer)

        if (offerResponse.firstHotelOverview != null) {
            val sectionBody = HtmlCompat.stripHtml(offerResponse.firstHotelOverview)
            sectionBodyObservable.onNext(sectionBody)
        }

        hotelLatLngObservable.onNext(doubleArrayOf(offerResponse.latitude, offerResponse.longitude))
    }

    private fun getGalleryUrls(): ArrayList<HotelMedia> {
        val galleryUrls = ArrayList<HotelMedia>()

        val images = Images.getHotelImages(hotelOffersResponse, R.drawable.room_fallback)
        if (images.isNotEmpty()) {
            galleryUrls.addAll(images.toMutableList())
        } else {
            val placeHolder = HotelMedia()
            placeHolder.setIsPlaceholder(true)
            galleryUrls.add(placeHolder)
        }
        return galleryUrls
    }

    private fun getAllValueAdds(hotelOffersResponse: HotelOffersResponse): List<List<String>> {
        val allValueAdds: List<List<String>> = hotelOffersResponse.hotelRoomResponse
                .filter { it.valueAdds != null }
                .map {
                    it.valueAdds.map { it.description }
                }
        return allValueAdds
    }

    private fun getCommonValueAdds(hotelOffersResponse: HotelOffersResponse): List<String> {
        return getCommonValueAdds(getAllValueAdds(hotelOffersResponse))
    }

    private fun getCommonValueAdds(allValueAdds: List<List<String>>): List<String> {
        if (!allValueAdds.isEmpty()) {
            return allValueAdds
                    .drop(1)
                    .fold(allValueAdds.first().toMutableList(), { initial, nextValueAdds ->
                        initial.retainAll(nextValueAdds)
                        initial
                    })
        }
        return emptyList()
    }

    private fun hasFreeCancellation(response: HotelOffersResponse): Boolean {
        return CollectionUtils.isNotEmpty(response.hotelRoomResponse) && response.hotelRoomResponse.any { it.hasFreeCancellation == true }
    }

    private fun getPromoText(roomOffer: HotelOffersResponse.HotelRoomResponse?): String {
        // NOTE: Any changes to this logic should also be made in HotelViewModel (see: urgencyMessageObservable)
        if (roomOffer == null) {
            return ""
        }

        val roomsLeft = roomOffer.currentAllotment.toInt()
        return if (hasMemberDeal(roomOffer)) {
            context.resources.getString(R.string.member_pricing)
        } else if (roomsLeft in 1..ROOMS_LEFT_CUTOFF) {
            context.resources.getQuantityString(R.plurals.num_rooms_left, roomsLeft, roomsLeft)
        } else if (roomOffer.isSameDayDRR) {
            context.resources.getString(R.string.tonight_only)
        } else if (roomOffer.isDiscountRestrictedToCurrentSourceType &&
                ProductFlavorFeatureConfiguration.getInstance().hotelDealImageDrawable == 0) {
            context.resources.getString(R.string.mobile_exclusive)
        } else {
            ""
        }
    }

    fun getPromoImage(roomOffer: HotelOffersResponse.HotelRoomResponse?): Int {
        if (roomOffer == null) {
            return 0
        }
        // Return PromoImage only for Mobile Exclusive type of deals.
        if (!hasMemberDeal(roomOffer) && roomOffer.currentAllotment.toInt() > ROOMS_LEFT_CUTOFF &&
                !roomOffer.isSameDayDRR && roomOffer.isDiscountRestrictedToCurrentSourceType) {
            return ProductFlavorFeatureConfiguration.getInstance().hotelDealImageDrawable
        }
        return 0
    }
}
