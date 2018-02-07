package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.util.Optional
import com.squareup.phrase.Phrase
import io.reactivex.subjects.BehaviorSubject
import org.joda.time.LocalDate

open class BaseToolbarViewModel(private val context: Context) {
    //input
    val refreshToolBar = BehaviorSubject.create<Boolean>()
    val isOutboundSearch = BehaviorSubject.create<Boolean>() // TODO - move this into flightSearchViewModel
    val setTitleOnly = BehaviorSubject.create<String>()
    val regionNames = BehaviorSubject.create<Optional<SuggestionV4.RegionNames>>()
    val country = BehaviorSubject.create<Optional<String>>()
    val airport = BehaviorSubject.create<Optional<String>>()
    val travelers = BehaviorSubject.create<Int>()
    val date = BehaviorSubject.create<LocalDate>()

    //output
    val titleSubject = BehaviorSubject.create<String>()
    val subtitleSubject = BehaviorSubject.create<CharSequence>()
    val menuVisibilitySubject = BehaviorSubject.create<Boolean>()

    init {
        setTitleOnly.subscribe { title ->
            titleSubject.onNext(title)
            subtitleSubject.onNext("")
            menuVisibilitySubject.onNext(false)
        }
    }

    protected fun getSubtitle(date: LocalDate, numTravelers: Int): String {
        val travelers = context.resources.getQuantityString(R.plurals.number_of_travelers_TEMPLATE, numTravelers, numTravelers)
        val subtitle = Phrase.from(context, R.string.flight_calendar_instructions_date_with_guests_TEMPLATE)
                .put("startdate", LocaleBasedDateFormatUtils.dateTimeToEEEMMMddyyyy(date))
                .put("guests", travelers)
                .format()
                .toString()
        return subtitle
    }
}
