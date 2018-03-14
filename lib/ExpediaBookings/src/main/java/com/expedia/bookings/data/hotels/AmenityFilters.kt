package com.expedia.bookings.data.hotels

class AmenityFilters {
    var amenityOptionList: List<Amenity> = emptyList()
    var accessibilityOptionList: List<AccessibilityOption> = emptyList()

    data class Amenity(val id: String, val name: String)
    data class AccessibilityOption(val id: String, val name: String)
}
