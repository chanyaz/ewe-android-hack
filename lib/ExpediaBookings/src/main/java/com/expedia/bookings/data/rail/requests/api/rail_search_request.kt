package com.expedia.bookings.data.rail.requests.api

import com.expedia.bookings.data.rail.requests.RailSearchRequest
import com.expedia.bookings.utils.DateUtils
import org.joda.time.LocalDate
import java.util.ArrayList
import java.util.UUID

/* note: the variable names in this model have to match 1:1 to the format the api expects. this whole
            model is getting serialized to json and sent to the search endpoint
 */
class RailApiSearchModel(val departDate: LocalDate, val returnDate: LocalDate?) {

    var messageInfo = MessageInfo()
    var pointOfSaleKey = PointOfSaleKey()
    var clientCode = "1001" //TODO - what is this?
    var railSearchCriteria = RailSearchCriteria(departDate, returnDate)
    var passengers: MutableList<RailPassenger> = ArrayList()

    init {
        passengers.add(RailPassenger("PAX_01", 30))
        passengers.add(RailPassenger("PAX_02", 14))
    }

    class MessageInfo {
        internal var messageGUID = UUID.randomUUID().toString()
        internal var transactionGUID = UUID.randomUUID().toString()
        internal var userGUID = UUID.randomUUID().toString()
    }

    class PointOfSaleKey {
        internal var jurisdictionCountryCode: String
        internal var companyCode: String
        internal var managementUnitCode: String

        init {
            //TODO real constructor
            jurisdictionCountryCode = "USA"
            companyCode = "USA"
            managementUnitCode = "USA"
        }
    }

    class RailSearchCriteria(val departDate: LocalDate, val returnDate: LocalDate?) {

        var originDestinationPairs: MutableList<OriginDestinationPair> = ArrayList()

        init {
            this.originDestinationPairs.add(OriginDestinationPair(departDate, returnDate)) //hardcoded to specific params for now
        }

        class OriginDestinationPair(val departDate: LocalDate, val returnDate: LocalDate?) {

            var originStation = RailLocation("GBQQM", "Goteborg C, Sverige")
            var destinationStation = RailLocation("GBRDG", "Malmo C, Sverige")
            var departureTime = DateUtils.localDateToMMddyyyy(departDate) //format: MM/DD/YYYY
            var arrivalTime = if (returnDate != null) {
                DateUtils.localDateToMMddyyyy(returnDate)
            } else {
                null
            }

            class RailLocation(var stationCode: String, var stationName: String)
        }
    }

    class RailPassenger(passengerId: String, age: Int) {
        internal var passengerId = "PAX_01"
        internal var firstName: String? = null
        internal var middleName: String? = null
        internal var lastName: String? = null
        internal var email: String? = null
        internal var telephoneNumber: String? = null
        internal var isPrimaryTraveler = false
        internal var age = 30
        internal var fareQualifierRefId: String? = null
        internal var fareQualifier: String? = null

        init {
            this.passengerId = passengerId
            this.age = age
        }
    }

    companion object {

        fun fromSearchParams(request: RailSearchRequest): RailApiSearchModel {
            val requestModel = RailApiSearchModel(request.departDate, request.returnDate)
            //TODO - set all the stuff
            return requestModel
        }
    }
}