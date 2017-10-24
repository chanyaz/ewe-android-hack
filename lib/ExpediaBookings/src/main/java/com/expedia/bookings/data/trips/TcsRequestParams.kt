package com.expedia.bookings.data.trips

data class TcsRequestParams(
        val latitude: String,
        val longitude: String,
        val tcsApkKey: String,
        val langId: String,
        val sections: Array<String>,
        val version: Int,
        val useCache: Boolean
)