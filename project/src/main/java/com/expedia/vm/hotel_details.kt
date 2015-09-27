package com.expedia.vm

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.Html
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.HotelMedia
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.User
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.utils.Amenity
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.Images
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

class HotelDetailViewModel(val context: Context, val hotelServices: HotelServices) : RecyclerGallery.GalleryItemListener {

    override fun onGalleryItemClicked(item: Any) {
        throw UnsupportedOperationException()
    }
    val hotelOffersSubject = BehaviorSubject.create<HotelOffersResponse>()
    var hotelOffersResponse: HotelOffersResponse by Delegates.notNull()
    var etpOffersList = ArrayList<HotelOffersResponse.HotelRoomResponse>()
    var sectionBody: String by Delegates.notNull()
    var untruncated: String by Delegates.notNull()
    var commonList: ArrayList<String> = ArrayList<String>()

    var isSectionExpanded = false
    val sectionBodyObservable = BehaviorSubject.create<String>()
    val sectionImageObservable = BehaviorSubject.create<Boolean>()
    val showBookByPhoneObservable = BehaviorSubject.create<Boolean>()
    val galleryObservable = BehaviorSubject.create<List<HotelMedia>>()

    val commonAmenityTextObservable = BehaviorSubject.create<String>()

    val amenitiesListObservable = BehaviorSubject.create<List<Amenity>>()
    val noAmenityObservable = BehaviorSubject.create<Unit>()

    var hasETPObservable = BehaviorSubject.create<Boolean>(false)
    var hasFreeCancellationObservable = BehaviorSubject.create<Boolean>(false)
    var renovationObservable = BehaviorSubject.create<Unit>()
    val hotelRenovationObservable = BehaviorSubject.create<Pair<String, String>>()
    val hotelPayLaterInfoObservable = BehaviorSubject.create<String>()

    var propertyInfoListObservable = BehaviorSubject.create<List<HotelOffersResponse.HotelText>>(emptyList())

    var roomResponseListObservable = BehaviorSubject.create<Pair<List<HotelOffersResponse.HotelRoomResponse>, List<String>>>()
    var uniqueValueAddForRooms: ArrayList<String> by Delegates.notNull()

    var etpUniqueValueAddForRooms: ArrayList<String> by Delegates.notNull()
    var etpRoomResponseListObservable = BehaviorSubject.create<Pair<List<HotelOffersResponse.HotelRoomResponse>, List<String>>>()


    val hotelResortFeeObservable = BehaviorSubject.create<String>(null)
    val hotelNameObservable = BehaviorSubject.create<String>()
    val hotelRatingObservable = BehaviorSubject.create<Float>()
    val pricePerNightObservable = BehaviorSubject.create<String>()
    val searchInfoObservable = BehaviorSubject.create<String>()
    val userRatingObservable = BehaviorSubject.create<String>()
    val numberOfReviewsObservable = BehaviorSubject.create<String>()
    val ratingContainerObservable = BehaviorSubject.create<Unit>()
    val hotelLatLngObservable = BehaviorSubject.create<DoubleArray>()
    val discountPercentageObservable = BehaviorSubject.create<String>()
    val hasDiscountPercentageObservable = BehaviorSubject.create<Boolean>()
    val hasVipAccessObservable = BehaviorSubject.create<Boolean>()
    val promoMessageObservable = BehaviorSubject.create<String>()
    val strikeThroughPriceObservable = BehaviorSubject.create<String>()

