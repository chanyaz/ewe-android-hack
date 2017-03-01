package com.expedia.bookings.launch.widget

open class LaunchDataItem(private val key: Int) {
    fun getKey(): Int {
        return key
    }

    companion object LAUNCH_DATA_KEYS {
        @JvmField val LOADING_VIEW = 0
        @JvmField val LOB_VIEW = 1
        @JvmField val SIGN_IN_VIEW = 2
        @JvmField val POPULAR_HOTELS = 3
        @JvmField val MEMBER_ONLY_DEALS = 4
        @JvmField val HEADER_VIEW = 5
        @JvmField val HOTEL_VIEW = 6
        @JvmField val COLLECTION_VIEW = 7
    }
}
