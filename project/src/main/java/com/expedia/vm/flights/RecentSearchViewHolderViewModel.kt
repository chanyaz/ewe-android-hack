package com.expedia.vm.flights

import android.content.Context
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.flights.FlightServiceClassType
import com.expedia.bookings.data.flights.RecentSearch
import com.expedia.bookings.utils.FlightV2Utils.convertStringToIntList
import com.expedia.bookings.utils.FlightsV2DataUtil
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
        }
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

    fun convertRecentSearchToSearchParams(recentSearch: RecentSearch): FlightSearchParams {
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
}
