package com.expedia.bookings.features

interface Feature {
    fun enabled(): Boolean
}

class Features {
    companion object {
        val all = Features()
    }

    val legacyItinCardInActivity: Feature by RemoteFeatureDelegate()
}
