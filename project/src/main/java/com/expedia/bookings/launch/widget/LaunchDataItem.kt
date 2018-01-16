package com.expedia.bookings.launch.widget

open class LaunchDataItem(private val key: Int) {
    fun getKey(): Int {
        return key
    }

    companion object LAUNCH_DATA_KEYS {
        @JvmField val LOADING_VIEW = 0
        @JvmField val LOB_VIEW = 1
        @JvmField val SIGN_IN_VIEW = 2
        @JvmField val MESO_HOTEL_AD_VIEW = 3
        @JvmField val MEMBER_ONLY_DEALS = 4
        @JvmField val LAST_MINUTE_DEALS = 5
        @JvmField val HEADER_VIEW = 6
        @JvmField val HOTEL_VIEW = 7
        @JvmField val COLLECTION_VIEW = 8
        @JvmField val ITIN_VIEW = 9
        @JvmField val AIR_ATTACH_VIEW = 10

    }
}
