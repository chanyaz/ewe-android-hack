package com.expedia.vm.flights

import android.content.Context
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.flights.RecentSearch
import com.expedia.bookings.extensions.subscribeObserver
import com.expedia.bookings.utils.FlightsV2DataUtil
import com.expedia.bookings.utils.Ui
import com.expedia.util.endlessObserver
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.Callable

class RecentSearchViewModel(val context: Context) {

    val appDB = Ui.getApplication(context).appComponent().provideAppDatabase()
    val recentSearchDao = appDB.recentSearchDAO()
    val observeOn = AndroidSchedulers.mainThread()
    val subscribeOn = Schedulers.io()
    val recentSearchesObservable = PublishSubject.create<List<RecentSearch>>()
    val fetchRecentSearchesObservable = PublishSubject.create<Unit>()
    val saveRecentSearchObservable = PublishSubject.create<Money>()
    val recentSearchContainerObservable = PublishSubject.create<Boolean>()
    val maxCount = 3

    init {
        fetchRecentSearchesObservable.subscribe {
            fetchAllRecentSearches(observer)
        }
        saveRecentSearchObservable.subscribe { price ->
            val flightSearchParam = Db.getFlightSearchParams()
            insertRecentSearch(convertSearchParams(flightSearchParam, price))
        }
    }

    val observer =  endlessObserver<List<RecentSearch>> { recentSearches ->
        if (recentSearches.isEmpty()) {
            recentSearchContainerObservable.onNext(false)
        } else {
            recentSearchContainerObservable.onNext(true)
            recentSearchesObservable.onNext(recentSearches)
        }

    }

    private fun fetchAllRecentSearches(observer: Observer<List<RecentSearch>>) : Disposable {
        return recentSearchDao.loadAll()
                .subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .subscribeObserver(observer)
    }

    private fun insertRecentSearch(recentSearch: RecentSearch) : Disposable {
        return Observable.fromCallable(InsertRecentSearchCallable(recentSearch))
                .subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .subscribe()
    }

    private fun convertSearchParams(searchParams: FlightSearchParams, flightPrice: Money): RecentSearch {

        val sourceAirportCode = searchParams.departureAirport.hierarchyInfo?.airport?.airportCode
        val destinationAirportCode = searchParams.arrivalAirport.hierarchyInfo?.airport?.airportCode
        val departureAirport = FlightsV2DataUtil.generateGson().toJson(searchParams.departureAirport).toByteArray()
        val arrivalAirport = FlightsV2DataUtil.generateGson().toJson(searchParams.arrivalAirport).toByteArray()
        val startDate = searchParams.startDate.toString()
        val endDate = if (searchParams.isRoundTrip()) {
            searchParams.endDate?.toString()
        } else {
            ""
        }
        val flightClass = searchParams.flightCabinClass
        val dateSearchedOn = System.currentTimeMillis()
        val amount = flightPrice.roundedAmount.toLong()
        val currencyCode = flightPrice.currencyCode
        val adultTravelerCount = searchParams.adults
        val childCount = searchParams.childrenString
        val isInfantInLap = searchParams.infantSeatingInLap
        val isRoundTrip = searchParams.isRoundTrip()

        return RecentSearch(sourceAirportCode!!, destinationAirportCode!!, departureAirport, arrivalAirport, startDate,
                endDate!!, flightClass!!, dateSearchedOn, amount, currencyCode, adultTravelerCount, childCount, isInfantInLap, isRoundTrip)
    }

    private inner class InsertRecentSearchCallable(val recentSearch: RecentSearch) : Callable<Any> {
        override fun call() {
            if (recentSearchDao.count() < maxCount) {
                 recentSearchDao.insert(recentSearch)
            } else {
                if (!checkIfExist(recentSearch)) {
                    val oldestRecentSearch = recentSearchDao.getOldestRecentSearch()
                    recentSearchDao.deleteAndInsertTransaction(oldestRecentSearch, recentSearch)
                } else {
                     recentSearchDao.insert(recentSearch)
                }
            }
        }
    }

    fun checkIfExist(recentSearch: RecentSearch): Boolean {
        return recentSearchDao.checkIfExist(recentSearch.sourceAirportCode, recentSearch.destinationAirportCode,
                recentSearch.startDate, recentSearch.endDate, recentSearch.flightClass) == 1
    }

}
