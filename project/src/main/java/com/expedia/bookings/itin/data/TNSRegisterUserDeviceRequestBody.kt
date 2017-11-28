package com.expedia.bookings.itin.data

class TNSRegisterUserDeviceFlightsRequestBody(val courier: Courier, val flights: List<TNSFlight>, val user: TNSUser)

class TNSRegisterUserDeviceRequestBody(val courier: Courier, val user: TNSUser)

class TNSUser(val siteid: Int, val tuid: String)

class Courier(val group: String, val name: String, val token: String)

class TNSRegisterDeviceResponse(val activityId: String, var errorMessage: String, val status: String)

class TNSFlight(val airline: String, val arrival_date: String, val departure_date: String, val departureDay: String, val destination: String, val flight_no: String, val origin: String)

