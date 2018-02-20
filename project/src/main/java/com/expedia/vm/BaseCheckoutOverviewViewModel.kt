package com.expedia.vm

import android.content.Context
import com.expedia.bookings.extensions.ObservableOld
import com.expedia.bookings.R
import com.expedia.bookings.utils.DateRangeUtils
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.squareup.phrase.Phrase
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

open class BaseCheckoutOverviewViewModel(context: Context) {
    val city = PublishSubject.create<String>()

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
        city.subscribe(cityTitle)

        checkInAndCheckOutDate.subscribe { (checkIn, checkOut) ->
            datesTitle.onNext(DateRangeUtils.formatPackageDateRange(context, checkIn, checkOut))
            datesTitleContDesc.onNext(DateRangeUtils.formatPackageDateRangeContDesc(context, checkIn, checkOut))
        }

        checkInWithoutCheckoutDate.subscribe { checkIn ->
            val text = LocaleBasedDateFormatUtils.yyyyMMddStringToEEEMMMddyyyy(checkIn)
            datesTitle.onNext(text)
            datesTitleContDesc.onNext(text)
        }

        guests.subscribe { travelers ->
            val text = context.resources.getQuantityString(R.plurals.number_of_travelers_TEMPLATE, travelers, travelers)
            travelersTitle.onNext(text)
        }

        ObservableOld.combineLatest(datesTitle, travelersTitle) { datesTitle, travelersTitle ->
            subTitleText.onNext(Phrase.from(context, R.string.flight_overview_toolbar_TEMPLATE)
                    .put("date", datesTitle).put("guests", travelersTitle)
                    .format().toString())
        }.subscribe()

        ObservableOld.combineLatest(datesTitleContDesc, travelersTitle) { datesTitleContDesc, travelersTitle ->
            subTitleContDesc.onNext(Phrase.from(context, R.string.flight_overview_toolbar_TEMPLATE)
                    .put("date", datesTitleContDesc).put("guests", travelersTitle)
                    .format().toString())
        }.subscribe()
    }
}
