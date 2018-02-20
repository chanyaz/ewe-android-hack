package com.expedia.bookings.analytics.cesc

import com.expedia.bookings.utils.DateRangeUtils
import org.joda.time.DateTime
import org.joda.time.Days

class PersistingCESCDataUtil(private val persistenceProvider: CESCPersistenceProvider) {

    fun clearData() {
        persistenceProvider.clear()
    }

    fun shouldTrackStoredCidVisit(dateTime: DateTime, cidVisit: String = "cidVisit"): Boolean {
        val cescStoredData = persistenceProvider.get(cidVisit)
        cescStoredData?.second?.let {
            val storedCidVisitDateTime = DateTime(it)
            return DateRangeUtils.getMinutesBetween(storedCidVisitDateTime, dateTime) <= 30
        }
        return false
    }

    fun shouldTrackStoredCesc(dateTime: DateTime, cescVar: String): Boolean {
        val cescStoredData = persistenceProvider.get(cescVar)
        cescStoredData?.second?.let {
            val storedCescDateTime = DateTime(it)
            return Days.daysBetween(storedCescDateTime, dateTime).days <= 30
        }
        return false
    }

    fun getEvarValue(cescVar: String): String? {
        val storedData = persistenceProvider.get(cescVar)
        return storedData?.first
    }

    fun add(cescVariable: String, omnitureValue: String, dateTime: DateTime) {
        persistenceProvider.put(cescVariable, Pair(omnitureValue, dateTime.millis))
    }
}
