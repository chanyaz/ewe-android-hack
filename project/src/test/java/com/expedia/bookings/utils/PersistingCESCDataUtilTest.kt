package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.analytics.cesc.PersistingCESCDataUtil
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.tracking.MockCESCPersistenceProvider
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class PersistingCESCDataUtilTest {
    private lateinit var context: Context
    private lateinit var currDate: DateTime
    private lateinit var validDateInMinutes: DateTime
    private lateinit var validDateInDays: DateTime
    private lateinit var invalidDateInMinutes: DateTime
    private lateinit var invalidDateInDays: DateTime
    private lateinit var persistingDataUtil: PersistingCESCDataUtil

    @Before
    fun setup() {
        context = RuntimeEnvironment.application
        persistingDataUtil = PersistingCESCDataUtil(MockCESCPersistenceProvider())
        currDate = DateTime.now()
        validDateInMinutes = currDate.minusMinutes(20)
        validDateInDays = currDate.minusDays(20)
        invalidDateInMinutes = currDate.minusMinutes(40)
        invalidDateInDays = currDate.minusDays(40)
    }

    @Test
    fun testLocalStorageIsCleared() {
        persistingDataUtil.add("cesc", "cesc_value", DateTime.now())
        persistingDataUtil.clearData()
        assertNull(persistingDataUtil.getEvarValue("cesc"))
    }

    @Test
    fun testGetCescEvarValue() {
        persistingDataUtil.add("cesc", "cesc_value", DateTime.now())
        assertEquals("cesc_value", persistingDataUtil.getEvarValue("cesc"))
    }

    @Test
    fun testShouldTrackStoredCesc() {
        persistingDataUtil.add("cesc", "cesc_value", validDateInDays)
        assertTrue(persistingDataUtil.shouldTrackStoredCesc(currDate, "cesc"))
    }

    @Test
    fun testShouldNotTrackStoredCesc() {
        persistingDataUtil.add("cesc", "cesc_value", invalidDateInDays)
        assertFalse(persistingDataUtil.shouldTrackStoredCesc(currDate, "cesc"))
    }

    @Test
    fun testShouldTrackStoredCidVisit() {
        persistingDataUtil.add("cidVisit", "cesc_value", validDateInMinutes)
        assertTrue(persistingDataUtil.shouldTrackStoredCidVisit(currDate, "cidVisit"))
    }

    @Test
    fun testShouldNotTrackStoredCidVisit() {
        persistingDataUtil.add("cidVisit", "cesc_value", invalidDateInMinutes)
        assertFalse(persistingDataUtil.shouldTrackStoredCidVisit(currDate, "cidVisit"))
    }
}
