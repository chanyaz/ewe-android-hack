package com.expedia.vm

import android.content.Context
import android.support.annotation.CallSuper
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.data.HotelMedia
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.LoyaltyMembershipTier
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.extensions.ObservableOld
import com.expedia.bookings.features.Features
import com.expedia.bookings.hotel.DEFAULT_HOTEL_GALLERY_CODE
import com.expedia.bookings.hotel.data.Amenity
import com.expedia.bookings.hotel.util.HotelResortFeeFormatter
import com.expedia.bookings.hotel.widget.adapter.priceFormatter
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.bookings.tracking.hotel.PageUsableData
import com.expedia.bookings.utils.CollectionUtils
import com.expedia.bookings.utils.HotelUtils
import com.expedia.bookings.utils.HotelsV2DataUtil
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.Ui
import com.expedia.util.LoyaltyUtil
import com.expedia.util.endlessObserver
import com.expedia.util.getGuestRatingText
import com.mobiata.android.FormatUtils
import com.mobiata.android.SocialUtils
import com.squareup.phrase.Phrase
import io.reactivex.Observer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.joda.time.LocalDate
import java.math.BigDecimal
import java.util.ArrayList
import java.util.LinkedHashMap
import java.util.Locale
import kotlin.properties.Delegates

abstract class BaseHotelDetailViewModel(val context: Context) {

    abstract fun isChangeDatesEnabled(): Boolean
    abstract fun getLobPriceObservable(rate: HotelRate)
    abstract fun pricePerDescriptor(): String
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
    abstract fun trackHotelDetailRoomGalleryClick()
    abstract fun trackHotelDetailLoad(isRoomSoldOut: Boolean)
    abstract fun shouldShowBookByPhone(): Boolean
    abstract fun getTelesalesNumber(): String

    val checkInDate: LocalDate? get() = paramsSubject.value.checkIn
    val checkOutDate: LocalDate? get() = paramsSubject.value.checkOut

    val roomSelectedSubject = BehaviorSubject.create<HotelOffersResponse.HotelRoomResponse>()
    val hotelSoldOut = BehaviorSubject.createDefault<Boolean>(false)
    val selectedRoomSoldOut = PublishSubject.create<Unit>()
    val hotelPriceContentDesc = PublishSubject.create<String>()

    val hotelOffersSubject = BehaviorSubject.create<HotelOffersResponse>()
    var hotelOffersResponse: HotelOffersResponse by Delegates.notNull()
    var etpOffersList = ArrayList<HotelOffersResponse.HotelRoomResponse>()

    val sectionBodyObservable = BehaviorSubject.create<String>()
    val showBookByPhoneObservable = BehaviorSubject.createDefault<Boolean>(false)
    val galleryObservable = PublishSubject.create<ArrayList<HotelMedia>>()

    val commonAmenityTextObservable = BehaviorSubject.create<String>()

    val amenitiesListObservable = BehaviorSubject.create<List<Amenity>>()
    val noAmenityObservable = BehaviorSubject.create<Unit>()

    val hasETPObservable = BehaviorSubject.createDefault<Boolean>(false)
    val hasFreeCancellationObservable = BehaviorSubject.create<Boolean>()
    val renovationObservable = BehaviorSubject.create<Boolean>()
    val hotelRenovationObservable = BehaviorSubject.create<Pair<String, String>>()
    val hotelPayLaterInfoObservable = BehaviorSubject.create<Pair<String, List<HotelOffersResponse.HotelRoomResponse>>>()
    val vipAccessInfoObservable = BehaviorSubject.create<Unit>()
    val propertyInfoListObservable = BehaviorSubject.createDefault<List<HotelOffersResponse.HotelText>>(emptyList())

    val roomResponseListObservable = BehaviorSubject.create<Pair<List<HotelOffersResponse.HotelRoomResponse>, List<String>>>()
    var uniqueValueAddForRooms: List<String> by Delegates.notNull()

    var etpUniqueValueAddForRooms: List<String> by Delegates.notNull()
    val etpRoomResponseListObservable = BehaviorSubject.create<Pair<List<HotelOffersResponse.HotelRoomResponse>, List<String>>>()

    val hotelRoomDetailViewModelsObservable = BehaviorSubject.create<ArrayList<HotelRoomDetailViewModel>>()

