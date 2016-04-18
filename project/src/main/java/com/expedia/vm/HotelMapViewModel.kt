package com.expedia.vm

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.widget.priceFormatter
import com.expedia.util.endlessObserver
import rx.Observable
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.math.BigDecimal

val ROOMS_LEFT_CUTOFF = 5

class HotelMapViewModel(val context: Context, val selectARoomObserver: Observer<Unit>, val hotelSoldOut: Observable<Boolean>) {
    //Outputs for View
    val hotelName = BehaviorSubject.create<String>()
    val hotelStarRating = BehaviorSubject.create<Float>()
    val hotelStarRatingVisibility = BehaviorSubject.create<Boolean>()
    val strikethroughPrice = BehaviorSubject.create<CharSequence>()
    private val price = BehaviorSubject.create<CharSequence>()
    val fromPrice = BehaviorSubject.create<CharSequence>("")
    val fromPriceVisibility = fromPrice.map { it != null && !it.equals("") }
    val strikethroughPriceVisibility = Observable.combineLatest(fromPriceVisibility, strikethroughPrice)
                                        {fromPriceVisible, strikethroughPrice -> fromPriceVisible && strikethroughPrice.isNotEmpty()}
    val hotelLatLng = BehaviorSubject.create<DoubleArray>()
    val resetCameraPosition = PublishSubject.create<Unit>()
    val selectARoomInvisibility = BehaviorSubject.create<Boolean>(false)

    //Setup the data I need to behave as a View Model for my View
    val offersObserver = endlessObserver<HotelOffersResponse> { response ->
        hotelName.onNext(response.hotelName)
        hotelStarRating.onNext(response.hotelStarRating.toFloat())
        hotelStarRatingVisibility.onNext(response.hotelStarRating > 0)
        price.onNext(priceFormatter(context.resources, response.hotelRoomResponse?.firstOrNull()?.rateInfo?.chargeableRateInfo, false, !response.isPackage))
        strikethroughPrice.onNext(priceFormatter(context.resources, response.hotelRoomResponse?.firstOrNull()?.rateInfo?.chargeableRateInfo, true, !response.isPackage))
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

            val roomDailyPrice = Money(BigDecimal(hotelRoomRate.priceToShowUsersFallbackToZeroIfNegative.toDouble()), hotelRoomRate.currencyCode).getFormattedMoney(Money.F_NO_DECIMAL)

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
