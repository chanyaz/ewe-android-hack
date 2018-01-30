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
        @JvmField val MESO_DESTINATION_AD_VIEW = 4
        @JvmField val MEMBER_ONLY_DEALS = 5
        @JvmField val LAST_MINUTE_DEALS = 6
        @JvmField val HEADER_VIEW = 7
        @JvmField val HOTEL_VIEW = 8
        @JvmField val COLLECTION_VIEW = 9
        @JvmField val ITIN_VIEW = 10
        @JvmField val AIR_ATTACH_VIEW = 11
        @JvmField val MESO_LMD_SECTION_HEADER_VIEW = 12
        @JvmField val EARN_2X_MESSAGING_BANNER = 13
    }
}
