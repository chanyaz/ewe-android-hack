package com.expedia.bookings.utils

import android.content.Context
import android.content.SharedPreferences
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class UniqueIdentifierHelperTest {

    private val PREF_DEVICE_ID = "PREF_DEVICE_ID"
    val context = RuntimeEnvironment.application
    private lateinit var sharedPrefs: SharedPreferences

    @Before
    fun setup() {
        sharedPrefs = context.getSharedPreferences(
                PREF_DEVICE_ID, Context.MODE_PRIVATE)
    }

    @Test
    fun testID() {
        assertFalse(sharedPrefs.contains(PREF_DEVICE_ID))
        UniqueIdentifierHelper.getID(context)
        assertTrue(sharedPrefs.contains(PREF_DEVICE_ID))
    }

    @Test
    fun testIDConsistency() {
        val first = UniqueIdentifierHelper.getID(context)
        val second = UniqueIdentifierHelper.getID(context)
        assertEquals(first, second)
    }

    @Test
    fun testCreateUniqueID() {
        assertFalse(sharedPrefs.contains(PREF_DEVICE_ID))
        sharedPrefs.edit().putString(PREF_DEVICE_ID, "").apply()
        assertTrue(sharedPrefs.contains(PREF_DEVICE_ID))
        UniqueIdentifierHelper.createUniqueID(context)
        assertNotEquals("", sharedPrefs.getString(PREF_DEVICE_ID, ""))
    }

    @After
    fun tearDown() {
        sharedPrefs.edit().clear().apply()
    }
}
