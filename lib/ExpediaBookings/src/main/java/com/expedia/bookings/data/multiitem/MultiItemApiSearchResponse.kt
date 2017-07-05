package com.expedia.bookings.data.multiitem

data class MultiItemApiSearchResponse(
        val offers: List<MultiItemOffer>,
        val hotels: Map<String, HotelOffer>,
        val flights: Map<String, FlightOffer>,
//        val cars: Map<String, CarOffer>?,
        val errors: List<MultiItemError>?
//        val messageInfo: MessageInfo?,
)