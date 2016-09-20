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
                         departTimeMillis: Int, returnTimeMillis: Int?, isSearchRoundTrip: Boolean, val fareQualifierList: List<RailCard>) {

    var pos = PointOfSaleKey()
    var clientCode = "1001" //TODO - what is this?
    var searchCriteriaList: MutableList<OriginDestinationPair> = ArrayList()

    var passengerList: MutableList<RailPassenger> = ArrayList()

    init {
        passengerList.add(RailPassenger(1, 65, true)) //hardcoded to specific params for now
        this.searchCriteriaList.add(OriginDestinationPair(origin, destination, departDate, departTimeMillis))
        if (isSearchRoundTrip && returnDate != null && returnTimeMillis != null) {
            // isRoundTrip search switch origin-destination
            this.searchCriteriaList.add(OriginDestinationPair(destination, origin, returnDate, returnTimeMillis))
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
            //TODO - set passenger and railcards data
            return RailApiSearchModel(request.origin!!, request.destination!!, request.departDate, request.returnDate,
                    request.departDateTimeMillis, request.returnDateTimeMillis, request.isRoundTripSearch(), request.selectedRailCards)

        }
    }
}