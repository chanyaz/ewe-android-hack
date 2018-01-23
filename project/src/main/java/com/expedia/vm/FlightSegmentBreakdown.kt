package com.expedia.vm

import com.expedia.bookings.data.flights.FlightLeg

data class FlightSegmentBreakdown(val segment: FlightLeg.FlightSegment, val hasLayover: Boolean, val showSeatClassAndBookingCode: Boolean, val showCollapseIcon: Boolean = false)
