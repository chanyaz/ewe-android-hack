package com.expedia.vm

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import com.expedia.bookings.ObservableOld
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.utils.HotelsV2DataUtil
import com.expedia.bookings.widget.priceFormatter
import com.expedia.util.LoyaltyUtil
import com.expedia.util.endlessObserver
import com.squareup.phrase.Phrase
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.math.BigDecimal

val ROOMS_LEFT_CUTOFF = 5

class HotelMapViewModel(val context: Context, val selectARoomObserver: Observer<Unit>, val hotelSoldOut: Observable<Boolean>, val lob:LineOfBusiness) {
    //Outputs for View
    val hotelName = BehaviorSubject.create<String>()
    val hotelStarRating = BehaviorSubject.create<Float>()
    val hotelStarRatingVisibility = BehaviorSubject.create<Boolean>()
    val hotelStarRatingContentDescription= BehaviorSubject.create<String>()
    val strikethroughPrice = BehaviorSubject.create<CharSequence>()
    private val price = BehaviorSubject.create<CharSequence>()
    val fromPrice = BehaviorSubject.createDefault<CharSequence>("")
    val fromPriceVisibility = fromPrice.map { it != null && !it.equals("") }
    var isShopWithPoints = PublishSubject.create<Boolean>()
    var isAirAttached = PublishSubject.create<Boolean>()
    val strikethroughPriceVisibility = ObservableOld.combineLatest(fromPriceVisibility, strikethroughPrice, isShopWithPoints, isAirAttached)
                                        {fromPriceVisible, strikethroughPrice, isShopWithPoints, isAirAttached ->
                                            ((fromPriceVisible && strikethroughPrice.isNotEmpty()) && (isShopWithPoints || !isAirAttached))}
    val hotelLatLng = BehaviorSubject.create<DoubleArray>()
    val selectARoomInvisibility = BehaviorSubject.createDefault<Boolean>(false)
    var selectRoomContDescription = PublishSubject.create<String>()

    //Setup the data I need to behave as a View Model for my View
    val offersObserver = endlessObserver<HotelOffersResponse> { response ->
        hotelName.onNext(response.hotelName)
        hotelStarRating.onNext(response.hotelStarRating.toFloat())
        hotelStarRatingContentDescription.onNext(HotelsV2DataUtil.getHotelRatingContentDescription(context, response.hotelStarRating))
        hotelStarRatingVisibility.onNext(response.hotelStarRating > 0)
        price.onNext(priceFormatter(context.resources, response.hotelRoomResponse?.firstOrNull()?.rateInfo?.chargeableRateInfo, false, !response.isPackage))
        strikethroughPrice.onNext(priceFormatter(context.resources, response.hotelRoomResponse?.firstOrNull()?.rateInfo?.chargeableRateInfo, true, !response.isPackage))
        hotelLatLng.onNext(doubleArrayOf(response.latitude, response.longitude))
        isShopWithPoints.onNext(LoyaltyUtil.isShopWithPoints(response.hotelRoomResponse?.firstOrNull()?.rateInfo?.chargeableRateInfo))
        isAirAttached.onNext(response.hotelRoomResponse?.firstOrNull()?.rateInfo?.chargeableRateInfo?.airAttached ?: false)

        val firstHotelRoomResponse = response.hotelRoomResponse?.firstOrNull()
        if (firstHotelRoomResponse != null) {
            val firstRoomRate = firstHotelRoomResponse.rateInfo?.chargeableRateInfo
            fromPrice.onNext(fromPriceStyledString(context, firstRoomRate))
        }
    }

    init {
        hotelSoldOut.subscribe(selectARoomInvisibility)
        fromPrice.subscribe { it ->
            selectRoomContDescription.onNext(Phrase.from(context, R.string.map_select_a_room_cont_desc_TEMPLATE)
                    .put("price", it).format().toString())
        }
    }

    companion object {
        fun fromPriceStyledString(context: Context, hotelRoomRate: HotelRate?): CharSequence {
            if (hotelRoomRate == null) {
                return ""
            }

            val roomDailyPrice = Money(BigDecimal(hotelRoomRate.displayPrice.toDouble()), hotelRoomRate.currencyCode).getFormattedMoney(Money.F_NO_DECIMAL)

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
