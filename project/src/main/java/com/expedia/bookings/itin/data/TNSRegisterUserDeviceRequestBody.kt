package com.expedia.bookings.itin.data

data class TNSRegisterUserDeviceFlightsRequestBody(val courier: Courier, val flights: List<TNSFlight>, val user: TNSUser)

data class TNSRegisterUserDeviceRequestBody(val courier: Courier, val user: TNSUser)

data class TNSUser(val siteid: Int, val tuid: String)

data class Courier(val group: String, val name: String, val token: String)

data class TNSRegisterDeviceResponse(val activityId: String, val errorMessage: String, val status: String)

data class TNSFlight(val airline: String, val arrival_date: String, val departure_date: String, val departureDay: String, val destination: String, val flight_no: String, val origin: String)
