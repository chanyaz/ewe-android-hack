package com.expedia.bookings.data

import org.joda.time.LocalDate

class Bookmark(val title: String, val startDate: LocalDate, val endDate: LocalDate, val numberOfGuests: Int, val deeplinkURL: String, lineOfBusiness: LineOfBusiness)