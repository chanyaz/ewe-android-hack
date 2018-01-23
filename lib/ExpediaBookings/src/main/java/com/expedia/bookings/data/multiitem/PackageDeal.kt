package com.expedia.bookings.data.multiitem

data class PackageDeal(
        val markers: List<Marker>,
        val savingsPercentage: Double,
        val savingsAmount: Double,
        val rank: Int
) {
    fun getDeal(): Marker? {
        if (rank < 0) {
            return null
        }
        return markers.firstOrNull()
    }
}
