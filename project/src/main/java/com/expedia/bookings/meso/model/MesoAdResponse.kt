package com.expedia.bookings.meso.model

data class MesoAdResponse (val HotelAdResponse: MesoHotelAdResponse? = null, val DestinationAdResponse: MesoDestinationAdResponse? = null)