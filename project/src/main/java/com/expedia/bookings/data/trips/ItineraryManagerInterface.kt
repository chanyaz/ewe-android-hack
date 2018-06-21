package com.expedia.bookings.data.trips

/**
 * This singleton keeps all itinerary data together from loading, sync & storage.
 * <p/>
 * Call init() before using in the app for the first time
 * Call startSync() before manipulating data.
 * <p/>
 * Sync uses a Priority SyncOperation queue, allowing dynamic ordering and reordering of tasks.
 * <p/>
 * SyncOperation Steps:-
 * 1. Initial loadStateFromDisk - can be safely called whenever
 * 2. Refresh/loadStateFromDisk trips - loadStateFromDisk data from all sources
 * 3. Load ancillary data for trips e.g. flight stats data about trips.
 * Any call other than normal refresh goes here.
 * Loading trip data in #2 should be followed by this step.
 * 4. Post-processing operations - these assume that all of the data in the itins have been loaded.
 * These operations include
 * saving the loaded data to disk
 * generating data for the app to consume
 * registering loaded data with notifications.
 * <p/>
 */
interface ItineraryManagerInterface {
    fun getItinCardDataFromItinId (id: String?): ItinCardData?
    fun addSyncListener(listener: ItineraryManager.ItinerarySyncListener)
    fun removeSyncListener(listener: ItineraryManager.ItinerarySyncListener)
    fun isSyncing(): Boolean

    /**
     * Start a sync operation.
     * <p/>
     * If a sync is already in progress then calls to this are ignored.
     *
     * @return true if the sync started or is in progress, false if it never started
     */
    fun startSync(boolean: Boolean): Boolean
    fun deepRefreshTrip(key: String, doSyncIfNotFound: Boolean): Boolean
    fun getItinCardDataFromFlightHistoryId(fhid: Int): ItinCardData?
}
