package com.expedia.vm

import android.content.Context
import android.support.annotation.DrawableRes
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.extension.getEarnMessage
import com.expedia.bookings.extension.isShowAirAttached
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.tracking.HotelTracking
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.HotelUtils
import com.expedia.bookings.utils.Images
import com.expedia.bookings.utils.Strings
import com.expedia.util.endlessObserver
import com.mobiata.android.text.StrikethroughTagHandler
import com.squareup.phrase.Phrase
import rx.Observable
import rx.Observer
import rx.Subscription
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.math.BigDecimal

class HotelRoomRateViewModel(val context: Context, var hotelId: String, var hotelRoomResponse: HotelOffersResponse.HotelRoomResponse, var amenity: String, var rowIndex: Int, var rowExpanding: PublishSubject<Int>, var selectedRoomObserver: Observer<HotelOffersResponse.HotelRoomResponse>, val hasETP: Boolean, val lob:LineOfBusiness) {

    var lastRoomSelectedSubscription: Subscription? = null

    //Output
    val roomSelectedObservable = PublishSubject.create<HotelOffersResponse.HotelRoomResponse>()

    val roomSoldOut = BehaviorSubject.create<Boolean>(false)

    // TODO: null all these out on init
    var roomTypeObservable = BehaviorSubject.create<String>(hotelRoomResponse.roomTypeDescription)
    var currencyCode = hotelRoomResponse.rateInfo.chargeableRateInfo.currencyCode
    val hotelRate = hotelRoomResponse.rateInfo.chargeableRateInfo
    var priceToShowUsers = if (hotelRoomResponse.isPackage) hotelRate.priceToShowUsers.toDouble() else hotelRate.priceToShowUsersFallbackToZeroIfNegative.toDouble()
    var dailyPrice = Money(BigDecimal(priceToShowUsers), currencyCode)
    var roomHeaderImageObservable = BehaviorSubject.create<String>(Images.getMediaHost() + hotelRoomResponse.roomThumbnailUrl)
    var roomRateInfoTextObservable = BehaviorSubject.create<String>(hotelRoomResponse.roomLongDescription)
    var roomInfoVisibiltyObservable = roomRateInfoTextObservable.map { it != "" }
    var soldOutButtonLabelObservable: Observable<CharSequence> = roomSoldOut.filter { it == true }.map { context.getString(R.string.trip_bucket_sold_out) }

    val collapsedBedTypeObservable = BehaviorSubject.create<String>()
    val expandedBedTypeObservable = BehaviorSubject.create<String>()
    val expandedAmenityObservable = BehaviorSubject.create<String>()
    val expandedMessageObservable = BehaviorSubject.create<Pair<String, @DrawableRes Int>>()
    val collapsedUrgencyObservable = BehaviorSubject.create<String>()
    val collapsedEarnMessageObservable = BehaviorSubject.create<String>()
    val collapsedUrgencyVisibilityObservable = BehaviorSubject.create<Boolean>()
    val collapsedEarnMessageVisibilityObservable = BehaviorSubject.create<Boolean>()
    val strikeThroughPriceObservable = BehaviorSubject.create<CharSequence>()
    val dailyPricePerNightObservable = BehaviorSubject.create<String>()
    val perNightPriceVisibleObservable = BehaviorSubject.create<Boolean>()
    val depositTermsClickedObservable = PublishSubject.create<Unit>()

    val onlyShowTotalPrice = BehaviorSubject.create<Boolean>()
    val viewRoomObservable = BehaviorSubject.create<Unit>()
    val expandRoomObservable = PublishSubject.create<Boolean>()
    val collapseRoomObservable = PublishSubject.create<Boolean>()
    val expandedMeasurementsDone = PublishSubject.create<Unit>()
    val roomInfoExpandCollapseObservable = PublishSubject.create<Unit>()
    val roomInfoExpandCollapseObservable1 = PublishSubject.create<Unit>()
    val shouldShowDiscountPercentage = BehaviorSubject.create<Boolean>()
    val discountPercentage = BehaviorSubject.create<String>()
    val depositTerms = BehaviorSubject.create<List<String>>()
    val setViewRoomContentDescription = BehaviorSubject.create<CharSequence>()

