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
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.utils.Amenity
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.Images
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.widget.RecyclerGallery
import com.expedia.util.endlessObserver
import com.mobiata.android.FormatUtils
import com.mobiata.android.SocialUtils
import com.squareup.phrase.Phrase
import rx.Observable
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.math.BigDecimal
import java.util.ArrayList
import java.util.Locale
import kotlin.properties.Delegates

class HotelDetailViewModel(val context: Context, val hotelServices: HotelServices) : RecyclerGallery.GalleryItemListener {

    override fun onGalleryItemClicked(item: Any) {
        throw UnsupportedOperationException()
    }
    val hotelOffersSubject = BehaviorSubject.create<HotelOffersResponse>()
    var hotelOffersResponse: HotelOffersResponse by Delegates.notNull()
    var etpOffersList = ArrayList<HotelOffersResponse.HotelRoomResponse>()
    val INTRO_PARAGRAPH_CUTOFF = 120
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
    val noAmenityTextObservable = BehaviorSubject.create<String>()

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
    val hotelLatLngObservable = BehaviorSubject.create<DoubleArray>()

    private fun bindDetails(response: HotelOffersResponse) {
    hotelOffersResponse = response
        if (response.hotelRenovationText?.content != null) renovationObservable.onNext(Unit)
        galleryObservable.onNext(Images.getHotelImages(response))

        if (response.firstHotelOverview != null) {
            sectionBody = Html.fromHtml(response.firstHotelOverview).toString()

            //add read more if hotel intro is too long
            if (sectionBody.length() > INTRO_PARAGRAPH_CUTOFF) {
                untruncated = sectionBody
                sectionBody = Phrase.from(context, R.string.hotel_ellipsize_text_template).put("text",
                        sectionBody.substring(0, Strings.cutAtWordBarrier(sectionBody, INTRO_PARAGRAPH_CUTOFF))).format().toString()
            }
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
        propertyInfoListObservable.onNext(listHotelInfo)


        val amenityList: List<Amenity> = Amenity.amenitiesToShow(response.hotelAmenities)
        //Here have to pass the list of amenities which we want to show
        amenitiesListObservable.onNext(amenityList)
        if (amenityList.isEmpty()) {
            val noAmenityText = context.getResources().getString(R.string.AmenityNone)
            noAmenityTextObservable.onNext(noAmenityText)
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

                val commonValueAddsString = context.getString(R.string.common_value_add_template, FormatUtils.series(context, commonValueAdds, ",", FormatUtils.Conjunction.AND)
                        .toLowerCase(Locale.getDefault()))

                commonAmenityTextObservable.onNext(commonValueAddsString)
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
        expandSection(untruncated, sectionBody)
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

    val shareHotelClickedSubject = PublishSubject.create<Unit>()

    val renovationContainerClickObserver: Observer<Unit> = endlessObserver {
        var renovationInfo = Pair<String, String>(context.getResources().getString(R.string.renovation_notice),
                hotelOffersResponse.hotelRenovationText?.content ?: "")
        hotelRenovationObservable.onNext(renovationInfo)
    }

    val payLaterInfoContainerClickObserver: Observer<Unit> = endlessObserver {
        hotelPayLaterInfoObservable.onNext(hotelOffersResponse.hotelCountry)
    }

    val paramsSubject = BehaviorSubject.create<HotelSearchParams>()
    val hotelSelectedSubject = BehaviorSubject.create<Hotel>()

    // Every time a new hotel is emitted, emit an Observable<Hotel>
    // that will return the outer hotel every time the map is clicked
    val mapClickedWithHotelData: Observable<Hotel> = Observable.switchOnNext(hotelSelectedSubject.map { hotel ->
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
    val reviewsClickedWithHotelData: Observable<Hotel> = Observable.switchOnNext(hotelSelectedSubject.map { hotel ->
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

    private fun expandSection(untruncated: String, sectionBody: String) {
        if (!isSectionExpanded) {
            sectionBodyObservable.onNext(untruncated)
            isSectionExpanded = true
        } else {
            sectionBodyObservable.onNext(sectionBody)
            isSectionExpanded = false
        }
        sectionImageObservable.onNext(isSectionExpanded)
    }

    init {
        hotelSelectedSubject.subscribe { hotel ->
            hotelNameObservable.onNext(hotel.localizedName)

            hotelRatingObservable.onNext(hotel.hotelStarRating)

            val pricePerNight = Phrase.from(context.getResources(), R.string.per_nt_TEMPLATE)
                    .put("price", hotel.lowRateInfo?.nightlyRateTotal.toString())
                    .format()
                    .toString()

            pricePerNightObservable.onNext(pricePerNight)

            userRatingObservable.onNext(hotel.hotelGuestRating.toString())

            numberOfReviewsObservable.onNext(context.getResources().getQuantityString(R.plurals.hotel_number_of_reviews, hotel.totalReviews, hotel.totalReviews))

            hotelLatLngObservable.onNext(doubleArrayOf(hotel.latitude, hotel.longitude))
        }

        paramsSubject.subscribe { params ->
            searchInfoObservable.onNext(Phrase.from(context, R.string.calendar_instructions_date_range_with_guests_TEMPLATE).put("startdate",
                    DateUtils.localDateToMMMd(params.checkIn)).put("enddate",
                    DateUtils.localDateToMMMd(params.checkOut)).put("guests",
                    params.getGuestString()).format().toString())
        }

        hotelOffersSubject.subscribe { response ->
            bindDetails(response)
        }

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

    val totalPricePerNightObservable = BehaviorSubject.create<String>(context.getResources().getString(R.string.cars_total_template, currencyCode + hotelRoomResponse.rateInfo.chargeableRateInfo.total))
    val roomHeaderImageObservable = BehaviorSubject.create<String>(Images.getMediaHost() + hotelRoomResponse.roomThumbnailUrl)
    val expandRoomObservable = BehaviorSubject.create<Boolean>()
    val collapseRoomObservable = BehaviorSubject.create<Int>()
    val roomRateInfoTextObservable = BehaviorSubject.create<String>(hotelRoomResponse.roomLongDescription)
    val roomInfoObservable = BehaviorSubject.create<Int>()

    val expandCollapseRoomRate: Observer<Boolean> = endlessObserver {
        isChecked ->
        if (!isChecked) {
            roomSelectedObservable.onNext(hotelRoomResponse)
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
    }

    val expandCollapseRoomRateInfo: Observer<Unit> = endlessObserver {
        if (roomRateInfoVisible == View.VISIBLE) roomRateInfoVisible = View.GONE else roomRateInfoVisible = View.VISIBLE
        roomInfoObservable.onNext(roomRateInfoVisible)
    }

    init {
        val dailyPrice = Money(BigDecimal(hotelRoomResponse.rateInfo.chargeableRateInfo.priceToShowUsers.toDouble()), currencyCode)
        dailyPricePerNightObservable.onNext(dailyPrice.getFormattedMoney() + context.getResources().getString(R.string.per_night))
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
