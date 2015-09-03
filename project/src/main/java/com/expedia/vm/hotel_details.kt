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
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.utils.Amenity
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.Images
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.widget.RecyclerGallery
import com.expedia.util.endlessObserver
import com.mobiata.android.FormatUtils
import com.squareup.phrase.Phrase
import rx.Observable
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.math.BigDecimal
import java.util.ArrayList
import java.util.HashSet
import java.util.LinkedHashSet
import java.util.Locale
import kotlin.properties.Delegates

class HotelDetailViewModel(val context: Context, val hotelServices: HotelServices) : RecyclerGallery.GalleryItemListener {

    override fun onGalleryItemClicked(item: Any) {
        throw UnsupportedOperationException()
    }

    var hotelOffersResponse: HotelOffersResponse by Delegates.notNull()
    var etpOffersList = ArrayList<HotelOffersResponse.HotelRoomResponse>()
    val INTRO_PARAGRAPH_CUTOFF = 120
    var sectionBody: String by Delegates.notNull()
    var untruncated: String by Delegates.notNull()
    var amenityTitle: String by Delegates.notNull()

    var isSectionExpanded = false
    val sectionBodyObservable = BehaviorSubject.create<String>()
    val galleryObservable = BehaviorSubject.create<List<HotelMedia>>()

    val commonAmenityTextObservable = BehaviorSubject.create<CharSequence>()
    val amenityHeaderTextObservable = BehaviorSubject.create<String>()
    val amenityTextObservable = BehaviorSubject.create<String>()

    val amenitiesListObservable = BehaviorSubject.create<List<Amenity>>()
    val amenityTitleTextObservable = BehaviorSubject.create<String>()

    var etpHolderObservable = BehaviorSubject.create<Unit>()
    var renovationObservable = BehaviorSubject.create<Unit>()
    val hotelRenovationObservable = BehaviorSubject.create<Pair<String, String>>()

    var roomResponseListObservable = BehaviorSubject.create<List<HotelOffersResponse.HotelRoomResponse>>()
    var etpRoomResponseListObservable = BehaviorSubject.create<List<HotelOffersResponse.HotelRoomResponse>>()

    val hotelResortFeeObservable = BehaviorSubject.create<String>(null)
    val hotelNameObservable = BehaviorSubject.create<String>()
    val hotelRatingObservable = BehaviorSubject.create<Float>()
    val pricePerNightObservable = BehaviorSubject.create<String>()
    val searchInfoObservable = BehaviorSubject.create<String>()
    val userRatingObservable = BehaviorSubject.create<String>()
    val numberOfReviewsObservable = BehaviorSubject.create<String>()
    val hotelLatLngObservable = BehaviorSubject.create<DoubleArray>()
    val downloadListener: Observer<HotelOffersResponse> = endlessObserver { response ->
        if (response.hotelRenovationText?.content != null) renovationObservable.onNext(Unit)
        galleryObservable.onNext(Images.getHotelImages(response))

        roomResponseListObservable.onNext(response.hotelRoomResponse)

        if (hasEtpOffer(response)) {
            etpHolderObservable.onNext(Unit)
            etpOffersList = response.hotelRoomResponse
                    .filter { it.payLaterOffer != null }.toArrayList()
        }

        amenityHeaderTextObservable.onNext(response.hotelAmenitiesText.name)
        amenityTextObservable.onNext(Html.fromHtml(response.hotelAmenitiesText.content).toString())

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

        val amenityList: List<Amenity> = Amenity.amenitiesToShow(response.hotelAmenities)
        //Here have to pass the list of amenities which we want to show
        amenitiesListObservable.onNext(amenityList)
        if (amenityList.isEmpty()) amenityTitle = context.getResources().getString(R.string.AmenityNone)
        else amenityTitle = context.getResources().getString(R.string.AmenityTitle)
        amenityTitleTextObservable.onNext(amenityTitle)

        // common amenities text
        if (response.hotelRoomResponse.size() > 0) {
            val allValueAdds: List<List<String>> = response.hotelRoomResponse
                    .map {
                        it.valueAdds.map { it.description }
                    }

            val commonValueAdds: List<String> = allValueAdds
                    .drop(1)
                    .fold(allValueAdds.first().toArrayList(), { initial, nextValueAdds ->
                        initial.retainAll(nextValueAdds)
                        initial
                    })

            val commonValueAddsString = FormatUtils.series(context, commonValueAdds, ",", FormatUtils.Conjunction.AND)
                    .toLowerCase(Locale.getDefault())

            commonAmenityTextObservable.onNext(commonValueAddsString)
        }

        if (response.hotelRoomResponse.get(0).rateInfo.chargeableRateInfo.showResortFeeMessage) {
            val rate = response.hotelRoomResponse.get(0).rateInfo.chargeableRateInfo
            val hotelResortFee = Money(BigDecimal(rate.totalMandatoryFees.toDouble()), rate.currencyCode)
            hotelResortFeeObservable.onNext(hotelResortFee.getFormattedMoney(Money.F_NO_DECIMAL))
        }
    }

