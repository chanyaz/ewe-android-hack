package com.expedia.bookings.utils

import java.util.UUID

class UniqueIdentifierHelper {

    companion object {
        private var uniqueID: String = ""
        private const val PREF_DEVICE_ID = "PREF_DEVICE_ID"

        @JvmStatic
        @Synchronized
        fun getID(persistenceProvider: StringPersistenceProvider): String {
            if (uniqueID.isEmpty()) {
                uniqueID = persistenceProvider.getString(PREF_DEVICE_ID)
                if (uniqueID.isEmpty()) {
                    createUniqueID(persistenceProvider)
                }
            }
            return uniqueID
        }

        private fun createUniqueID(persistenceProvider: StringPersistenceProvider) {
            uniqueID = UUID.randomUUID().toString()
            persistenceProvider.putString(PREF_DEVICE_ID, uniqueID)
        }
    }
}
