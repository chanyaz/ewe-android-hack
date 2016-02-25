package com.expedia.vm

import com.expedia.bookings.data.FlightFilter
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.packages.FlightLeg
import com.expedia.util.endlessObserver
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator

class PackageFlightFilterViewModel() {
    val hourMinuteFormatter = DateTimeFormat.forPattern("hh:mma")
    val doneObservable = PublishSubject.create<Unit>()
    val doneButtonEnableObservable = PublishSubject.create<Boolean>()
    val clearObservable = PublishSubject.create<Unit>()
    val filterObservable = PublishSubject.create<List<FlightLeg>>()
    val flightResultsObservable = PublishSubject.create<List<FlightLeg>>()

    var originalList: List<FlightLeg>? = null
    var filteredList: List<FlightLeg> = emptyList()

    val updateDynamicFeedbackWidget = BehaviorSubject.create<Int>()
    val clearChecks = BehaviorSubject.create<Unit>()
    val filterCountObservable = BehaviorSubject.create<Int>()
    val sortContainerObservable = BehaviorSubject.create<Boolean>()

    val userFilterChoices = UserFilterChoices()
    val stopsObservable = PublishSubject.create<List<Int>>()
    val airlinesObservable = PublishSubject.create<List<String>>()
    val airlinesExpandObservable = BehaviorSubject.create<Boolean>()
    val newPriceRangeObservable = PublishSubject.create<PriceRange>()
    val newDurationRangeObservable = PublishSubject.create<TimeRange>()
    val newDepartureRangeObservable = PublishSubject.create<TimeRange>()
    val newArrivalRangeObservable = PublishSubject.create<TimeRange>()
    val filteredZeroResultObservable = PublishSubject.create<Unit>()
    var previousSort = FlightFilter.Sort.PRICE
    var isAirlinesExpanded: Boolean = false

    data class UserFilterChoices(var userSort: FlightFilter.Sort = FlightFilter.Sort.PRICE,
                                 var minPrice: Int = 0,
                                 var maxPrice: Int = 0,
                                 var minDuration: Int = 0,
                                 var maxDuration: Int = 0,
                                 var minDeparture: Int = 0,
                                 var maxDeparture: Int = 0,
                                 var minArrival: Int = 0,
                                 var maxArrival: Int = 0,
                                 var stops: ArrayList<Int> = ArrayList<Int>(),
                                 var airlines: ArrayList<String> = ArrayList<String>()) {

        fun filterCount(): Int {
            var count = 0
            if (minPrice != 0 || maxPrice != 0) count++
            if (minDuration != 0 || maxDuration != 0) count++
            if (minDeparture != 0 || maxDeparture != 0) count++
            if (minArrival != 0 || maxArrival != 0) count++
            if (stops.isNotEmpty()) count += stops.size
            if (airlines.isNotEmpty()) count += airlines.size
            return count
        }
    }

    data class PriceRange(val currencyCode: String, val minPrice: Int, val maxPrice: Int) {
        val notches = maxPrice - minPrice
        val defaultMinPriceText = formatValue(toValue(minPrice))
        val defaultMaxPriceText = formatValue(toValue(maxPrice))

        private fun toValue(price: Int): Int = price - minPrice
        private fun toPrice(value: Int): Int = value + minPrice

        fun formatValue(value: Int): String {
            return Money(toPrice(value), currencyCode).getFormattedMoney(Money.F_NO_DECIMAL)
        }

        fun update(minValue: Int, maxValue: Int): Pair<Int, Int> {
            val newMaxPrice = toPrice(maxValue)
            return Pair(toPrice(minValue), if (newMaxPrice == maxPrice) 0 else newMaxPrice)
        }
    }

    data class TimeRange(val minDurationHours: Int, val maxDurationHours: Int) {
        val notches = maxDurationHours - minDurationHours
        val defaultMinText = formatValue(toValue(minDurationHours))
        val defaultMaxText = formatValue(toValue(maxDurationHours))

        private fun toValue(hour: Int): Int = hour - minDurationHours
        private fun toHour(value: Int): Int = value + minDurationHours

        fun formatValue(value: Int): String {
            return toHour(value).toString() + "%s"
        }

        fun update(minValue: Int, maxValue: Int): Pair<Int, Int> {
            val newMaxDuration = toHour(maxValue)
            return Pair(toHour(minValue), if (newMaxDuration == maxDurationHours) 0 else newMaxDuration)
        }
    }

