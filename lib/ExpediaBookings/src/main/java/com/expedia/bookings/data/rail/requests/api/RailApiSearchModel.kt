package com.expedia.bookings.data.rail.requests.api

import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.rail.requests.RailSearchRequest
import com.expedia.bookings.utils.DateUtils
import org.joda.time.DateTime
import org.joda.time.LocalDate
import java.sql.Time
import java.text.SimpleDateFormat
import java.util.*

/* note: the variable names in this model have to match 1:1 to the format the api expects. this whole
            model is getting serialized to json and sent to the search endpoint
 */
class RailApiSearchModel(val origin: SuggestionV4?, val destination: SuggestionV4?, val departDate: LocalDate,
                         val returnDate: LocalDate?, val departTimeMillis: Long, val returnTimeMillis: Long?) {

    var pos = PointOfSaleKey()
    var clientCode = "1001" //TODO - what is this?
    var searchCriteriaList: MutableList<OriginDestinationPair> = ArrayList()

    var passengerList: MutableList<RailPassenger> = ArrayList()

    init {
        passengerList.add(RailPassenger(1, 65, true)) //hardcoded to specific params for now
        this.searchCriteriaList.add(OriginDestinationPair(departDate, returnDate, departTimeMillis, returnTimeMillis))
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

    class OriginDestinationPair(departDate: LocalDate, returnDate: LocalDate?, departTimeMillis: Long, returnTimeMillis: Long?) {
        // TODO update when ESS ready
        var originStationCode = "GBQQM" //Rail location "GBQQM", "Goteborg C, Sverige"
        var destinationStationCode = "GBRDG" //Rail location "GBRDG", "Malmo C, Sverige"
        var departureDate = DateUtils.localDateToyyyyMMdd(departDate) //format: yyyy-MM-dd
        var returnDate = if (returnDate != null) DateUtils.localDateToyyyyMMdd(returnDate) else null

        //format "16:00:00"
        val departureTime = DateUtils.formatMillisToHHmmss(departTimeMillis)
        val returnTime = if (returnTimeMillis != null) DateUtils.formatMillisToHHmmss(returnTimeMillis) else null
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
            return RailApiSearchModel(request.origin, request.destination, request.departDate, request.returnDate,
                    request.departTime, request.returnTime)
        }
    }
}