    val hotelResortFeeObservable = BehaviorSubject.createDefault<String>("")
    val hotelResortFeeIncludedTextObservable = BehaviorSubject.create<String>()
    val hotelNameObservable = BehaviorSubject.create<String>()
    val hotelRatingObservable = BehaviorSubject.create<Float>()
    val hotelRatingContentDescriptionObservable = BehaviorSubject.create<String>()
    val hotelRatingObservableVisibility = BehaviorSubject.create<Boolean>()
    val onlyShowTotalPrice = BehaviorSubject.createDefault<Boolean>(false)
    val roomPriceToShowCustomer = BehaviorSubject.create<String>()
    val totalPriceObservable = BehaviorSubject.create<String>()
    val priceToShowCustomerObservable = BehaviorSubject.create<String>()
    val searchInfoObservable = BehaviorSubject.create<String>()
    val searchInfoTextColorObservable = BehaviorSubject.create<Int>()
    val searchInfoGuestsObservable = BehaviorSubject.create<String>()
    val searchDatesObservable = BehaviorSubject.create<String>()
    val userRatingObservable = BehaviorSubject.create<String>()
    val isUserRatingAvailableObservable = BehaviorSubject.create<Boolean>()
    val userRatingRecommendationTextObservable = BehaviorSubject.create<String>()
    val ratingContainerBackground = isUserRatingAvailableObservable.map { ratingAvailable ->
        if (ratingAvailable) ContextCompat.getDrawable(context, R.drawable.white_back_gray_ripple)
        else (ContextCompat.getDrawable(context, R.color.white))
    }
    val numberOfReviewsObservable = BehaviorSubject.create<String>()
    val hotelLatLngObservable = BehaviorSubject.create<DoubleArray>()
    val memberOnlyDealTagVisibilityObservable = BehaviorSubject.create<Boolean>()
    val discountPercentageBackgroundObservable = BehaviorSubject.create<Int>()
    val discountPercentageTextColorObservable = BehaviorSubject.create<Int>()
    val discountPercentageObservable = BehaviorSubject.create<Pair<String, String>>()
    val showDiscountPercentageObservable = BehaviorSubject.createDefault<Boolean>(false)
    val shopWithPointsObservable = PublishSubject.create<Boolean>()
    val showAirAttachedObservable = BehaviorSubject.createDefault<Boolean>(false)
    val showGenericAttachedObservable = BehaviorSubject.createDefault<Boolean>(false)
    val hasVipAccessObservable = BehaviorSubject.createDefault<Boolean>(false)
    val hasVipLoyaltyPointsAppliedObservable = BehaviorSubject.createDefault<Boolean>(false)
    val hasRegularLoyaltyPointsAppliedObservable = BehaviorSubject.createDefault<Boolean>(false)
    val promoMessageObservable = BehaviorSubject.createDefault<String>("")
    val earnMessageObservable = BehaviorSubject.create<String>()
    val earnMessageVisibilityObservable = BehaviorSubject.create<Boolean>()
    val isDatelessObservable: BehaviorSubject<Boolean> = BehaviorSubject.create<Boolean>()
    val newDatesSelected: BehaviorSubject<Pair<LocalDate, LocalDate>> = BehaviorSubject.create<Pair<LocalDate, LocalDate>>()

    val strikeThroughPriceObservable = BehaviorSubject.create<CharSequence>()
    val strikeThroughPriceGreaterThanPriceToShowUsersObservable = PublishSubject.create<Boolean>()
    val depositInfoContainerClickObservable = BehaviorSubject.create<Pair<String, HotelOffersResponse.HotelRoomResponse>>()
    val scrollToRoom = PublishSubject.create<Unit>()
    val returnToSearchSubject = PublishSubject.create<Unit>()
    val hotelSelectedObservable = PublishSubject.create<Unit>()
    val allRoomsSoldOut = BehaviorSubject.createDefault<Boolean>(false)

    lateinit var hotelId: String
    var isCurrentLocationSearch = false
    var selectedRoomIndex = -1
    val loadTimeData = PageUsableData()

    private var hasSoldOutRoom = false
    private val resortFeeFormatter = HotelResortFeeFormatter()
    private var roomSubscriptions: CompositeDisposable? = null

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

