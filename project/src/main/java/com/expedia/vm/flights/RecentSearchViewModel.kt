package com.expedia.vm.flights

import android.content.Context
import com.expedia.bookings.data.flights.RecentSearch
import io.reactivex.subjects.PublishSubject

class RecentSearchViewModel(val context: Context) {

    val recentSearchesObservable = PublishSubject.create<List<RecentSearch>>()
    val fetchandShowRecentObservable = PublishSubject.create<Unit>()

    init {
        fetchandShowRecentObservable.subscribe {
            val recentSearches = ArrayList<RecentSearch>()

            recentSearchesObservable.onNext(recentSearches)
        }
    }
}
