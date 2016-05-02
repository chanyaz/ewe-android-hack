package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.data.rail.requests.RailSearchRequest
import com.expedia.vm.BaseCheckoutOverviewViewModel
import org.joda.time.format.DateTimeFormat
import rx.subjects.PublishSubject

class RailCheckoutOverviewViewModel(context: Context) : BaseCheckoutOverviewViewModel(context) {
    val params = PublishSubject.create<RailSearchRequest>()

    init {
        params.subscribe { searchParams ->
            val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
            val city = searchParams.destination.regionNames.shortName
            val link = "https://media.expedia.com/hotels/1000000/10000/900/897/897_36_z.jpg"  //TODO fixme
            val links = listOf<String>(link)

            cityTitle.onNext(city)
            checkIn.onNext(searchParams?.departDate?.toString(formatter))
            checkOut.onNext(searchParams?.returnDate?.toString(formatter))
            guests.onNext(searchParams.guests)
            url.onNext(links)
        }
    }
}