    private fun bindDetails(response: HotelOffersResponse) {
        hotelOffersResponse = response

        hotelNameObservable.onNext(response.hotelName)

        hotelRatingObservable.onNext(response.hotelStarRating.toFloat())

        if (response.hotelRoomResponse.first() != null) {
            var rate = response.hotelRoomResponse.first().rateInfo.chargeableRateInfo;
            val dailyPrice = Money(BigDecimal(rate.averageRate.toDouble()), rate.currencyCode)
            pricePerNightObservable.onNext(dailyPrice.getFormattedMoney(Money.F_NO_DECIMAL))
        }

        userRatingObservable.onNext(response.hotelGuestRating.toString())

        if(response.totalReviews > 0) {
            numberOfReviewsObservable.onNext(context.resources.getQuantityString(R.plurals.hotel_number_of_reviews, response.totalReviews, response.totalReviews))
        } else {
            ratingContainerObservable.onNext(Unit)
        }
        hotelLatLngObservable.onNext(doubleArrayOf(response.latitude, response.longitude))

        var discountPercentage : Int? = response.hotelRoomResponse.first()?.rateInfo?.chargeableRateInfo?.discountPercent?.toInt()
        discountPercentageObservable.onNext(Phrase.from(context.resources, R.string.hotel_discount_percent_Template)
                .put("discount", discountPercentage ?: 0).format().toString())
        hasDiscountPercentageObservable.onNext(discountPercentage ?: 0 < 0)
        hasVipAccessObservable.onNext(response.isVipAccess)
        promoMessageObservable.onNext(getPromoText(response.hotelRoomResponse.first()))
        strikeThroughPriceObservable.onNext(priceFormatter(response.hotelRoomResponse.first()?.rateInfo?.chargeableRateInfo, true))

        if (response.hotelRenovationText?.content != null) renovationObservable.onNext(Unit)
        galleryObservable.onNext(Images.getHotelImages(response))

        if (response.firstHotelOverview != null) {
            sectionBody = Html.fromHtml(response.firstHotelOverview).toString()
            sectionBodyObservable.onNext(sectionBody)
        }

        var listHotelInfo = ArrayList<HotelOffersResponse.HotelText>()

        //Set up entire text for hotel info
        if (response.hotelOverviewText != null && response.hotelOverviewText.size() > 1 ) {
            for (index in 1..response.hotelOverviewText.size() - 1) {
                listHotelInfo.add(response.hotelOverviewText.get(index))
            }
        }

        if (response.hotelAmenitiesText != null) {
            listHotelInfo.add(response.hotelAmenitiesText)
        }

        if (response.hotelPoliciesText != null) {
            listHotelInfo.add(response.hotelPoliciesText)
        }

        if (response.hotelFeesText != null) {
            listHotelInfo.add(response.hotelFeesText)
        }

        if (response.hotelMandatoryFeesText != null) {
            listHotelInfo.add(response.hotelMandatoryFeesText)
        }
        propertyInfoListObservable.onNext(listHotelInfo)


        val amenityList: List<Amenity> = emptyList()
        if(response.hotelAmenities != null) {
            amenityList.toArrayList().addAll(Amenity.amenitiesToShow(response.hotelAmenities))
        }

        if (amenityList.isEmpty()) {
            noAmenityObservable.onNext(Unit)
        } else {
            //Here have to pass the list of amenities which we want to show
            amenitiesListObservable.onNext(amenityList)
        }

        // common amenities text
        if (response.hotelRoomResponse.size() > 0) {
            val allValueAdds: List<List<String>> = response.hotelRoomResponse
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

                if(!commonValueAdds.isEmpty()) {
                    val commonValueAddsString = context.getString(R.string.common_value_add_template, FormatUtils.series(context, commonValueAdds, ",", FormatUtils.Conjunction.AND)
                            .toLowerCase(Locale.getDefault()))

                    commonAmenityTextObservable.onNext(commonValueAddsString)
                }
            }
        }

        val hasETPOffer = hasEtpOffer(response)
        if (hasETPOffer) {
            etpOffersList = hotelOffersResponse.hotelRoomResponse
                    .filter { it.payLaterOffer != null }.toArrayList()

            etpUniqueValueAddForRooms = getValueAdd(etpOffersList)
        }

        hasETPObservable.onNext(hasETPOffer)
        hasFreeCancellationObservable.onNext(hasFreeCancellation(response))

        uniqueValueAddForRooms = getValueAdd(response.hotelRoomResponse)
        roomResponseListObservable.onNext(Pair(response.hotelRoomResponse, uniqueValueAddForRooms))


        if (response.hotelRoomResponse.get(0).rateInfo.chargeableRateInfo.showResortFeeMessage) {
            val rate = response.hotelRoomResponse.get(0).rateInfo.chargeableRateInfo
            val hotelResortFee = Money(BigDecimal(rate.totalMandatoryFees.toDouble()), rate.currencyCode)
            hotelResortFeeObservable.onNext(hotelResortFee.getFormattedMoney(Money.F_NO_DECIMAL))
        }

