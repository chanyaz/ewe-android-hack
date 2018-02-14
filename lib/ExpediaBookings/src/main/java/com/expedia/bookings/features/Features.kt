package com.expedia.bookings.features

interface Feature {
    val name: String
    fun enabled(): Boolean
}

class Features {
    companion object {
        val all = Features()
    }

    val legacyItinCardInActivity: Feature by RemoteFeatureDelegate()
}
