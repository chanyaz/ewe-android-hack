package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.requests.RailSearchRequest
import com.expedia.vm.BaseCheckoutOverviewViewModel
import io.reactivex.subjects.PublishSubject
import org.joda.time.format.DateTimeFormat

class RailCheckoutOverviewViewModel(context: Context) : BaseCheckoutOverviewViewModel(context) {
    val params = PublishSubject.create<RailSearchRequest>()

    init {
        params.subscribe { searchParams ->
            val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
            val city = searchParams.destination?.regionNames?.shortName ?: ""

            cityTitle.onNext(city)

            val startDateFormatted = searchParams.departDate.toString(formatter)
            val endDateFormatted = searchParams.returnDate?.toString(formatter)

            if (endDateFormatted != null) {
                checkInAndCheckOutDate.onNext(Pair(startDateFormatted, endDateFormatted))
            } else {
                checkInWithoutCheckoutDate.onNext(startDateFormatted)
            }

            guests.onNext(searchParams.guests)
            placeHolderDrawable.onNext(R.drawable.scratchpad_intro_placeholder)
        }
    }
}