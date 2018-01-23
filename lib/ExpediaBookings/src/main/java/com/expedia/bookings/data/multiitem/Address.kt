package com.expedia.bookings.data.multiitem

data class Address(
    var firstAddressLine: String,
    var secondAddressLine: String,
    var city: String,
    var provinceCode: String,
    var threeLetterCountryCode: String,
    var postalCode: String,
    var latitude: Double,
    var longitude: Double
)
