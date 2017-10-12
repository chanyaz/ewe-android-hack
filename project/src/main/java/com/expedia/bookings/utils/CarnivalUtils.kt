package com.expedia.bookings.utils

import android.content.Context
import android.util.Log
import com.carnival.sdk.AttributeMap
import com.carnival.sdk.Carnival
import com.expedia.bookings.tracking.hotel.HotelSearchTrackingData
import com.expedia.bookings.R
import com.expedia.bookings.data.packages.PackageSearchParams
import org.joda.time.Days
import org.joda.time.LocalDate

open class CarnivalUtils {

    companion object {
        private val TAG = "CarnivalTracker"
        private lateinit var appContext: Context
        private var carnivalUtils: CarnivalUtils? = null
        private var initialized = false

        @JvmStatic @Synchronized
        fun getInstance(): CarnivalUtils {
            if (carnivalUtils == null) {
                carnivalUtils = CarnivalUtils()
            }
            return carnivalUtils as CarnivalUtils
        }
    }

    fun initialize(context: Context) {
        appContext = context
        initialized = true
        if (isFeatureToggledOn()) {
            Carnival.startEngine(appContext, appContext.getString(R.string.carnival_sdk_debug_key))
        }
    }

    fun trackFlightSearch(destination: String?, adults: Int, departure_date: LocalDate) {
        if (isFeatureToggledOn()) {
            val attributes = AttributeMap()
            attributes.putString("search_flight_destination", destination)
            attributes.putInt("search_flight_number_of_adults", adults)
            attributes.putDate("search_flight_departure_date", departure_date.toDate())
            setAttributes(attributes, "search_flight")
        }
    }

    fun trackHotelSearch(trackingParams: HotelSearchTrackingData) {
        if (isFeatureToggledOn()) {
            val attributes = AttributeMap()
            attributes.putString("search_hotel_destination", trackingParams.city + ", " + trackingParams.stateProvinceCode)
            attributes.putInt("search_hotel_number_of_adults", trackingParams.numberOfAdults)
            attributes.putDate("search_hotel_check-in_date", trackingParams.checkInDate?.toDate())
            trackingParams.duration?.let { attributes.putInt("search_hotel_length_of_stay", it) }
            setAttributes(attributes, "search_hotel")
        }
    }

    fun trackLxConfirmation(activityTitle: String, activityDate: String) {
        if (isFeatureToggledOn()) {
            val attributes = AttributeMap()
            attributes.putString("confirmation_lx_activity_name", activityTitle)
            attributes.putDate("confirmation_lx_date_of_activity", DateUtils.yyyyMMddHHmmssToLocalDate(activityDate).toDate())
            setAttributes(attributes, "confirmation_lx")
        }
    }

    fun trackPackagesConfirmation(packageParams: PackageSearchParams) {
        if (isFeatureToggledOn()) {
            val attributes = AttributeMap()
            attributes.putString("confirmation_pkg_destination", packageParams.destination?.regionNames?.fullName)
            attributes.putDate("confirmation_pkg_departure_date", packageParams.startDate.toDate())
            attributes.putInt("confirmation_pkg_length_of_stay", Days.daysBetween(packageParams.startDate, packageParams.endDate).days)
            setAttributes(attributes, "confirmation_pkg")
        }
    }

    private fun isFeatureToggledOn() : Boolean = initialized && FeatureToggleUtil.isFeatureEnabled(appContext, R.string.preference_new_carnival_notifications)

    open fun setAttributes(attributes: AttributeMap, eventName: String) {
        Carnival.logEvent(eventName)
        Carnival.setAttributes(attributes, object : Carnival.AttributesHandler {
            override fun onSuccess() {
                Log.d(TAG, "Carnival attributes sent successfully.")
            }

            override fun onFailure(error: Error) {
                Log.d(TAG, error.message)
            }
        })
    }
}