    // TODO - create sepearate observable+observer for room selection.
    // -- This should only take care of the expanding and collapsing of the view
    val expandCollapseRoomRateInfoDescription: Observer<Unit> = endlessObserver {
        roomInfoExpandCollapseObservable.onNext(Unit)
    }

    val depositInfoContainerClick: Observer<Unit> = endlessObserver {
        depositTermsClickedObservable.onNext(Unit)
    }

    val expandCollapseRoomRate: Observer<Boolean> = endlessObserver {
        isChecked ->
        if (!isChecked) {
            setViewRoomContentDescription.onNext(context.getString(R.string.hotel_room_expand_cont_desc))

            roomSelectedObservable.onNext(hotelRoomResponse)
            //don't change the state of toggle button
            viewRoomObservable.onNext(Unit)

            if (lob == LineOfBusiness.PACKAGES) {
                PackagesTracking().trackHotelRoomBookClick()
            }
            else {
                HotelTracking().trackLinkHotelRoomBookClick(hotelRoomResponse, hasETP)
            }

            if (hotelRoomResponse.rateInfo.chargeableRateInfo?.airAttached ?: false) {
                HotelTracking().trackLinkHotelAirAttachEligible(hotelRoomResponse, hotelId)
            }
        } else {
            setViewRoomContentDescription.onNext(context.getString(R.string.hotel_expanded_room_select_cont_desc))

            // expand row
            expandRoomObservable.onNext(true)
            if (lob == LineOfBusiness.PACKAGES) {
                PackagesTracking().trackHotelViewBookClick()
            }
            else {
                HotelTracking().trackLinkHotelViewRoomClick()
            }
        }
    }

    fun makePayLaterPriceToShow(): String {
        val sb = StringBuilder(dailyPrice.formattedMoney)
        if (!onlyShowTotalPrice.value) sb.append(context.resources.getString(R.string.per_night))
        return sb.toString()
    }

    fun makePayNowPriceToShow(): String {
        if (hotelRoomResponse.isPackage && dailyPrice.amount.compareTo(BigDecimal.ZERO) >= 0) {
            return "+" + dailyPrice.formattedMoney
        }
        return dailyPrice.formattedMoney
    }

