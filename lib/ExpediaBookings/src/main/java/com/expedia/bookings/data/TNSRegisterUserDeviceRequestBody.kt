package com.expedia.bookings.data

data class TNSRegisterUserDeviceFlightsRequestBody(val courier: Courier, val flights: List<TNSFlight>, val user: TNSUser)

data class TNSDeregister(val courier: Courier)

data class TNSRegisterUserDeviceRequestBody(val courier: Courier, val user: TNSUser)

data class TNSUser(val siteid: Int, val tuid: Long?, val expUserID: Long?)

data class Courier(val group: String, val name: String, val token: String, val uniqueIdentifier: String)

data class TNSRegisterDeviceResponse(val activityId: String, val errorMessage: String, val status: String)

data class TNSFlight(val airlineCode: String, val arrival_date: String, val departureDateWithTZ: String, val departureDay: String, val destination: String, val flightNumber: String, val origin: String)
