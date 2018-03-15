package com.expedia.bookings.data.rail.requests

// variable names in this model have to match 1:1 to the format the api expects
class PointOfSaleKey(
    val jurisdictionCountryCode: String = "GBR",
    val companyCode: String = "10111",
    val managementUnitCode: String = "1050"
)
