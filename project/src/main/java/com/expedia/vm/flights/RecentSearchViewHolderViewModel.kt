package com.expedia.vm.flights

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.flights.FlightServiceClassType
import com.expedia.bookings.data.flights.RecentSearch
import com.expedia.bookings.extensions.withLatestFrom
import com.expedia.bookings.utils.FlightsV2DataUtil
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
    val clickObserver = PublishSubject.create<Unit>()
    val selectedSearchItem = PublishSubject.create<FlightSearchParams>()

    init {

        clickObserver.withLatestFrom(recentSearchObservable, { _, recentSearch ->
            val searchParam = convertRecentSearchToSearchParams(recentSearch)
            selectedSearchItem.onNext(searchParam)
        }).subscribe()

        recentSearchObservable.subscribe { recentSearch ->
            originObservable.onNext(recentSearch.sourceAirportCode)
            destinationObservable.onNext(recentSearch.destinationAirportCode)
            priceObservable.onNext(getPriceText(Money(recentSearch.amount.toBigDecimal(), recentSearch.currencyCode)))
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

    fun getPriceText(price: Money): String {
        return Money.getFormattedMoneyFromAmountAndCurrencyCode(price.amount, price.currencyCode, Money.F_NO_DECIMAL)
    }

    fun getCabinClass(cabinCode: String): String
    {
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

    private fun convertRecentSearchToSearchParams(recentSearch: RecentSearch): FlightSearchParams {
        val sourceSuggestion = getObjectFromJSON(String(recentSearch.sourceSuggestion))
        val destinationSuggestion = getObjectFromJSON(String(recentSearch.destinationSuggestion))
        val departureDate = LocalDate(recentSearch.startDate)
        val returnDate = if (recentSearch.isRoundTrip) {
            LocalDate(recentSearch.endDate)
        } else {
            null
        }
        val adults = recentSearch.adultTravelerCount
        val children = convertStringToIntList(recentSearch.childTraveler)
        val infantInLap = recentSearch.isInfantInLap
        val flightCabinClass = recentSearch.flightClass
        return FlightSearchParams(sourceSuggestion, destinationSuggestion, departureDate, returnDate, adults, children,
                infantInLap, flightCabinClass, null, null, null, null, null)
    }

    fun getObjectFromJSON(json: String): SuggestionV4 {
        val gson = FlightsV2DataUtil.generateGson()
        return gson.fromJson(json, SuggestionV4::class.java)
    }

    private fun convertStringToIntList(string: String): List<Int> {
        val intArray = ArrayList<Int>()
        if(!string.trim().isEmpty()) {
            val strArray = string.split(",")
            for (s in strArray) intArray.add(Integer.valueOf(s))
        }

        return intArray
    }
}
