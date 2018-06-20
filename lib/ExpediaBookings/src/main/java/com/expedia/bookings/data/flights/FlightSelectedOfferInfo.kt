package com.expedia.bookings.data.flights

data class FlightSelectedOfferInfo(var flightOffer: FlightTripDetails.FlightOffer? = null,
                                   var selectedLegList: ArrayList<FlightLeg> = ArrayList(),
                                   var productId: String? = null)
