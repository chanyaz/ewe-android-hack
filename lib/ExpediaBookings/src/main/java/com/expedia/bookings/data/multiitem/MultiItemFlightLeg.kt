package com.expedia.bookings.data.multiitem

data class MultiItemFlightLeg(
    val segments: List<MultiItemFlightSegment>,
    val hasObFees: Boolean,
    val obFeesUrl: String?,
    val baggageFeesUrl: String,
    val stops: Int,
    val duration: Duration,
    val elapsedDays: Int,
    var isBasicEconomy: Boolean,
    val basicEconomyRuleLocIds: List<String>?
)
