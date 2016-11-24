package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.utils.DateFormatUtils
import com.squareup.phrase.Phrase
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

open class BaseCheckoutOverviewViewModel(context: Context) {
    val city = PublishSubject.create<String>()
    val country = PublishSubject.create<String>()

    val checkIn = PublishSubject.create<String>()
    val checkOut = PublishSubject.create<String?>()
    val guests = PublishSubject.create<Int>()

    val cityTitle = BehaviorSubject.create<String>()
    val datesTitle = BehaviorSubject.create<String>()
    val travelersTitle = BehaviorSubject.create<String>()
    val url = BehaviorSubject.create<List<String>>()
    val placeHolderDrawable = BehaviorSubject.create<Int>()

    init {
        Observable.zip(city, country, { city, country ->
            val text = Phrase.from(context, R.string.hotel_city_country_TEMPLATE)
                    .put("city", city)
                    .put("country", country)
                    .format().toString()
            cityTitle.onNext(text)
        }).subscribe()

        Observable.zip(checkIn, checkOut, { checkIn, checkOut ->
            val text =
                    if (checkIn != null && checkOut != null) {
                        DateFormatUtils.formatPackageDateRange(context, checkIn, checkOut)
                    } else {
                        DateFormatUtils.formatLocalDateToShortDayAndDate(checkIn)
                    }
            datesTitle.onNext(text)
        }).subscribe()

        guests.subscribe { travelers ->
            val text = context.resources.getQuantityString(R.plurals.number_of_travelers_TEMPLATE, travelers, travelers);
            travelersTitle.onNext(text)
        }
    }
}