    val reviewsDataObservable = BehaviorSubject.create<HotelOffersResponse>()

    val reviewsClickObserver: Observer<Unit> = endlessObserver {
        reviewsDataObservable.onNext(hotelOffersResponse)
    }

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

    val strikeThroughPriceVisibility = ObservableOld.combineLatest(strikeThroughPriceGreaterThanPriceToShowUsersObservable, hotelSoldOut, shopWithPointsObservable, showAirAttachedObservable, showGenericAttachedObservable) {
        strikeThroughPriceGreaterThanPriceToShowUsers, hotelSoldOut, shopWithPointsObservable, showAirAttachedObservable, showGenericAttachedObservable ->
        (strikeThroughPriceGreaterThanPriceToShowUsers && !hotelSoldOut) && (shopWithPointsObservable || !showAirAttachedObservable || showGenericAttachedObservable)
    }

    val perNightVisibility = ObservableOld.combineLatest(onlyShowTotalPrice, hotelSoldOut) { onlyShowTotalPrice, hotelSoldOut ->
        !(onlyShowTotalPrice || hotelSoldOut)
    }

    val payByPhoneContainerVisibility = ObservableOld.combineLatest(showBookByPhoneObservable, hotelSoldOut) { showBookByPhoneObservable, hotelSoldOut -> showBookByPhoneObservable && !hotelSoldOut }

    val hotelMessagingContainerVisibility = ObservableOld.combineLatest(showDiscountPercentageObservable, hasVipAccessObservable, promoMessageObservable, hotelSoldOut, hasRegularLoyaltyPointsAppliedObservable, showAirAttachedObservable, showGenericAttachedObservable) {
        hasDiscount, hasVipAccess, promoMessage, hotelSoldOut, hasRegularLoyaltyPointsApplied, showAirAttached, showGenericAttached ->
        (hasDiscount || hasVipAccess || Strings.isNotEmpty(promoMessage) || hasRegularLoyaltyPointsApplied || showAirAttached || showGenericAttached) && !hotelSoldOut
    }

    val etpContainerVisibility = ObservableOld.combineLatest(hasETPObservable, hotelSoldOut) { hasETPOffer, hotelSoldOut -> hasETPOffer && !hotelSoldOut }

    val paramsSubject = BehaviorSubject.create<HotelSearchParams>()

    protected val userStateManager = Ui.getApplication(context).appComponent().userStateManager()
    private val galleryManager = Ui.getApplication(context).appComponent().hotelGalleryManager()

    init {
        allRoomsSoldOut.subscribe(hotelSoldOut)

        selectedRoomSoldOut.subscribe {
            if (selectedRoomIndex != -1) {
                hasSoldOutRoom = true
                hotelRoomDetailViewModelsObservable.value.elementAt(selectedRoomIndex).roomSoldOut.onNext(true)
            }
        }

        onlyShowTotalPrice.subscribe { onlyShowTotalPrice ->
            (if (onlyShowTotalPrice) totalPriceObservable else priceToShowCustomerObservable).subscribe(roomPriceToShowCustomer)
        }

        hotelRoomDetailViewModelsObservable.subscribe { roomDetailViewModels ->
            roomSubscriptions?.dispose()
            if (roomDetailViewModels.isEmpty()) {
                return@subscribe
            }

            roomSubscriptions = CompositeDisposable()
            roomDetailViewModels.forEach { roomViewModel ->
                roomSubscriptions?.add(roomViewModel.roomSoldOut.subscribe {
                    if (areAllRoomDetailsSoldOut(roomDetailViewModels)) {
                        allRoomsSoldOut.onNext(true)
                    }
                })
            }

            allRoomsSoldOut.onNext(areAllRoomDetailsSoldOut(roomDetailViewModels))
        }

        hotelSelectedObservable.subscribe { loadTimeData.markPageLoadStarted(System.currentTimeMillis()) }

        hotelOffersSubject.subscribe { response ->
            offerReturned(response)
        }
    }

