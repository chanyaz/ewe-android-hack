package com.expedia.bookings.utils

import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class UniqueIdentifierHelperTest {

    private lateinit var mockPersistenceProvider: StringPersistenceProvider

    @Before
    fun setup() {
        mockPersistenceProvider = HashMapStringPersistenceProvider()
    }

    @Test
    fun testID() {
        val id = UniqueIdentifierHelper.getID(mockPersistenceProvider)
        assertNotEquals("", id)
    }

    @Test
    fun testIDConsistency() {
        val first = UniqueIdentifierHelper.getID(mockPersistenceProvider)
        val second = UniqueIdentifierHelper.getID(mockPersistenceProvider)
        assertEquals(first, second)
    }

    inner class HashMapStringPersistenceProvider : StringPersistenceProvider {
        private val map = HashMap<String, String>()

        override fun getString(key: String, defaultValue: String): String {
            return map.getOrDefault(key, defaultValue)
        }

        override fun putString(key: String, value: String) {
            map[key] = value
        }
    }
}
