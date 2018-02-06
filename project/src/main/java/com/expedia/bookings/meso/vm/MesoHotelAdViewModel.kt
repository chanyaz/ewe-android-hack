package com.expedia.bookings.meso.vm

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.annotation.VisibleForTesting
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.meso.MesoAdResponseProvider
import com.expedia.bookings.meso.model.MesoAdResponse
import com.expedia.bookings.meso.model.MesoHotelAdResponse
import com.expedia.util.Optional
import com.squareup.phrase.Phrase
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import org.joda.time.DateTimeConstants
import org.joda.time.LocalDate

class MesoHotelAdViewModel(val context: Context) {
    private val NUMBER_OF_ADULTS = 2
    var mesoHotelAdResponse: MesoHotelAdResponse? = null
    val oneLineSubText: String by lazy { getSubTextFormattedString(" · ") }
    val twoLineSubText: String by lazy { getSubTextFormattedString("<br/>") }
    val backgroundImage: Drawable by lazy { getMesoHotelBackgroundImage() }
    val percentageOff: String by lazy { getPercentageOffString().toString() }
    val hotelName: String by lazy { getMesoHotelName().toString() }
    val hotelParamsForSearch: HotelSearchParams by lazy { getHotelSearchParams() }

    fun dataIsValid(): Boolean {
        return !mesoHotelAdResponse?.hotelId.isNullOrBlank() &&
                mesoHotelAdResponse?.percentageOff != null &&
                mesoHotelAdResponse?.hotelName != null &&
                mesoHotelAdResponse?.propertyLocation != null &&
                mesoHotelAdResponse?.regionId != null
    }

    fun fetchHotelMesoAd(mesoHotelAdObserver: Observer<Optional<MesoHotelAdResponse>>) {
        MesoAdResponseProvider.fetchHotelMesoAd(context, getMesoHotelSubject(mesoHotelAdObserver))
    }

    @VisibleForTesting
    fun getMesoHotelSubject(mesoHotelAdObserver: Observer<Optional<MesoHotelAdResponse>>): PublishSubject<MesoAdResponse> {
        val mesoHotelAdResponseSubject: PublishSubject<MesoAdResponse> = PublishSubject.create<MesoAdResponse>()
        mesoHotelAdResponseSubject.subscribe(object : Observer<MesoAdResponse> {
            override fun onSubscribe(d: Disposable) {
                mesoHotelAdObserver.onSubscribe(d)
            }

            override fun onNext(mesoAdResponse: MesoAdResponse) {
                mesoHotelAdResponse = mesoAdResponse.HotelAdResponse
                mesoHotelAdObserver.onNext(Optional(mesoHotelAdResponse))
            }

            override fun onComplete() {
                mesoHotelAdObserver.onComplete()
            }

            override fun onError(e: Throwable) {
                mesoHotelAdObserver.onError(e)
            }
        })

        return mesoHotelAdResponseSubject
    }

    fun shouldFormatSubText(numberOfLines: Int): Boolean {
        return numberOfLines > 1
    }

    private fun getMesoHotelBackgroundImage(): Drawable {
        return mesoHotelAdResponse?.background?.drawable
                ?: ContextCompat.getDrawable(context, R.color.launch_screen_placeholder_color)
    }

    private fun getPercentageOffString(): CharSequence {
        return mesoHotelAdResponse?.percentageOff ?: ""
    }

    private fun getMesoHotelName(): CharSequence {
        return mesoHotelAdResponse?.hotelName ?: ""
    }

    private fun getSubTextFormattedString(bulletOrNewLine: String): String {
        return if (subTextDataIsValid()) {
            Phrase.from(context, R.string.meso_sub_text_TEMPLATE)
                    .put("location", mesoHotelAdResponse?.propertyLocation)
                    .put("bullet_or_new_line", bulletOrNewLine)
                    .put("strike_price", mesoHotelAdResponse?.strikethroughPrice)
                    .put("price", mesoHotelAdResponse?.offerPrice)
                    .format()
                    .toString()
        } else (mesoHotelAdResponse?.propertyLocation ?: "").toString()
    }

    private fun subTextDataIsValid(): Boolean {
        return !mesoHotelAdResponse?.propertyLocation.isNullOrBlank() &&
                !mesoHotelAdResponse?.strikethroughPrice.isNullOrBlank() &&
                !mesoHotelAdResponse?.offerPrice.isNullOrBlank()
    }

    private fun getHotelSearchParams(): HotelSearchParams {
        val startDate = getMesoHotelStartDate()

        val suggestionV4 = SuggestionV4()
        suggestionV4.hotelId = (mesoHotelAdResponse?.hotelId ?: "").toString()
        suggestionV4.gaiaId = (mesoHotelAdResponse?.regionId ?: "").toString()
        suggestionV4.regionNames = SuggestionV4.RegionNames()
        suggestionV4.regionNames.fullName = (mesoHotelAdResponse?.propertyLocation ?: "").toString()

        return HotelSearchParams.Builder(0, 0)
                .destination(suggestionV4)
                .startDate(startDate)
                .endDate(startDate.plusDays(2))
                .adults(NUMBER_OF_ADULTS)
                .children(emptyList())
                .build() as HotelSearchParams
    }

    private fun getMesoHotelStartDate(): LocalDate {
        var localDate = LocalDate.now()
        if (localDate.dayOfWeek >= DateTimeConstants.FRIDAY) {
            localDate = localDate.plusWeeks(1)
        }

        return localDate.withDayOfWeek(DateTimeConstants.FRIDAY)
    }
}