    val readMore: Observer<Unit> = endlessObserver {
        expandSection(untruncated, sectionBody)
    }

    val mapClickedSubject = PublishSubject.create<Unit>()

    val reviewsClickedSubject = PublishSubject.create<Unit>()

    val shareHotelClickedSubject = PublishSubject.create<Unit>()

    val renovationClickContainerObserver: Observer<Unit> = endlessObserver {
        var renovationInfo = Pair<String, String>(context.getResources().getString(R.string.renovation_notice),
                hotelOffersResponse.hotelRenovationText?.content ?: "")
        hotelRenovationObservable.onNext(renovationInfo)
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

    private fun expandSection(untruncated: String, sectionBody: String) {
        if (!isSectionExpanded) {
            sectionBodyObservable.onNext(untruncated)
            isSectionExpanded = true
        } else {
            sectionBodyObservable.onNext(sectionBody)
            isSectionExpanded = false
        }
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

            numberOfReviewsObservable.onNext(context.getResources().getQuantityString(R.plurals.number_of_reviews, hotel.totalReviews, hotel.totalReviews))

            hotelLatLngObservable.onNext(doubleArrayOf(hotel.latitude, hotel.longitude))
        }

        Observable.combineLatest(paramsSubject, hotelSelectedSubject, { p, h -> Pair(p, h) }).subscribe { data ->
            val (params, hotel) = data

            val subject = PublishSubject.create<HotelOffersResponse>()
            subject.subscribe { downloadListener.onNext(it) }
            hotelServices.details(params, hotel.hotelId, subject)

            searchInfoObservable.onNext(Phrase.from(context, R.string.calendar_instructions_date_range_with_guests_TEMPLATE).put("startdate",
                    DateUtils.localDateToMMMd(params.checkIn)).put("enddate",
                    DateUtils.localDateToMMMd(params.checkOut)).put("guests",
                    params.getGuestString()).format().toString())
        }
    }

}

var lastExpanded: Int = 0

public class HotelRoomRateViewModel(val context: Context, val hotelRoomResponse: HotelOffersResponse.HotelRoomResponse, val index: Int) {

    var roomRateInfoVisible: Int = View.GONE

    //Output
    val rateObservable = BehaviorSubject.create(hotelRoomResponse)
    val roomBackgroundViewObservable = BehaviorSubject.create<Drawable>()
    val roomSelectedObservable = BehaviorSubject.create<HotelOffersResponse.HotelRoomResponse>()
    val roomTypeObservable = BehaviorSubject.create<String>(hotelRoomResponse.roomTypeDescription)
    val collapsedBedTypeObservable = BehaviorSubject.create<String>()
    val expandedBedTypeObservable = BehaviorSubject.create<String>()
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
        }

        if (index == 0) {
            expandRoomObservable.onNext(true)
            roomBackgroundViewObservable.onNext(context.getResources().getDrawable(R.drawable.card_background))
        } else {
            expandRoomObservable.onNext(false)
        }
    }
}