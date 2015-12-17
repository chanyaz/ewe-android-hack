package com.expedia.vm

import android.content.Context
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.utils.HotelSearchParamsUtil
import com.expedia.util.endlessObserver
import rx.subjects.PublishSubject

class RecentSearchesAdapterViewModel(val context: Context) {

    var recentSearches: List<HotelSearchParams> = emptyList()

    val recentSearchSelectedSubject = PublishSubject.create<HotelSearchParams>()
    val recentSearchesObservable = PublishSubject.create<List<HotelSearchParams>>()

    val recentSearchesObserver = endlessObserver<Unit> {
        recentSearchesObservable.onNext(HotelSearchParamsUtil.loadSearchHistory(context))
    }
}
