package com.expedia.bookings.data.hotels

class AmenityFilters {
    var amenityOptionList: List<Amenity> = emptyList()
    var accessibilityOptionList: List<AccessibilityOption> = emptyList()

    companion object {
        private val supportedAmenities = setOf(
                "7",
                "14",
                "16",
                "17",
                "19",
                "27",
                "30",
                "66")

        private val legacyIdMapping = mapOf(
                "pool" to "7",
                "childPool" to "7",
                "freeParking" to "14",
                "freeBreakfast" to "16",
                "petsAllowed" to "17",
                "highSpeedInternet" to "19",
                "airConditioning" to "27",
                "allInclusive" to "30",
                "freeAirportTransport" to "66")

        fun mapToLegacyId(id: String): String? {
            if (supportedAmenities.contains(id)) {
                return id
            }
            return legacyIdMapping[id]
        }
    }

    data class Amenity(val id: String, val name: String)
    data class AccessibilityOption(val id: String, val name: String)
}
