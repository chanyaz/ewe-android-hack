package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.FlightFilter
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.util.endlessObserver
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator
import java.util.TreeMap

class BaseFlightFilterViewModel(private val context: Context) {
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
    val stopsObservable = PublishSubject.create<TreeMap<Stops, Int>>()
    val airlinesObservable = PublishSubject.create<TreeMap<String, Int>>()
    val airlinesExpandObservable = BehaviorSubject.create<Boolean>()
    val newDurationRangeObservable = PublishSubject.create<DurationRange>()

    val newDepartureRangeObservable = PublishSubject.create<TimeRange>()
    val newArrivalRangeObservable = PublishSubject.create<TimeRange>()
    val filteredZeroResultObservable = PublishSubject.create<Unit>()
    var previousSort = FlightFilter.Sort.PRICE
    var isAirlinesExpanded: Boolean = false

    enum class Stops(val stops: Int) {
        NONSTOP(0),
        ONE_STOP(1),
        TWO_PLUS_STOPS(2)
    }

    data class UserFilterChoices(var userSort: FlightFilter.Sort = FlightFilter.Sort.PRICE,
                                 var maxDuration: Int = 0,
                                 var minDeparture: Int = 0,
                                 var maxDeparture: Int = 0,
                                 var minArrival: Int = 0,
                                 var maxArrival: Int = 0,
                                 var stops: ArrayList<Stops> = ArrayList<Stops>(),
                                 var airlines: ArrayList<String> = ArrayList<String>()) {

        fun filterCount(): Int {
            var count = 0
            if (maxDuration != 0) count++
            if (minDeparture != 0 || maxDeparture != 0) count++
            if (minArrival != 0 || maxArrival != 0) count++
            if (stops.isNotEmpty()) count += stops.size
            if (airlines.isNotEmpty()) count += airlines.size
            return count
        }
    }

    data class TimeRange(val context: Context, val minDurationHours: Int, val maxDurationHours: Int) {
        val notches = maxDurationHours - minDurationHours
        val defaultMinText = formatValue(toValue(minDurationHours))
        val defaultMaxText = formatValue(toValue(maxDurationHours))

        private fun toValue(hour: Int): Int = hour - minDurationHours
        private fun toHour(value: Int): Int = value + minDurationHours

        fun formatValue(value: Int): String {
            return context.resources.getStringArray(R.array.hoursList)[toHour(value)]
        }

        fun update(minValue: Int, maxValue: Int): Pair<Int, Int> {
            val newMaxDuration = toHour(maxValue)
            val newMinDuration = toHour(minValue)
            val min = if (newMinDuration == minDurationHours) 0 else newMinDuration
            val max = if (newMaxDuration == maxDurationHours) 0 else newMaxDuration
            return Pair(min, max)
        }
    }

    data class DurationRange(val maxDurationHours: Int) {
        val notches = maxDurationHours
        val defaultMaxText = formatHour(maxDurationHours)

        fun formatHour(value: Int): String {
            return value.toString() + "%s"
        }

        fun update(maxValue: Int): Int {
            return if (maxValue == maxDurationHours) 0 else maxValue
        }
    }

    private val departureComparator: Comparator<FlightLeg> = Comparator { lhs, rhs ->
        val leftStart = DateTime.parse(lhs?.flightSegments?.first()?.departureTime, hourMinuteFormatter)
        val rightStart = DateTime.parse(rhs?.flightSegments?.first()?.departureTime, hourMinuteFormatter)

        if (leftStart.isBefore(rightStart)) {
            -1;
        } else if (leftStart.isAfter(rightStart)) {
            1;
        } else {
            0;
        }
    }

    private val arrivalComparator: Comparator<FlightLeg> = Comparator { lhs, rhs ->
        val leftStart = DateTime.parse(lhs?.flightSegments?.last()?.arrivalTime, hourMinuteFormatter)
        val rightStart = DateTime.parse(rhs?.flightSegments?.last()?.arrivalTime, hourMinuteFormatter)

        if (leftStart.isBefore(rightStart)) {
            -1;
        } else if (leftStart.isAfter(rightStart)) {
            1;
        } else {
            0;
        }
    }