    // TODO: We could have an observable for hotelRoomResponse here and do all this when we get a new one
    fun setupModel(hotelRoomResponse: HotelOffersResponse.HotelRoomResponse, hotelId: String, amenity: String, rowIndex: Int, rowExpanding: PublishSubject<Int>, selectedRoomObserver: Observer<HotelOffersResponse.HotelRoomResponse>) {
        this.hotelRoomResponse = hotelRoomResponse
        this.hotelId = hotelId
        this.amenity = amenity
        this.rowIndex = rowIndex
        this.rowExpanding = rowExpanding
        this.selectedRoomObserver = selectedRoomObserver

        roomTypeObservable.onNext(hotelRoomResponse.roomTypeDescription)
        currencyCode = hotelRoomResponse.rateInfo.chargeableRateInfo.currencyCode
        priceToShowUsers = if (hotelRoomResponse.isPackage) hotelRate.priceToShowUsers.toDouble() else hotelRate.priceToShowUsersFallbackToZeroIfNegative.toDouble()
        dailyPrice = Money(BigDecimal(priceToShowUsers), currencyCode)
        roomHeaderImageObservable.onNext(Images.getMediaHost() + hotelRoomResponse.roomThumbnailUrl)
        roomRateInfoTextObservable.onNext(hotelRoomResponse.roomLongDescription)
        roomInfoVisibiltyObservable = roomRateInfoTextObservable.map { it != "" }
        soldOutButtonLabelObservable = roomSoldOut.filter { it == true }.map { context.getString(R.string.trip_bucket_sold_out) }

        val rateInfo = hotelRoomResponse.rateInfo
        val isPayLater = hotelRoomResponse.isPayLater
        val chargeableRateInfo = rateInfo.chargeableRateInfo
        val discountPercent = HotelUtils.getDiscountPercent(chargeableRateInfo)
        val isBurnApplied = chargeableRateInfo.loyaltyInfo?.isBurnApplied ?: false

        //resetting the text for views
        strikeThroughPriceObservable.onNext("")
        discountPercentage.onNext("")
        expandedAmenityObservable.onNext("")

        if (lob == LineOfBusiness.PACKAGES) {
            shouldShowDiscountPercentage.onNext(false)
        }
        else {
            shouldShowDiscountPercentage.onNext(chargeableRateInfo.isDiscountPercentNotZero && !isBurnApplied && !chargeableRateInfo.isShowAirAttached())
        }
        discountPercentage.onNext(context.resources.getString(R.string.percent_off_TEMPLATE, discountPercent))
        if (!isPayLater && (chargeableRateInfo.priceToShowUsers < chargeableRateInfo.strikethroughPriceToShowUsers)) {
            val strikeThroughPriceToShowUsers = Money(BigDecimal(chargeableRateInfo.strikethroughPriceToShowUsers.toDouble()), currencyCode).formattedMoney
            strikeThroughPriceObservable.onNext(HtmlCompat.fromHtml(context.resources.getString(R.string.strike_template, strikeThroughPriceToShowUsers), null, StrikethroughTagHandler()))
        }

        //TODO: Get Package hotel Delta Price Type
        onlyShowTotalPrice.onNext(chargeableRateInfo?.getUserPriceType() == HotelRate.UserPriceType.RATE_FOR_WHOLE_STAY_WITH_TAXES)

        if (isPayLater) {
            perNightPriceVisibleObservable.onNext(false)
            depositTerms.onNext(hotelRoomResponse.depositPolicy)
            // we show price per night in strikeThroughPriceObservable in case of pay later option
            if (hotelRoomResponse.depositPolicy == null || hotelRoomResponse.depositPolicy.isEmpty()) {
                val depositAmount = chargeableRateInfo?.depositAmountToShowUsers?.toDouble() ?: 0.0
                val depositAmountMoney = Money(BigDecimal(depositAmount), currencyCode)
                val payLaterText = Phrase.from(context, R.string.room_rate_pay_later_due_now).put("amount", depositAmountMoney.formattedMoney).format().toString()
                dailyPricePerNightObservable.onNext(payLaterText)
            } else {
                dailyPricePerNightObservable.onNext(makePayLaterPriceToShow())
            }
            strikeThroughPriceObservable.onNext(makePayLaterPriceToShow())
        } else {
            perNightPriceVisibleObservable.onNext(true)
            dailyPricePerNightObservable.onNext(makePayNowPriceToShow())
            depositTerms.onNext(null)
        }

        val bedTypes = (hotelRoomResponse.bedTypes ?: emptyList()).map { it.description }.joinToString(context.resources.getString(R.string.delimiter_multiple_bed))
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

        val earnMessage = chargeableRateInfo?.loyaltyInfo?.earn?.getEarnMessage(context) ?: ""
        val earnMessageVisibility = earnMessage.isNotBlank() && PointOfSale.getPointOfSale().isEarnMessageEnabledForHotels

        collapsedEarnMessageVisibilityObservable.onNext(earnMessageVisibility)
        collapsedEarnMessageObservable.onNext(earnMessage)
        collapsedUrgencyVisibilityObservable.onNext(!earnMessageVisibility)


        if (Strings.isNotEmpty(amenity)) expandedAmenityObservable.onNext(amenity)
        roomInfoExpandCollapseObservable1.onNext(Unit)
        lastRoomSelectedSubscription?.unsubscribe()
        roomSoldOut.onNext(false)
        lastRoomSelectedSubscription = roomSelectedObservable.subscribe(selectedRoomObserver)
    }

    init {
        setupModel(hotelRoomResponse, hotelId, amenity, rowIndex, rowExpanding, selectedRoomObserver)
    }
}