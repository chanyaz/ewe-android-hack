package com.expedia.bookings.data.rail.requests.api

import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.rail.requests.RailSearchRequest
import com.expedia.bookings.data.rail.responses.RailCard
import com.expedia.bookings.utils.DateUtils
import org.joda.time.LocalDate
import java.util.*

/* note: the variable names in this model have to match 1:1 to the format the api expects. this whole
            model is getting serialized to json and sent to the search endpoint
 */
class RailApiSearchModel(origin: SuggestionV4, destination: SuggestionV4, departDate: LocalDate, returnDate: LocalDate?,
                         departTimeMillis: Int, returnTimeMillis: Int?, isSearchRoundTrip: Boolean, val adults: Int, val children: List<Int>, val youths: List<Int>, val seniors: List<Int>, val fareQualifierList: List<RailCard>) {
    private val DEFAULT_ADULT_AGE = 30
    private val DEFAULT_CHILDREN_PRIMARY_TRAVELER_STATUS = false
    var pos = PointOfSaleKey()
    var clientCode = "1001" //TODO - what is this?
    var searchCriteriaList: MutableList<OriginDestinationPair> = ArrayList()

    var passengerList: MutableList<RailPassenger> = ArrayList()

    init {
        buildPassengerList()
        this.searchCriteriaList.add(OriginDestinationPair(origin, destination, departDate, departTimeMillis))
        if (isSearchRoundTrip && returnDate != null && returnTimeMillis != null) {
            // isRoundTrip search switch origin-destination
            this.searchCriteriaList.add(OriginDestinationPair(destination, origin, returnDate, returnTimeMillis))
        }
    }

    private fun buildPassengerList() {
        var passengerIndex = 1
        var primaryTravelerStatus = true //first non-adult is primary traveler

        for (listIndex in 0..adults - 1) {
            passengerList.add(RailPassenger(passengerIndex++, DEFAULT_ADULT_AGE, primaryTravelerStatus))
            primaryTravelerStatus = false
        }

        for (listIndex in 0..youths.size - 1) {
            passengerList.add(RailPassenger(passengerIndex++, youths[listIndex], primaryTravelerStatus))
            primaryTravelerStatus = false
        }

        for (listIndex in 0..seniors.size - 1) {
            passengerList.add(RailPassenger(passengerIndex++, seniors[listIndex], primaryTravelerStatus))
            primaryTravelerStatus = false
        }

        for (listIndex in 0..children.size - 1) {
            passengerList.add(RailPassenger(passengerIndex++, children[listIndex], DEFAULT_CHILDREN_PRIMARY_TRAVELER_STATUS))
        }
    }

    class PointOfSaleKey {
        internal var jurisdictionCountryCode: String
        internal var companyCode: String
        internal var managementUnitCode: String

        init {
            //TODO real constructor
            jurisdictionCountryCode = "GBR"
            companyCode = "10111"
            managementUnitCode = "1050"
        }
    }

    class OriginDestinationPair(origin: SuggestionV4, destination: SuggestionV4, departDate: LocalDate, departTimeMillis: Int) {
        var originStationCode = origin.hierarchyInfo?.rails?.stationCode
        var destinationStationCode = destination.hierarchyInfo?.rails?.stationCode
        var departureDate = DateUtils.localDateToyyyyMMdd(departDate) //format: yyyy-MM-dd

        //format "16:00:00"
        val departureTime = DateUtils.formatMillisToHHmmss(departDate, departTimeMillis)
    }

    class RailPassenger(passengerIndex: Int, age: Int, primaryTraveler: Boolean) {
        internal var passengerIndex = 1
        internal var age = 30
        internal var primaryTraveler = false

        init {
            this.passengerIndex = passengerIndex
            this.age = age
            this.primaryTraveler = primaryTraveler
        }
    }

    companion object {
        fun fromSearchParams(request: RailSearchRequest): RailApiSearchModel {
            return RailApiSearchModel(request.origin!!, request.destination!!, request.departDate, request.returnDate,
                    request.departDateTimeMillis, request.returnDateTimeMillis, request.isRoundTripSearch(), request.adults, request.children, request.youths, request.seniors, request.selectedRailCards)

        }
    }
}