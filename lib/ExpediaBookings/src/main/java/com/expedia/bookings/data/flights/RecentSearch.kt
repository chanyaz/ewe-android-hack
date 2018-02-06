package com.expedia.bookings.data.flights

data class RecentSearch(val originLocation: String, val destinationLocation: String, val amount: String,
                        val dateRange: String, val searchDate: String, val travelerCount: String, val flightClass: String, val isRoundTrip: Boolean)
