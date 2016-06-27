package com.expedia.bookings.data.rail.requests.api

import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.rail.requests.RailSearchRequest
import com.expedia.bookings.utils.DateUtils
import org.joda.time.LocalDate
import java.util.ArrayList
import java.util.UUID

/* note: the variable names in this model have to match 1:1 to the format the api expects. this whole
            model is getting serialized to json and sent to the search endpoint
 */
class RailApiSearchModel(val origin: SuggestionV4, val destination: SuggestionV4, val departDate: LocalDate, val returnDate: LocalDate?) {

    var pos = PointOfSaleKey()
    var clientCode = "1001" //TODO - what is this?
    var searchCriteriaList: MutableList<OriginDestinationPair> = ArrayList()

    var passengerList: MutableList<RailPassenger> = ArrayList()

    init {
        passengerList.add(RailPassenger(1, 65, true))
        this.searchCriteriaList.add(OriginDestinationPair(departDate)) //hardcoded to specific params for now
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

    class OriginDestinationPair(departDate: LocalDate) {
        // TODO update when ESS ready
        var originStationCode = "GBQQM" //Rail location "GBQQM", "Goteborg C, Sverige"
        var destinationStationCode = "GBRDG" //Rail location "GBRDG", "Malmo C, Sverige"
        var departureDate = DateUtils.localDateToyyyyMMdd(departDate) //format: yyyy-MM-dd
        var departureTime = "12:00:00"
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
            val requestModel = RailApiSearchModel(request.origin, request.destination, request.departDate, request.returnDate)
            //TODO - set all the stuff
            return requestModel
        }
    }
}