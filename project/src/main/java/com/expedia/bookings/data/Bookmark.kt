package com.expedia.bookings.data

import org.joda.time.LocalDate

class Bookmark(val title: String, val startDate: LocalDate, val endDate: LocalDate, var numberOfGuests: Int, var deeplinkURL: String, val lineOfBusiness: LineOfBusiness)