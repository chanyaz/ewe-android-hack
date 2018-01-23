package com.expedia.bookings.meso.vm

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.meso.MesoAdResponseProvider
import com.expedia.bookings.meso.model.MesoAdResponse
import com.expedia.bookings.meso.model.MesoHotelAdResponse
import com.expedia.util.Optional
import com.squareup.phrase.Phrase
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

class MesoHotelAdViewModel(val context: Context) {
    var mesoHotelAdResponse: MesoHotelAdResponse? = null
    val oneLineSubText: String by lazy { getSubTextFormattedString(" · ") }
    val twoLineSubText: String by lazy { getSubTextFormattedString("<br/>") }
    val backgroundImage: Drawable by lazy { getMesoHotelBackgroundImage() }
    val percentageOff: String? by lazy { getPriceOffString() }
    val hotelName: String? by lazy { getMesoHotelName() }

    fun fetchHotelMesoAd(mesoHotelAdObserver: Observer<Optional<MesoHotelAdResponse>>) {
        MesoAdResponseProvider.fetchHotelMesoAd(context, getMesoHotelSubject(mesoHotelAdObserver))
    }

    private fun getMesoHotelSubject(mesoHotelAdObserver: Observer<Optional<MesoHotelAdResponse>>): PublishSubject<MesoAdResponse> {
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

    private fun getPriceOffString(): String? {
        return Phrase.from(context, R.string.meso_percent_off_TEMPLATE)
                .put("percent", mesoHotelAdResponse?.percentageOff)
                .format()
                .toString()
    }

    private fun getMesoHotelName(): String? {
        return mesoHotelAdResponse?.hotelName.toString()
    }

    private fun getSubTextFormattedString(bulletOrNewLine: String): String {
        return Phrase.from(context, R.string.meso_sub_text_TEMPLATE)
                .put("location", mesoHotelAdResponse?.propertyLocation)
                .put("bullet_or_new_line", bulletOrNewLine)
                .put("strike_price", mesoHotelAdResponse?.strikethroughPrice)
                .put("price", mesoHotelAdResponse?.offerPrice)
                .format()
                .toString()
    }
}
