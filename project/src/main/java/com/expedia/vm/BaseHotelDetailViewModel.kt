package com.expedia.vm

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.text.Html
import com.expedia.bookings.R
import com.expedia.bookings.data.HotelMedia
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.LoyaltyMembershipTier
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.User
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.extension.getEarnMessage
import com.expedia.bookings.extension.isShowAirAttached
import com.expedia.bookings.tracking.HotelTracking
import com.expedia.bookings.utils.Amenity
import com.expedia.bookings.utils.CollectionUtils
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.HotelUtils
import com.expedia.bookings.utils.Images
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.HotelDetailView
import com.expedia.bookings.widget.RecyclerGallery
import com.expedia.bookings.widget.priceFormatter
import com.expedia.util.endlessObserver
import com.mobiata.android.FormatUtils
import com.mobiata.android.SocialUtils
import com.squareup.phrase.Phrase
import rx.Observable
import rx.Observer
import rx.Subscription
import rx.internal.util.RxRingBuffer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.math.BigDecimal
import java.util.ArrayList
import java.util.Locale
import kotlin.properties.Delegates

abstract class BaseHotelDetailViewModel(val context: Context, val roomSelectedObserver: Observer<HotelOffersResponse.HotelRoomResponse>) :
        RecyclerGallery.GalleryItemListener, RecyclerGallery.GalleryItemScrollListener {

    abstract fun getLOB() : LineOfBusiness
    abstract fun hasMemberDeal(roomOffer: HotelOffersResponse.HotelRoomResponse): Boolean
    abstract fun getGuestRatingRecommendedText(rating: Float, resources: Resources) : String
    abstract fun getGuestRatingBackground(rating: Float, context: Context): Drawable
    abstract fun trackHotelResortFeeInfoClick()
    abstract fun trackHotelRenovationInfoClick()
    abstract fun trackHotelDetailBookPhoneClick()
    abstract fun trackHotelDetailSelectRoomClick(isStickyButton: Boolean)
    abstract fun trackHotelViewBookClick()
    abstract fun trackHotelDetailMapViewClick()
    abstract fun trackHotelDetailLoad(hotelOffersResponse: HotelOffersResponse, hotelSearchParams: HotelSearchParams, hasEtpOffer: Boolean, currentLocationSearch: Boolean, hotelSoldOut: Boolean, isRoomSoldOut: Boolean)

    override fun onGalleryItemClicked(item: Any) {
        galleryClickedSubject.onNext(Unit)
    }

    override fun onGalleryItemScrolled(position: Int) {
        val havePhotoWithIndex = CollectionUtils.isNotEmpty(hotelOffersResponse.photos) && (position < hotelOffersResponse.photos.count())
        if (havePhotoWithIndex && hotelOffersResponse.photos[position].displayText != null)
            galleryItemChangeObservable.onNext(Pair(position, hotelOffersResponse.photos[position].displayText))
        else
            galleryItemChangeObservable.onNext(Pair(position, ""))

    }

    var subscriber: Subscription? = null
    private val allRoomsSoldOut = BehaviorSubject.create<Boolean>(false)
    private val noRoomsInOffersResponse = BehaviorSubject.create<Boolean>(false)
    val hotelSoldOut = BehaviorSubject.create<Boolean>(false)
    val selectedRoomSoldOut = PublishSubject.create<Unit>()

    val toolBarRatingColor = hotelSoldOut.map { if (it) ContextCompat.getColor(context, android.R.color.white) else ContextCompat.getColor(context, R.color.hotelsv2_detail_star_color) }
    val galleryColorFilter = hotelSoldOut.map { if (it) HotelDetailView.zeroSaturationColorMatrixColorFilter else null }
    val priceWidgetBackground = hotelSoldOut.map { if (it) ContextCompat.getColor(context, R.color.hotel_cell_gray_text) else ContextCompat.getColor(context, Ui.obtainThemeResID(context, R.attr.primary_color)) }

    val hotelOffersSubject = BehaviorSubject.create<HotelOffersResponse>()
    var hotelOffersResponse: HotelOffersResponse by Delegates.notNull()
    var etpOffersList = ArrayList<HotelOffersResponse.HotelRoomResponse>()
    var sectionBody: String by Delegates.notNull()

    var isSectionExpanded = false
    val sectionBodyObservable = BehaviorSubject.create<String>()
    val sectionImageObservable = BehaviorSubject.create<Boolean>()
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

    val lastExpandedRowObservable = BehaviorSubject.create<Int>()
    val rowExpandingObservable = PublishSubject.create<Int>()
    val hotelRoomRateViewModelsObservable = BehaviorSubject.create<ArrayList<HotelRoomRateViewModel>>()

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
    val searchDatesObservable = BehaviorSubject.create<String>()
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
    val discountPercentageObservable = BehaviorSubject.create<String>()
    val showDiscountPercentageObservable = BehaviorSubject.create<Boolean>(false)
    val showAirAttachSWPImageObservable = BehaviorSubject.create<Boolean>(false)
    val hasVipAccessObservable = BehaviorSubject.create<Boolean>(false)
    val hasVipAccessLoyaltyObservable = BehaviorSubject.create<Boolean>(false)
    val hasRegularLoyaltyPointsAppliedObservable = BehaviorSubject.create<Boolean>(false)
    val promoMessageObservable = BehaviorSubject.create<String>("")
    val promoMessageVisibilityObservable = BehaviorSubject.create<Boolean>()
    val earnMessageObservable = BehaviorSubject.create<String>()
    val earnMessageVisibilityObservable = BehaviorSubject.create<Boolean>()

    val strikeThroughPriceObservable = BehaviorSubject.create<CharSequence>()
    val strikeThroughPriceGreaterThanPriceToShowUsersObservable = PublishSubject.create<Boolean>()
    val galleryItemChangeObservable = BehaviorSubject.create<Pair<Int, String>>()
    val depositInfoContainerClickObservable = BehaviorSubject.create<Pair<String, HotelOffersResponse.HotelRoomResponse>>()
    val bundlePricePerPersonObservable = BehaviorSubject.create<Money>()
    val bundleTotalPriceObservable = BehaviorSubject.create<Money>()
    val bundleSavingsObservable = BehaviorSubject.create<Money>()
    val isPackageHotelObservable = BehaviorSubject.create<Boolean>(false)

    var isCurrentLocationSearch = false
    val scrollToRoom = PublishSubject.create<Unit>()
    val changeDates = PublishSubject.create<Unit>()

    private val offersObserver = endlessObserver<HotelOffersResponse> { response ->
        hotelOffersResponse = response

        isPackageHotelObservable.onNext(response.isPackage)

        var galleryUrls = ArrayList<HotelMedia>()

        if (Images.getHotelImages(hotelOffersResponse).isNotEmpty()) {
            galleryUrls.addAll(Images.getHotelImages(hotelOffersResponse).toMutableList())
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
            onlyShowTotalPrice.onNext(rate.getUserPriceType() == HotelRate.UserPriceType.RATE_FOR_WHOLE_STAY_WITH_TAXES)
            pricePerNightObservable.onNext(Money(BigDecimal(rate.averageRate.toDouble()), rate.currencyCode).getFormattedMoney(Money.F_NO_DECIMAL))
            if (rate.packagePricePerPerson != null && rate.packageTotalPrice != null && rate.packageSavings != null) {
                bundlePricePerPersonObservable.onNext(Money(BigDecimal(rate.packagePricePerPerson.amount.toDouble()), rate.packagePricePerPerson.currencyCode))
                bundleTotalPriceObservable.onNext(rate.packageTotalPrice)
                bundleSavingsObservable.onNext(rate.packageSavings)
            }
            totalPriceObservable.onNext(Money(BigDecimal(rate.totalPriceWithMandatoryFees.toDouble()), rate.currencyCode).getFormattedMoney(Money.F_NO_DECIMAL))
            discountPercentageBackgroundObservable.onNext(if (rate.isShowAirAttached()) R.drawable.air_attach_background else R.drawable.guest_rating_background)
            showAirAttachSWPImageObservable.onNext(rate.loyaltyInfo?.isBurnApplied ?: false && rate.isShowAirAttached())
        }

        userRatingObservable.onNext(response.hotelGuestRating.toString())
        userRatingBackgroundColorObservable.onNext(getGuestRatingBackground(response.hotelGuestRating.toFloat(), context))
        userRatingRecommendationTextObservable.onNext(getGuestRatingRecommendedText(response.hotelGuestRating.toFloat(), context.resources))
        isUserRatingAvailableObservable.onNext(hotelOffersResponse.hotelGuestRating > 0)

        numberOfReviewsObservable.onNext(
                if (hotelOffersResponse.totalReviews > 0)
                    context.resources.getQuantityString(R.plurals.hotel_number_of_reviews, hotelOffersResponse.totalReviews, HotelUtils.formattedReviewCount(hotelOffersResponse.totalReviews))
                else context.resources.getString(R.string.zero_reviews))

        val chargeableRateInfo = response.hotelRoomResponse?.firstOrNull()?.rateInfo?.chargeableRateInfo
        val isRateShopWithPoints = chargeableRateInfo?.loyaltyInfo?.isBurnApplied ?: false
        var discountPercentage: Int? = chargeableRateInfo?.discountPercent?.toInt()
        discountPercentageObservable.onNext(Phrase.from(context.resources, R.string.hotel_discount_percent_Template)
                .put("discount", discountPercentage ?: 0).format().toString())

        showDiscountPercentageObservable.onNext(!response.isPackage && !isRateShopWithPoints && chargeableRateInfo?.isDiscountPercentNotZero ?: false)
        val isVipAccess = response.isVipAccess && PointOfSale.getPointOfSale().supportsVipAccess()
        hasVipAccessObservable.onNext(isVipAccess)
        hasVipAccessLoyaltyObservable.onNext(isVipAccess && response.doesAnyHotelRateOfAnyRoomHaveLoyaltyInfo)
        hasRegularLoyaltyPointsAppliedObservable.onNext(!isVipAccess && response.doesAnyHotelRateOfAnyRoomHaveLoyaltyInfo)
        promoMessageObservable.onNext(getPromoText(firstHotelRoomResponse))
        val earnMessage = chargeableRateInfo?.loyaltyInfo?.earn?.getEarnMessage(context) ?: ""
        val earnMessageVisibility = earnMessage.isNotBlank() && PointOfSale.getPointOfSale().isEarnMessageEnabledForHotels
        earnMessageObservable.onNext(earnMessage)
        earnMessageVisibilityObservable.onNext(earnMessageVisibility)
        promoMessageVisibilityObservable.onNext(!earnMessageVisibility)

        val priceToShowUsers = chargeableRateInfo?.priceToShowUsers ?: 0f
        val strikethroughPriceToShowUsers = chargeableRateInfo?.strikethroughPriceToShowUsers ?: 0f

        val isStrikeThroughPriceGreaterThanPriceToShowUsers = priceToShowUsers < strikethroughPriceToShowUsers
        strikeThroughPriceGreaterThanPriceToShowUsersObservable.onNext(isStrikeThroughPriceGreaterThanPriceToShowUsers)
        if (isStrikeThroughPriceGreaterThanPriceToShowUsers) {
            strikeThroughPriceObservable.onNext(priceFormatter(context.resources, chargeableRateInfo, true, !hotelOffersResponse.isPackage))
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
        var supportPhoneNumber = when (User.getLoggedInLoyaltyMembershipTier(context)) {
            LoyaltyMembershipTier.BASE -> PointOfSale.getPointOfSale().supportPhoneNumberBaseTier
            LoyaltyMembershipTier.MIDDLE -> PointOfSale.getPointOfSale().supportPhoneNumberMiddleTier
            LoyaltyMembershipTier.TOP -> PointOfSale.getPointOfSale().supportPhoneNumberTopTier
            else -> hotelOffersResponse.telesalesNumber
        }

        if(supportPhoneNumber == null) {
            supportPhoneNumber = PointOfSale.getPointOfSale().getDefaultSupportPhoneNumber()
        }

        SocialUtils.call(context, supportPhoneNumber)
        trackHotelDetailBookPhoneClick()
    }

    val mapClickedSubject = PublishSubject.create<Unit>()

    val reviewsClickedSubject = PublishSubject.create<Unit>()

    val galleryClickedSubject = PublishSubject.create<Unit>()

    val renovationContainerClickObserver: Observer<Unit> = endlessObserver {
        var renovationInfo = Pair<String, String>(context.resources.getString(R.string.renovation_notice),
                hotelOffersResponse.hotelRenovationText?.content ?: "")
        hotelRenovationObservable.onNext(renovationInfo)
        trackHotelRenovationInfoClick()
    }

    val resortFeeContainerClickObserver: Observer<Unit> = endlessObserver {
        var renovationInfo = Pair<String, String>(context.resources.getString(R.string.additional_fees),
                hotelOffersResponse.hotelMandatoryFeesText?.content ?: "")
        hotelRenovationObservable.onNext(renovationInfo)
        trackHotelResortFeeInfoClick()
    }

    val payLaterInfoContainerClickObserver: Observer<Unit> = endlessObserver {
        hotelPayLaterInfoObservable.onNext(Pair(hotelOffersResponse.hotelCountry, hotelOffersResponse.hotelRoomResponse))
        HotelTracking().trackHotelEtpInfo()
    }

    val strikeThroughPriceVisibility = Observable.combineLatest(strikeThroughPriceGreaterThanPriceToShowUsersObservable, hotelSoldOut)
    { strikeThroughPriceGreaterThanPriceToShowUsers, hotelSoldOut -> strikeThroughPriceGreaterThanPriceToShowUsers && !hotelSoldOut }

    val perNightVisibility = Observable.combineLatest(onlyShowTotalPrice, hotelSoldOut) { onlyShowTotalPrice, hotelSoldOut -> onlyShowTotalPrice || hotelSoldOut }

    val payByPhoneContainerVisibility = Observable.combineLatest(showBookByPhoneObservable, hotelSoldOut) { showBookByPhoneObservable, hotelSoldOut -> showBookByPhoneObservable && !hotelSoldOut }

    val hotelMessagingContainerVisibility = Observable.combineLatest(showDiscountPercentageObservable, hasVipAccessObservable, promoMessageObservable, hotelSoldOut, hasRegularLoyaltyPointsAppliedObservable, showAirAttachSWPImageObservable, earnMessageVisibilityObservable)
    {
        hasDiscount, hasVipAccess, promoMessage, hotelSoldOut, hasRegularLoyaltyPointsApplied, shouldShowAirAttachSWPImage, earnMessage ->
        (hasDiscount || hasVipAccess || Strings.isNotEmpty(promoMessage) || earnMessage || hasRegularLoyaltyPointsApplied || shouldShowAirAttachSWPImage) && !hotelSoldOut
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
            if (params.forPackage) {
                searchInfoObservable.onNext(Phrase.from(context, R.string.room_with_guests_TEMPLATE)
                        .put("guests", StrUtils.formatGuestString(context, params.guests))
                        .format()
                        .toString())
                val dates = Phrase.from(context, R.string.calendar_instructions_date_range_TEMPLATE).put("startdate", DateUtils.localDateToMMMd(params.checkIn)).put("enddate", DateUtils.localDateToMMMd(params.checkOut)).format().toString()
                searchDatesObservable.onNext(dates)
            } else {
                searchInfoObservable.onNext(Phrase.from(context, R.string.calendar_instructions_date_range_with_guests_TEMPLATE).put("startdate",
                        DateUtils.localDateToMMMd(params.checkIn)).put("enddate",
                        DateUtils.localDateToMMMd(params.checkOut)).put("guests", StrUtils.formatGuestString(context, params.guests))
                        .format()
                        .toString())
            }

            isCurrentLocationSearch = params.suggestion.isCurrentLocationSearch
        }

        hotelOffersSubject.subscribe(offersObserver)

        hotelRoomRateViewModelsObservable.subscribe {
            subscriber?.unsubscribe()
            val hotelRoomRateViewModels = hotelRoomRateViewModelsObservable.value

            if (hotelRoomRateViewModels == null || hotelRoomRateViewModels.isEmpty()) {
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

            subscriber = selectedRoomSoldOut.subscribe selectedRoomSoldOutLabel@{
                for (hotelRoomRateViewModel in hotelRoomRateViewModels.drop(lastExpandedRowObservable.value)) {
                    if (!hotelRoomRateViewModel.roomSoldOut.value) {
                        hotelRoomRateViewModel.expandRoomObservable.onNext(false)
                        hotelRoomRateViewModels.clear()
                        return@selectedRoomSoldOutLabel
                    }
                }

                for (hotelRoomRateViewModel in hotelRoomRateViewModels.take(lastExpandedRowObservable.value)) {
                    if (!(hotelRoomRateViewModel.roomSoldOut.value)) {
                        hotelRoomRateViewModel.expandRoomObservable.onNext(false)
                        hotelRoomRateViewModels.clear()
                        return@selectedRoomSoldOutLabel
                    }
                }
            }
        }

        rowExpandingObservable.subscribe { indexOfRowBeingExpanded ->
            //collapse already expanded row if there is one
            if (lastExpandedRowObservable.value >= 0 && lastExpandedRowObservable.value < hotelRoomRateViewModelsObservable.value.size && lastExpandedRowObservable.value != indexOfRowBeingExpanded) {
                hotelRoomRateViewModelsObservable.value.elementAt(lastExpandedRowObservable.value).collapseRoomObservable.onNext(true)
            }
            lastExpandedRowObservable.onNext(indexOfRowBeingExpanded)
        }

    }

    open fun addViewsAfterTransition() {

        if (hotelOffersResponse.hotelRoomResponse != null && hotelOffersResponse.hotelRoomResponse.isNotEmpty()) {
            uniqueValueAddForRooms = getValueAdd(hotelOffersResponse.hotelRoomResponse)
            roomResponseListObservable.onNext(Pair(hotelOffersResponse.hotelRoomResponse, uniqueValueAddForRooms))
        }

        var listHotelInfo = ArrayList<HotelOffersResponse.HotelText>()

        //Set up entire text for hotel info
        if (hotelOffersResponse.hotelOverviewText != null && hotelOffersResponse.hotelOverviewText.size > 1 ) {
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

        renovationObservable.onNext(if (hotelOffersResponse.hotelRenovationText?.content != null) true else false)

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
            val hotelResortFee = Money(BigDecimal(rate.totalMandatoryFees.toDouble()), rate.currencyCode)
            hotelResortFeeObservable.onNext(hotelResortFee.getFormattedMoney(Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL))
            val includedNotIncludedStrId = if (rate.resortFeeInclusion) R.string.included_in_the_price else R.string.not_included_in_the_price
            hotelResortFeeIncludedTextObservable.onNext(context.resources.getString(includedNotIncludedStrId))
        } else {
            hotelResortFeeObservable.onNext(null)
            hotelResortFeeIncludedTextObservable.onNext(null)
        }
        trackHotelDetailLoad(hotelOffersResponse, paramsSubject.value, hasETPOffer, isCurrentLocationSearch, hotelSoldOut.value, false)
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
        if (!allValueAdds?.isEmpty()) {
            return allValueAdds
                    .drop(1)
                    .fold(allValueAdds.first().toMutableList(), { initial, nextValueAdds ->
                        initial.retainAll(nextValueAdds)
                        initial
                    })
        }
        return emptyList()
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
        var list = Array(hotelRooms!!.size, { i -> "" }).toMutableList()
        for (iRoom in 0..hotelRooms.size - 1) {
            val rate = hotelOffersResponse.hotelRoomResponse.get(iRoom)
            if (rate.valueAdds != null) {
                val unique = rate.valueAdds.filter { !commonValueAdds.contains(it.description) }
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

    private fun getPromoText(roomOffer: HotelOffersResponse.HotelRoomResponse?): String {
        // NOTE: Any changes to this logic should also be made in HotelViewModel (see: urgencyMessageObservable)
        if (roomOffer == null) {
            return ""
        }

        val roomsLeft = roomOffer.currentAllotment.toInt()
        return if (hasMemberDeal(roomOffer)) {
            context.resources.getString(R.string.member_pricing)
        } else if (roomsLeft > 0 && roomsLeft <= ROOMS_LEFT_CUTOFF) {
            context.resources.getQuantityString(R.plurals.num_rooms_left, roomsLeft, roomsLeft)
        } else if (roomOffer.isSameDayDRR) {
            context.resources.getString(R.string.tonight_only)
        } else if (roomOffer.isDiscountRestrictedToCurrentSourceType) {
            context.resources.getString(R.string.mobile_exclusive)
        } else {
            ""
        }
    }
}