    val sortObserver = endlessObserver<FlightFilter.Sort> { sort ->
        // if best Flight is filtered out, sort the whole list
        // if there is best flight, should always stay on top
        var bestFlight = filteredList.find { it.isBestFlight }
        var filteredListToSort = if (bestFlight == null) filteredList else filteredList.subList(1, filteredList.size)
        when (sort) {
            FlightFilter.Sort.PRICE -> filteredListToSort = filteredListToSort.sortedBy { it.packageOfferModel.price.packageTotalPrice.amount.toInt() }.toMutableList()
            FlightFilter.Sort.DEPARTURE -> Collections.sort(filteredListToSort, departureComparator)
            FlightFilter.Sort.ARRIVAL -> Collections.sort(filteredListToSort, arrivalComparator)
            FlightFilter.Sort.DURATION -> filteredListToSort = filteredListToSort.sortedBy { it.durationHour * 60 + it.durationMinute }.toMutableList()
        }
        if (bestFlight != null) {
            filteredListToSort = filteredListToSort.toMutableList()
            filteredListToSort.add(0, bestFlight)
        }
        filteredList = filteredListToSort
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

    fun getStops(stops: Int): Stops {
        return when (stops) {
            0 -> Stops.NONSTOP
            1 -> Stops.ONE_STOP
            2 -> Stops.TWO_PLUS_STOPS
            else -> Stops.TWO_PLUS_STOPS
        }
    }

    fun handleFiltering() {
        filteredList = originalList.orEmpty().filter { isAllowed(it) }
        val filterCount = userFilterChoices.filterCount()
        // not to include best flight in the count
        val allFlightsListSize = if (filteredList.isNotEmpty() && filteredList[0].isBestFlight) filteredList.size - 1 else filteredList.size
        val dynamicFeedbackWidgetCount = if (filterCount > 0) allFlightsListSize else -1
        updateDynamicFeedbackWidget.onNext(dynamicFeedbackWidgetCount)
        doneButtonEnableObservable.onNext(filteredList.size > 0)
        filterCountObservable.onNext(filterCount)
    }

    fun resetUserFilters() {
        userFilterChoices.maxDuration = 0
        userFilterChoices.minDeparture = 0
        userFilterChoices.maxDeparture = 0
        userFilterChoices.minArrival = 0
        userFilterChoices.maxArrival = 0
        userFilterChoices.stops = ArrayList<Stops>()
        userFilterChoices.airlines = ArrayList<String>()
    }

    fun isAllowed(flightLeg: FlightLeg): Boolean {
        return filterDuration(flightLeg)
                && filterStops(flightLeg)
                && filterDeparture(flightLeg)
                && filterArrival(flightLeg)
                && filterAirlines(flightLeg)
    }

    fun filterDuration(flightLeg: FlightLeg): Boolean {
        return (userFilterChoices.maxDuration == 0 || flightLeg.durationHour < userFilterChoices.maxDuration)
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
        return userFilterChoices.stops.isEmpty() || userFilterChoices.stops.contains(getStops(flightLeg.stopCount))
    }

    fun filterAirlines(flightLeg: FlightLeg): Boolean {
        return userFilterChoices.airlines.isEmpty() || userFilterChoices.airlines.contains(flightLeg.carrierName)
    }

    val durationRangeChangedObserver = endlessObserver<Int> { p ->
        userFilterChoices.maxDuration = p
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
        val stops = TreeMap<Stops, Int>()
        val airlines = TreeMap<String, Int>()

        originalList?.forEach { leg ->
            val airlineCount = if (airlines.containsKey(leg.carrierName)) airlines[leg.carrierName] else 0
            airlines.put(leg.carrierName, airlineCount!! + 1)

            val key = getStops(leg.stopCount)
            val stopCount = if (stops.containsKey(key)) stops[key] else 0
            stops.put(key, stopCount!! + 1)
        }
        stopsObservable.onNext(stops)
        airlinesObservable.onNext(airlines)
    }

    private fun resetRangeBars() {
        resetDurationRange()
        resetDepartureRange()
        resetArrivalRange()
    }

    private fun resetDurationRange() {
        val list = ArrayList(originalList)
        if (list.isNotEmpty()) {
            val sortedList = list.sortedBy { it.durationHour }
            val max = sortedList.last().durationHour
            newDurationRangeObservable.onNext(DurationRange(max + 1))
        }
    }

    private fun resetDepartureRange() {
        val list = ArrayList(originalList)
        if (list.isNotEmpty()) {
            newDepartureRangeObservable.onNext(TimeRange(context, 0, context.resources.getStringArray(R.array.hoursList).size - 1))
        }
    }

    private fun resetArrivalRange() {
        val list = ArrayList(originalList)
        if (list.isNotEmpty()) {
            newArrivalRangeObservable.onNext(TimeRange(context, 0, context.resources.getStringArray(R.array.hoursList).size - 1))
        }
    }

    private fun getHourOfTheDay(string: String): Int {
        return DateTime.parse(string).hourOfDay
    }

    val selectStop = endlessObserver<Int> { s ->
        if (userFilterChoices.stops.isEmpty() || !userFilterChoices.stops.contains(getStops(s))) {
            var stops: Stops = getStops(s)
            userFilterChoices.stops.add(stops)
            PackagesTracking().trackFlightFilterStops(stops)
        } else {
            userFilterChoices.stops.remove(getStops(s))
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
        PackagesTracking().trackFlightFilterAirlines()
    }

    val airlinesMoreLessObservable: Observer<Unit> = endlessObserver {
        isAirlinesExpanded = !isAirlinesExpanded
        airlinesExpandObservable.onNext(isAirlinesExpanded)
    }

    fun isFilteredToZeroResults(): Boolean {
        return userFilterChoices.filterCount() > 0 && filteredList.isEmpty()
    }
}