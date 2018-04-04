package com.expedia.vm.flights

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.flights.FlightServiceClassType
import com.expedia.bookings.data.flights.RecentSearch
import com.expedia.bookings.utils.FlightV2Utils.convertStringToIntList
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
    val contentDescriptionObservable = PublishSubject.create<String>()

    init {
        recentSearchObservable.subscribe { recentSearch ->
            originObservable.onNext(recentSearch.sourceAirportCode)
            destinationObservable.onNext(recentSearch.destinationAirportCode)
            val amount = Money.getFormattedMoneyFromAmountAndCurrencyCode(recentSearch.amount.toBigDecimal(), recentSearch.currencyCode, Money.F_NO_DECIMAL)
            val isRoundTrip = recentSearch.isRoundTrip
            val startDate = LocalDate(recentSearch.startDate)
            var endDate: LocalDate? = null
            if (recentSearch.isRoundTrip) {
                endDate = LocalDate(recentSearch.endDate)
            }
            val travelerCount = getTravelerCount(recentSearch.adultTravelerCount, recentSearch.childTraveler)
            val cabinClass = getCabinClass(recentSearch.flightClass)
            priceObservable.onNext(amount)
            dateRangeObservable.onNext(Pair(startDate, endDate))
            travelerCountObservable.onNext(travelerCount.toString())
            classObservable.onNext(cabinClass)
            roundTripObservable.onNext(isRoundTrip)
            val dateContentDescription = Phrase.from(context,
                    if (isRoundTrip) {
                        R.string.start_to_end_date_range_cont_desc_TEMPLATE
                    } else {
                        R.string.calendar_instructions_date_rail_one_way_TEMPLATE
                    })
                    .put("startdate", LocaleBasedDateFormatUtils.localDateToMMMd(startDate))
            if (isRoundTrip) {
                dateContentDescription.put("enddate", LocaleBasedDateFormatUtils.localDateToMMMd(endDate!!))
            }
            val travelerContentDescription = Phrase.from(context.resources.getQuantityString(R.plurals.recent_search_travelers_cont_desc_TEMPLATE, travelerCount))
                    .put("travelers", travelerCount).format().toString()
            contentDescriptionObservable.onNext(
                    Phrase.from(context, R.string.flight_recent_search_item_cont_desc_TEMPLATE)
                            .put("source", recentSearch.sourceAirportCode)
                            .put("destination", recentSearch.destinationAirportCode)
                            .put("trip_type", if (isRoundTrip) {
                                context.getResources().getString(R.string.flights_round_trip_label)
                            } else {
                                context.getResources().getString(R.string.flights_one_way_label)
                            })
                            .put("date", dateContentDescription.format().toString())
                            .put("travelers", travelerContentDescription)
                            .put("cabin_class", cabinClass)
                            .put("amount", amount)
                            .format().toString())
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
