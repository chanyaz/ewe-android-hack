package com.expedia.bookings.data.multiitem

import com.expedia.bookings.data.packages.PackageOfferModel

data class Marker(
        val sticker: PackageOfferModel.DealVariation,
        val magnitude: String
)