    private val departureComparator: Comparator<FlightLeg> = object : Comparator<FlightLeg> {
        override fun compare(lhs: FlightLeg?, rhs: FlightLeg?): Int {
            val leftStart = DateTime.parse(lhs?.flightSegments?.first()?.departureTime, hourMinuteFormatter)
            val rightStart = DateTime.parse(rhs?.flightSegments?.first()?.departureTime, hourMinuteFormatter)

            if (leftStart.isBefore(rightStart)) {
                return -1;
            } else if (leftStart.isAfter(rightStart)) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    private val arrivalComparator: Comparator<FlightLeg> = object : Comparator<FlightLeg> {
        override fun compare(lhs: FlightLeg?, rhs: FlightLeg?): Int {
            val leftStart = DateTime.parse(lhs?.flightSegments?.last()?.arrivalTime, hourMinuteFormatter)
            val rightStart = DateTime.parse(rhs?.flightSegments?.last()?.arrivalTime, hourMinuteFormatter)

            if (leftStart.isBefore(rightStart)) {
                return -1;
            } else if (leftStart.isAfter(rightStart)) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    val sortObserver = endlessObserver<FlightFilter.Sort> { sort ->
        when (sort) {
            FlightFilter.Sort.PRICE -> filteredList = filteredList.sortedBy { it.packageOfferModel.price.packageTotalPrice.amount.toInt() }
            FlightFilter.Sort.DEPARTURE -> Collections.sort(filteredList, departureComparator)
            FlightFilter.Sort.ARRIVAL -> Collections.sort(filteredList, arrivalComparator)
            FlightFilter.Sort.DURATION -> filteredList = filteredList.sortedBy { it.durationHour * 60 + it.durationMinute }
        }
    }

    init {
        flightResultsObservable.subscribe { list ->
            originalList = list
            filteredList = ArrayList(list)
            resetRangeBars()
            resetCheckboxes()
            previousSort = FlightFilter.Sort.PRICE
        }

        doneObservable.subscribe { params ->
            //if previousSort and userSort is both by price(default), no need to call sort method. Otherwise, always do sort.
            if (userFilterChoices.userSort != FlightFilter.Sort.PRICE || previousSort != FlightFilter.Sort.PRICE) {
                previousSort = userFilterChoices.userSort
                sortObserver.onNext(userFilterChoices.userSort)
            }

            if (filteredList.isNotEmpty()) {
                filterObservable.onNext(filteredList)
            } else {
                filteredZeroResultObservable.onNext(Unit)
            }
        }

        clearObservable.subscribe { params ->
            resetUserFilters()
            resetRangeBars()
            handleFiltering()
            clearChecks.onNext(Unit)
        }
    }

    fun handleFiltering() {
        filteredList = originalList.orEmpty().filter { isAllowed(it) }
        val filterCount = userFilterChoices.filterCount()
        val dynamicFeedbackWidgetCount = if (filterCount > 0) filteredList.size else -1
        updateDynamicFeedbackWidget.onNext(dynamicFeedbackWidgetCount)
        doneButtonEnableObservable.onNext(filteredList.size > 0)
        filterCountObservable.onNext(filterCount)
    }

    fun resetUserFilters() {
        userFilterChoices.minPrice = 0
        userFilterChoices.maxPrice = 0
        userFilterChoices.minDuration = 0
        userFilterChoices.maxDuration = 0
        userFilterChoices.minDeparture = 0
        userFilterChoices.maxDeparture = 0
        userFilterChoices.minArrival = 0
        userFilterChoices.maxArrival = 0
        userFilterChoices.stops = ArrayList<Int>()
        userFilterChoices.airlines = ArrayList<String>()
    }

    fun isAllowed(flightLeg: FlightLeg): Boolean {
        return filterPrice(flightLeg)
                && filterDuration(flightLeg)
                && filterStops(flightLeg)
                && filterDeparture(flightLeg)
                && filterArrival(flightLeg)
                && filterAirlines(flightLeg)
    }

    fun filterPrice(flightLeg: FlightLeg): Boolean {
        return userFilterChoices.minPrice <= flightLeg.packageOfferModel.price.packageTotalPrice.amount.toInt() &&
                (userFilterChoices.maxPrice == 0 || flightLeg.packageOfferModel.price.packageTotalPrice.amount.toInt() <= userFilterChoices.maxPrice)
    }

    fun filterDuration(flightLeg: FlightLeg): Boolean {
        return userFilterChoices.minDuration <= flightLeg.durationHour &&
                (userFilterChoices.maxDuration == 0 || flightLeg.durationHour < userFilterChoices.maxDuration)
    }

    fun filterDeparture(flightLeg: FlightLeg): Boolean {
        return userFilterChoices.minDeparture <= getHourOfTheDay(flightLeg.departureDateTimeISO) &&
                (userFilterChoices.maxDeparture == 0 || getHourOfTheDay(flightLeg.departureDateTimeISO) < userFilterChoices.maxDeparture)
    }

    fun filterArrival(flightLeg: FlightLeg): Boolean {
        return userFilterChoices.minArrival <= getHourOfTheDay(flightLeg.arrivalDateTimeISO) &&
                (userFilterChoices.maxArrival == 0 || getHourOfTheDay(flightLeg.arrivalDateTimeISO) < userFilterChoices.maxArrival)
    }

    fun filterStops(flightLeg: FlightLeg): Boolean {
        return userFilterChoices.stops.isEmpty() || userFilterChoices.stops.contains(flightLeg.stopCount)
    }

    fun filterAirlines(flightLeg: FlightLeg): Boolean {
        return userFilterChoices.airlines.isEmpty() || userFilterChoices.airlines.contains(flightLeg.carrierName)
    }

    val priceRangeChangedObserver = endlessObserver<Pair<Int, Int>> { p ->
        userFilterChoices.minPrice = p.first
        userFilterChoices.maxPrice = p.second
        handleFiltering()
    }

    val durationRangeChangedObserver = endlessObserver<Pair<Int, Int>> { p ->
        userFilterChoices.minDuration = p.first
        userFilterChoices.maxDuration = p.second
        handleFiltering()
    }

    val departureRangeChangedObserver = endlessObserver<Pair<Int, Int>> { p ->
        userFilterChoices.minDeparture = p.first
        userFilterChoices.maxDeparture = p.second
        handleFiltering()
    }

    val arrivalRangeChangedObserver = endlessObserver<Pair<Int, Int>> { p ->
        userFilterChoices.minArrival = p.first
        userFilterChoices.maxArrival = p.second
        handleFiltering()
    }

    private fun resetCheckboxes() {
        val stops = ArrayList<Int>()
        val airlines = ArrayList<String>()
        originalList?.forEach { leg ->
            if (!airlines.contains(leg.carrierName)) airlines.add(leg.carrierName)
            if (!stops.contains(leg.stopCount)) stops.add(leg.stopCount)
        }

        stopsObservable.onNext(ArrayList(stops).sortedBy { it })
        airlinesObservable.onNext(ArrayList(airlines).sortedBy { it })
    }

    private fun resetRangeBars() {
        resetPriceRange()
        resetDurationRange()
        resetDepartureRange()
        resetArrivalRange()
    }

    private fun resetPriceRange() {
        val list = ArrayList(originalList)
        if (list.isNotEmpty()) {
            val sortedList = list.sortedBy { it.packageOfferModel.price.packageTotalPrice.amount.toInt() }
            val min = sortedList.first().packageOfferModel.price.packageTotalPrice.amount.toInt()
            val max = sortedList.last().packageOfferModel.price.packageTotalPrice.amount.toInt()
            val currency = sortedList.first().packageOfferModel.price.packageTotalPrice.currency
            newPriceRangeObservable.onNext(PriceRange(currency, min, max))
        }
    }

    private fun resetDurationRange() {
        val list = ArrayList(originalList)
        if (list.isNotEmpty()) {
            val sortedList = list.sortedBy { it.durationHour }
            val min = sortedList.first().durationHour
            val max = sortedList.last().durationHour
            newDurationRangeObservable.onNext(TimeRange(min, max + 1))
        }
    }

    private fun resetDepartureRange() {
        val list = ArrayList(originalList)
        if (list.isNotEmpty()) {
            val sortedList = list.sortedBy { getHourOfTheDay(it.departureDateTimeISO) }
            val min = getHourOfTheDay(sortedList.first().departureDateTimeISO)
            val max = getHourOfTheDay(sortedList.last().departureDateTimeISO)
            newDepartureRangeObservable.onNext(TimeRange(min, max + 1))
        }
    }

    private fun resetArrivalRange() {
        val list = ArrayList(originalList)
        if (list.isNotEmpty()) {
            val sortedList = list.sortedBy { getHourOfTheDay(it.arrivalDateTimeISO) }
            val min = getHourOfTheDay(sortedList.first().arrivalDateTimeISO)
            val max = getHourOfTheDay(sortedList.last().arrivalDateTimeISO)
            newArrivalRangeObservable.onNext(TimeRange(min, max + 1))
        }
    }

    private fun getHourOfTheDay(string: String): Int {
        return DateTime.parse(string).hourOfDay
    }

    val selectStop = endlessObserver<Int> { s ->
        if (userFilterChoices.stops.isEmpty() || !userFilterChoices.stops.contains(s)) {
            userFilterChoices.stops.add(s)
        } else {
            userFilterChoices.stops.remove(s)
        }
        handleFiltering()
    }

    val selectAirline = endlessObserver<String> { s ->
        if (userFilterChoices.airlines.isEmpty() || !userFilterChoices.airlines.contains(s)) {
            userFilterChoices.airlines.add(s)
        } else {
            userFilterChoices.airlines.remove(s)
        }
        handleFiltering()
    }

    val airlinesMoreLessObservable: Observer<Unit> = endlessObserver {
        isAirlinesExpanded = !isAirlinesExpanded
        airlinesExpandObservable.onNext(isAirlinesExpanded)
    }

    fun isFilteredToZeroResults(): Boolean {
        return userFilterChoices.filterCount() > 0 && filteredList.isEmpty()
    }
}