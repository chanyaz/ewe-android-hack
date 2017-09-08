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

    val checkInAndCheckOutDate = PublishSubject.create<Pair<String, String>>()
    val checkInWithoutCheckoutDate = PublishSubject.create<String>()
    val guests = PublishSubject.create<Int>()

    val cityTitle = BehaviorSubject.create<String>()
    val datesTitle = BehaviorSubject.create<String>()
    val datesTitleContDesc = BehaviorSubject.create<String>()
    val travelersTitle = BehaviorSubject.create<String>()
    val url = BehaviorSubject.create<List<String>>()
    val placeHolderDrawable = BehaviorSubject.create<Int>()
    val subTitleText = BehaviorSubject.create<String>()
    val subTitleContDesc = BehaviorSubject.create<String>()

    init {
        Observable.zip(city, country, { city, country ->
            val text = Phrase.from(context, R.string.hotel_city_country_TEMPLATE)
                    .put("city", city)
                    .put("country", country)
                    .format().toString()
            cityTitle.onNext(text)
        }).subscribe()

        checkInAndCheckOutDate.subscribe { (checkIn, checkOut) ->
            datesTitle.onNext(DateFormatUtils.formatPackageDateRange(context, checkIn, checkOut))
            datesTitleContDesc.onNext(DateFormatUtils.formatPackageDateRangeContDesc(context, checkIn, checkOut))
        }

        checkInWithoutCheckoutDate.subscribe { checkIn ->
            val text = DateFormatUtils.formatLocalDateToShortDayAndDate(checkIn)
            datesTitle.onNext(text)
            datesTitleContDesc.onNext(text)
        }

        guests.subscribe { travelers ->
            val text = context.resources.getQuantityString(R.plurals.number_of_travelers_TEMPLATE, travelers, travelers)
            travelersTitle.onNext(text)
        }

        Observable.combineLatest(datesTitle, travelersTitle) { datesTitle, travelersTitle ->
            subTitleText.onNext(Phrase.from(context, R.string.flight_overview_toolbar_TEMPLATE)
                    .put("date", datesTitle).put("guests", travelersTitle)
                    .format().toString())
        }.subscribe()

        Observable.combineLatest(datesTitleContDesc, travelersTitle) { datesTitleContDesc, travelersTitle ->
            subTitleContDesc.onNext(Phrase.from(context, R.string.flight_overview_toolbar_TEMPLATE)
                    .put("date", datesTitleContDesc).put("guests", travelersTitle)
                    .format().toString())
        }.subscribe()

    }
}