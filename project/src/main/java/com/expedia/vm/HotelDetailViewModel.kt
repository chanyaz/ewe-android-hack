package com.expedia.vm

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.Html
import com.expedia.bookings.R
import com.expedia.bookings.data.HotelMedia
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.utils.*
import com.expedia.bookings.widget.RecyclerGallery
import com.expedia.util.endlessObserver
import com.mobiata.android.FormatUtils
import com.squareup.phrase.Phrase
import rx.Observer
import rx.Subscription
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

    var hotelOffersResponse: HotelOffersResponse by Delegates.notNull()
    var etpOffersList = ArrayList<HotelOffersResponse.HotelRoomResponse>()
    val INTRO_PARAGRAPH_CUTOFF = 120
    var sectionBody: String by Delegates.notNull()
    var untruncated: String by Delegates.notNull()
    var amenityTitle: String by Delegates.notNull()
    var hotelSearchParams: HotelSearchParams by Delegates.notNull()
    var hotel: Hotel by Delegates.notNull()

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
    val reviewsObservable = BehaviorSubject.create<Hotel>()
    val numberOfReviewsObservable = BehaviorSubject.create<String>()
    val hotelLatLngObservable = BehaviorSubject.create<DoubleArray>()
    val downloadListener: Observer<HotelOffersResponse> = endlessObserver { response ->
        hotelOffersResponse = response
        bindDetails()
    }

    fun getDetail() {
        val subject = PublishSubject.create<HotelOffersResponse>()
        subject.subscribe { downloadListener.onNext(it) }
        hotelServices.details(hotelSearchParams, hotel.hotelId, subject)
    }

    val readMore: Observer<Unit> = endlessObserver {
        expandSection(untruncated, sectionBody)
    }

    val mapClickContainer: Observer<Unit> = endlessObserver {
        val uri = "geo:" + hotel.latitude + "," + hotel.longitude
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        context.startActivity(intent)
    }

    val reviewsClickObserver: Observer<Unit> = endlessObserver {
        reviewsObservable.onNext(hotel)
    }

    val shareClickContainerObserver: Observer<Unit> = endlessObserver {
        context.startActivity(Intent.createChooser(ShareUtils.generateShareIntent(context, hotel.hotelId, hotelSearchParams),
                context.getResources().getString(R.string.share_via)));
    }

    val renovationClickContainerObserver: Observer<Unit> = endlessObserver {
        var renovationInfo = Pair<String, String>(context.getResources().getString(R.string.renovation_notice),
                hotelOffersResponse.hotelRenovationText?.content ?: "")
        hotelRenovationObservable.onNext(renovationInfo)
    }

    val searchObserver: Observer<HotelSearchParams> = endlessObserver { params ->
        hotelSearchParams = params
    }

    val hotelObserver: Observer<Hotel> = endlessObserver { h ->
        hotel = h
        hotelNameObservable.onNext(hotel.localizedName)
        hotelRatingObservable.onNext(hotel.hotelStarRating)
        pricePerNightObservable.onNext(Phrase.from(context.getResources(),
                R.string.per_nt_TEMPLATE).put("price",
                hotel.lowRateInfo.nightlyRateTotal.toString()).format().toString())
        searchInfoObservable.onNext(Phrase.from(context, R.string.calendar_instructions_date_range_with_guests_TEMPLATE).put("startdate",
                DateUtils.localDateToMMMd(hotelSearchParams.checkIn)).put("enddate",
                DateUtils.localDateToMMMd(hotelSearchParams.checkOut)).put("guests",
                hotelSearchParams.getGuestString()).format().toString())
        userRatingObservable.onNext(hotel.hotelGuestRating.toString())
        numberOfReviewsObservable.onNext(context.getResources().getQuantityString(R.plurals.number_of_reviews, hotel.totalReviews, hotel.totalReviews))
        hotelLatLngObservable.onNext(doubleArrayOf(hotel.latitude, hotel.longitude))
    }

    fun bindDetails() {
        if (hotelOffersResponse.hotelRenovationText?.content != null) renovationObservable.onNext(Unit)
        galleryObservable.onNext(Images.getHotelImages(hotelOffersResponse))

        roomResponseListObservable.onNext(hotelOffersResponse.hotelRoomResponse)
        if (hasEtpOffer()) {
            etpHolderObservable.onNext(Unit)
            etpOffersList = hotelOffersResponse.hotelRoomResponse
                    .filter { it.payLaterOffer != null }.toArrayList()
        }
        amenityHeaderTextObservable.onNext(hotelOffersResponse.hotelAmenitiesText.name)
        amenityTextObservable.onNext(Html.fromHtml(hotelOffersResponse.hotelAmenitiesText.content).toString())
        if (hotelOffersResponse.firstHotelOverview != null) {
            sectionBody = Html.fromHtml(hotelOffersResponse.firstHotelOverview).toString()

            //add read more if hotel intro is too long
            if (sectionBody.length() > INTRO_PARAGRAPH_CUTOFF) {
                untruncated = sectionBody
                sectionBody = Phrase.from(context, R.string.hotel_ellipsize_text_template).put("text",
                        sectionBody.substring(0, Strings.cutAtWordBarrier(sectionBody, INTRO_PARAGRAPH_CUTOFF))).format().toString()
            }
            sectionBodyObservable.onNext(sectionBody)
        }

        val amenityList: List<Amenity> = Amenity.amenitiesToShow(hotelOffersResponse.hotelAmenities)
        //Here have to pass the list of amenities which we want to show
        amenitiesListObservable.onNext(amenityList)
        if (amenityList.isEmpty()) amenityTitle = context.getResources().getString(R.string.AmenityNone)
        else amenityTitle = context.getResources().getString(R.string.AmenityTitle)
        amenityTitleTextObservable.onNext(amenityTitle)

        // getting common amenities text
        var rateCount = hotelOffersResponse.hotelRoomResponse.size()
        if (rateCount > 0) {
            var list: List<String> = getCommonAdListForRate(hotelOffersResponse.hotelRoomResponse.get(0))
            if (!list.isEmpty()) {
                for (index in 0..rateCount - 1) {
                    list.toArrayList().retainAll(getCommonAdListForRate(hotelOffersResponse.hotelRoomResponse.get(index)))
                }
                if (list.size() > 0) {
                    val text = Html.fromHtml(context.getString(R.string.common_value_add_template,
                            FormatUtils.series(context, list, ",", FormatUtils.Conjunction.AND).toLowerCase(Locale.getDefault())));
                    commonAmenityTextObservable.onNext(text)
                }
            }
        }

        if (hotelOffersResponse.hotelRoomResponse.get(0).rateInfo.chargeableRateInfo.showResortFeeMessage) {
            val rate = hotelOffersResponse.hotelRoomResponse.get(0).rateInfo.chargeableRateInfo
            val hotelResortFee = Money(BigDecimal(rate.totalMandatoryFees.toDouble()), rate.currencyCode)
            hotelResortFeeObservable.onNext(hotelResortFee.getFormattedMoney(Money.F_NO_DECIMAL))
        }
    }

    private fun getCommonAdListForRate(hotelRoomResponse: HotelOffersResponse.HotelRoomResponse): List<String> {
        if (hotelRoomResponse.valueAdds == null) return emptyList()
        var commonlist = ArrayList<String>(hotelRoomResponse.valueAdds.size())
        for (index in 0..hotelRoomResponse.valueAdds.size() - 1) {
            commonlist.add(hotelRoomResponse.valueAdds.get(index).description)
        }
        return commonlist
    }

    public fun hasEtpOffer(): Boolean {
        return hotelOffersResponse.hotelRoomResponse
                .any { it.payLaterOffer != null }
    }

    public fun expandSection(untruncated: String, sectionBody: String) {
        if (!isSectionExpanded) {
            sectionBodyObservable.onNext(untruncated)
            isSectionExpanded = true
        } else {
            sectionBodyObservable.onNext(sectionBody)
            isSectionExpanded = false
        }
    }

}
