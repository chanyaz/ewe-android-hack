package com.expedia.bookings.data

data class TNSRegisterUserDeviceFlightsRequestBody(val courier: Courier, val flights: List<TNSFlight>, val user: TNSUser)

data class TNSDeregister(val courier: Courier)

data class TNSRegisterUserDeviceRequestBody(val courier: Courier, val user: TNSUser)

data class TNSUser(val siteid: String, val tuid: String?, val expUserID: String?, val guid: String)

data class Courier(val group: String, val langid: String, val name: String, val token: String, val uniqueIdentifier: String)

data class TNSRegisterDeviceResponse(val activityId: String, val errorMessage: String, val status: String)

data class TNSFlight(val airline: String, val arrival_date: String, val departure_date: String, val destination: String, val flight_no: String, val origin: String)
