package com.expedia.bookings.services

enum class ProductSearchType(productType: String) {
    OldPackageSearch("PSS"),
    MultiItemHotels("hotels"),
    MultiItemHotelRooms("rooms"),
    MultiItemOutboundFlights("flights"),
    MultiItemInboundFlights("flights")
}