    fun groupAndSortRoomList(roomList: List<HotelOffersResponse.HotelRoomResponse>): LinkedHashMap<String, ArrayList<HotelOffersResponse.HotelRoomResponse>> {
        val roomOrderedMap = LinkedHashMap<String, ArrayList<HotelOffersResponse.HotelRoomResponse>>()
        val sortedRoomList = roomList.sortedWith(compareBy({ it.rateInfo.chargeableRateInfo.priceToShowUsers }, { it.hasFreeCancellation }, { if (it.valueAdds != null) -it.valueAdds.count() else 0 }))

        sortedRoomList.forEach { room ->
            val groupingKey = room.roomGroupingKey()

            if (roomOrderedMap[groupingKey] == null) {
                roomOrderedMap[groupingKey] = ArrayList()
            }

            roomOrderedMap[groupingKey]?.add(room)
        }

        return roomOrderedMap
    }

    fun addViewsAfterTransition() {
        fetchHotelImages()

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
        val resortFee = resortFeeFormatter.getResortFee(context, firstRoomDetails,
                hotelOffersResponse.isPackage, hotelOffersResponse.hotelCountry)
        val inclusionText = resortFeeFormatter.getResortFeeInclusionText(context, firstRoomDetails)
        hotelResortFeeObservable.onNext(resortFee)
        hotelResortFeeIncludedTextObservable.onNext(inclusionText)

        showBookByPhoneObservable.onNext(shouldShowBookByPhone())

        loadTimeData.markAllViewsLoaded(System.currentTimeMillis())
        trackHotelDetailLoad(false)
    }

    fun hasEtpOffer(response: HotelOffersResponse): Boolean {
        return CollectionUtils.isNotEmpty(response.hotelRoomResponse) && response.hotelRoomResponse.any { it.payLaterOffer != null }
    }

