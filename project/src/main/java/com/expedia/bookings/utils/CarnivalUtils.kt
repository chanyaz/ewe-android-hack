package com.expedia.bookings.utils

import android.content.Context
import android.util.Log
import com.carnival.sdk.AttributeMap
import com.carnival.sdk.Carnival
import com.expedia.bookings.R
import org.joda.time.LocalDate

object CarnivalUtils {

    private val TAG = "CarnivalTracker"

    fun trackFlightSearch(destination: String?, adults: Int, departure_date: LocalDate) {
        Carnival.logEvent("search_flight")

        val attributes = AttributeMap()
        attributes.putString("search_flight_destination", destination)
        attributes.putInt("search_flight_number_of_adults", adults)
        attributes.putDate("search_flight_departure_date", departure_date.toDate())
        setAttributes(attributes)
    }

    @JvmStatic
    fun trackLxConfirmation(context: Context, activityTitle: String, activityDate: String) {
        if (isFeatureToggledOn(context)) {
            Carnival.logEvent("confirmation_lx")

            val attributes = AttributeMap()
            attributes.putString("confirmation_lx_activity_name", activityTitle)
            attributes.putDate("confirmation_lx_date_of_activity", DateUtils.yyyyMMddHHmmssToLocalDate(activityDate).toDate())
            setAttributes(attributes)
        }
    }

    @JvmStatic
    fun isFeatureToggledOn(context: Context) : Boolean = FeatureToggleUtil.isFeatureEnabled(context, R.string.preference_new_carnival_notifications)

    private fun setAttributes(attributes: AttributeMap) {
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
