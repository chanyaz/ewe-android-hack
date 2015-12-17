package com.expedia.vm

import android.content.Context
import android.text.Html
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.StrUtils
import com.squareup.phrase.Phrase
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

public class RecentSearchViewModel(val context: Context) {

    val titleObservable = BehaviorSubject.create<String>()
    val descriptionObservable = BehaviorSubject.create<CharSequence>()
    val recentSearchSelected = PublishSubject.create<HotelSearchParams>()

    // Inputs
    val recentSearchObserver = BehaviorSubject.create<HotelSearchParams>()

    init {
        recentSearchObserver.subscribe { hotelSearchParam ->

            descriptionObservable.onNext(Phrase.from(context, R.string.calendar_instructions_date_range_with_guests_TEMPLATE)
                    .put("startdate", DateUtils.localDateToMMMd(hotelSearchParam.checkIn))
                    .put("enddate", DateUtils.localDateToMMMd(hotelSearchParam.checkOut))
                    .put("guests", StrUtils.formatGuests(context, hotelSearchParam.adults, hotelSearchParam.children.size))
                    .format())

            titleObservable.onNext(Html.fromHtml(hotelSearchParam.suggestion.regionNames.displayName).toString())

        }
    }
}