    //get list of unique amenities for hotel room offers
    //we display this unique amenities offered in the expanded room view
    fun getValueAdd(hotelRooms: List<HotelOffersResponse.HotelRoomResponse>?): List<String> {
        if (CollectionUtils.isEmpty(hotelRooms)) {
            return emptyList()
        }
        val commonValueAdds = getCommonValueAdds(hotelOffersResponse)
        val list = Array(hotelRooms!!.size, { "" }).toMutableList()
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

        galleryManager?.saveHotelOfferMedia(offerResponse)

        val amenityList = arrayListOf<Amenity>()
        if (offerResponse.hotelAmenities != null) {
            amenityList.addAll(Amenity.amenitiesToShow(offerResponse.hotelAmenities, context))
        }

        if (amenityList.isEmpty()) {
            noAmenityObservable.onNext(Unit)
        } else {
            //Here have to pass the list of amenities which we want to show
            amenitiesListObservable.onNext(amenityList)
        }

        hotelNameObservable.onNext(offerResponse.hotelName ?: "")

        hotelRatingObservable.onNext(offerResponse.hotelStarRating.toFloat())
        hotelRatingObservableVisibility.onNext(offerResponse.hotelStarRating > 0)
        hotelRatingContentDescriptionObservable.onNext(HotelsV2DataUtil.getHotelRatingContentDescription(context, offerResponse.hotelStarRating))

        val noRoomsAvailable = CollectionUtils.isEmpty(offerResponse.hotelRoomResponse)
        hasSoldOutRoom = noRoomsAvailable
        allRoomsSoldOut.onNext(noRoomsAvailable)

        val firstHotelRoomResponse = offerResponse.hotelRoomResponse?.firstOrNull()
        if (firstHotelRoomResponse != null) {
            val rate = firstHotelRoomResponse.rateInfo.chargeableRateInfo
            onlyShowTotalPrice.onNext(rate.getUserPriceType() == HotelRate.UserPriceType.RATE_FOR_WHOLE_STAY_WITH_TAXES)
            getLobPriceObservable(rate)
            totalPriceObservable.onNext(Money(BigDecimal(rate.priceToShowUsers.toDouble()), rate.currencyCode).getFormattedMoney(Money.F_NO_DECIMAL))

            val hasMemberDeal = hasMemberDeal(firstHotelRoomResponse)
            memberOnlyDealTagVisibilityObservable.onNext(!offerResponse.doesAnyRoomHaveAttach && hasMemberDeal)
            val discountPercentageBackground = when {
                shouldUseGenericAttach(offerResponse) -> R.drawable.member_only_discount_percentage_background
                shouldUseLegacyAttach(offerResponse) -> R.drawable.air_attach_background
                hasMemberDeal(firstHotelRoomResponse) -> R.drawable.member_only_discount_percentage_background
                else -> R.drawable.discount_percentage_background
            }
            discountPercentageBackgroundObservable.onNext(discountPercentageBackground)

            val discountPercentageTextColor = when {
                shouldUseGenericAttach(offerResponse) -> ContextCompat.getColor(context, R.color.member_pricing_text_color)
                shouldUseLegacyAttach(offerResponse) -> ContextCompat.getColor(context, R.color.white)
                hasMemberDeal -> ContextCompat.getColor(context, R.color.member_pricing_text_color)
                else -> ContextCompat.getColor(context, R.color.white)
            }
            discountPercentageTextColorObservable.onNext(discountPercentageTextColor)
        }

        showAirAttachedObservable.onNext(shouldUseLegacyAttach(offerResponse))
        showGenericAttachedObservable.onNext(shouldUseGenericAttach(offerResponse))

        userRatingObservable.onNext(offerResponse.hotelGuestRating.toString())
        userRatingRecommendationTextObservable.onNext(getGuestRatingText(offerResponse.hotelGuestRating.toFloat(), context.resources))
        isUserRatingAvailableObservable.onNext(offerResponse.hotelGuestRating > 0)

        numberOfReviewsObservable.onNext(
                if (offerResponse.totalReviews > 0)
                    context.resources.getQuantityString(R.plurals.hotel_number_of_reviews, offerResponse.totalReviews, HotelUtils.formattedReviewCount(offerResponse.totalReviews))
                else context.resources.getString(R.string.zero_reviews))

        val chargeableRateInfo = offerResponse.hotelRoomResponse?.firstOrNull()?.rateInfo?.chargeableRateInfo
        val packageLoyaltyInformation = offerResponse.hotelRoomResponse?.firstOrNull()?.packageLoyaltyInformation
        val isRateShopWithPoints = offerResponse.doesAnyRoomHaveBurnApplied
        val discountPercentage: Int? = chargeableRateInfo?.discountPercent?.toInt()
        discountPercentageObservable.onNext(Pair(Phrase.from(context.resources, R.string.hotel_discount_percent_Template)
                .put("discount", discountPercentage ?: 0).format().toString(),
                Phrase.from(context, R.string.hotel_discount_cont_desc_TEMPLATE)
                        .put("percent", Math.abs(discountPercentage ?: 0)).format().toString()))

        showDiscountPercentageObservable.onNext(!offerResponse.isPackage && !isRateShopWithPoints && chargeableRateInfo?.isDiscountPercentNotZero ?: false)
        shopWithPointsObservable.onNext(isRateShopWithPoints)
        val isVipAccess = offerResponse.isVipAccess && PointOfSale.getPointOfSale().supportsVipAccess()
        hasVipAccessObservable.onNext(isVipAccess)
        hasVipLoyaltyPointsAppliedObservable.onNext(isVipAccess && offerResponse.doesAnyRoomHaveBurnApplied)
        hasRegularLoyaltyPointsAppliedObservable.onNext(!isVipAccess && offerResponse.doesAnyRoomHaveBurnApplied)
        promoMessageObservable.onNext(getPromoText(offerResponse))
        val earnMessage = LoyaltyUtil.getEarnMessagingString(context, offerResponse.isPackage, chargeableRateInfo?.loyaltyInfo?.earn, packageLoyaltyInformation?.earn)
        val earnMessageVisibility = LoyaltyUtil.shouldShowEarnMessage(earnMessage, offerResponse.isPackage)
        earnMessageObservable.onNext(earnMessage)
        earnMessageVisibilityObservable.onNext(earnMessageVisibility)

        val priceToShowUsers = chargeableRateInfo?.priceToShowUsers ?: 0f
        val strikethroughPriceToShowUsers = chargeableRateInfo?.strikethroughPriceToShowUsers ?: 0f

        val isStrikeThroughPriceGreaterThanPriceToShowUsers = priceToShowUsers < strikethroughPriceToShowUsers
        if (isStrikeThroughPriceGreaterThanPriceToShowUsers) {
            strikeThroughPriceObservable.onNext(priceFormatter(context.resources, chargeableRateInfo, true, !offerResponse.isPackage))
        }
        strikeThroughPriceGreaterThanPriceToShowUsersObservable.onNext(isStrikeThroughPriceGreaterThanPriceToShowUsers)
        hotelPriceContentDesc.onNext(getHotelPriceContentDescription(isStrikeThroughPriceGreaterThanPriceToShowUsers))

        hasFreeCancellationObservable.onNext(hasFreeCancellation(offerResponse))
        val hasETPOffer = hasEtpOffer(offerResponse)
        hasETPObservable.onNext(hasETPOffer)

        if (offerResponse.firstHotelOverview != null) {
            val sectionBody = HtmlCompat.stripHtml(offerResponse.firstHotelOverview)
            sectionBodyObservable.onNext(sectionBody)
        }

        hotelLatLngObservable.onNext(doubleArrayOf(offerResponse.latitude, offerResponse.longitude))
    }

