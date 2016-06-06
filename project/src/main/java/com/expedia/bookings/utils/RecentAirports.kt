package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.data.Location
import com.expedia.bookings.data.RecentList
import com.mobiata.flightlib.data.Airport

class RecentAirports(val context: Context) {

    private val RECENT_SELECTED_AIRPORT_ROUTES_LIST_FILE = "recent-airports-routes-list.dat"

    val recentSearches = RecentList(Location::class.java, context, RECENT_SELECTED_AIRPORT_ROUTES_LIST_FILE, 3)

    init {
        load()
    }

    fun add(airport: Airport) {
        recentSearches.addItem(airportToLocation(airport))
        Thread(Runnable { recentSearches.saveList(context, RECENT_SELECTED_AIRPORT_ROUTES_LIST_FILE) }).start()
    }

    fun load() {
        Thread(Runnable { recentSearches.loadList(context, RECENT_SELECTED_AIRPORT_ROUTES_LIST_FILE, true) }).start()
    }

    private fun airportToLocation(airport: Airport): Location {
        val location = Location()
        location.destinationId = airport.mAirportCode
        location.city = airport.mCity
        return location
    }
}
