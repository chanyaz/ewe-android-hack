package com.expedia.vm.flights

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.flights.FlightServiceClassType
import com.expedia.bookings.data.flights.RecentSearch
import com.expedia.bookings.utils.DateFormatUtils
import com.squareup.phrase.Phrase
import io.reactivex.subjects.PublishSubject
import org.joda.time.LocalDate

class RecentSearchViewHolderViewModel(val context: Context) {

    val originObservable = PublishSubject.create<String>()
    val destinationObservable = PublishSubject.create<String>()
    val priceObservable = PublishSubject.create<String>()
    val dateRangeObservable = PublishSubject.create<String>()
    val travelerCountObservable = PublishSubject.create<String>()
    val searchDateObservable = PublishSubject.create<String>()
    val classObservable = PublishSubject.create<String>()
    val roundTripObservable = PublishSubject.create<Boolean>()
    val recentSearchObservable = PublishSubject.create<RecentSearch>()
    lateinit var recentSearchItem: RecentSearch

    init {
        recentSearchObservable.subscribe { recentSearch ->
            recentSearchItem = recentSearch
            originObservable.onNext(recentSearch.sourceAirportCode)
            destinationObservable.onNext(recentSearch.destinationAirportCode)
            priceObservable.onNext(getPriceText(Money(recentSearch.amount.toBigDecimal(), recentSearch.currencyCode)))
            dateRangeObservable.onNext(getDateText(recentSearch.startDate, recentSearch.endDate, recentSearch.isRoundTrip))
            travelerCountObservable.onNext(getTravelerCount(recentSearch.adultTravelerCount, recentSearch.childTraveler).toString())
            classObservable.onNext(getCabinClass(recentSearch.flightClass))
            roundTripObservable.onNext(recentSearch.isRoundTrip)
            searchDateObservable.onNext(getSearchedOnDateText(recentSearch.dateSearchedOn))
        }
    }

    fun getDateText(startDateStr: String, endDateStr: String, isRoundTrip: Boolean): String {
        lateinit var dateString: String
        val startDate =  LocalDate(startDateStr)
        if (isRoundTrip) {
            val endDate =  LocalDate(endDateStr)
            dateString = Phrase.from(context.resources, R.string.calendar_instructions_date_range_flight_extra_spacing_TEMPLATE)
                    .put("startdate", getFormattedDate(startDate))
                    .put("enddate", getFormattedDate(endDate))
                    .format().toString()
        } else {
            dateString = Phrase.from(context.resources, R.string.calendar_instructions_date_rail_one_way_TEMPLATE)
                    .put("startdate", getFormattedDate(startDate))
                    .format().toString()
        }
        return dateString
    }

    fun getSearchedOnDateText(dateSearchedOn: Long): String {
        val dateSearchedOnObj = LocalDate(dateSearchedOn)
        val dateString = Phrase.from(context.resources, R.string.flight_recent_search_date_searched_TEMPLATE)
                .put("searchdate", getFormattedDate(dateSearchedOnObj))
                .format().toString()
        return dateString
    }

    fun getFormattedDate(date: LocalDate): String? {
        return DateFormatUtils.formatLocalDateToMMMdBasedOnLocale(date)
    }

    fun getPriceText(price: Money): String {
        return Money.getFormattedMoneyFromAmountAndCurrencyCode(price.amount, price.currencyCode, Money.F_NO_DECIMAL)
    }

    fun getCabinClass(cabinCode: String): String
    {
        val cabinClass = FlightServiceClassType.CabinCode.valueOf(cabinCode)
        return context.resources.getString(cabinClass.resId)
    }

    fun getTravelerCount(adultCount: Int, childrenSting: String): Int {

        if (childrenSting.trim().isEmpty())  {
            return adultCount
        } else {
            return adultCount + childrenSting.split(",").size
        }

    }
}