    open fun getHotelPriceContentDescription(showStrikeThrough: Boolean): String {
        return if (showStrikeThrough) {
            Phrase.from(context, R.string.hotel_price_strike_through_discount_percent_cont_desc_TEMPLATE)
                    .put("strikethroughprice", strikeThroughPriceObservable.value)
                    .put("price", priceToShowCustomerObservable.value)
                    .put("percentage", discountPercentageObservable.value.first)
                    .format()
                    .toString()
        } else {
            priceToShowCustomerObservable.value + context.getString(R.string.per_night)
        }
    }

    fun shouldTrackPartialSoldOut(): Boolean {
        return hasSoldOutRoom && !allRoomsSoldOut.value
    }

    private fun getCommonValueAdds(hotelOffersResponse: HotelOffersResponse): List<String> {
        return getCommonValueAdds(getAllValueAdds(hotelOffersResponse))
    }

    private fun getAllValueAdds(hotelOffersResponse: HotelOffersResponse): List<List<String>> {
        val allValueAdds: List<List<String>> = hotelOffersResponse.hotelRoomResponse
                .filter { it.valueAdds != null }
                .map {
                    it.valueAdds.map { it.description }
                }
        return allValueAdds
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

    private fun getPromoText(offerResponse: HotelOffersResponse): String {
        // NOTE: Any changes to this logic should also be made in HotelViewModel (see: getHighestPriorityUrgencyMessage)
        val roomOffer = offerResponse.hotelRoomResponse?.firstOrNull()
        if (roomOffer == null) {
            return ""
        }

        val roomsLeft = roomOffer.currentAllotment.toInt()
        return when {
            shouldUseGenericAttach(offerResponse) ->
                context.resources.getString(R.string.bundled_savings)
            hasMemberDeal(roomOffer) ->
                context.resources.getString(R.string.member_pricing)
            roomsLeft in 1..ROOMS_LEFT_CUTOFF ->
                context.resources.getQuantityString(R.plurals.num_rooms_left, roomsLeft, roomsLeft)
            roomOffer.isSameDayDRR ->
                context.resources.getString(R.string.tonight_only)
            roomOffer.isDiscountRestrictedToCurrentSourceType ->
                context.resources.getString(R.string.mobile_exclusive)
            else ->
                ""
        }
    }

    private fun areAllRoomDetailsSoldOut(viewModels: ArrayList<HotelRoomDetailViewModel>): Boolean {
        for (vm in viewModels) {
            if (!vm.roomSoldOut.value) return false
        }
        return true
    }

    private fun fetchHotelImages() {
        val list = galleryManager.fetchMediaList(DEFAULT_HOTEL_GALLERY_CODE)
        if (list.isEmpty()) {
            val placeHolder = HotelMedia()
            placeHolder.setIsPlaceholder(true)
            list.add(placeHolder)
        }
        galleryObservable.onNext(list)
    }

    private fun shouldUseGenericAttach(offerResponse: HotelOffersResponse): Boolean {
        return offerResponse.doesAnyRoomHaveAttach && isGenericAttachEnabled()
    }

    private fun shouldUseLegacyAttach(offerResponse: HotelOffersResponse): Boolean {
        return offerResponse.doesAnyRoomHaveAttach && !isGenericAttachEnabled()
    }

    private fun isGenericAttachEnabled(): Boolean {
        return Features.all.genericAttach.enabled()
    }
}
