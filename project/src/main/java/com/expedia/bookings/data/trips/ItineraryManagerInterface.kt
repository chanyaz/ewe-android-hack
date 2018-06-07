package com.expedia.bookings.data.trips

interface ItineraryManagerInterface {
    fun getItinCardDataFromItinId (id: String?): ItinCardData?
    fun addSyncListener(listener: ItineraryManager.ItinerarySyncListener)
    fun removeSyncListener(listener: ItineraryManager.ItinerarySyncListener)
    fun getTripComponentFromFlightHistoryId(id: Int): TripFlight
    fun isSyncing(): Boolean
    fun startSync(boolean: Boolean): Boolean
    fun deepRefreshTrip(key: String, doSyncIfNotFound: Boolean): Boolean
}
