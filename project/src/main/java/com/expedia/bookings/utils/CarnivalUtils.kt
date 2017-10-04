package com.expedia.bookings.utils

import android.util.Log
import com.carnival.sdk.AttributeMap
import com.carnival.sdk.Carnival
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