        showBookByPhoneObservable.onNext(isAvailable() && (response.deskTopOverrideNumber != null && !response.deskTopOverrideNumber)
                && !Strings.isEmpty(response.telesalesNumber))
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
    }

    fun isAvailable(): Boolean {
        return hotelOffersResponse.hotelRoomResponse.size() > 0
    }

    val mapClickedSubject = PublishSubject.create<Unit>()

    val reviewsClickedSubject = PublishSubject.create<Unit>()

    val renovationContainerClickObserver: Observer<Unit> = endlessObserver {
        var renovationInfo = Pair<String, String>(context.resources.getString(R.string.renovation_notice),
                hotelOffersResponse.hotelRenovationText?.content ?: "")
        hotelRenovationObservable.onNext(renovationInfo)
    }

    val resortFeeContainerClickObserver: Observer<Unit> = endlessObserver {
        var renovationInfo = Pair<String, String>(context.getResources().getString(R.string.additional_fees),
                hotelOffersResponse.hotelMandatoryFeesText?.content ?: "")
        hotelRenovationObservable.onNext(renovationInfo)
    }

    val payLaterInfoContainerClickObserver: Observer<Unit> = endlessObserver {
        hotelPayLaterInfoObservable.onNext(hotelOffersResponse.hotelCountry)
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
        paramsSubject.subscribe { params ->
            searchInfoObservable.onNext(Phrase.from(context, R.string.calendar_instructions_date_range_with_guests_TEMPLATE).put("startdate",
                    DateUtils.localDateToMMMd(params.checkIn)).put("enddate",
                    DateUtils.localDateToMMMd(params.checkOut)).put("guests",
                    context.getResources().getQuantityString(R.plurals.number_of_guests, params.children.size() + params.adults, params.children.size() + params.adults))
                    .format()
                    .toString())
        }

        hotelOffersSubject.subscribe { response ->
            bindDetails(response)
        }

    }

    private fun getPromoText(hotel: HotelOffersResponse.HotelRoomResponse):String {
        if (hotel.isDiscountRestrictedToCurrentSourceType) return context.getResources().getString(R.string.mobile_exclusive)
        else if (hotel.isSameDayDRR) return context.getResources().getString(R.string.tonight_only)
        else return ""

        }

}

var lastExpanded: Int = 0
val ROOMS_LEFT_CUTOFF = 5


public class HotelRoomRateViewModel(val context: Context, val hotelRoomResponse: HotelOffersResponse.HotelRoomResponse, val index: Int, val amenity: String) {

    var roomRateInfoVisible: Int = View.GONE

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

    var dailyPricePerNightObservable = BehaviorSubject.create<String>()
    var perNightObservable =  BehaviorSubject.create<Boolean>()

    val totalPricePerNightObservable = BehaviorSubject.create<String>(context.getResources().getString(R.string.cars_total_template, Money.getFormattedMoneyFromAmountAndCurrencyCode(BigDecimal(hotelRoomResponse.rateInfo.chargeableRateInfo.total.toDouble()), currencyCode, Money.F_NO_DECIMAL)))
    val roomHeaderImageObservable = BehaviorSubject.create<String>(Images.getMediaHost() + hotelRoomResponse.roomThumbnailUrl)
    val expandRoomObservable = BehaviorSubject.create<Boolean>()
    val viewRoomObservable = BehaviorSubject.create<Unit>()
    val collapseRoomObservable = BehaviorSubject.create<Int>()
    val roomRateInfoTextObservable = BehaviorSubject.create<String>(hotelRoomResponse.roomLongDescription)
    val roomInfoObservable = BehaviorSubject.create<Int>()

    val expandCollapseRoomRate: Observer<Boolean> = endlessObserver {
        isChecked ->
        if (!isChecked) {
            roomSelectedObservable.onNext(hotelRoomResponse)
            //don't change the state of toggle button
            viewRoomObservable.onNext(Unit)
        } else {
            // expand row if it's not expanded
            if (lastExpanded != index) {
                collapseRoomObservable.onNext(lastExpanded)

                if (isChecked) {
                    expandRoomObservable.onNext(true)
                    roomBackgroundViewObservable.onNext(context.getResources().getDrawable(R.drawable.card_background))
                    lastExpanded = index
                }
            }
        }
    }

    val payLaterObserver: Observer<Unit> = endlessObserver {
        val depositAmount = hotelRoomResponse.payLaterOffer?.rateInfo?.chargeableRateInfo?.depositAmountToShowUsers?.toDouble() ?: 0.0
        val depositAmountMoney = Money(BigDecimal(depositAmount), currencyCode)
        val payLaterText = depositAmountMoney.getFormattedMoney() + " " + context.getResources().getString(R.string.room_rate_pay_later_due_now)
        dailyPricePerNightObservable.onNext(payLaterText)
        perNightObservable.onNext(false)
    }

    val expandCollapseRoomRateInfo: Observer<Unit> = endlessObserver {
        if (roomRateInfoVisible == View.VISIBLE) roomRateInfoVisible = View.GONE else roomRateInfoVisible = View.VISIBLE
        roomInfoObservable.onNext(roomRateInfoVisible)
    }

    init {
        val dailyPrice = Money(BigDecimal(hotelRoomResponse.rateInfo.chargeableRateInfo.priceToShowUsers.toDouble()), currencyCode)
        dailyPricePerNightObservable.onNext(dailyPrice.getFormattedMoney())
        perNightObservable.onNext(true)
        rateObservable.subscribe { hotelRoom ->
            val bedTypes = hotelRoom.bedTypes.map { it.description }.join("")
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
        if (index == 0) {
            expandRoomObservable.onNext(true)
            roomBackgroundViewObservable.onNext(context.getResources().getDrawable(R.drawable.card_background))
        } else {
            expandRoomObservable.onNext(false)
        }
    }
}
