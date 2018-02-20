package com.expedia.vm.flights

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.flights.FlightServiceClassType
import com.expedia.bookings.data.flights.RecentSearch
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.squareup.phrase.Phrase
import io.reactivex.subjects.PublishSubject
import org.joda.time.LocalDate

class RecentSearchViewHolderViewModel(val context: Context) {

    val originObservable = PublishSubject.create<String>()
    val destinationObservable = PublishSubject.create<String>()
    val priceObservable = PublishSubject.create<String>()
    val dateRangeObservable = PublishSubject.create<Pair<LocalDate, LocalDate?>>()
    val travelerCountObservable = PublishSubject.create<String>()
    val searchDateObservable = PublishSubject.create<String>()
    val classObservable = PublishSubject.create<String>()
    val roundTripObservable = PublishSubject.create<Boolean>()
    val recentSearchObservable = PublishSubject.create<RecentSearch>()

    init {
        recentSearchObservable.subscribe { recentSearch ->
            originObservable.onNext(recentSearch.sourceAirportCode)
            destinationObservable.onNext(recentSearch.destinationAirportCode)
            priceObservable.onNext(Money.getFormattedMoneyFromAmountAndCurrencyCode(recentSearch.amount.toBigDecimal(), recentSearch.currencyCode, Money.F_NO_DECIMAL))
            val startDate = LocalDate(recentSearch.startDate)
            var endDate: LocalDate? = null
            if (recentSearch.isRoundTrip) {
                endDate = LocalDate(recentSearch.endDate)
            }

            dateRangeObservable.onNext(Pair(startDate, endDate))
            travelerCountObservable.onNext(getTravelerCount(recentSearch.adultTravelerCount, recentSearch.childTraveler).toString())
            classObservable.onNext(getCabinClass(recentSearch.flightClass))
            roundTripObservable.onNext(recentSearch.isRoundTrip)
            searchDateObservable.onNext(getSearchedOnDateText(recentSearch.dateSearchedOn))
        }
    }

    fun getSearchedOnDateText(dateSearchedOn: Long): String {
        val dateSearchedOnObj = LocalDate(dateSearchedOn)
        val dateString = Phrase.from(context.resources, R.string.flight_recent_search_date_searched_TEMPLATE)
                .put("searchdate", LocaleBasedDateFormatUtils.localDateToMMMd(dateSearchedOnObj))
                .format().toString()
        return dateString
    }

    fun getCabinClass(cabinCode: String): String {
        val cabinClass = FlightServiceClassType.CabinCode.valueOf(cabinCode)
        return context.resources.getString(cabinClass.resId)
    }

    fun getTravelerCount(adultCount: Int, childrenSting: String): Int {
        if (childrenSting.trim().isEmpty()) {
            return adultCount
        } else {
            return adultCount + childrenSting.split(",").size
        }
    }
}
