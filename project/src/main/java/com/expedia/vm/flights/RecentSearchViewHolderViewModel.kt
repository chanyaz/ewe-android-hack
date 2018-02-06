package com.expedia.vm.flights

import com.expedia.bookings.data.flights.RecentSearch
import io.reactivex.subjects.PublishSubject

class RecentSearchViewHolderViewModel() {

    val originObservable = PublishSubject.create<String>()
    val destinationObservable = PublishSubject.create<String>()
    val priceObservable = PublishSubject.create<String>()
    val dateRangeObservable = PublishSubject.create<String>()
    val travelerCountObservable = PublishSubject.create<String>()
    val searchDateObservable = PublishSubject.create<String>()
    val classObservable = PublishSubject.create<String>()
    val roundTripObservable = PublishSubject.create<Boolean>()
    val recentSearchObservable = PublishSubject.create<RecentSearch>()

    init {
        recentSearchObservable.subscribe { recentSearch ->
            originObservable.onNext(recentSearch.originLocation)
            destinationObservable.onNext(recentSearch.destinationLocation)
            priceObservable.onNext(recentSearch.amount)
            dateRangeObservable.onNext(recentSearch.dateRange)
            travelerCountObservable.onNext(recentSearch.travelerCount)
            classObservable.onNext(recentSearch.flightClass)
            roundTripObservable.onNext(recentSearch.isRoundTrip)
            searchDateObservable.onNext(recentSearch.searchDate)
        }
    }
}
