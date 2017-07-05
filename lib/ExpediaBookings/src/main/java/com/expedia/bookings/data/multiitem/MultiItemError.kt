package com.expedia.bookings.data.multiitem

data class MultiItemError(
        var description: String,
        var key: String,
        var productType: ProductType
)